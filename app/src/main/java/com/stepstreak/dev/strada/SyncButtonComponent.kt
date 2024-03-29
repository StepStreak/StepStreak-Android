package com.stepstreak.dev.strada

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import dev.hotwire.strada.BridgeComponent
import dev.hotwire.strada.BridgeDelegate
import dev.hotwire.strada.Message
import com.stepstreak.dev.base.NavDestination
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.stepstreak.dev.R
import com.stepstreak.dev.googleFit.GoogleFitManager
import androidx.lifecycle.lifecycleScope
import com.stepstreak.dev.SyncMessageData
import kotlinx.coroutines.launch

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

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .build()

    private lateinit var googleFitManager: GoogleFitManager

    override fun onReceive(message: Message) {
        Log.d("TurboDemo", "onReceive $message")
        when (message.event) {
            "connect" -> handleConnectEvent(message)
            else -> Log.w("TurboDemo", "Unknown event for message: $message")
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun handleConnectEvent(message: Message) {
        showFloatingButton(message)
    }

    private fun showFloatingButton(message: Message) {
        val data = message.data<SyncMessageData>() ?: return

        val turboView = fragment.view?.findViewById<FrameLayout>(dev.hotwire.turbo.R.id.turbo_view) ?: return
        val context = turboView.context

        val marginY = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, data.syncY.toFloat(), fragment.resources.displayMetrics).toInt()
        val marginX = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, data.syncX.toFloat(), fragment.resources.displayMetrics).toInt()

        val floatingActionButton = FloatingActionButton(context).apply {
            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.END or Gravity.BOTTOM
            ).apply {
                setMargins(marginX, marginY, marginX, marginY)
            }
            layoutParams = params
            setImageResource(R.drawable.baseline_sync_24)
            setOnClickListener {
                syncData()
            }
            backgroundTintList = ColorStateList.valueOf(Color.parseColor("#1a56db"))
            elevation = 0f
        }

        turboView.addView(floatingActionButton)
    }

    private fun syncData() {
        googleFitManager = GoogleFitManager(fragment.requireActivity(), fitnessOptions)

        fragment.lifecycleScope.launch {
            googleFitManager.accessGoogleFit()
        }
        replyTo("connect")
    }
}
