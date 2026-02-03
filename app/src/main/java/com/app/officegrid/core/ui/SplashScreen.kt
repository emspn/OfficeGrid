package com.app.officegrid.core.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.officegrid.core.ui.components.OfficeGridLogo
import com.app.officegrid.ui.theme.*
import kotlinx.coroutines.delay

/**
 * ✨ ELITE SPLASH SCREEN
 * Professional brand reveal using the OfficeGrid logo.
 */
@Composable
fun SplashScreen(
    onTimeout: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "alpha"
    )

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.7f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500)
        onTimeout()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DeepCharcoal
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(alpha)
                    .scale(scale)
            ) {
                // ✅ LOGO INTEGRATION (Fixed: Removed non-existent 'shape' parameter)
                OfficeGridLogo(
                    size = 120.dp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "OFFICEGRID",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 6.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "SECURE_PROTOCOL_ACTIVE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = ProfessionalSuccess.copy(alpha = 0.8f)
                )
            }
            
            // Bottom progress bar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 64.dp)
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .width(150.dp)
                        .height(1.dp),
                    color = ProfessionalSuccess,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }
        }
    }
}
