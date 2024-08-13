package com.hossameid.iotalerts.presentation.alerts

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.hossameid.iotalerts.R
import com.hossameid.iotalerts.databinding.FragmentFirstBinding
import com.hossameid.iotalerts.domain.models.TopicResponseModel
import com.hossameid.iotalerts.presentation.alerts.adapter.AlertsAdapter
import com.hossameid.iotalerts.utils.PreferencesHelper
import com.hossameid.iotalerts.utils.PreferencesHelper.brokerUri

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class AlertsFragment : Fragment() {
    private lateinit var sharedPreferences : SharedPreferences
    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        //Init the shared preference
        sharedPreferences = PreferencesHelper.getSharedPreference(this.requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Set the listener for the button that navigates to the second fragment(Broker Settings)
        binding.changeBrokerBtn.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        binding.addTopicBtn.setOnClickListener { _ ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(binding.addTopicBtn).show()
        }

        //Setting the alert list adapter
        val alertsList = listOf(
            TopicResponseModel("TA001","12-8-2024 12:00:PM", "Normal", "The temperature is 40 Degrees"),
            TopicResponseModel("TA002","12-8-2024 12:00:PM", "Warning", "The pressure passed the threshold"),
            TopicResponseModel("TA001","12-8-2024 12:00:PM", "Critical", "The garage door is open"),
            TopicResponseModel("TA002","12-8-2024 12:00:PM", "Normal",
                "The living room lights are on with no one in the room"),
            )
        val adapter = AlertsAdapter()
        binding.alertsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.alertsRecyclerView.adapter = adapter

        adapter.submitList(alertsList)
    }

    override fun onStart() {
        super.onStart()

        //Update the current broker UI element
        val uri = sharedPreferences.brokerUri
        binding.currentBrokerTextView.text = uri
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}