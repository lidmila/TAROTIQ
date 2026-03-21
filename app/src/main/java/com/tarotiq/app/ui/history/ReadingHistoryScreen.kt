package com.tarotiq.app.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tarotiq.app.R
import com.tarotiq.app.domain.model.TarotReading
import com.tarotiq.app.ui.components.AnimatedBackground
import com.tarotiq.app.ui.components.GlassCard
import com.tarotiq.app.ui.theme.*
import com.tarotiq.app.viewmodel.ReadingHistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingHistoryScreen(
    onReadingClick: (String) -> Unit,
    historyViewModel: ReadingHistoryViewModel = viewModel()
) {
    val readings by historyViewModel.readings.collectAsState(initial = emptyList())
    var selectedFilter by remember { mutableStateOf("all") }
    val filters = listOf("all" to "Vše", "love" to "Láska", "career" to "Kariéra", "general" to "Obecné", "yes_no" to "Ano/Ne", "spiritual" to "Duchovní")
    val filteredReadings = if (selectedFilter == "all") readings else readings.filter { it.topic == selectedFilter }
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(modifier = Modifier.fillMaxSize())
        Scaffold(containerColor = Color.Transparent) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Filter chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters) { (key, label) ->
                        FilterChip(
                            selected = selectedFilter == key,
                            onClick = { selectedFilter = key },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MysticPrimary.copy(alpha = 0.3f))
                        )
                    }
                }

                if (filteredReadings.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.history_empty), color = TextSecondary, style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredReadings, key = { it.id }) { reading ->
                            GlassCard(onClick = { onReadingClick(reading.id) }, modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        when (reading.spreadType) {
                                            "single" -> Icons.Default.Style
                                            "three_card" -> Icons.Default.ViewColumn
                                            "celtic_cross" -> Icons.Default.GridView
                                            else -> Icons.Default.Layers
                                        },
                                        null, tint = GoldSecondary, modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(reading.topic.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                                        Text(dateFormat.format(Date(reading.timestamp)), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                        Text(reading.aiInterpretation.take(80) + "...", style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
