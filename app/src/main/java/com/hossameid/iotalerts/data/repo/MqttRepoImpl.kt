package com.hossameid.iotalerts.data.repo

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.hossameid.iotalerts.data.db.TopicsDao
import com.hossameid.iotalerts.domain.models.AlertDto
import com.hossameid.iotalerts.domain.models.TopicModel
import com.hossameid.iotalerts.domain.models.TopicResponseModel
import com.hossameid.iotalerts.domain.repo.AlertsRepo
import com.hossameid.iotalerts.domain.repo.MqttRepo
import com.hossameid.iotalerts.utils.AlertReceivedDialog
import info.mqtt.android.service.MqttAndroidClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import javax.inject.Inject

class MqttRepoImpl @Inject constructor(
    private val context: Context,
    private val alertsRepo: AlertsRepo,
    private val topicsDao: TopicsDao
) : MqttRepo {
    private var mqttClient: MqttAndroidClient? = null
    private lateinit var options: MqttConnectOptions

    /**
     * @brief Connects to the MQTT broker.
     *
     * @param brokerURI The URI of the MQTT broker.
     * @param username The username to use for authentication.
     * @param password The password to use for authentication.
     * @param onSuccess A function to be called when the connection is successful.
     * @param onFailure A function to be called when the connection fails.
     */
    override fun connect(
        brokerURI: String,
        username: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {

        mqttClient = MqttAndroidClient(context, brokerURI, "mqttAlerts")

        //Set the username and password used for authentication
        options = MqttConnectOptions()
        options.userName = username
        options.password = password.toCharArray()

        try {
            mqttClient?.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    //Our onSuccess callback function will be called in case of a successful connection
                    onSuccess()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    //Our onFailure callback function will be called in case of a unsuccessful connection
                    onFailure(exception ?: Exception("Connection failed"))
                }

            })
        } catch (e: MqttException) {
            e.printStackTrace()
            onFailure(e)
        }

    }

    override fun connect() {
        try {
            mqttClient?.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    resubscribeToTopics()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    //repeat until connected
                    Log.d("MQTT_CLIENT", "onFailure: Repeat attempt connection")
                    connect()
                }
            })
        } catch (e: MqttException) {
            connect()
        }
    }

    /**
     * @brief Subscribe to a topic on the MQTT broker to be notified when it changes.
     *
     * @param topic The topic to subscribe to.
     * @param qos The quality of service to use when subscribing (Default = 1).
     * @param onSuccess A function to be called when the subscription is successful.
     * @param onFailure A function to be called when the subscription fails.
     */
    override fun subscribe(
        topic: String,
        qos: Int,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        try {
            mqttClient?.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    runBlocking {
                        withContext(Dispatchers.IO)
                        {
                            topicsDao.insertTopic(TopicModel(topic = topic))
                        }
                    }
                    onSuccess()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    onFailure(exception ?: Exception("Subscription failed"))
                }

            })
        } catch (e: MqttException) {
            e.printStackTrace()
            onFailure(e)
        }
    }

    /**
     * @brief Unsubscribe from a topic on the MQTT broker.
     *
     * @param topic The topic to unsubscribe from.
     * @param onSuccess A function to be called when the unsubscribe is successful.
     * @param onFailure A function to be called when the unsubscribe fails.
     */
    override fun unsubscribe(topic: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {
        try {
            mqttClient?.unsubscribe(topic, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    runBlocking {
                        withContext(Dispatchers.IO)
                        {
                            topicsDao.deleteTopic(topic)
                        }
                    }
                    onSuccess()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    onFailure(exception ?: Exception("Unsubscribe failed"))
                }

            })
        } catch (e: MqttException) {
            e.printStackTrace()
            onFailure(e)
        }
    }

    /**
     * @brief Sets a callback function for the MQTT client
     *
     */
    override fun setCallback() {
        mqttClient?.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                Log.d("MQTT_CLIENT", "connectionLost: Attempting reconnect")

                //Try to connect again
                connect()
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d("MQTT_CLIENT", "Message arrived")

                val alert: AlertDto = Gson().fromJson(message.toString(), AlertDto::class.java)
                val alertModel = TopicResponseModel(
                    topic = topic!!,
                    alertType = alert.alert,
                    message = alert.message
                )

                CoroutineScope(Dispatchers.Main).launch {
                    val alertDialog = AlertReceivedDialog(context, alertsRepo)
                    alertDialog.showDialog(alertModel)

                    withContext(Dispatchers.IO){
                        //Save the alert to the database
                        alertsRepo.addReceivedAlert(alertModel)

                    }
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
            }

        })
    }

    private fun resubscribeToTopics() {
        runBlocking {
            val topicsList = async { topicsDao.getAllTopics() }

            for (topic in topicsList.await()) {
                mqttClient?.subscribe(topic.topic, 1)
            }
        }
    }

    /**
     * @brief Disconnects from the MQTT broker.
     *
     * @param onSuccess A function to be called when the disconnection is successful.
     * @param onFailure A function to be called when the disconnection fails.
     */
    override fun disconnect(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit) {

        try {
            mqttClient?.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    runBlocking {
                        withContext(Dispatchers.IO)
                        {
                            topicsDao.deleteAllTopics()
                        }
                    }
                    onSuccess()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    onFailure(exception ?: Exception("Disconnection failed"))
                }
            })
        } catch (e: MqttException) {
            onFailure(e)
        }
    }

    /**
     * @brief Checks if the client is connected to the broker.
     *
     * @return True if the client is connected, false otherwise.
     */
    override fun isConnected() = mqttClient?.isConnected ?: false
}