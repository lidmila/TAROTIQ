package com.tarotiq.app.ui.info

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tarotiq.app.R
import com.tarotiq.app.ui.components.AnimatedBackground
import com.tarotiq.app.ui.components.GlassCard
import com.tarotiq.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(modifier = Modifier.fillMaxSize())
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(title = { Text(stringResource(R.string.contact_title)) },
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MidnightBg2.copy(alpha = 0.8f)))
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text(stringResource(R.string.contact_subject)) },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text(stringResource(R.string.contact_message)) },
                            modifier = Modifier.fillMaxWidth().height(200.dp), shape = RoundedCornerShape(12.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:lidmilamarsalkova@gmail.com")
                            putExtra(Intent.EXTRA_SUBJECT, "[TAROTIQ] $subject")
                            putExtra(Intent.EXTRA_TEXT, message)
                        }
                        context.startActivity(Intent.createChooser(intent, "Send email"))
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = subject.isNotBlank() && message.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = MysticPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Send, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.contact_send))
                }
            }
        }
    }
}
