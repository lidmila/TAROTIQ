package com.tarotiq.app.ui.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tarotiq.app.R
import com.tarotiq.app.ui.components.*
import com.tarotiq.app.ui.theme.*
import com.tarotiq.app.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val emailLinkSent by authViewModel.emailLinkSent.collectAsState()
    val context = LocalContext.current
    val credentialManager = remember { CredentialManager.create(context) }
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }
    var passwordVisible by remember { mutableStateOf(false) }
    var showForgotPassword by remember { mutableStateOf(false) }
    var resetEmailSent by remember { mutableStateOf(false) }

    // Navigate on successful auth
    LaunchedEffect(authState.user) {
        if (authState.user != null) {
            onLoginSuccess()
        }
    }

    // Logo breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "logo_breathe")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Logo
            Image(
                painter = painterResource(R.drawable.ic_tarotiq_logo),
                contentDescription = "TAROTIQ",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(140.dp)
                    .graphicsLayer { scaleX = scale; scaleY = scale }
            )
            Text(
                text = "TAROTIQ",
                style = MaterialTheme.typography.headlineLarge,
                color = GoldSecondary,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                text = "Your mystical guide",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Form Card
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Login/Register toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FilterChip(
                            selected = isLoginMode,
                            onClick = { isLoginMode = true; authViewModel.clearError() },
                            label = { Text(stringResource(R.string.auth_login)) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = !isLoginMode,
                            onClick = { isLoginMode = false; authViewModel.clearError() },
                            label = { Text(stringResource(R.string.auth_register)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(R.string.auth_email)) },
                        leadingIcon = { Icon(Icons.Default.Email, null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.auth_password)) },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    null
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Forgot password
                    if (isLoginMode) {
                        TextButton(
                            onClick = {
                                if (email.isNotBlank()) {
                                    authViewModel.sendPasswordResetEmail(email) { success, _ ->
                                        resetEmailSent = success
                                    }
                                } else {
                                    showForgotPassword = true
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(stringResource(R.string.auth_forgot_password), color = MysticLight)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Login/Register Button
                    Button(
                        onClick = {
                            if (isLoginMode) authViewModel.loginWithEmail(email, password)
                            else authViewModel.registerWithEmail(email, password)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = email.isNotBlank() && password.isNotBlank() && !authState.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MysticPrimary)
                    ) {
                        if (authState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = OnPrimary)
                        } else {
                            Text(
                                if (isLoginMode) stringResource(R.string.auth_login) else stringResource(R.string.auth_register)
                            )
                        }
                    }

                    // Error
                    authState.error?.let { error ->
                        Text(
                            text = error,
                            color = ErrorColor,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Email link sent confirmation
                    if (emailLinkSent) {
                        Text(
                            text = stringResource(R.string.auth_email_sent),
                            color = SuccessColor,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    if (resetEmailSent) {
                        Text(
                            text = stringResource(R.string.auth_reset_sent),
                            color = SuccessColor,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Divider)
                Text("  or  ", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                HorizontalDivider(modifier = Modifier.weight(1f), color = Divider)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Passwordless
            OutlinedButton(
                onClick = { if (email.isNotBlank()) authViewModel.sendSignInLinkToEmail(email) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Link, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.auth_passwordless))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Google Sign-In
            OutlinedButton(
                onClick = {
                    scope.launch { authViewModel.signInWithGoogle(credentialManager) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.auth_google))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
