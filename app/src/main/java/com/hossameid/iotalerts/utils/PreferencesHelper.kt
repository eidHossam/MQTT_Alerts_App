package com.hossameid.iotalerts.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * @brief Singleton shared preference object that is used to store values in the device memory
 */
object PreferencesHelper {
    private const val PREF_NAME = "APP_SETTINGS"
    private const val BROKER_URI = "BROKER_URI"
    private const val USERNAME = "USERNAME"
    private const val PASSWORD = "PASSWORD"

    fun getSharedPreference(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * @brief Extension function used to update values stores in the shared preference
     */
    private inline fun SharedPreferences.editMe(operation: (SharedPreferences.Editor) -> Unit) {
        val editMe = edit()
        operation(editMe)
        editMe.apply()
    }

    var SharedPreferences.brokerUri
        get() = getString(BROKER_URI, "")
        set(value){
            editMe {
                it.putString(BROKER_URI, value)
            }
        }

    var SharedPreferences.username
        get() = getString(USERNAME, "")
        set(value) {
            editMe {
                it.putString(USERNAME, value)
            }
        }

    var SharedPreferences.password
        get() = getString(PASSWORD, "")
        set(value) {
            editMe {
                it.putString(PASSWORD, value)
            }
        }
}