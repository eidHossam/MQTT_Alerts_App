package com.hossameid.iotalerts.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.hossameid.iotalerts.domain.models.TopicResponseModel

@Dao
interface AlertsDao {

    @Query("SELECT COUNT(*) FROM alerts WHERE topic = :topic")
    suspend fun getTopicCount(topic: String): Int

    @Query("DELETE FROM alerts WHERE id IN (SELECT id FROM alerts WHERE topic = :topic ORDER BY id ASC LIMIT :limit)")
    suspend fun deleteOldestEntries(topic: String, limit: Int)

    @Insert
    suspend fun addReceivedAlert(alert: TopicResponseModel)

    @Transaction
    suspend fun insertAlertWithLimit(alert: TopicResponseModel) {
        val topicCount = getTopicCount(alert.topic)
        if (topicCount >= 10) {
            deleteOldestEntries(alert.topic, 1)  // Remove the oldest entry
        }
        addReceivedAlert(alert)  // Insert the new alert
    }

    @Delete
    suspend fun deleteAlert(alert: TopicResponseModel)


    @Query("DELETE FROM alerts WHERE topic = :topic")
    suspend fun deleteTopic(topic: String)

    @Query("UPDATE alerts SET acknowledged = 1 WHERE timestamp = :timestamp")
    suspend fun acknowledgeAlert(timestamp: String)

    @Query("SELECT * FROM alerts")
    fun getAllAlerts(): LiveData<List<TopicResponseModel>>

    /**
     * This will return the type of the latest alert and -1 if there are no alerts yet
     */
    @Query("""SELECT COALESCE((SELECT type FROM alerts WHERE topic = :topic ORDER BY timestamp DESC LIMIT 1), -1)""")
    suspend fun getLatestAlertType(topic: String): Int
}