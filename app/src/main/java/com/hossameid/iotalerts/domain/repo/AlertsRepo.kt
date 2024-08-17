package com.hossameid.iotalerts.domain.repo

import androidx.lifecycle.LiveData
import com.hossameid.iotalerts.domain.models.TopicResponseModel

interface AlertsRepo{

    /**
     * @brief Gets a list of all the alerts in the database
     */
    fun getAllAlerts() : LiveData<List<TopicResponseModel>>

    /**
     * @brief Adds the latest received alert to the database to be saved and displayed on the
     * history list
     *
     * @param alert Alert to be added
     */
    suspend fun addReceivedAlert(alert: TopicResponseModel)

    /**
     * @brief Removes an alert from the database
     *
     * @param alert Alert to be removed
     */
    suspend fun removeAlert(alert: TopicResponseModel)

    /**
     * @brief Removes all alerts from a specific topic
     *
     * @param topic Topic to remove all alerts from
     */
    suspend fun removeTopic(topic: String)

    /**
     * @brief Acknowledges an alert
     *
     * @param timestamp Timestamp of the alert to be acknowledged.
     */
    suspend fun acknowledgeAlert(timestamp: String)
}