package com.tarotiq.app.ui.reading

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tarotiq.app.R
import com.tarotiq.app.domain.model.MessageRole
import com.tarotiq.app.ui.components.AnimatedBackground
import com.tarotiq.app.ui.components.GlassCard
import com.tarotiq.app.ui.theme.*
import com.tarotiq.app.viewmodel.ReadingViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingInterpretationScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    readingViewModel: ReadingViewModel = viewModel()
) {
    val uiState by readingViewModel.uiState.collectAsState()
    var followUpText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Request interpretation on first composition
    LaunchedEffect(Unit) {
        if (uiState.interpretation.isEmpty() && !uiState.isInterpreting) {
            readingViewModel.requestInterpretation()
        }
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.followUpMessages.size) {
        if (uiState.followUpMessages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(modifier = Modifier.fillMaxSize())

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.reading_summary),
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = TextPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MidnightBg2.copy(alpha = 0.8f)
                    )
                )
            },
            bottomBar = {
                // Follow-up input bar (visible after interpretation is loaded)
                if (uiState.interpretation.isNotEmpty()) {
                    Surface(
                        color = MidnightBg2.copy(alpha = 0.95f),
                        tonalElevation = 8.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .navigationBarsPadding(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = followUpText,
                                onValueChange = { followUpText = it },
                                placeholder = {
                                    Text(
                                        text = stringResource(R.string.reading_follow_up_hint),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary.copy(alpha = 0.6f)
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(24.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MysticPrimary,
                                    unfocusedBorderColor = Outline,
                                    focusedContainerColor = SurfaceVariant.copy(alpha = 0.5f),
                                    unfocusedContainerColor = SurfaceVariant.copy(alpha = 0.3f),
                                    cursorColor = GoldSecondary,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                maxLines = 3,
                                enabled = !uiState.isFollowUpLoading
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (followUpText.isNotBlank()) {
                                        readingViewModel.sendFollowUp(followUpText.trim())
                                        followUpText = ""
                                    }
                                },
                                enabled = followUpText.isNotBlank() && !uiState.isFollowUpLoading,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (followUpText.isNotBlank()) MysticPrimary
                                        else SurfaceVariant
                                    )
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = stringResource(R.string.reading_follow_up),
                                    tint = if (followUpText.isNotBlank()) OnPrimary
                                           else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        ) { padding ->
            if (uiState.isInterpreting) {
                // Loading state with animated text
                LoadingInterpretation(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            } else if (uiState.error != null) {
                // Error state
                ErrorState(
                    error = uiState.error!!,
                    onRetry = { readingViewModel.requestInterpretation() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            } else {
                // Interpretation + follow-up chat
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Main interpretation
                    if (uiState.interpretation.isNotEmpty()) {
                        item(key = "interpretation") {
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + slideInVertically()
                            ) {
                                InterpretationBubble(text = uiState.interpretation)
                            }
                        }

                        // Follow-up prompt
                        item(key = "follow_up_prompt") {
                            Text(
                                text = stringResource(R.string.reading_follow_up),
                                style = MaterialTheme.typography.labelMedium,
                                color = GoldSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }

                    // Follow-up messages
                    items(
                        items = uiState.followUpMessages,
                        key = { it.id }
                    ) { message ->
                        when (message.role) {
                            MessageRole.USER -> UserMessageBubble(text = message.content)
                            MessageRole.ASSISTANT -> AssistantMessageBubble(
                                text = message.content,
                                isError = message.isError
                            )
                            MessageRole.SYSTEM -> { /* not displayed */ }
                        }
                    }

                    // Loading indicator for follow-up
                    if (uiState.isFollowUpLoading) {
                        item(key = "follow_up_loading") {
                            TypingIndicator()
                        }
                    }

                    // "Done" button at bottom
                    if (uiState.interpretation.isNotEmpty()) {
                        item(key = "done_button") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onDone,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldSecondary.copy(alpha = 0.2f)
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.reading_summary),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = GoldSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Loading animation: "Madame Stella reads your cards..."
// ─────────────────────────────────────────────

@Composable
private fun LoadingInterpretation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_breathe")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    val dotCount by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dots"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Crystal ball icon with glow
        Icon(
            Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = MysticLight.copy(alpha = glowAlpha),
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Animated loading text
        Text(
            text = stringResource(R.string.reading_interpreting) +
                   ".".repeat(dotCount.toInt().coerceAtMost(3)),
            style = MaterialTheme.typography.titleMedium,
            color = GoldSecondary.copy(alpha = glowAlpha),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LinearProgressIndicator(
            modifier = Modifier
                .width(200.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MysticPrimary,
            trackColor = SurfaceVariant
        )
    }
}

// ─────────────────────────────────────────────
// Error state
// ─────────────────────────────────────────────

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.error_generic),
            style = MaterialTheme.typography.titleMedium,
            color = ErrorColor,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MysticPrimary)
        ) {
            Text(text = "Retry", color = OnPrimary)
        }
    }
}

// ─────────────────────────────────────────────
// Chat bubbles
// ─────────────────────────────────────────────

@Composable
private fun InterpretationBubble(text: String) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = MysticLight.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = GoldSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Madame Stella",
                    style = MaterialTheme.typography.labelMedium,
                    color = GoldSecondary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun UserMessageBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = 16.dp, bottomEnd = 4.dp
            ),
            color = MysticPrimary.copy(alpha = 0.8f),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = OnPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun AssistantMessageBubble(text: String, isError: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = 4.dp, bottomEnd = 16.dp
            ),
            color = if (isError) ErrorColor.copy(alpha = 0.15f)
                    else SurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) ErrorColor else TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = SurfaceVariant.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(3) { index ->
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = index * 200, easing = EaseInOutSine),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot_$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MysticLight.copy(alpha = alpha))
                    )
                }
            }
        }
    }
}
