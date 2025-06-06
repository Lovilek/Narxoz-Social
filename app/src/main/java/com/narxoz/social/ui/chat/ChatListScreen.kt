package com.narxoz.social.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavController,
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val chats by viewModel.chats.collectAsState()

    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text("Мои чаты") })
    }) { padding ->
        // Не забываем наружный padding, если нужен
        LazyColumn(contentPadding = padding) {

            items(chats) { chat ->
                ListItem(
                    headlineContent   = { Text(chat.name ?: "Direct") },
                    supportingContent = { Text("Непрочитано: ${chat.unread}") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("chat/${chat.id}")
                        }
                )
                Divider()
            }
        }
    }
}