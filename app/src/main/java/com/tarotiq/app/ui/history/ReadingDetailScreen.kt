package com.tarotiq.app.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tarotiq.app.R
import com.tarotiq.app.domain.model.TarotReading
import com.tarotiq.app.ui.components.AnimatedBackground
import com.tarotiq.app.ui.components.GlassCard
import com.tarotiq.app.ui.theme.*
import com.tarotiq.app.viewmodel.ReadingHistoryViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingDetailScreen(
    readingId: String,
    onBack: () -> Unit,
    historyViewModel: ReadingHistoryViewModel = viewModel()
) {
    var reading by remember { mutableStateOf<TarotReading?>(null) }
    val scope = rememberCoroutineScope()
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    LaunchedEffect(readingId) {
        reading = historyViewModel.getReadingById(readingId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(modifier = Modifier.fillMaxSize())
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(reading?.topic?.replaceFirstChar { it.uppercase() } ?: "") },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                    actions = {
                        reading?.let { r ->
                            IconButton(onClick = {
                                scope.launch { historyViewModel.deleteReading(r); onBack() }
                            }) { Icon(Icons.Default.Delete, null, tint = ErrorColor) }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MidnightBg2.copy(alpha = 0.8f))
                )
            }
        ) { padding ->
            reading?.let { r ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState())
                ) {
                    // Info
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(dateFormat.format(Date(r.timestamp)), style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                            r.question?.let { Text(it, style = MaterialTheme.typography.bodyLarge, color = GoldSecondary, modifier = Modifier.padding(top = 8.dp)) }
                            Text("${r.spreadType} · ${r.coinsCost} coins", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Interpretation
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(r.aiInterpretation, style = MaterialTheme.typography.bodyLarge, color = TextPrimary, modifier = Modifier.padding(16.dp))
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}
