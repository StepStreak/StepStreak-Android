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
    private val REQUEST_ACTIVITY_RECOGNITION = 1

    private val googleSignInLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        getSignedInAccount(task)
    }

    fun signIn(onSignInComplete: () -> Unit) {
        checkPermissions()
        val signInIntent = GoogleSignIn.getClient(activity, GoogleSignInOptions.DEFAULT_SIGN_IN)
            .signInIntent
        googleSignInLauncher.launch(signInIntent)
        this.onSignInComplete = onSignInComplete
    }
    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.ACTIVITY_RECOGNITION)) {
            } else {
                ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    REQUEST_ACTIVITY_RECOGNITION)
            }
        }
    }

    fun onRequestPermissionsResult(requestCode: Int,
                                   permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_ACTIVITY_RECOGNITION -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
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