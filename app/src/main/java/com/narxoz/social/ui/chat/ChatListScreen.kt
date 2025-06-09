package com.narxoz.social.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.narxoz.social.repository.AuthRepository
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavController,
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val chats by viewModel.chats.collectAsState()
    val role = remember { AuthRepository.getUserRole() }
    val allowCreate = role in listOf("teacher", "organization")
    var showDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var members by remember { mutableStateOf("") }

    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text("Мои чаты") })
    }, floatingActionButton = {
        if (allowCreate) {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
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
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Создать группу") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Название") }
                    )
                    OutlinedTextField(
                        value = members,
                        onValueChange = { members = it },
                        label = { Text("ID участников, через запятую") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val ids = members.split(',')
                            .mapNotNull { it.trim().toIntOrNull() }
                        viewModel.createGroup(name, ids)
                        showDialog = false
                        name = ""
                        members = ""
                    },
                    enabled = name.isNotBlank() && members.isNotBlank()
                ) { Text("Создать") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Отмена") }
            }
        )
    }
}