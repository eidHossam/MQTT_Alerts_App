package com.hossameid.iotalerts.domain.repo

/**
 * Defines the interface for any class that wants to implement
 * the MqttClient.
 */
interface MqttRepo {
    /**
     * @brief Connects to the MQTT broker.
     *
     * @param brokerURI The URI of the MQTT broker.
     * @param username The username to use for authentication.
     * @param password The password to use for authentication.
     * @param onSuccess A function to be called when the connection is successful.
     * @param onFailure A function to be called when the connection fails.
     */
    fun connect(
        brokerURI: String,
        username: String = "",
        password: String = "",
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
        )

    fun connect()

    /**
     * @brief Subscribe to a topic on the MQTT broker to be notified when it changes.
     *
     * @param topic The topic to subscribe to.
     * @param qos The quality of service to use when subscribing (Default = 1).
     * @param onSuccess A function to be called when the subscription is successful.
     * @param onFailure A function to be called when the subscription fails.
     */
    fun subscribe(
        topic: String,
        qos: Int = 1,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit
    )

    /**
     * @brief Unsubscribe from a topic on the MQTT broker.
     *
     * @param topic The topic to unsubscribe from.
     * @param onSuccess A function to be called when the unsubscribe is successful.
     * @param onFailure A function to be called when the unsubscribe fails.
     */
    fun unsubscribe(topic: String, onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)

    /**
     * @brief Sets a callback function for the MQTT client
     *
     */
    fun setCallback()

    /**
     * @brief Disconnects from the MQTT broker.
     *
     * @param onSuccess A function to be called when the disconnection is successful.
     * @param onFailure A function to be called when the disconnection fails.
     */
    fun disconnect(onSuccess: () -> Unit, onFailure: (Throwable) -> Unit)

    /**
     * @brief Checks if the client is connected to the broker.
     *
     * @return True if the client is connected, false otherwise.
     */
    fun isConnected() : Boolean
}