package com.hossameid.iotalerts.presentation.di

import android.content.Context
import androidx.room.Room
import com.hossameid.iotalerts.data.db.AlertsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DbModule {

    @Provides
    @Singleton
    fun providesDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, AlertsDatabase::class.java, "alerts_db")
            .fallbackToDestructiveMigration().build()

    @Provides
    fun providesAlertsDao(database: AlertsDatabase) =
        database.alertsDao()

    @Provides
    fun providesTopicsDao(database: AlertsDatabase) =
        database.topicsDao()
}