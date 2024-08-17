package com.hossameid.iotalerts.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hossameid.iotalerts.domain.models.TopicModel
import com.hossameid.iotalerts.domain.models.TopicResponseModel

@Database(entities = [TopicResponseModel::class, TopicModel::class], version = 3, exportSchema = false)
abstract class AlertsDatabase: RoomDatabase() {
    abstract fun alertsDao(): AlertsDao
    abstract fun topicsDao(): TopicsDao
}