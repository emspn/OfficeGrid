package com.app.officegrid.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.officegrid.R

/**
 * ✨ OFFICEGRID BRAND LOGO
 * Programmatically cropped to focus on the core logo and remove excessive padding.
 */
@Composable
fun OfficeGridLogo(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    // We use a Box as a viewport and scale the image inside it to "crop" the padding.
    Box(
        modifier = modifier
            .size(size)
            .clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_1),
            contentDescription = "OfficeGrid Logo",
            modifier = Modifier.size(size * 1.6f), // Zoom in by 60% to crop white space
            contentScale = ContentScale.Fit
        )
    }
}
