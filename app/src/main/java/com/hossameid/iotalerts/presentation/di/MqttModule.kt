package com.hossameid.iotalerts.presentation.di

import android.content.Context
import android.content.SharedPreferences
import com.hossameid.iotalerts.data.db.AlertsDao
import com.hossameid.iotalerts.data.db.TopicsDao
import com.hossameid.iotalerts.data.repo.AlertsRepoImpl
import com.hossameid.iotalerts.data.repo.MqttRepoImpl
import com.hossameid.iotalerts.domain.repo.AlertsRepo
import com.hossameid.iotalerts.domain.repo.MqttRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MqttModule {

    @Provides
    @Singleton
    fun providesMqttRepo(
        @ApplicationContext context: Context,
        alertsRepo: AlertsRepo,
        topicsDao: TopicsDao,
        sharedPreferences: SharedPreferences
    ): MqttRepo = MqttRepoImpl(context, alertsRepo, topicsDao, sharedPreferences)

    @Provides
    fun providesAlertsRepo(alertsDao: AlertsDao): AlertsRepo = AlertsRepoImpl(alertsDao)
}