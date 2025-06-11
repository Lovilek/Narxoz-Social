package com.narxoz.social.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.narxoz.social.api.NotificationDto
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val vm: NotificationsViewModel = viewModel()
    val state by vm.state.collectAsState()

    val pull = rememberPullRefreshState(
        refreshing = state.isLoading,
        onRefresh = { vm.reload() }
    )

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { inner ->
        Box(Modifier.pullRefresh(pull).padding(inner)) {
            LazyColumn(contentPadding = PaddingValues(12.dp)) {
                items(state.list, key = { it.id }) { notif ->
                    val dismissState = rememberDismissState()
                    if (dismissState.isDismissed(androidx.compose.material3.DismissDirection.EndToStart)) {
                        vm.markRead(notif.id)
                    }
                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {},
                        content = { NotificationRow(notif) }
                    )
                }
            }
            PullRefreshIndicator(state.isLoading, pull, Modifier.align(Alignment.TopCenter))
        }
    }
}

@Composable
private fun NotificationRow(n: NotificationDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(8.dp)) {
            Text(n.text)
            if (!n.isRead) {
                Text("Unread", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
