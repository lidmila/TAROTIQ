package com.tarotiq.app.ui.info

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tarotiq.app.BuildConfig
import com.tarotiq.app.R
import com.tarotiq.app.ui.components.*
import com.tarotiq.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        MysticBackground(modifier = Modifier.fillMaxSize())
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.about_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = CelestialGold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                null,
                                tint = AstralPurple
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CosmicDeep.copy(alpha = 0.85f))
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(
                    "TAROTIQ",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = NewsreaderFamily,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 4.sp
                    ),
                    color = CelestialGold
                )
                Text(
                    stringResource(R.string.about_version, BuildConfig.VERSION_NAME).uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = SpaceGroteskFamily),
                    color = MoonSilver,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.about_description),
                        style = MaterialTheme.typography.bodyLarge, color = StarWhite.copy(alpha = 0.9f), textAlign = TextAlign.Center, modifier = Modifier.padding(24.dp))
                }
            }
        }
    }
}
