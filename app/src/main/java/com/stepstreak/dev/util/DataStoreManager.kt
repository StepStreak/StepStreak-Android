package com.stepstreak.dev.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class DataStoreManager(context: Context) {

    private val AUTH_PREF_NAME = "auth_pref"
    private val AUTH_TOKEN_KEY = "auth_token"
    private val authSharedPref: SharedPreferences = context.getSharedPreferences(AUTH_PREF_NAME, Context.MODE_PRIVATE)

    private val NOTIFICATION_PREF_NAME = "notification_pref"
    private val NOTIFICATION_TOKEN_KEY = "notification_token"
    private val notificationSharedPref: SharedPreferences = context.getSharedPreferences(NOTIFICATION_PREF_NAME, Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        val editor = authSharedPref.edit()
        editor.putString(AUTH_TOKEN_KEY, token)
        editor.apply()
    }

    fun getToken(): String? {
        return authSharedPref.getString(AUTH_TOKEN_KEY, null)
    }

    fun saveNotificationToken(token: String) {
        val editor = notificationSharedPref.edit()
        Log.d("FCM", "Saving token: $token")
        editor.putString(NOTIFICATION_TOKEN_KEY, token)
        editor.apply()
    }

    fun getNotificationToken(): String? {
        val token = notificationSharedPref.getString(NOTIFICATION_TOKEN_KEY, null)
        Log.d("FCM", "getting token: $token")

        return notificationSharedPref.getString(NOTIFICATION_TOKEN_KEY, null)
    }
}