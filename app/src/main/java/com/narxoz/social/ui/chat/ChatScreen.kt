package com.narxoz.social.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.narxoz.social.repository.AuthRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBack: () -> Unit = {},
    viewModel: ChatViewModel = hiltViewModel()
) {
    val msgs by viewModel.messages.collectAsState()
    val myId = remember { AuthRepository.getUserId() }
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Чат") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { inner ->
        Column(Modifier.padding(inner)) {
            LazyColumn(
                reverseLayout = true,
                state = listState,
                modifier = Modifier.weight(1f)
            ) {
                items(msgs.reversed()) { m ->
                    MessageBubble(
                        message = m,
                        isMine = m.sender == myId
                    )
                }
            }

            var input by remember { mutableStateOf("") }
            Row {
                TextField(
                    modifier = Modifier.weight(1f),
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Сообщение") }
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
}

@Composable
private fun MessageBubble(message: com.narxoz.social.network.dto.MessageDto, isMine: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isMine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                if (!isMine) {
                    Text(
                        text = message.sender.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(message.text)
                message.createdAt?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
