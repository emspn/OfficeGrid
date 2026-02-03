package com.app.officegrid.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.officegrid.ui.theme.*

/**
 * Beautiful error state with retry option
 */
@Composable
fun ErrorState(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Error Icon
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = ProfessionalError.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Error Title
        Text(
            text = "Oops! Something went wrong",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = DeepCharcoal,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Error Message
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = StoneGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        if (onRetry != null) {
            Spacer(modifier = Modifier.height(32.dp))

            // Retry Button
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProfessionalError
                ),
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    "TRY AGAIN",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}

/**
 * Empty state with action
 */
@Composable
fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Empty Icon
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = WarmBorder
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Empty Title
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = DeepCharcoal,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Empty Message
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = StoneGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(32.dp))

            // Action Button
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepCharcoal
                ),
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    actionText,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }
        }
    }
}

