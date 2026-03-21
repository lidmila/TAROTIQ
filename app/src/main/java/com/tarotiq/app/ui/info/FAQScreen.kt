package com.tarotiq.app.ui.info

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tarotiq.app.R
import com.tarotiq.app.ui.components.AnimatedBackground
import com.tarotiq.app.ui.components.GlassCard
import com.tarotiq.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQScreen(onBack: () -> Unit) {
    val faqItems = listOf(
        R.string.faq_q1 to R.string.faq_a1, R.string.faq_q2 to R.string.faq_a2,
        R.string.faq_q3 to R.string.faq_a3, R.string.faq_q4 to R.string.faq_a4, R.string.faq_q5 to R.string.faq_a5
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(modifier = Modifier.fillMaxSize())
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(title = { Text(stringResource(R.string.faq_title)) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MidnightBg2.copy(alpha = 0.8f)))
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                faqItems.forEach { (qRes, aRes) ->
                    var expanded by remember { mutableStateOf(false) }
                    GlassCard(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row { Text(stringResource(qRes), style = MaterialTheme.typography.titleSmall, color = GoldSecondary, modifier = Modifier.weight(1f))
                                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = TextSecondary) }
                            AnimatedVisibility(visible = expanded) {
                                Text(stringResource(aRes), style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
