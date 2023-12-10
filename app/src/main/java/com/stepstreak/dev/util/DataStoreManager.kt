package com.stepstreak.dev.util

import android.content.Context
import android.content.SharedPreferences

class DataStoreManager(context: Context) {

    private val PREF_NAME = "auth_pref"
    private val TOKEN_KEY = "auth_token"
    private val sharedPref: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        val editor = sharedPref.edit()
        editor.putString(TOKEN_KEY, token)
        editor.apply()
    }

    fun getToken(): String? {
        return sharedPref.getString(TOKEN_KEY, null)
    }
}