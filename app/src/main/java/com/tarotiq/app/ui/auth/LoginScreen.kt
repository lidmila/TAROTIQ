package com.tarotiq.app.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    LaunchedEffect(authState.user) {
        if (authState.user != null) {
            onLoginSuccess()
        }
    }

    // Slow dramatic logo entrance
    val infiniteTransition = rememberInfiniteTransition(label = "logo_glow")
    val logoGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        MysticBackground(modifier = Modifier.fillMaxSize())

        var screenVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { screenVisible = true }
        AnimatedVisibility(
            visible = screenVisible,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 16 }
        ) {
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
                    .graphicsLayer { alpha = logoGlow }
            )
            Text(
                text = "TAROTIQ",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = NewsreaderFamily,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 4.sp
                ),
                color = AstralPurple,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                text = stringResource(R.string.auth_subtitle),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = SpaceGroteskFamily,
                    letterSpacing = 3.sp
                ),
                color = MoonSilver,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Form Card
            ArtNouveauFrame(
                modifier = Modifier.fillMaxWidth(),
                frameStyle = FrameStyle.ORNATE
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
                            label = {
                                Text(
                                    stringResource(R.string.auth_login).uppercase(),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        letterSpacing = 1.5.sp
                                    )
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AstralPurple,
                                selectedLabelColor = StarWhite,
                                containerColor = CosmicMid.copy(alpha = 0.5f),
                                labelColor = MoonSilver
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilterChip(
                            selected = !isLoginMode,
                            onClick = { isLoginMode = false; authViewModel.clearError() },
                            label = {
                                Text(
                                    stringResource(R.string.auth_register).uppercase(),
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        letterSpacing = 1.5.sp
                                    )
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AstralPurple,
                                selectedLabelColor = StarWhite,
                                containerColor = CosmicMid.copy(alpha = 0.5f),
                                labelColor = MoonSilver
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(R.string.auth_email)) },
                        leadingIcon = { Icon(Icons.Outlined.Email, null, tint = MoonSilver) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AstralPurple,
                            unfocusedBorderColor = GlassBorder,
                            focusedContainerColor = CosmicMid.copy(alpha = 0.5f),
                            unfocusedContainerColor = CosmicMid.copy(alpha = 0.3f),
                            cursorColor = CelestialGold,
                            focusedTextColor = StarWhite,
                            unfocusedTextColor = StarWhite
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.auth_password)) },
                        leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = MoonSilver) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    null,
                                    tint = MoonSilver
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AstralPurple,
                            unfocusedBorderColor = GlassBorder,
                            focusedContainerColor = CosmicMid.copy(alpha = 0.5f),
                            unfocusedContainerColor = CosmicMid.copy(alpha = 0.3f),
                            cursorColor = CelestialGold,
                            focusedTextColor = StarWhite,
                            unfocusedTextColor = StarWhite
                        )
                    )

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
                            Text(stringResource(R.string.auth_forgot_password), color = CelestialGoldLight)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ArtNouveauButton(
                        text = if (isLoginMode) stringResource(R.string.auth_login) else stringResource(R.string.auth_register),
                        onClick = {
                            if (isLoginMode) authViewModel.loginWithEmail(email, password)
                            else authViewModel.registerWithEmail(email, password)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = email.isNotBlank() && password.isNotBlank() && !authState.isLoading,
                        variant = ButtonVariant.PRIMARY
                    )

                    if (authState.isLoading) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = CelestialGold
                        )
                    }

                    authState.error?.let { error ->
                        Text(
                            text = error,
                            color = ErrorCrimson,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    if (emailLinkSent) {
                        Text(
                            text = stringResource(R.string.auth_email_sent),
                            color = SuccessEmerald,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    if (resetEmailSent) {
                        Text(
                            text = stringResource(R.string.auth_reset_sent),
                            color = SuccessEmerald,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OrnamentalDivider()

            Spacer(modifier = Modifier.height(20.dp))

            // Passwordless
            ArtNouveauButton(
                text = stringResource(R.string.auth_passwordless),
                onClick = { if (email.isNotBlank()) authViewModel.sendSignInLinkToEmail(email) },
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.SECONDARY
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Google Sign-In
            ArtNouveauButton(
                text = stringResource(R.string.auth_google),
                onClick = {
                    scope.launch { authViewModel.signInWithGoogle(credentialManager) }
                },
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.SECONDARY
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
        }
    }
}
