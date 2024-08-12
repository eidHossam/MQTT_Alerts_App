package com.hossameid.iotalerts.presentation.settings

import android.annotation.SuppressLint
import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import com.hossameid.iotalerts.R
import com.hossameid.iotalerts.domain.repo.MqttRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MqttClientViewModel @Inject constructor(
    application: Application,
    private val mqttRepo: MqttRepo,
    private val sharedPreferences : SharedPreferences
) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val context = getApplication<Application>().applicationContext

    fun connect(uri: String, username: String, password: String) {
        //Check if the client is already connected to the current broker
        if(isConnected(uri))
        {
            Toast.makeText(context, "Client is already connected to this broker.", Toast.LENGTH_SHORT).show()
            return
        }


        mqttRepo.connect(uri, username, password,
            onSuccess = {
                //Update the saved broker
                updateSavedBroker(uri)

                Log.d("MQTT_CLIENT", "Connected successfully")
            },
            onFailure = {
                Toast.makeText(context, "Failed to connect!", Toast.LENGTH_SHORT).show()
                Log.d("MQTT_CLIENT", "Failed to connect")
            })
    }

    /**
     * @brief checks if the client is connected to the broker.
     */
    private fun isConnected(uri: String) : Boolean
    {
        val oldURI = sharedPreferences.getString(context.getString(R.string.BROKER_URI), "")
        if(oldURI == uri && mqttRepo.isConnected())
            return  true

        return false
    }

    fun disconnect() {
        mqttRepo.disconnect(
            onSuccess = {
                //Delete the saved broker
                updateSavedBroker("")

                Log.d("MQTT_CLIENT", "Disconnected successfully")
            },
            onFailure = {
                Log.d("MQTT_CLIENT", "Failed to disconnect")
            })
    }

    private fun updateSavedBroker(uri: String)
    {
        val editor = sharedPreferences.edit()
        editor.putString(context.getString(R.string.BROKER_URI), uri)
        editor.apply()
    }
}