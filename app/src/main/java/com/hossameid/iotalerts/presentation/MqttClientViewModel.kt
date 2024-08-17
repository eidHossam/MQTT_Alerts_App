package com.hossameid.iotalerts.presentation

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hossameid.iotalerts.domain.models.TopicResponseModel
import com.hossameid.iotalerts.domain.repo.AlertsRepo
import com.hossameid.iotalerts.domain.repo.MqttRepo
import com.hossameid.iotalerts.utils.PreferencesHelper.brokerUri
import com.hossameid.iotalerts.utils.PreferencesHelper.password
import com.hossameid.iotalerts.utils.PreferencesHelper.username
import com.hossameid.iotalerts.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MqttClientViewModel @Inject constructor(
    private val mqttRepo: MqttRepo,
    private val sharedPreferences: SharedPreferences,
    private val alertsRepo: AlertsRepo,
) : ViewModel() {

    private val _connectionStatus: MutableStateFlow<String?> = MutableStateFlow(null)
    val connectionStatus: StateFlow<String?> = _connectionStatus

    private val _connectBtnState: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val connectBtnState: StateFlow<Boolean> = _connectBtnState

    private val _disconnectBtnState: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val disconnectBtnState: StateFlow<Boolean> = _disconnectBtnState

    private val _subscriptionStatus: MutableSharedFlow<Result<String>?> = MutableSharedFlow()
    val subscriptionStatus: SharedFlow<Result<String>?> = _subscriptionStatus

    private val _unsubscribeStatus: MutableSharedFlow<Result<String>?> = MutableSharedFlow()
    val unsubscribeStatus: SharedFlow<Result<String>?> = _unsubscribeStatus

    val alerts: LiveData<List<TopicResponseModel>> = alertsRepo.getAllAlerts()

    /**
     * @brief Connect to the broker using the MqttRepo.
     */
    fun connect(uri: String, username: String, password: String) {
        //Check if the client is already connected to the current broker
        if (isConnected(uri)) {
            _connectionStatus.value = "already connected"
            return
        }

        //Disable the button to indicate loading
        _connectBtnState.value = false

        mqttRepo.connect(uri, username, password,
            onSuccess = {
                //Update the saved broker
                updateSavedBroker(uri, username, password)

                //Update the state flow to notify the UI
                _connectionStatus.value = "SUCCESS"

                //Enable the button
                _connectBtnState.value = true

                Log.d("MQTT_CLIENT", "Connected successfully")
            },
            onFailure = {
                //Update the state flow to notify the UI
                _connectionStatus.value = "FAILURE"

                //Enable the button
                _connectBtnState.value = true

                Log.d("MQTT_CLIENT", "Failed to connect")
            })
    }

    /**
     * @brief checks if the client is connected to the broker.
     */
    private fun isConnected(uri: String): Boolean {
        val oldURI = sharedPreferences.brokerUri

        return oldURI == uri && mqttRepo.isConnected()
    }

    /**
     * @brief Disconnect from the broker.
     */
    fun disconnect() {
        //Check if the client is already disconnected
        if (!mqttRepo.isConnected()) {
            _connectionStatus.value = "Disconnected Successfully"
            return
        }

        //Disable the button to indicate loading
        _disconnectBtnState.value = false

        mqttRepo.disconnect(
            onSuccess = {
                //Delete the saved broker
                updateSavedBroker("", "", "")
                _connectionStatus.value = "Disconnected Successfully"

                _disconnectBtnState.value = true

                Log.d("MQTT_CLIENT", "Disconnected successfully")
            },
            onFailure = {
                _connectionStatus.value = "Failed to disconnect!"

                _disconnectBtnState.value = true

                Log.d("MQTT_CLIENT", "Failed to disconnect")
            })
    }


    /**
     * @brief Subscribe to a new topic
     *
     * @param topic Topic name to subscribe to.
     */
    fun subscribe(topic: String) {
        //Check if the client is connected to the current broker
        if (!mqttRepo.isConnected()) {
            viewModelScope.launch {
                _subscriptionStatus.emit(Result.Failure(Exception("The client is disconnected")))
            }
            return
        }

        mqttRepo.subscribe(topic,
            onSuccess = {
                viewModelScope.launch {
                    _subscriptionStatus.emit(Result.Success("SUCCESS"))
                }
            },
            onFailure = {
                viewModelScope.launch {
                    _subscriptionStatus.emit(Result.Failure(Exception("The client is disconnected")))
                }
            })
    }

    /**
     * @brief unsubscribe from a topic
     *
     * @param topic Topic name to unsubscribe from.
     */
    fun unsubscribe(topic: String) {
        mqttRepo.unsubscribe(topic,
            onSuccess = {
                viewModelScope.launch {
                    _unsubscribeStatus.emit(Result.Success("SUCCESS"))

                    //Delete the history for that topic
                    withContext(Dispatchers.IO)
                    {
                        alertsRepo.removeTopic(topic)
                    }
                }
            },
            onFailure = {
                viewModelScope.launch {
                    _unsubscribeStatus.emit(Result.Failure(Exception("Failed to unsubscribe")))
                }
            })
    }

    /**
     * @brief Removes as alerts from the alerts history list
     *
     * @param alert The alert to be deleted
     */
    fun deleteAlert(alert: TopicResponseModel)
    {
        viewModelScope.launch(Dispatchers.IO) {
            alertsRepo.removeAlert(alert)
        }
    }

    private fun updateSavedBroker(uri: String, username: String, password: String) {
        sharedPreferences.brokerUri = uri
        sharedPreferences.username = username
        sharedPreferences.password = password
    }
}