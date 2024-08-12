package com.hossameid.iotalerts.presentation.settings


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.hossameid.iotalerts.databinding.FragmentSecondBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private val viewModel: MqttClientViewModel by viewModels()
    private var _binding: FragmentSecondBinding? = null

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

        binding.connectBtn.setOnClickListener {
            if(!checkBrokerFields())
                return@setOnClickListener

            viewModel.connect(
                binding.brokerURIEditText.text.toString(),
                binding.usernameEditText.text.toString(),
                binding.passwordEditText.text.toString()
                )
        }
    }

    /**
     * @brief checks the validity of the inputs of the broker fields before connecting
     */
    private fun checkBrokerFields() : Boolean
    {
        binding.brokerURILayout.error = null

        if(binding.brokerURIEditText.text.isNullOrEmpty())
        {
            binding.brokerURILayout.error = "Please enter the broker URI"
            return false
        }

        return true
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}