package com.narxoz.social.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.narxoz.social.api.ClubDto

@Composable
fun ClubAvatarItemRemote(club: ClubDto) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        /* круглая карточка‑контейнер */
        Card(
            shape   = CircleShape,
            colors  = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = .2f)),
            modifier = Modifier.size(64.dp)
        ) {
            AsyncImage(
                model         = club.avatarUrl,
                contentScale  = ContentScale.Crop,
                contentDescription = club.title,
                modifier      = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text  = club.title,
            style = MaterialTheme.typography.labelSmall
        )
    }
}