package com.hossameid.iotalerts.domain.models

data class TopicResponseModel(
    val topic: String,
    val timestamp: String,
    val alertType: String,
    val message: String
)
