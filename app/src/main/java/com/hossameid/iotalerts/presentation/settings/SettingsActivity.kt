package com.hossameid.iotalerts.presentation.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.hossameid.iotalerts.system.services.MqttService
import com.hossameid.iotalerts.databinding.ActivitySettingsBinding
import com.hossameid.iotalerts.presentation.MqttClientViewModel
import com.hossameid.iotalerts.utils.PreferencesHelper.brokerUri
import com.hossameid.iotalerts.utils.PreferencesHelper.username
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {
    private val viewModel: MqttClientViewModel by viewModels()
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var serviceIntent: Intent

    @Inject
    lateinit var sharedPreference: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Setup the toolbar
        setSupportActionBar(binding.toolbar)

        // Enable the Up button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        subscribeToObservers()

        //Update the UI with the stored values in the shared preferences
        binding.brokerURIEditText.setText(sharedPreference.brokerUri)
        binding.usernameEditText.setText(sharedPreference.username)

        binding.connectBtn.setOnClickListener {
            if (!checkBrokerFields())
                return@setOnClickListener

            viewModel.connect(
                binding.brokerURIEditText.text.toString(),
                binding.usernameEditText.text.toString(),
                binding.passwordEditText.text.toString()
            )
        }

        //Set the disconnect button listener
        binding.disconnectBtn.setOnClickListener {
            viewModel.disconnect()
        }

        serviceIntent = Intent(this@SettingsActivity, MqttService::class.java)
    }

    /**
     * @brief checks the validity of the inputs of the broker fields before connecting
     */
    private fun checkBrokerFields(): Boolean {
        binding.brokerURILayout.error = null

        if (binding.brokerURIEditText.text.isNullOrEmpty()) {
            binding.brokerURILayout.error = "Please enter the broker URI"
            return false
        }

        return true
    }

    /**
     * @brief Subscribe to the view model observers
     */
    private fun subscribeToObservers() {
        lifecycleScope.launch(Dispatchers.Main) {
            launch {
                viewModel.connectBtnState.collect {
                    binding.connectBtn.isEnabled = it
                }
            }

            launch {
                viewModel.disconnectBtnState.collect {
                    binding.disconnectBtn.isEnabled = it
                }
            }

            launch {
                viewModel.connectionStatus.collect {
                    when (it) {
                        //End the fragment in case of a successful connection
                        "SUCCESS" -> {
                            //Start the foreground service
                            ContextCompat.startForegroundService(this@SettingsActivity, serviceIntent)

                            finish()
                        }

                        "already connected" ->
                            Toast.makeText(
                                this@SettingsActivity,
                                "Client is already connected to this broker.",
                                Toast.LENGTH_SHORT
                            )
                                .show()

                        "FAILURE" -> Toast.makeText(
                            this@SettingsActivity,
                            "Failed to connect!",
                            Toast.LENGTH_SHORT
                        )
                            .show()

                        "Disconnected Successfully" ->
                        {
                            stopService(serviceIntent)

                            Toast.makeText(
                                this@SettingsActivity,
                                "Disconnected Successfully",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }


                        "Failed to disconnect!" ->
                            Toast.makeText(
                                this@SettingsActivity,
                                "Failed to disconnect!",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                    }

                }
            }
        }
    }

    // Handle the back button press
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Finish the current activity and go back
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}