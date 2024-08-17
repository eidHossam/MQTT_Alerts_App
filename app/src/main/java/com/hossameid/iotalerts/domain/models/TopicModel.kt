package com.hossameid.iotalerts.domain.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "topics")
data class TopicModel(
    @PrimaryKey
    val topic: String
)
