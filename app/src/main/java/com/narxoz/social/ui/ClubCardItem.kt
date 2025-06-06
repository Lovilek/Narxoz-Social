package com.narxoz.social.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun ClubCardItem(club: Club) {
    Card(
        modifier = Modifier.size(width = 160.dp, height = 100.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp), // скругляем углы
        elevation = CardDefaults.cardElevation(4.dp) // для тени
    ) {
        Box {
            Image(
                painter = painterResource(club.bannerRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Текст названия клуба
            Text(
                text = club.title,
                style = MaterialTheme.typography.bodyMedium.copy(color = androidx.compose.ui.graphics.Color.White),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                color = Color.White
            )
        }
    }
}