package com.stepstreak.dev.googleFit

import android.util.Log
import com.google.android.gms.fitness.FitnessOptions
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class GoogleSignInManager(private val activity: FragmentActivity, private val fitnessOptions: FitnessOptions) {
    private val REQUEST_OAUTH_REQUEST_CODE = 123
    private var onSignInComplete: (() -> Unit)? = null

    private val googleSignInLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        getSignedInAccount(task)
    }

    fun signIn(onSignInComplete: () -> Unit) {
        val signInIntent = GoogleSignIn.getClient(activity, GoogleSignInOptions.DEFAULT_SIGN_IN)
            .signInIntent
        googleSignInLauncher.launch(signInIntent)
        this.onSignInComplete = onSignInComplete
    }

    private fun getSignedInAccount(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                GoogleSignIn.requestPermissions(
                    activity,
                    REQUEST_OAUTH_REQUEST_CODE,
                    account,
                    fitnessOptions)
            } else {
                onSignInComplete?.invoke()
            }
        } catch (e: ApiException) {
            Log.e("GoogleFit", "Sign-in failed: ${e.statusCode}")
        }
    }
}