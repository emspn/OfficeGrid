package com.app.officegrid.core.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.officegrid.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Beautiful custom snackbar with animations and colors
 */
@Composable
fun CustomSnackbar(
    message: String,
    type: SnackbarType = SnackbarType.INFO,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(3000) // Show for 3 seconds
        visible = false
        delay(300) // Wait for animation
        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300)
        ) + fadeOut()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = when (type) {
                SnackbarType.SUCCESS -> ProfessionalSuccess
                SnackbarType.ERROR -> ProfessionalError
                SnackbarType.WARNING -> ProfessionalWarning
                SnackbarType.INFO -> DeepCharcoal
            },
            shadowElevation = 8.dp,
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (type) {
                        SnackbarType.SUCCESS -> Icons.Default.CheckCircle
                        SnackbarType.ERROR -> Icons.Default.Error
                        SnackbarType.WARNING -> Icons.Default.Warning
                        SnackbarType.INFO -> Icons.Default.Info
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = message,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

enum class SnackbarType {
    SUCCESS, ERROR, WARNING, INFO
}

