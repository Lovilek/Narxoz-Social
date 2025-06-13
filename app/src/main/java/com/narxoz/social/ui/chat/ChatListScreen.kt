package com.narxoz.social.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.narxoz.social.repository.AuthRepository
import com.narxoz.social.ui.chat.components.ChatRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.pullrefresh.*
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavController,
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val chats by viewModel.chats.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val role = remember { AuthRepository.getUserRole() }
    val allowCreate = role in listOf("teacher", "organization")
    var showDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var members by remember { mutableStateOf("") }

    val pullState = rememberPullRefreshState(
        refreshing = loading,
        onRefresh = { viewModel.refresh() }
    )

    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = { Text("Мои чаты") },
            actions = {
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                }
            }
        )
    }, floatingActionButton = {
        if (allowCreate) {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    }) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .pullRefresh(pullState)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues()
            ) {
                items(chats) { chat ->
                    ChatRow(chat = chat, navController = navController)
                    Divider()
                }
            }
            PullRefreshIndicator(
                refreshing = loading,
                state = pullState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
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
