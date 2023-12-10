package com.stepstreak.dev.strada

import android.annotation.SuppressLint
import android.util.Log
import androidx.fragment.app.Fragment
import com.stepstreak.dev.MessageData
import dev.hotwire.strada.BridgeComponent
import dev.hotwire.strada.BridgeDelegate
import dev.hotwire.strada.Message
import com.stepstreak.dev.base.NavDestination
import com.stepstreak.dev.util.DataStoreManager

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
        Log.d("TurboDemo", "onReceive $message")
        val data = message.data<MessageData>() ?: return

        when (message.event) {
            "connect" -> handleConnectEvent(data.token)
            else -> Log.w("TurboDemo", "Unknown event for message: $message")
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun handleConnectEvent(token: String) {
        sharedPrefManager.saveToken(token)
    }
}
