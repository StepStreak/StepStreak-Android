package com.stepstreak.dev

import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.stepstreak.dev.googleFit.GoogleSignInManager
import com.stepstreak.dev.util.DataStoreManager

class MainActivity : AppCompatActivity(), TurboActivity {
    private val REQUEST_ACTIVITY_RECOGNITION = 123
    override lateinit var delegate: TurboActivityDelegate
    private lateinit var sharedPrefManager : DataStoreManager

    private val permissions = arrayOf(
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.ACTIVITY_RECOGNITION
    )

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .build()

    private lateinit var googleSignInManger: GoogleSignInManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestMultiplePermissionsLauncher.launch(permissions)

        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            googleSignInManger = GoogleSignInManager(this, fitnessOptions)

            googleSignInManger.signIn{}
        }

        delegate = TurboActivityDelegate(this, R.id.main_nav_host)
        configApp()
    }

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestMultiplePermissionsLauncher = registerForActivityResult(
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

    private fun handlePostNotificationsPermissionGranted() {
        sharedPrefManager = DataStoreManager(this)
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }

            val token = task.result
            sharedPrefManager.saveNotificationToken(token.toString())
        })
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
