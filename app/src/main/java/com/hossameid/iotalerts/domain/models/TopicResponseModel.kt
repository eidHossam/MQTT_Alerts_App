package com.hossameid.iotalerts.domain.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "alerts")
data class TopicResponseModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "topic")
    val topic: String,
    @ColumnInfo(name = "timestamp")
    val timestamp: String = Date().toString(),
    @ColumnInfo(name = "type")
    val alertType: Int,
    @ColumnInfo(name = "message")
    val message: String,
    @ColumnInfo(name = "acknowledged")
    val acknowledge: Boolean = false
)
