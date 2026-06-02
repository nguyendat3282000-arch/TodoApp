// File: ui/screens/auth/AuthScreen.kt
package com.example.todoapp.ui.screens.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todoapp.presentation.auth.AuthUiState
import com.example.todoapp.presentation.auth.AuthViewModel
import com.example.todoapp.ui.components.RoundedTextField
import com.example.todoapp.ui.theme.BabyBlue200
import com.example.todoapp.ui.theme.Coral100
import com.example.todoapp.ui.theme.Coral500
import com.example.todoapp.ui.theme.Mint100
import com.example.todoapp.ui.theme.Mint500
import com.example.todoapp.ui.theme.PillShape
import kotlinx.coroutines.launch

// ══════════════════════════════════════════════════════════════════════════════
//  AuthScreen
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Single entry-point for both Login and Register flows.
 * Toggles between modes with an animated content swap.
 *
 * @param viewModel      Provided by the NavGraph via viewModel().
 * @param onAuthSuccess  Called when the user is fully authenticated.
 * @param onGoogleSignIn Called to trigger the Credential Manager flow in MainActivity.
 */
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit,
    onGoogleSignIn: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success ->
                onAuthSuccess()
            is AuthUiState.Error ->
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = (uiState as AuthUiState.Error).message,
                    )
                    viewModel.resetState()
                }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Mint100, BabyBlue200, Coral100),
                    )
                )
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 28.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // ── Hero ──────────────────────────────────────────────────────
                Text(text = "✅", fontSize = 64.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = "TodoApp",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Mint500,
                )
                Text(
                    text      = "Stay cute, stay organized 🌸",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(36.dp))

                // ── Form card ─────────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                            shape = MaterialTheme.shapes.extraLarge,
                        )
                        .padding(24.dp),
                ) {
                    AuthForm(
                        uiState          = uiState,
                        onLogin          = { email, pw -> viewModel.login(email, pw) },
                        onRegister       = { email, pw -> viewModel.register(email, pw) },
                        onGoogleSignIn   = onGoogleSignIn,
                        onForgotPassword = { email -> viewModel.sendPasswordReset(email) },
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  AuthForm — toggleable Login / Register
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun AuthForm(
    uiState: AuthUiState,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onForgotPassword: (String) -> Unit,
) {
    var isLoginMode     by remember { mutableStateOf(true) }
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager     = LocalFocusManager.current
    val isLoading        = uiState is AuthUiState.Loading

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        // ── Mode title ────────────────────────────────────────────────────────
        AnimatedContent(
            targetState = isLoginMode,
            transitionSpec = {
                (fadeIn(tween(300)) + slideInVertically(tween(300)) { -20 })
                    .togetherWith(fadeOut(tween(200)) + slideOutVertically(tween(200)) { 20 })
            },
            label = "auth_title",
        ) { login ->
            Text(
                text  = if (login) "Welcome back! 👋" else "Create account 🎉",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── Email field ───────────────────────────────────────────────────────
        RoundedTextField(
            value         = email,
            onValueChange = { email = it },
            label         = "Email",
            leadingIcon   = {
                Icon(Icons.Rounded.Email, contentDescription = null, tint = Mint500)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction    = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
            enabled = !isLoading,
        )

        Spacer(Modifier.height(12.dp))

        // ── Password field ────────────────────────────────────────────────────
        RoundedTextField(
            value         = password,
            onValueChange = { password = it },
            label         = "Password",
            leadingIcon   = {
                Icon(Icons.Rounded.Lock, contentDescription = null, tint = Mint500)
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector        = if (passwordVisible) Icons.Rounded.VisibilityOff
                                             else Icons.Rounded.Visibility,
                        contentDescription = "Toggle password visibility",
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None
                                   else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction    = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    if (isLoginMode) onLogin(email, password)
                    else             onRegister(email, password)
                },
            ),
            enabled = !isLoading,
        )

        // ── Forgot password ───────────────────────────────────────────────────
        AnimatedVisibility(visible = isLoginMode) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onForgotPassword(email) }) {
                    Text(
                        text  = "Forgot password?",
                        style = MaterialTheme.typography.labelMedium,
                        color = Coral500,
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Primary action button ─────────────────────────────────────────────
        Button(
            onClick = {
                focusManager.clearFocus()
                if (isLoginMode) onLogin(email, password)
                else             onRegister(email, password)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape    = PillShape,
            colors   = ButtonDefaults.buttonColors(
                containerColor = Mint500,
                contentColor   = Color.White,
            ),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier    = Modifier.size(22.dp),
                    color       = Color.White,
                    strokeWidth = 2.5.dp,
                )
            } else {
                Text(
                    text  = if (isLoginMode) "Sign In" else "Create Account",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── OR divider ────────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.fillMaxWidth(),
        ) {
            Divider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
            Text(
                text  = "  OR  ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Divider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
        }

        Spacer(Modifier.height(20.dp))

        // ── Google Sign-In button ─────────────────────────────────────────────
        OutlinedButton(
            onClick  = onGoogleSignIn,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape    = PillShape,
            colors   = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
            border  = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                MaterialTheme.colorScheme.outline,
            ),
            enabled = !isLoading,
        ) {
            Text(text = "🇬", fontSize = 20.sp)
            Spacer(Modifier.width(12.dp))
            Text(
                text  = "Continue with Google",
                style = MaterialTheme.typography.labelLarge,
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Toggle Login / Register ───────────────────────────────────────────
        TextButton(onClick = { isLoginMode = !isLoginMode }) {
            Text(
                text  = if (isLoginMode) "Don't have an account? Register"
                        else             "Already have an account? Sign in",
                style = MaterialTheme.typography.bodySmall,
                color = Mint500,
            )
        }
    }
}
