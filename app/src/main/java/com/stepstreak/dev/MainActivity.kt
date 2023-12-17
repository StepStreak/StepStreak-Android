package com.stepstreak.dev

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import dev.hotwire.strada.KotlinXJsonConverter
import dev.hotwire.strada.Strada
import dev.hotwire.turbo.BuildConfig
import dev.hotwire.turbo.activities.TurboActivity
import dev.hotwire.turbo.config.Turbo
import dev.hotwire.turbo.delegates.TurboActivityDelegate
import android.Manifest
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.stepstreak.dev.util.DataStoreManager

class MainActivity : AppCompatActivity(), TurboActivity {
    private val REQUEST_ACTIVITY_RECOGNITION = 123
    override lateinit var delegate: TurboActivityDelegate
    private lateinit var sharedPrefManager : DataStoreManager

    private val permissions = arrayOf(
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.ACTIVITY_RECOGNITION
    )

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            sharedPrefManager = DataStoreManager(this)
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result

                // Log and toast
                val notificationToken = token.toString()
                sharedPrefManager.saveNotificationToken(notificationToken)
            })
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestMultiplePermissionsLauncher.launch(permissions)

        delegate = TurboActivityDelegate(this, R.id.main_nav_host)
        configApp()
    }

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all {
            it.value == true
        }
        if (!granted) {
            Log.e("PERMISSIONS", "Permissions not granted.")
            checkPermissions()
            askNotificationPermission()
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACTIVITY_RECOGNITION)) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    REQUEST_ACTIVITY_RECOGNITION)
            }
        }
    }
    private fun configApp() {
        Strada.config.jsonConverter = KotlinXJsonConverter()

        if (BuildConfig.DEBUG) {
            Turbo.config.debugLoggingEnabled = true
            Strada.config.debugLoggingEnabled = true
            WebView.setWebContentsDebuggingEnabled(true)
        }
    }
}
