package com.stepstreak.dev

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import dev.hotwire.strada.KotlinXJsonConverter
import dev.hotwire.strada.Strada
import dev.hotwire.turbo.BuildConfig
import dev.hotwire.turbo.activities.TurboActivity
import dev.hotwire.turbo.config.Turbo
import dev.hotwire.turbo.delegates.TurboActivityDelegate
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.security.ProviderInstaller
import com.stepstreak.dev.googleFit.GoogleSignInManager

class MainActivity : AppCompatActivity(), TurboActivity {
    override lateinit var delegate: TurboActivityDelegate

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .build()

    val googleSignInManger = GoogleSignInManager(this, fitnessOptions)

    fun googleManager () : GoogleSignInManager {
        return googleSignInManger
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            ProviderInstaller.installIfNeeded(this)
        } catch (e: GooglePlayServicesRepairableException) {
            // Google Play services is not installed, up-to-date, or enabled
            // Show dialog to the user to update, enable, or install Google Play services
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.connectionStatusCode, 0)
                ?.show()
        } catch (e: GooglePlayServicesNotAvailableException) {
            // Google Play services is not available entirely
            // Handle this case as you want, e.g., show a dialog or a toast to the user
        }


        setContentView(R.layout.activity_main)

        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            googleSignInManger
        }

        delegate = TurboActivityDelegate(this, R.id.main_nav_host)
        configApp()
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
