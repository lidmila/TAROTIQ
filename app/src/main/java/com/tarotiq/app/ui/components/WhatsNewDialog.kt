package com.tarotiq.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tarotiq.app.R
import com.tarotiq.app.ui.theme.*

@Composable
fun WhatsNewDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(CosmicMid, CosmicDeep)
                    )
                )
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(CelestialGold.copy(alpha = 0.5f), CelestialGoldDim.copy(alpha = 0.3f))
                    ),
                    RoundedCornerShape(20.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "\u2728",
                    fontSize = 36.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.whats_new_title),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = CelestialGold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.whats_new_version),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = SpaceGroteskFamily,
                        letterSpacing = 1.5.sp
                    ),
                    color = MoonSilver
                )
                Spacer(modifier = Modifier.height(16.dp))

                val features = listOf(
                    R.string.whats_new_1,
                    R.string.whats_new_2,
                    R.string.whats_new_3,
                    R.string.whats_new_4,
                    R.string.whats_new_5,
                    R.string.whats_new_6
                )

                features.forEach { resId ->
                    Text(
                        text = stringResource(resId),
                        style = MaterialTheme.typography.bodyMedium,
                        color = StarWhite.copy(alpha = 0.9f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                ArtNouveauButton(
                    text = stringResource(R.string.whats_new_dismiss),
                    onClick = onDismiss,
                    variant = ButtonVariant.PRIMARY,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
