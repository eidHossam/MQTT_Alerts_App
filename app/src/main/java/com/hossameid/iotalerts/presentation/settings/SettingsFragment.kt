package com.hossameid.iotalerts.presentation.settings


import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.hossameid.iotalerts.databinding.FragmentSecondBinding
import com.hossameid.iotalerts.utils.PreferencesHelper.brokerUri
import com.hossameid.iotalerts.utils.PreferencesHelper.username
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private val viewModel: MqttClientViewModel by viewModels()
    private var _binding: FragmentSecondBinding? = null

    @Inject
    lateinit var sharedPreference: SharedPreferences

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        lifecycleScope.launch {
            launch {
                viewModel.connectBtnState.collect {
                    binding.connectBtn.isEnabled = it ?: true
                }
            }

            launch {
                viewModel.disconnectBtnState.collect {
                    binding.disconnectBtn.isEnabled = it ?: true
                }
            }

            launch {
                viewModel.connectionStatus.collect {
                    when (it) {
                        //End the fragment in case of a successful connection
                        "SUCCESS" -> activity?.supportFragmentManager?.popBackStack()

                        "already connected" ->
                            Toast.makeText(
                                context,
                                "Client is already connected to this broker.",
                                Toast.LENGTH_SHORT
                            )
                                .show()

                        "FAILURE" -> Toast.makeText(
                            context,
                            "Failed to connect!",
                            Toast.LENGTH_SHORT
                        )
                            .show()

                        "Disconnected Successfully" ->
                            Toast.makeText(context, "Disconnected Successfully", Toast.LENGTH_SHORT)
                                .show()

                        "Failed to disconnect!" ->
                            Toast.makeText(context, "Failed to disconnect!", Toast.LENGTH_SHORT)
                                .show()
                    }

                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}