package com.app.officegrid.core.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.app.officegrid.ui.theme.*

/**
 * Shimmer loading effect for cards
 * Shows while content is loading
 */
@Composable
fun ShimmerLoadingCard(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 2.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Title shimmer
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = shimmerBrush(translateAnim)
                    )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description shimmer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = shimmerBrush(translateAnim)
                    )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        brush = shimmerBrush(translateAnim)
                    )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Footer shimmer
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            brush = shimmerBrush(translateAnim)
                        )
                )

                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            brush = shimmerBrush(translateAnim)
                        )
                )
            }
        }
    }
}

private fun shimmerBrush(translateAnim: Float): androidx.compose.ui.graphics.Brush {
    return androidx.compose.ui.graphics.Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFEEEEEE),
            Color(0xFFDDDDDD),
            Color(0xFFEEEEEE)
        ),
        startX = translateAnim - 1000f,
        endX = translateAnim
    )
}

/**
 * Show shimmer cards while loading
 */
@Composable
fun ShimmerLoadingList(
    count: Int = 3
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(24.dp)
    ) {
        repeat(count) {
            ShimmerLoadingCard()
        }
    }
}

