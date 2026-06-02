// File: MainActivity.kt
package com.example.todoapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todoapp.alarm.AlarmSchedulerImpl
import com.example.todoapp.presentation.ViewModelFactory
import com.example.todoapp.presentation.auth.AuthViewModel
import com.example.todoapp.ui.theme.TodoTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

/**
 * Single-Activity entry point for the TodoApp.
 *
 * Responsibilities:
 *  1. Request the POST_NOTIFICATIONS runtime permission on Android 13+.
 *  2. Set up [TodoTheme] and launch [TodoNavGraph].
 *  3. Handle the Credential Manager Google Sign-In flow when triggered by [AuthScreen].
 *
 * The ViewModel factory is constructed here and shared via [TodoNavGraph]
 * so both [AuthViewModel] and [TaskViewModel] share the same graph instance.
 */
class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* No action needed — permission handled reactively by notification helpers */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermissionIfNeeded()
        enableEdgeToEdge()

        setContent {
            TodoTheme {
                val factory = ViewModelFactory(
                    application    = application,
                    alarmScheduler = AlarmSchedulerImpl(application),
                )
                val authViewModel: AuthViewModel = viewModel(factory = factory)
                TodoNavGraph(
                    authViewModel  = authViewModel,
                    onGoogleSignIn = { handleGoogleSignIn(authViewModel) },
                )
            }
        }
    }

    // ── Permissions ───────────────────────────────────────────────────────────

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // ── Google Sign-In ────────────────────────────────────────────────────────

    /**
     * Launches the Credential Manager Google Sign-In flow.
     *
     * Strategy:
     *  1. Try with [GetGoogleIdOption.filterByAuthorizedAccounts] = true (silent one-tap).
     *  2. On failure, fall back to [filterByAuthorizedAccounts] = false (full account chooser).
     *
     * The raw Google ID token is passed back to [AuthViewModel] which exchanges
     * it for a Firebase credential.
     */
    private fun handleGoogleSignIn(authViewModel: AuthViewModel) {
        val credentialManager = CredentialManager.create(this)
        authViewModel.startGoogleSignIn()

        lifecycleScope.launch {
            val result = runCatching {
                credentialManager.getCredential(this@MainActivity, buildGoogleRequest(filterByAuthorized = true))
            }.recoverCatching {
                credentialManager.getCredential(this@MainActivity, buildGoogleRequest(filterByAuthorized = false))
            }

            result
                .onSuccess { credentialResult ->
                    val credential = credentialResult.credential
                    if (credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                    ) {
                        val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                        authViewModel.loginWithGoogle(idToken)
                    } else {
                        authViewModel.handleGoogleSignInError(
                            Exception("Unsupported credential type: ${credential.type}")
                        )
                    }
                }
                .onFailure { exception ->
                    authViewModel.handleGoogleSignInError(exception)
                }
        }
    }

    private fun buildGoogleRequest(filterByAuthorized: Boolean): GetCredentialRequest {
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(filterByAuthorized)
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .build()
        return GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()
    }
}
