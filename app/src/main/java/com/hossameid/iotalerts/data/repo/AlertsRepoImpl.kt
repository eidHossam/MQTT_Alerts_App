package com.hossameid.iotalerts.data.repo

import androidx.lifecycle.LiveData
import com.hossameid.iotalerts.data.db.AlertsDao
import com.hossameid.iotalerts.domain.models.TopicResponseModel
import com.hossameid.iotalerts.domain.repo.AlertsRepo
import javax.inject.Inject

class AlertsRepoImpl @Inject constructor(private val alertsDao: AlertsDao) : AlertsRepo {
    override fun getAllAlerts(): LiveData<List<TopicResponseModel>> = alertsDao.getAllAlerts()

    override suspend fun addReceivedAlert(alert: TopicResponseModel) = alertsDao.insertAlertWithLimit(alert)

    override suspend fun removeAlert(alert: TopicResponseModel) = alertsDao.deleteAlert(alert)

    override suspend fun removeTopic(topic: String) = alertsDao.deleteTopic(topic)

    override suspend fun acknowledgeAlert(timestamp: String) = alertsDao.acknowledgeAlert(timestamp)
}