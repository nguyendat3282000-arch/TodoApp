// File: ui/screens/auth/AuthScreen.kt
package com.example.todoapp.ui.screens.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
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
import com.example.todoapp.ui.theme.Background
import com.example.todoapp.ui.theme.CardShape
import com.example.todoapp.ui.theme.Outline
import com.example.todoapp.ui.theme.PillShape
import com.example.todoapp.ui.theme.Primary
import com.example.todoapp.ui.theme.PrimaryContainer
import com.example.todoapp.ui.theme.PrimaryFixed
import com.example.todoapp.ui.theme.SurfaceContainerLow
import com.example.todoapp.ui.theme.TertiaryFixedDim
import kotlinx.coroutines.launch

// ══════════════════════════════════════════════════════════════════════════════
//  AuthScreen — Modern Buddy glassmorphic login
// ══════════════════════════════════════════════════════════════════════════════

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

        // ── Mint gradient background ───────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Background,
                            SurfaceContainerLow,
                            PrimaryFixed,
                        ),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end   = androidx.compose.ui.geometry.Offset(800f, 1600f),
                    )
                )
                .padding(innerPadding),
        ) {

            // ── Floating decorative stars ──────────────────────────────────────
            FloatingStars()

            // ── Main scroll content ────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 24.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {

                // ── App header ─────────────────────────────────────────────────
                Text(text = "🌱", fontSize = 64.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = "TodoApp ✨",
                    style = MaterialTheme.typography.displaySmall,
                    color = Primary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text      = "Chào mừng bạn quay trở lại khu vườn công việc!",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(32.dp))

                // ── Glass card form ────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CardShape)
                        .background(
                            color = Color.White.copy(alpha = 0.72f),
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
//  Floating stars decoration
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun FloatingStars() {
    val inf = rememberInfiniteTransition(label = "stars")
    val offset1 by inf.animateFloat(
        initialValue = 0f,
        targetValue  = -10f,
        animationSpec = infiniteRepeatable(tween(4000), RepeatMode.Reverse),
        label = "star1",
    )
    val offset2 by inf.animateFloat(
        initialValue = 0f,
        targetValue  = -10f,
        animationSpec = infiniteRepeatable(tween(5000), RepeatMode.Reverse),
        label = "star2",
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            "⭐", fontSize = 28.sp,
            modifier = Modifier
                .offset(x = 48.dp, y = (120 + offset1).dp)
                .align(Alignment.TopStart),
        )
        Text(
            "✨", fontSize = 20.sp,
            modifier = Modifier
                .offset(x = (-40).dp, y = (180 + offset2).dp)
                .align(Alignment.TopEnd),
        )
        Text(
            "⭐", fontSize = 24.sp,
            modifier = Modifier
                .offset(x = 24.dp, y = (-80 + offset1).dp)
                .align(Alignment.BottomStart),
        )
        Text(
            "✨", fontSize = 32.sp,
            modifier = Modifier
                .offset(x = (-56).dp, y = (-100 + offset2).dp)
                .align(Alignment.BottomEnd),
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  AuthForm — Login / Register toggle
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
    val focusManager    = LocalFocusManager.current
    val isLoading       = uiState is AuthUiState.Loading

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
                text  = if (login) "Đăng nhập" else "Tạo tài khoản 🎉",
                style = MaterialTheme.typography.headlineSmall,
                color = Primary,
            )
        }

        Spacer(Modifier.height(20.dp))

        // ── Email field ───────────────────────────────────────────────────────
        RoundedTextField(
            value         = email,
            onValueChange = { email = it },
            label         = "Email",
            leadingIcon   = {
                Icon(Icons.Rounded.Email, contentDescription = null, tint = Primary)
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
            label         = "Mật khẩu",
            leadingIcon   = {
                Icon(Icons.Rounded.Lock, contentDescription = null, tint = Primary)
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector        = if (passwordVisible) Icons.Rounded.VisibilityOff
                                             else Icons.Rounded.Visibility,
                        contentDescription = "Toggle password visibility",
                        tint               = Outline,
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
                        text  = "Quên mật khẩu?",
                        style = MaterialTheme.typography.labelMedium,
                        color = Primary,
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Primary button ────────────────────────────────────────────────────
        Button(
            onClick = {
                focusManager.clearFocus()
                if (isLoginMode) onLogin(email, password)
                else             onRegister(email, password)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape  = PillShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary,
                contentColor   = Color.White,
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 2.dp,
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
                    text  = if (isLoginMode) "Đăng nhập" else "Tạo tài khoản",
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
            HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
            Text(
                text  = "  HOẶC  ",
                style = MaterialTheme.typography.labelSmall,
                color = Outline,
            )
            HorizontalDivider(Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
        }

        Spacer(Modifier.height(20.dp))

        // ── Google Sign-In button ─────────────────────────────────────────────
        OutlinedButton(
            onClick  = onGoogleSignIn,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape  = PillShape,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White.copy(alpha = 0.6f),
                contentColor   = MaterialTheme.colorScheme.onSurface,
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
            ),
            enabled = !isLoading,
        ) {
            // Google G icon colours
            Text(text = "G", fontSize = 18.sp, color = Color(0xFF4285F4))
            Spacer(Modifier.width(12.dp))
            Text(
                text  = "Tiếp tục với Google",
                style = MaterialTheme.typography.labelLarge,
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Toggle Login / Register ───────────────────────────────────────────
        TextButton(onClick = { isLoginMode = !isLoginMode }) {
            Text(
                text  = if (isLoginMode) "Chưa có tài khoản? Đăng ký ngay"
                        else             "Đã có tài khoản? Đăng nhập",
                style = MaterialTheme.typography.bodyMedium,
                color = Primary,
            )
        }
    }
}
