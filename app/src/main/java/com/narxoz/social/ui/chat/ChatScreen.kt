package com.narxoz.social.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier

@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
    val msgs by viewModel.messages.collectAsState()

    Column {
        LazyColumn(reverseLayout = true) {
            items(msgs.reversed()) { m ->
                Text(text = "${m.sender}: ${m.text}")
                Text(
                    text = m.createdAt.orEmpty(),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        var input by remember { mutableStateOf("") }
        Row {
            TextField(
                modifier = Modifier.weight(1f),
                value = input,
                onValueChange = { input = it }
            )
            IconButton(onClick = {
                if (input.isNotBlank()) {
                    viewModel.sendMessage(input.trim())
                    input = ""
                }
            }) {
                Icon(Icons.Filled.Send, contentDescription = null)
            }
        }
    }
}