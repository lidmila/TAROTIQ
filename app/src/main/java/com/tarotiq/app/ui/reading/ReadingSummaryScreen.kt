package com.tarotiq.app.ui.reading

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tarotiq.app.R
import com.tarotiq.app.ui.components.*
import com.tarotiq.app.ui.theme.*
import com.tarotiq.app.viewmodel.ReadingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingSummaryScreen(
    onDone: () -> Unit,
    readingViewModel: ReadingViewModel
) {
    val uiState by readingViewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        MysticBackground(modifier = Modifier.fillMaxSize())

        var screenVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { screenVisible = true }
        AnimatedVisibility(
            visible = screenVisible,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 16 }
        ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.reading_summary),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = NewsreaderFamily,
                                fontWeight = FontWeight.Normal
                            ),
                            color = StarWhite
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CosmicDeep.copy(alpha = 0.8f))
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Topic & Question
                ArtNouveauFrame(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    frameStyle = FrameStyle.ORNATE,
                    backgroundColor = CosmicMid.copy(alpha = 0.3f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            uiState.topic.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = NewsreaderFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 20.sp
                            ),
                            color = CelestialGold
                        )
                        uiState.question?.let {
                            Text(
                                it,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = ManropeFamily
                                ),
                                color = MoonSilver,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Section label
                Text(
                    text = stringResource(R.string.reading_summary),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        letterSpacing = 2.sp
                    ),
                    color = CelestialGold.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Interpretation
                ArtNouveauFrame(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = AstralPurple.copy(alpha = 0.10f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    frameStyle = FrameStyle.ORNATE,
                    backgroundColor = CosmicMid.copy(alpha = 0.3f)
                ) {
                    val annotated = remember(uiState.interpretation) {
                        val raw = uiState.interpretation
                        androidx.compose.ui.text.buildAnnotatedString {
                            var i = 0
                            while (i < raw.length) {
                                val boldStart = raw.indexOf("**", i)
                                if (boldStart == -1) { append(raw.substring(i)); break }
                                append(raw.substring(i, boldStart))
                                val boldEnd = raw.indexOf("**", boldStart + 2)
                                if (boldEnd == -1) { append(raw.substring(boldStart)); break }
                                pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold, color = CelestialGold))
                                append(raw.substring(boldStart + 2, boldEnd))
                                pop()
                                i = boldEnd + 2
                            }
                        }
                    }
                    Text(
                        annotated,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = ManropeFamily,
                            fontWeight = FontWeight.Light,
                            fontSize = 16.sp,
                            lineHeight = 26.sp
                        ),
                        color = StarWhite,
                        modifier = Modifier.padding(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Done button
                ArtNouveauButton(
                    text = stringResource(R.string.reading_save),
                    onClick = { readingViewModel.resetReading(); onDone() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    variant = ButtonVariant.PRIMARY
                )
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
        }
    }
}
