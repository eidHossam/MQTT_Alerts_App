package com.hossameid.iotalerts.presentation.di

import android.content.Context
import android.content.SharedPreferences
import com.hossameid.iotalerts.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SharedPreferencesModule {

    @Provides
    @Singleton
    fun providesSharedPreference(@ApplicationContext context: Context) : SharedPreferences =
        context.getSharedPreferences(context.getString(R.string.PREF_NAME), Context.MODE_PRIVATE)
}