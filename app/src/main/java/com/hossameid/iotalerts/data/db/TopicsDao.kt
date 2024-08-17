package com.hossameid.iotalerts.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hossameid.iotalerts.domain.models.TopicModel

@Dao
interface TopicsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTopic(topic: TopicModel)

    @Query("DELETE FROM topics WHERE topics.topic = :topic")
    suspend fun deleteTopic(topic: String)

    @Query("DELETE FROM topics")
    suspend fun deleteAllTopics()

    @Query("SELECT * FROM topics")
    suspend fun getAllTopics() : List<TopicModel>
}