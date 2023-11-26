package dev.hotwire.turbo.demo.features.web

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.stepstreak.dev.R
import com.stepstreak.dev.googleFit.GoogleFitManager
import com.stepstreak.dev.googleFit.GoogleSignInManager
import dev.hotwire.turbo.nav.TurboNavGraphDestination
import kotlinx.coroutines.launch

@TurboNavGraphDestination(uri = "turbo://fragment/web/home")
class WebHomeFragment : WebFragment() {

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .build()

    private lateinit var googleSignInManger: GoogleSignInManager
    private lateinit var googleFitManager: GoogleFitManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        googleSignInManger = GoogleSignInManager(fragment.requireActivity(), fitnessOptions)
        googleFitManager = GoogleFitManager(fragment.requireActivity(), fitnessOptions)

        val account = GoogleSignIn.getLastSignedInAccount(fragment.requireContext())
        if (account == null || !GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            googleSignInManger.signIn{
                fragment.lifecycleScope.launch {
                    googleFitManager.accessGoogleFit()
                }
            }
        }

        return inflater.inflate(R.layout.fragment_web_home, container, false)
    }

    @SuppressLint("InflateParams")
    override fun createErrorView(statusCode: Int): View {
        return layoutInflater.inflate(R.layout.error_web_home, null)
    }

    override fun shouldObserveTitleChanges(): Boolean {
        return false
    }
}
