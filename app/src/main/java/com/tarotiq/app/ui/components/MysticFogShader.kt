package com.tarotiq.app.ui.components

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import com.tarotiq.app.ui.theme.AstralGlow
import com.tarotiq.app.ui.theme.AstralPurpleDim
import com.tarotiq.app.ui.theme.CelestialGold
import com.tarotiq.app.ui.theme.VoidBlack
import kotlin.math.sin

/**
 * AGSL shader pro efekt pomalu se prevalejici mysticky fialovy mlhy.
 *
 * Na API 33+ pouziva RuntimeShader (AGSL) -- plynuly, GPU-akcelerovany efekt.
 * Na starsich API fallback na Canvas-based multi-layer mlhu.
 */

private const val FOG_SHADER_SRC = """
    uniform float2 iResolution;
    uniform float iTime;

    // Simplex-inspired hash (fast, GPU-friendly)
    float hash(float2 p) {
        float3 p3 = fract(float3(p.xyx) * 0.1031);
        p3 += dot(p3, p3.yzx + 33.33);
        return fract((p3.x + p3.y) * p3.z);
    }

    // Value noise
    float noise(float2 p) {
        float2 i = floor(p);
        float2 f = fract(p);
        f = f * f * (3.0 - 2.0 * f); // smoothstep

        float a = hash(i);
        float b = hash(i + float2(1.0, 0.0));
        float c = hash(i + float2(0.0, 1.0));
        float d = hash(i + float2(1.0, 1.0));

        return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
    }

    // Fractal Brownian Motion -- 4 octaves pro bohatou mlhu
    float fbm(float2 p) {
        float value = 0.0;
        float amplitude = 0.5;
        float frequency = 1.0;
        for (int i = 0; i < 4; i++) {
            value += amplitude * noise(p * frequency);
            frequency *= 2.0;
            amplitude *= 0.5;
        }
        return value;
    }

    // Domain warping -- mlha se prevali organicky
    float warpedFog(float2 uv, float time) {
        float2 q = float2(
            fbm(uv + float2(0.0, 0.0) + time * 0.08),
            fbm(uv + float2(5.2, 1.3) + time * 0.06)
        );
        float2 r = float2(
            fbm(uv + 4.0 * q + float2(1.7, 9.2) + time * 0.04),
            fbm(uv + 4.0 * q + float2(8.3, 2.8) + time * 0.05)
        );
        return fbm(uv + 4.0 * r);
    }

    half4 main(float2 fragCoord) {
        float2 uv = fragCoord / iResolution;

        // Skala pro mlhovy efekt
        float2 fogUV = uv * 3.0;

        // Dve vrstvy mlhy s ruznou rychlosti
        float fog1 = warpedFog(fogUV, iTime);
        float fog2 = warpedFog(fogUV * 0.7 + float2(3.0, 1.0), iTime * 0.7);

        // Kombinace vrstev
        float fog = fog1 * 0.6 + fog2 * 0.4;

        // Celestial Archive palette
        half3 deepVoid   = half3(0.06, 0.05, 0.09);   // #0f0d16
        half3 astralPurp = half3(0.45, 0.37, 0.75);    // lavender mist
        half3 cosmicGlow = half3(0.80, 0.74, 1.00);    // light lavender #cbbeff
        half3 goldHint   = half3(0.91, 0.76, 0.29);    // gold #e9c349

        // Barva mlhy podle intenzity
        half3 col = deepVoid;
        col = mix(col, astralPurp, smoothstep(0.2, 0.6, fog));
        col = mix(col, cosmicGlow, smoothstep(0.55, 0.85, fog) * 0.4);

        // Zlaty nadech na nejsvetlejsich mistech
        col = mix(col, goldHint, smoothstep(0.75, 0.95, fog) * 0.12);

        // Vignetace -- okraje tmavsi
        float vignette = 1.0 - length((uv - 0.5) * 1.4);
        vignette = smoothstep(0.0, 0.7, vignette);
        col *= half3(vignette);

        // Alpha: mlha je pruhledna na tmavych mistech, viditelna na svetlych
        float alpha = smoothstep(0.15, 0.5, fog) * 0.55 * vignette;

        return half4(col * half3(alpha), alpha);
    }
"""

@Composable
fun MysticFogOverlay(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "fog_time")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 600f, // 600 sekund loop
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fog_time"
    )

    // AGSL RuntimeShader crashes on many devices due to driver issues
    // with createRuntimeShaderEffect uniform name resolution.
    // Using Canvas fallback universally for stability.
    FogFallback(time = time, modifier = modifier)
}

private data class FogBlob(
    val xPhase: Float, val yPhase: Float,
    val xSpeed: Float, val ySpeed: Float,
    val radius: Float, val color: Color
)

@Composable
private fun FogFallback(time: Float, modifier: Modifier = Modifier) {
    val blobs = remember {
        listOf(
            FogBlob(0f, 0f, 0.13f, 0.09f, 0.7f, AstralPurpleDim.copy(alpha = 0.12f)),
            FogBlob(2f, 1f, 0.08f, 0.11f, 0.6f, AstralGlow.copy(alpha = 0.08f)),
            FogBlob(4f, 3f, 0.1f, 0.07f, 0.55f, AstralPurpleDim.copy(alpha = 0.10f)),
            FogBlob(1f, 5f, 0.06f, 0.12f, 0.5f, CelestialGold.copy(alpha = 0.04f))
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        for (blob in blobs) {
            val cx = w * (0.5f + 0.3f * sin(time * blob.xSpeed + blob.xPhase))
            val cy = h * (0.5f + 0.2f * sin(time * blob.ySpeed + blob.yPhase))
            val r = w * blob.radius

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(blob.color, Color.Transparent),
                    center = Offset(cx, cy),
                    radius = r
                ),
                center = Offset(cx, cy),
                radius = r
            )
        }
    }
}
