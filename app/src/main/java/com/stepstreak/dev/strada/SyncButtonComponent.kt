package com.stepstreak.dev.strada

import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import dev.hotwire.strada.BridgeComponent
import dev.hotwire.strada.BridgeDelegate
import dev.hotwire.strada.Message
import com.stepstreak.dev.base.NavDestination
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.stepstreak.dev.R

/**
 * Bridge component to display a native bottom sheet menu, which will
 * send the selected index of the tapped menu item back to the web.
 */
class SyncButtonComponent(
    name: String,
    private val delegate: BridgeDelegate<NavDestination>
) : BridgeComponent<NavDestination>(name, delegate) {

    private val fragment: Fragment
        get() = delegate.destination.fragment

    override fun onReceive(message: Message) {
        Log.d("TurboDemo", "onReceive $message")
        when (message.event) {
            "connect" -> handleConnectEvent()
            else -> Log.w("TurboDemo", "Unknown event for message: $message")
        }
    }

    private fun handleConnectEvent() {
        showFloatingButton()
    }

    private fun showFloatingButton() {
        val view = fragment.view?.rootView ?: return
        val context = view.context

        val floatingActionButton = FloatingActionButton(context).apply {
            val marginY = resources.getDimensionPixelSize(R.dimen.action_button_y) // replace with your actual margin
            val marginX = resources.getDimensionPixelSize(R.dimen.action_button_x) // replace with your actual margin
            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.END or Gravity.BOTTOM
            ).apply {
                setMargins(marginX, marginY, marginX, marginY)
            }
            layoutParams = params
            setImageResource(R.drawable.baseline_sync_24) // replace with your icon
            setOnClickListener {
                onItemSelected()
            }
        }

        (view as ViewGroup).addView(floatingActionButton)
    }

    private fun onItemSelected() {
        replyTo("connect")
    }
}
