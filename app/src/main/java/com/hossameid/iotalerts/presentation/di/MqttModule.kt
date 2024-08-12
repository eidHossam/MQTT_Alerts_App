package com.hossameid.iotalerts.presentation.di

import android.content.Context
import com.hossameid.iotalerts.data.repo.MqttRepoImpl
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
    fun providesMqttRepo(@ApplicationContext context: Context) : MqttRepo = MqttRepoImpl(context)
}