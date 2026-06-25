package com.tarotiq.app.ui.components

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.android.play.core.review.ReviewManagerFactory
import com.tarotiq.app.R
import com.tarotiq.app.ui.theme.*

private const val FEEDBACK_EMAIL = "support@tarotiq.app"

@Composable
fun RatingGateDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    var selectedStars by remember { mutableIntStateOf(0) }
    var phase by remember { mutableStateOf("rating") }

    Dialog(onDismissRequest = onDismiss) {
        ArtNouveauFrame(
            frameStyle = FrameStyle.ORNATE,
            backgroundColor = CosmicDeep.copy(alpha = 0.97f)
        ) {
            AnimatedContent(
                targetState = phase,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "rating_phase"
            ) { currentPhase ->
                when (currentPhase) {
                    "rating" -> RatingPhase(
                        selectedStars = selectedStars,
                        onStarSelected = { selectedStars = it },
                        onSubmit = {
                            phase = "thanks"
                            if (selectedStars >= 4) {
                                activity?.let { act ->
                                    val manager = ReviewManagerFactory.create(act)
                                    manager.requestReviewFlow().addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            manager.launchReviewFlow(act, task.result)
                                        }
                                    }
                                }
                            } else {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:$FEEDBACK_EMAIL")
                                    putExtra(Intent.EXTRA_SUBJECT, "TarotIQ Feedback")
                                }
                                runCatching {
                                    context.startActivity(Intent.createChooser(intent, null))
                                }
                            }
                        },
                        onSkip = onDismiss
                    )
                    "thanks" -> ThanksPhase(onDismiss = onDismiss)
                }
            }
        }
    }
}

@Composable
private fun RatingPhase(
    selectedStars: Int,
    onStarSelected: (Int) -> Unit,
    onSubmit: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier.padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "✨",
            fontSize = 40.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            text = stringResource(R.string.rating_title),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = NewsreaderFamily,
                fontWeight = FontWeight.Normal
            ),
            color = StarWhite,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.rating_subtitle),
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = ManropeFamily),
            color = MoonSilver,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            (1..5).forEach { star ->
                Icon(
                    imageVector = if (star <= selectedStars) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "$star stars",
                    tint = if (star <= selectedStars) CelestialGold else MoonSilver.copy(alpha = 0.35f),
                    modifier = Modifier
                        .size(44.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onStarSelected(star) }
                )
            }
        }

        if (selectedStars > 0) {
            Spacer(modifier = Modifier.height(24.dp))
            ArtNouveauButton(
                text = if (selectedStars >= 4)
                    stringResource(R.string.rating_submit_positive)
                else
                    stringResource(R.string.rating_submit_negative),
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                variant = if (selectedStars >= 4) ButtonVariant.PRIMARY else ButtonVariant.SECONDARY
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        TextButton(onClick = onSkip) {
            Text(
                text = stringResource(R.string.rating_skip),
                color = MoonSilver.copy(alpha = 0.45f),
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = ManropeFamily)
            )
        }
    }
}

@Composable
private fun ThanksPhase(onDismiss: () -> Unit) {
    Column(
        modifier = Modifier.padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🙏",
            fontSize = 40.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            text = stringResource(R.string.rating_thanks),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontFamily = NewsreaderFamily,
                fontWeight = FontWeight.Normal
            ),
            color = CelestialGold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.rating_thanks_desc),
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = ManropeFamily),
            color = MoonSilver,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        ArtNouveauButton(
            text = stringResource(R.string.close),
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            variant = ButtonVariant.PRIMARY
        )
    }
}
