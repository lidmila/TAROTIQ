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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tarotiq.app.R
import com.tarotiq.app.data.preferences.SettingsManager
import com.tarotiq.app.ui.components.*
import com.tarotiq.app.ui.theme.*
import com.tarotiq.app.utils.CardNameTranslator
import com.tarotiq.app.utils.ShareUtils
import com.tarotiq.app.viewmodel.ReadingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingSummaryScreen(
    onDone: () -> Unit,
    readingViewModel: ReadingViewModel
) {
    val uiState by readingViewModel.uiState.collectAsState()

    val context = LocalContext.current
    val settingsManager = remember { SettingsManager(context) }
    val scope = rememberCoroutineScope()
    var showRatingGate by remember { mutableStateOf(false) }
    var notesText by remember { mutableStateOf("") }
    var notesSaved by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val journalAnalysis by readingViewModel.journalAnalysis.collectAsState()
    val isAnalyzingJournal by readingViewModel.isAnalyzingJournal.collectAsState()
    var showAnalysisDialog by remember { mutableStateOf(false) }

    if (showAnalysisDialog && journalAnalysis != null) {
        AlertDialog(
            onDismissRequest = { showAnalysisDialog = false; readingViewModel.clearJournalAnalysis() },
            containerColor = CosmicDeep,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    stringResource(R.string.journal_analysis_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = NewsreaderFamily),
                    color = CelestialGold
                )
            },
            text = {
                Text(
                    journalAnalysis!!,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = ManropeFamily, lineHeight = 24.sp),
                    color = StarWhite
                )
            },
            confirmButton = {
                TextButton(onClick = { showAnalysisDialog = false; readingViewModel.clearJournalAnalysis() }) {
                    Text(stringResource(R.string.reading_save), color = CelestialGold)
                }
            }
        )
    }

    LaunchedEffect(journalAnalysis) {
        if (journalAnalysis != null) showAnalysisDialog = true
    }

    LaunchedEffect(Unit) {
        val completedCount = settingsManager.incrementReadingsCompleted()
        val hasRated = settingsManager.hasRatedAppFlow.first()
        if (completedCount >= 2 && !hasRated) {
            delay(1500)
            showRatingGate = true
        }
    }

    if (showRatingGate) {
        RatingGateDialog(onDismiss = {
            showRatingGate = false
            scope.launch { settingsManager.setHasRatedApp(true) }
        })
    }

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
            snackbarHost = { SnackbarHost(snackbarHostState) },
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

                // Journal notes
                Text(
                    text = stringResource(R.string.journal_notes_label),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        letterSpacing = 2.sp
                    ),
                    color = CelestialGold.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                ArtNouveauFrame(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = AstralPurple.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    frameStyle = FrameStyle.ORNATE,
                    backgroundColor = CosmicMid.copy(alpha = 0.2f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = notesText,
                            onValueChange = { notesText = it; notesSaved = false },
                            placeholder = {
                                Text(
                                    stringResource(R.string.journal_notes_hint),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = ManropeFamily),
                                    color = MoonSilver.copy(alpha = 0.4f)
                                )
                            },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = ManropeFamily,
                                color = StarWhite
                            ),
                            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 100.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AstralPurple.copy(alpha = 0.4f),
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = CelestialGold,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            minLines = 3
                        )
                        if (notesText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            ArtNouveauButton(
                                text = if (notesSaved) stringResource(R.string.journal_notes_saved)
                                       else stringResource(R.string.journal_save_notes),
                                onClick = {
                                    uiState.readingId?.let { id ->
                                        readingViewModel.saveNotes(id, notesText)
                                        notesSaved = true
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                context.getString(R.string.journal_notes_saved)
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                variant = ButtonVariant.SECONDARY
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ArtNouveauButton(
                                text = if (isAnalyzingJournal) stringResource(R.string.journal_analyzing)
                                       else stringResource(R.string.journal_analyze_cta),
                                onClick = {
                                    if (!isAnalyzingJournal) {
                                        readingViewModel.analyzeJournalNotes(notesText)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                variant = ButtonVariant.SECONDARY,
                                enabled = !isAnalyzingJournal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Share button
                ArtNouveauButton(
                    text = stringResource(R.string.share_reading),
                    onClick = {
                        scope.launch {
                            val firstCard = uiState.drawnCards.firstOrNull()
                            val imageResName = readingViewModel.getFirstCardImageRes()
                            val cardResId = if (imageResName != null) {
                                context.resources.getIdentifier(imageResName, "drawable", context.packageName)
                            } else 0
                            val cardName = if (firstCard != null) {
                                CardNameTranslator.getDisplayName(context, firstCard.cardId)
                            } else ""
                            val spreadName = uiState.spread.key.replace("_", " ")
                                .replaceFirstChar { it.uppercase() }
                            val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                                .format(Date())
                            ShareUtils.shareReadingAsImage(
                                context = context,
                                cardResId = cardResId,
                                cardName = cardName,
                                spreadType = spreadName,
                                interpretation = uiState.interpretation,
                                date = dateStr
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.SECONDARY
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Done button
                ArtNouveauButton(
                    text = stringResource(R.string.reading_save),
                    onClick = { readingViewModel.resetReading(); onDone() },
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.PRIMARY
                )
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
        }
    }
}
