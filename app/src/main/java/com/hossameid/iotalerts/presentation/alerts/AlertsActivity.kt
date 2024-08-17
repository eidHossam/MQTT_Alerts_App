package com.hossameid.iotalerts.presentation.alerts

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.hossameid.iotalerts.R
import com.hossameid.iotalerts.databinding.ActivityAlertBinding
import com.hossameid.iotalerts.domain.repo.MqttRepo
import com.hossameid.iotalerts.presentation.MqttClientViewModel
import com.hossameid.iotalerts.presentation.alerts.adapter.AlertsAdapter
import com.hossameid.iotalerts.presentation.settings.SettingsActivity
import com.hossameid.iotalerts.utils.PreferencesHelper.brokerUri
import com.hossameid.iotalerts.utils.Result
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlertsActivity : AppCompatActivity() {
    @Inject
    lateinit var sharedPreferences: SharedPreferences
    @Inject
    lateinit var mqttClient: MqttRepo
    private lateinit var binding: ActivityAlertBinding
    private val viewModel: MqttClientViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlertBinding.inflate(layoutInflater)

        setContentView(binding.root)

        subscribeToObservers()

        binding.changeBrokerBtn.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.addTopicBtn.setOnClickListener {
            showNewTopicInputDialog("Subscribe to Topic")
        }

        binding.removeTopicBtn.setOnClickListener {
            showNewTopicInputDialog("Unsubscribe from topic")
        }

        val adapter = AlertsAdapter(viewModel::deleteAlert)
        binding.alertsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.alertsRecyclerView.adapter = adapter

        //Observe the database to update the recycler view with the new items
        viewModel.alerts.observe(this) { adapter.submitList(it) }

        requestOverlayPermission()
    }

    override fun onStart() {
        super.onStart()

        //Update the current broker UI element
        val uri = sharedPreferences.brokerUri
        binding.currentBrokerTextView.text = uri
    }

    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${this.packageName}")
            )

            startActivity(intent)
        }
    }

    private fun subscribeToObservers() {
        lifecycleScope.launch(Dispatchers.Main) {
            launch {
                viewModel.subscriptionStatus.collect {
                    it?.let {
                        when (it) {
                            is Result.Success -> {
                                Toast.makeText(
                                    this@AlertsActivity,
                                    "Subscribed to Topic",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.d("MQTT_CLIENT", "Subscribed to Topic")
                            }

                            is Result.Failure -> {
                                Toast.makeText(
                                    this@AlertsActivity,
                                    "Failed to subscribe to Topic",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.d("MQTT_CLIENT", "Didn't subscribe to Topic")
                            }
                        }
                    }
                }
            }

            launch {
                viewModel.unsubscribeStatus.collect {
                    it?.let {
                        when (it) {
                            is Result.Success -> {
                                Toast.makeText(
                                    this@AlertsActivity,
                                    "Unsubscribed from Topic",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.d("MQTT_CLIENT", "Unsubscribed from Topic")
                            }

                            is Result.Failure -> {
                                Toast.makeText(
                                    this@AlertsActivity,
                                    "Failed to unsubscribe from Topic",
                                    Toast.LENGTH_SHORT
                                ).show()

                                Log.d("MQTT_CLIENT", "Didn't unsubscribe from Topic")
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun showNewTopicInputDialog(title: String) {
        //Build the dialog
        val alertDialog = AlertDialog.Builder(this)

        //Inflate the custom layout
        val dialogView = layoutInflater.inflate(R.layout.new_topic_dialog, null)
        val titleTextView = dialogView.findViewById<TextView>(R.id.title)
        val topicEditText = dialogView.findViewById<EditText>(R.id.topicInputText)
        val confirmBtn = dialogView.findViewById<MaterialButton>(R.id.confirmBtn)
        val cancelBtn = dialogView.findViewById<MaterialButton>(R.id.cancelBtn)

        //Set the dialog view
        titleTextView.text = title
        alertDialog.setView(dialogView)
        val dialog = alertDialog.create()

        confirmBtn.setOnClickListener {
            topicEditText.error = null

            if (topicEditText.text.isNullOrEmpty())
                topicEditText.error = "Topic name can't be empty."
            else {
                when (title) {
                    //Subscribe to the new topic
                    "Subscribe to Topic" -> viewModel.subscribe(topicEditText.text.toString())
                    //unSubscribe from topic
                    "Unsubscribe from topic" -> viewModel.unsubscribe(topicEditText.text.toString())
                }

                //Dismiss the dialog
                dialog.dismiss()
            }
        }

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        //Show the dialog
        dialog.show()
    }
}