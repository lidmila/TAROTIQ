package com.tarotiq.app.ui.info

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tarotiq.app.BuildConfig
import com.tarotiq.app.R
import com.tarotiq.app.ui.components.AnimatedBackground
import com.tarotiq.app.ui.components.GlassCard
import com.tarotiq.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(modifier = Modifier.fillMaxSize())
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(title = { Text(stringResource(R.string.about_title)) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MidnightBg2.copy(alpha = 0.8f)))
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("TAROTIQ", style = MaterialTheme.typography.displaySmall, color = GoldSecondary)
                Text(stringResource(R.string.about_version, BuildConfig.VERSION_NAME), style = MaterialTheme.typography.bodyMedium, color = TextSecondary, modifier = Modifier.padding(top = 8.dp))
                Spacer(modifier = Modifier.height(24.dp))
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Your mystical AI tarot reader.\nDiscover insights through the ancient art of tarot, powered by modern AI.\n\nTAROTIQ combines centuries of tarot wisdom with cutting-edge technology to offer personalized, interactive readings.",
                        style = MaterialTheme.typography.bodyLarge, color = TextPrimary, textAlign = TextAlign.Center, modifier = Modifier.padding(24.dp))
                }
            }
        }
    }
}
