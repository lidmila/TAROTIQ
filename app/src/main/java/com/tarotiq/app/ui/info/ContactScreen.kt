package com.tarotiq.app.ui.info

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tarotiq.app.R
import com.tarotiq.app.ui.components.*
import com.tarotiq.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        MysticBackground(modifier = Modifier.fillMaxSize())
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.contact_title),
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
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            label = { Text(stringResource(R.string.contact_subject)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AstralPurple,
                                unfocusedBorderColor = GlassBorder,
                                focusedContainerColor = CosmicMid.copy(alpha = 0.5f),
                                unfocusedContainerColor = CosmicMid.copy(alpha = 0.3f),
                                cursorColor = CelestialGold,
                                focusedTextColor = StarWhite,
                                unfocusedTextColor = StarWhite
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = message,
                            onValueChange = { message = it },
                            label = { Text(stringResource(R.string.contact_message)) },
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AstralPurple,
                                unfocusedBorderColor = GlassBorder,
                                focusedContainerColor = CosmicMid.copy(alpha = 0.5f),
                                unfocusedContainerColor = CosmicMid.copy(alpha = 0.3f),
                                cursorColor = CelestialGold,
                                focusedTextColor = StarWhite,
                                unfocusedTextColor = StarWhite
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                ArtNouveauButton(
                    text = stringResource(R.string.contact_send),
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:lidmilamarsalkova@gmail.com")
                            putExtra(Intent.EXTRA_SUBJECT, "[TAROTIQ] $subject")
                            putExtra(Intent.EXTRA_TEXT, message)
                        }
                        context.startActivity(Intent.createChooser(intent, "Send email"))
                    },
                    variant = ButtonVariant.PRIMARY,
                    enabled = subject.isNotBlank() && message.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
