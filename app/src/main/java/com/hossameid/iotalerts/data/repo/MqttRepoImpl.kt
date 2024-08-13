package com.hossameid.iotalerts.data.repo

import android.content.Context
import com.hossameid.iotalerts.domain.repo.MqttRepo
import info.mqtt.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import javax.inject.Inject

class MqttRepoImpl @Inject constructor(
    private val context: Context
) : MqttRepo {
    private var mqttClient: MqttAndroidClient? = null

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
        //Generates a random ID for the client
        val clientID = MqttClient.generateClientId()
        mqttClient = MqttAndroidClient(context, brokerURI, clientID)

        //Set the username and password used for authentication
        val options = MqttConnectOptions()
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
        try{
            mqttClient?.subscribe(topic, qos, null, object : IMqttActionListener{
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    onSuccess()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    onFailure(exception ?: Exception("Subscription failed"))
                }

            })
        }catch(e : MqttException)
        {
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
        try{
            mqttClient?.unsubscribe(topic, null, object : IMqttActionListener{
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    onSuccess()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    onFailure(exception ?: Exception("Unsubscribe failed"))
                }

            })
        }catch (e : MqttException)
        {
            e.printStackTrace()
            onFailure(e)
        }
    }

    /**
     * @brief Sets a callback function for the MQTT client
     *
     * @param callback The callback function to set.
     */
    override fun setCallback(callback: MqttCallback) {
        mqttClient?.setCallback(callback)
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