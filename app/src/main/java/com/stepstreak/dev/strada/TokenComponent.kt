package com.stepstreak.dev.strada

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.stepstreak.dev.BuildConfig
import com.stepstreak.dev.TokenMessageData
import dev.hotwire.strada.BridgeComponent
import dev.hotwire.strada.BridgeDelegate
import dev.hotwire.strada.Message
import com.stepstreak.dev.base.NavDestination
import com.stepstreak.dev.util.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * Bridge component to display a native bottom sheet menu, which will
 * send the selected index of the tapped menu item back to the web.
 */
class TokenComponent(
    name: String,
    private val delegate: BridgeDelegate<NavDestination>
) : BridgeComponent<NavDestination>(name, delegate) {

    private val fragment: Fragment
        get() = delegate.destination.fragment

    private val sharedPrefManager = DataStoreManager(fragment.requireContext())

    override fun onReceive(message: Message) {
        val data = message.data<TokenMessageData>() ?: return

        when (message.event) {
            "connect" -> handleConnectEvent(data.token)
            else -> Log.w("TurboDemo", "Unknown event for message: $message")
        }
        sendPutRequest(sharedPrefManager.getNotificationToken().toString())
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun handleConnectEvent(token: String) {
        sharedPrefManager.saveToken(token)
    }

    private fun sendPutRequest(notificationToken: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val dataStoreManager = DataStoreManager(fragment.requireContext())

            val client = OkHttpClient()
            val url = BuildConfig.BASE_URL + "api/users"
            val mediaType = "application/json; charset=utf-8".toMediaType()

            // Create JSON object
            val jsonObject = JSONObject()
            val userObject = JSONObject()
            userObject.put("device_type", "android")
            userObject.put("notification_token", notificationToken)
            jsonObject.put("user", userObject)

            // Convert JSON object to string and use it as request body
            val body = jsonObject.toString().toRequestBody(mediaType)

            val headers = okhttp3.Headers.Builder()
                .add("Content-Type", "application/json")
                .add("Authorization", "Bearer ${dataStoreManager.getToken()}")
                .build()

            val request = Request.Builder()
                .url(url)
                .put(body)
                .headers(headers)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    fragment.requireActivity().runOnUiThread {
                        Toast.makeText(fragment.requireActivity(), "API authentication failed. Please try again later", Toast.LENGTH_LONG).show()
                    }
                    Log.i("NotificationToken", "API authentication failed. Error: ${response.body?.string()}")
                } else {
                    Log.i("NotificationToken", "Data sent to API: ${response.body?.string()}")
                }
            }
        }
    }
}
