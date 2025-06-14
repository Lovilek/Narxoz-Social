package com.narxoz.social.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.narxoz.social.R
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import com.narxoz.social.ui.navigation.LocalNavController

@Composable
fun PostCard(
    post: Post,
    onLike: (Int) -> Unit,
    onShare: (Post) -> Unit
) {
    val likeIcon  =
        if (post.likedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder
    val likeTint  =
        if (post.likedByMe) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant
    val navController = LocalNavController.current
    /* локальная переменная – доступна всему composable */
    val cardColor = MaterialTheme.colorScheme.surfaceContainerLow
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { navController.navigate("post/${post.id}") },

        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column {

            /* ---- СЕТЕВАЯ КАРТИНКА ---- */
            AsyncImage(
                model = post.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                placeholder = painterResource(R.drawable.placeholder),
                error = painterResource(R.drawable.placeholder)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = post.author,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
            Spacer(Modifier.height(8.dp))

            /* ---- Блок экшен-кнопок (лайк, комментарии, share) ---- */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                /* --- Like --- */
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onLike(post.id) }) {
                        val icon  = if (post.likedByMe) Icons.Filled.Favorite
                        else Icons.Outlined.FavoriteBorder
                        val tint  = if (post.likedByMe) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                        Icon(icon, contentDescription = "Like", tint = tint)
                    }
                    Text(
                        text   = post.likes.toString(),
                        style  = MaterialTheme.typography.labelMedium,
                        color  = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable {
                            navController.navigate("likes/${post.id}")
                        }
                    )
                }

                /* --- Comments --- */
                IconButton( onClick = { navController.navigate("comments/${post.id}") } ) {
                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Comments")
                }

                /* --- Share --- */
                IconButton(onClick = { onShare(post) }) {
                    Icon(Icons.Outlined.Share, contentDescription = "Share")
                }
            }
        }
    }
}