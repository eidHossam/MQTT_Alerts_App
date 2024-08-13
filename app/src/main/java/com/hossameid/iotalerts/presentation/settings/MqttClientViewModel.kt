package com.hossameid.iotalerts.presentation.settings

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import com.hossameid.iotalerts.domain.repo.MqttRepo
import com.hossameid.iotalerts.utils.PreferencesHelper.brokerUri
import com.hossameid.iotalerts.utils.PreferencesHelper.password
import com.hossameid.iotalerts.utils.PreferencesHelper.username
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MqttClientViewModel @Inject constructor(
    private val mqttRepo: MqttRepo,
    private val sharedPreferences : SharedPreferences
) : ViewModel() {

    private val _connectionStatus : MutableStateFlow<String?> = MutableStateFlow(null)
    val connectionStatus : StateFlow<String?> = _connectionStatus

    private val _connectBtnState : MutableStateFlow<Boolean?> = MutableStateFlow(true)
    val connectBtnState : StateFlow<Boolean?> = _connectBtnState

    private val _disconnectBtnState : MutableStateFlow<Boolean> = MutableStateFlow(true)
    val disconnectBtnState : StateFlow<Boolean> = _disconnectBtnState

    fun connect(uri: String, username: String, password: String) {
        //Check if the client is already connected to the current broker
        if(isConnected(uri))
        {
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
    private fun isConnected(uri: String) : Boolean
    {
        val oldURI = sharedPreferences.brokerUri

        return oldURI == uri && mqttRepo.isConnected()
    }

    fun disconnect() {
        //Check if the client is already disconnected
        if(!mqttRepo.isConnected())
        {
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

    private fun updateSavedBroker(uri: String, username: String, password: String)
    {
        sharedPreferences.brokerUri = uri
        sharedPreferences.username = username
        sharedPreferences.password = password
    }
}