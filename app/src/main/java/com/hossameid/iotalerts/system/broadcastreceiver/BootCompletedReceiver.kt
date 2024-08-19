package com.hossameid.iotalerts.system.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.ContextCompat
import com.hossameid.iotalerts.domain.repo.MqttRepo
import com.hossameid.iotalerts.system.services.MqttService
import com.hossameid.iotalerts.utils.PreferencesHelper.brokerUri
import com.hossameid.iotalerts.utils.PreferencesHelper.password
import com.hossameid.iotalerts.utils.PreferencesHelper.username
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {
    @Inject
    lateinit var mqttClient: MqttRepo

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("MQTT_CLIENT", "Device restarted, starting MQTT service")

            //If there is a registered broker reconnect to it
            sharedPreferences.brokerUri?.let {
                mqttClient.connect(
                    sharedPreferences.brokerUri!!,
                    sharedPreferences.username ?: "",
                    sharedPreferences.password ?: "",
                    onSuccess = {

                        Log.d("MQTT_CLIENT", "Device restarted: Reconnected")

                        //start the foreground service
                        val serviceIntent = Intent(context, MqttService::class.java)
                        context?.let {
                            ContextCompat.startForegroundService(context, serviceIntent)
                        }
                    },
                    onFailure = {
                        Log.d("MQTT_CLIENT", "Device restarted: Failed to reconnect}")
                    }
                )
            }
        }
    }
}