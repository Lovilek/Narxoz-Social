package com.narxoz.social.ui.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.DismissDirection as MaterialDismissDirection
import androidx.compose.material.DismissValue as MaterialDismissValue
import androidx.compose.material.rememberDismissState as rememberMaterialDismissState
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit = {},
    vm: NotificationsViewModel = viewModel()
) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Уведомления") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { inner ->
        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            state.error != null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner),
                contentAlignment = Alignment.Center
            ) { Text(state.error ?: "", color = MaterialTheme.colorScheme.error) }

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.notifications) { notif ->
                    val dismissState = rememberMaterialDismissState { value ->
                        if (value != MaterialDismissValue.Default) {
                            vm.markRead(notif.id)
                            false
                        } else {
                            true
                        }
                    }

                    SwipeToDismiss(
                        state = dismissState,
                        directions = setOf(
                            MaterialDismissDirection.EndToStart,
                            MaterialDismissDirection.StartToEnd
                        ),
                        background = {
                            val color by animateColorAsState(
                                if (dismissState.targetValue == MaterialDismissValue.Default)
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                else
                                    MaterialTheme.colorScheme.primaryContainer
                            )
                            val alignment = when (dismissState.dismissDirection) {
                                MaterialDismissDirection.StartToEnd -> Alignment.CenterStart
                                MaterialDismissDirection.EndToStart -> Alignment.CenterEnd
                                null -> Alignment.CenterStart
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color)
                                    .padding(horizontal = 24.dp),
                                contentAlignment = alignment
                            ) {
                                Icon(Icons.Default.Done, contentDescription = null)
                            }
                        }
                    ) {
                        ListItem(
                            headlineContent = { Text(notif.text) },
                            trailingContent = {
                                if (notif.type == "friend_request" && !notif.isRead) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(onClick = {
                                            notif.requestId?.let { vm.respondToFriendRequest(it, true, notif.id) }
                                        }) { Icon(Icons.Default.Check, null) }
                                        IconButton(onClick = {
                                            notif.requestId?.let { vm.respondToFriendRequest(it, false, notif.id) }
                                        }) { Icon(Icons.Default.Close, null) }
                                    }
                                } else if (!notif.isRead) {
                                    Badge { }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                    Divider()
                }
            }
        }
    }
}