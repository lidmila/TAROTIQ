package com.tarotiq.app.ui.reading

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tarotiq.app.R
import com.tarotiq.app.ui.components.*
import com.tarotiq.app.ui.theme.*

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionInputScreen(
    topic: String,
    onContinue: (question: String?) -> Unit,
    onBack: () -> Unit
) {
    var questionText by remember { mutableStateOf("") }

    // Subtle breathing animation for the decorative element
    val infiniteTransition = rememberInfiniteTransition(label = "question_breathe")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
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
            modifier = Modifier.fillMaxSize()
        ) {
            // Top bar
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.question_title),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = NewsreaderFamily,
                            fontWeight = FontWeight.Normal
                        ),
                        color = StarWhite
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = StarWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Section header: "POSING THE QUESTION" in gold, uppercase
                Text(
                    text = stringResource(R.string.question_title).uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        letterSpacing = 2.5.sp
                    ),
                    color = CelestialGold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Decorative element
                Box(
                    modifier = Modifier.graphicsLayer {
                        scaleX = breatheScale
                        scaleY = breatheScale
                    }
                ) {
                    Image(
                        painter = painterResource(R.drawable.fortune),
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Question input card — glass panel with gradient border
                ArtNouveauFrame(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    AstralPurple.copy(alpha = 0.20f),
                                    CelestialGold.copy(alpha = 0.20f)
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    frameStyle = FrameStyle.ORNATE,
                    backgroundColor = CosmicMid.copy(alpha = 0.3f)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        // Ghost bottom-border text field
                        TextField(
                            value = questionText,
                            onValueChange = { questionText = it },
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.question_hint),
                                    color = MoonSilver.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontFamily = NewsreaderFamily,
                                        fontWeight = FontWeight.Light,
                                        fontSize = 20.sp
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = AstralPurple,
                                unfocusedIndicatorColor = GlassBorder,
                                cursorColor = CelestialGold,
                                focusedTextColor = StarWhite,
                                unfocusedTextColor = StarWhite
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = ManropeFamily,
                                fontSize = 16.sp,
                                lineHeight = 26.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Skip button
                    ArtNouveauButton(
                        text = stringResource(R.string.question_skip),
                        onClick = { onContinue(null) },
                        modifier = Modifier
                            .weight(1f),
                        variant = ButtonVariant.SECONDARY
                    )

                    // Continue button
                    ArtNouveauButton(
                        text = stringResource(R.string.question_continue),
                        onClick = {
                            onContinue(questionText.ifBlank { null })
                        },
                        modifier = Modifier
                            .weight(1f),
                        variant = ButtonVariant.PRIMARY
                    )
                }
            }
        }
        }
    }
}
