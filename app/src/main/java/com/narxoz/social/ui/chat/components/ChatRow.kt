package com.narxoz.social.ui.chat.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Badge
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.narxoz.social.network.dto.ChatShortDto

@Composable
fun ChatRow(chat: ChatShortDto, navController: NavController) {
    ListItem(
        headlineContent = { Text(chat.name ?: "") },
        supportingContent = {
            chat.lastMessage?.let { msg ->
                Text(msg.text, maxLines = 1)
            }
        },
        trailingContent = {
            if (chat.unread > 0) {
                Badge { Text(chat.unread.toString()) }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("chat/${chat.id}") }
    )
}