package com.hossameid.iotalerts.data.repo

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.hossameid.iotalerts.data.db.TopicsDao
import com.hossameid.iotalerts.domain.models.AlertDto
import com.hossameid.iotalerts.domain.models.TopicModel
import com.hossameid.iotalerts.domain.models.TopicResponseModel
import com.hossameid.iotalerts.domain.repo.AlertsRepo
import com.hossameid.iotalerts.domain.repo.MqttRepo
import com.hossameid.iotalerts.utils.AlertReceivedDialog
import com.hossameid.iotalerts.utils.MediaPlayer
import com.hossameid.iotalerts.utils.PreferencesHelper.brokerUri
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
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import javax.inject.Inject

class MqttRepoImpl @Inject constructor(
    private val context: Context,
    private val alertsRepo: AlertsRepo,
    private val topicsDao: TopicsDao,
    private val sharedPreferences: SharedPreferences
) : MqttRepo {
    private var mqttClient: MqttAndroidClient? = null
    private lateinit var options: MqttConnectOptions
    private var attemptReconnection: Boolean = true

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
        val clientID = MqttClient.generateClientId()
        mqttClient = MqttAndroidClient(context, brokerURI, clientID)

        //Set the username and password used for authentication
        options = MqttConnectOptions()
        options.userName = username
        options.password = password.toCharArray()

        attemptReconnection = true
        try {
            mqttClient?.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    //If this is the our old broker reconnect to the topics
                    if (sharedPreferences.brokerUri == brokerURI)
                        resubscribeToTopics()
                    else
                        runBlocking {
                            withContext(Dispatchers.IO)
                            {
                                topicsDao.deleteAllTopics()
                            }
                        }
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
                            //If the subscription was successful store the topic name in the database
                            topicsDao.insertTopic(TopicModel(topic))
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
                            //If we unsubscribed successfully remove the topic from the database
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
                //Try to connect again
                if (attemptReconnection) {
                    Log.d("MQTT_CLIENT", "connectionLost: Attempting reconnect")
                    connect()
                }
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d("MQTT_CLIENT", "Message arrived")
                //Parse the JSON we received from the alert into AlertDto object
                val alert: AlertDto = Gson().fromJson(message.toString(), AlertDto::class.java)

                //Make an TopicResponseModel to store it in the database
                val alertModel = TopicResponseModel(
                    topic = topic!!,
                    alertType = alert.alert,
                    message = alert.message
                )

                CoroutineScope(Dispatchers.IO).launch {
                    if (checkIfAlertEligible(topic, alert.alert)) {
                        //Save the alert to the database
                        alertsRepo.addReceivedAlert(alertModel)

                        withContext(Dispatchers.Main)
                        {
                            //play the right alarm
                            MediaPlayer.playAlarm(context, alertModel.alertType)

                            //Open the alert overlay
                            val alertDialog = AlertReceivedDialog(context, alertsRepo)
                            alertDialog.showDialog(alertModel)
                        }
                    }
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
            }

        })
    }

    /**
     * @brief Check if we should show the current alert in both the overlay and the alerts history list.
     *
     * @param currentAlertType The type of the received alert.
     *
     * @note The only case where we don't show the alert is when we are in danger state
     * then went to warning.
     */
    private suspend fun checkIfAlertEligible(topic: String, currentAlertType: Int): Boolean {

        return withContext(Dispatchers.IO) {
            val latestAlertType = alertsRepo.getLatestAlertType(topic)
            Log.d("MQTT_CLIENT", "checkIfAlertEligible: $latestAlertType")
            //Don't play the alarm if the current is warning and the last is danger or the status didn't change
            !((currentAlertType == 1) and (latestAlertType == 2) or (currentAlertType == latestAlertType))
        }
    }

    /**
     * @brief Resubscribe to all the old topics after connecting again after losing the connection.
     */
    private fun resubscribeToTopics() {
        runBlocking {
            val topicsList = async { topicsDao.getAllTopics() }

            for (topic in topicsList.await()) {
                mqttClient?.subscribe(topic.topic, 1)
                Log.d("MQTT_CLIENT", "resubscribeToTopic: ${topic.topic}")
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
                    attemptReconnection = false

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