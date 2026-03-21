package com.tarotiq.app.ui.reading

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tarotiq.app.R
import com.tarotiq.app.ui.components.AnimatedBackground
import com.tarotiq.app.ui.components.GlassCard
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
        AnimatedBackground(modifier = Modifier.fillMaxSize())
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.reading_summary)) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MidnightBg2.copy(alpha = 0.8f))
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Topic & Question
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(uiState.topic.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium, color = GoldSecondary)
                        uiState.question?.let {
                            Text(it, style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Interpretation
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(uiState.interpretation, style = MaterialTheme.typography.bodyLarge, color = TextPrimary, modifier = Modifier.padding(16.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))

                // Done button
                Button(
                    onClick = { readingViewModel.resetReading(); onDone() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MysticPrimary)
                ) {
                    Icon(Icons.Default.Done, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.reading_save))
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
