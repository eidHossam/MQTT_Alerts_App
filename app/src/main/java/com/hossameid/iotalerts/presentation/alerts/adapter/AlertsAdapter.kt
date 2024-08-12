package com.hossameid.iotalerts.presentation.alerts.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hossameid.iotalerts.R
import com.hossameid.iotalerts.databinding.AlertItemBinding
import com.hossameid.iotalerts.domain.models.TopicResponseModel

/**
 * @brief This class is the adapter for the alerts recycler view, it's responsible for adding or removing
 * items from the recycler view, it's also responsible for binding the data to the view holder, and
 * changing the content of each item in the list as well as handling the buttons in it.
 */
class AlertsAdapter :
    ListAdapter<TopicResponseModel, AlertsAdapter.ViewHolder>(TopicDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertsAdapter.ViewHolder {
        //Inflate the alert item layout we created
        val itemBinding =
            AlertItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: AlertsAdapter.ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * @brief this class is the view holder for the alerts recycler view, it's responsible for
     * binding the data to the view holder.
     */
    inner class ViewHolder(private val binding: AlertItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TopicResponseModel) {
            binding.topicNameTextView.text = item.topic
            binding.dateContentTextView.text = item.timestamp
            binding.messageContentTextView.text = item.message

            //Change the background color based on the type of the alert
            when (item.alertType) {
                "Normal" -> binding.messageContentTextView.setTextColor(
                    binding.root.context.getColor(R.color.normalAlertColor)
                )

                "Warning" -> binding.messageContentTextView.setTextColor(
                    binding.root.context.getColor(R.color.warningAlertColor)
                )

                "Critical" -> binding.messageContentTextView.setTextColor(
                    binding.root.context.getColor(R.color.criticalAlertColor)
                )
            }
        }
    }

    /**
     * @brief this class is responsible for comparing the old list and the new list when changed,
     * so it can update the recycler view accordingly.
     */
    class TopicDiffCallback : DiffUtil.ItemCallback<TopicResponseModel>() {
        override fun areItemsTheSame(
            oldItem: TopicResponseModel,
            newItem: TopicResponseModel
        ) = oldItem == newItem


        override fun areContentsTheSame(
            oldItem: TopicResponseModel,
            newItem: TopicResponseModel
        ) = oldItem == newItem
    }
}