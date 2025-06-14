package com.narxoz.social.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnotherProfileScreen(userId: Int, onBack: () -> Unit = {}) {
    val vm: AnotherProfileViewModel = viewModel(factory = AnotherProfileVmFactory(userId))
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Профиль") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(inner)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when {
                state.isLoading -> CircularProgressIndicator()
                state.error != null -> Text(state.error!!, color = MaterialTheme.colorScheme.error)
                state.profile != null -> AnotherProfileContent(state)
            }
        }
    }
}

@Composable
private fun AnotherProfileContent(state: AnotherProfileState) {
    val profile = state.profile ?: return
    profile.avatarPath?.let { url ->
        AsyncImage(
            model = url.replace("127.0.0.1", "159.65.124.242"),
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )
    }
    Text(
        profile.fullName ?: profile.nickname ?: "ID ${profile.id}",
        style = MaterialTheme.typography.headlineSmall
    )
    state.friendStatus?.let { status ->
        Text("Статус: $status")
    }
}