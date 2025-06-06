package com.narxoz.social.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.narxoz.social.api.ClubDto
import com.narxoz.social.ui.theme.ThemeTokens

@Composable
fun ClubCardItemRemote(club: ClubDto) {
    Card(
        colors    = CardDefaults.cardColors(containerColor = ThemeTokens.cardSurface),
        modifier = Modifier
            .width(180.dp)
            .height(110.dp),
        shape  = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box {
            AsyncImage(
                model         = club.bannerUrl ?: club.avatarUrl,
                contentScale  = ContentScale.Crop,
                contentDescription = club.title,
                modifier      = Modifier.fillMaxSize()
            )
            Text(
                text      = club.title,
                style     = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onPrimary),
                modifier  = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            )
        }
    }
}