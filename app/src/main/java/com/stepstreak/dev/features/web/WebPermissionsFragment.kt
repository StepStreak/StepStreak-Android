package com.stepstreak.dev.features.web

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.stepstreak.dev.MainActivity
import com.stepstreak.dev.R
import com.stepstreak.dev.util.DataStoreManager
import dev.hotwire.turbo.nav.TurboNavGraphDestination

@TurboNavGraphDestination(uri = "turbo://fragment/web/permissions")
class WebPermissionsFragment : WebFragment() {
    private val REQUEST_ACTIVITY_RECOGNITION = 123

    private lateinit var sharedPrefManager : DataStoreManager

    private val permissions = arrayOf(
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.ACTIVITY_RECOGNITION
    )

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestMultiplePermissionsLauncher by lazy {
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permissions.forEach { (permission, isGranted) ->
                when (permission) {
                    Manifest.permission.POST_NOTIFICATIONS -> {
                        if (isGranted) {
                            handlePostNotificationsPermissionGranted()
                        }
                    }

                    Manifest.permission.ACTIVITY_RECOGNITION -> {
                        if (!isGranted) {
                            checkPermissions()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        requestMultiplePermissionsLauncher.launch(permissions)

        (activity as MainActivity).googleManager().signIn{}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_web_home, container, false)
    }

    @SuppressLint("InflateParams")
    override fun createErrorView(statusCode: Int): View {
        return layoutInflater.inflate(R.layout.error_web_home, null)
    }

    override fun shouldObserveTitleChanges(): Boolean {
        return false
    }

    private fun handlePostNotificationsPermissionGranted() {
        sharedPrefManager = DataStoreManager(requireActivity())
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }

            val token = task.result
            sharedPrefManager.saveNotificationToken(token.toString())
        })
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                    Manifest.permission.ACTIVITY_RECOGNITION)) {
                ActivityCompat.requestPermissions(requireActivity(),
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    REQUEST_ACTIVITY_RECOGNITION)
            }
        }
    }
}