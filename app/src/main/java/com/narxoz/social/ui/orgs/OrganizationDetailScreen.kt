package com.narxoz.social.ui.orgs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.narxoz.social.repository.AuthRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizationDetailScreen(
    orgId: Int,
    onBack: () -> Unit = {},
    onEdit: () -> Unit = {},
) {
    val vm: OrganizationDetailViewModel = viewModel(factory = OrganizationDetailVmFactory(orgId))
    val state by vm.state.collectAsState()
    val myId = AuthRepository.getUserId()
    val role = AuthRepository.getUserRole()
    val canEdit = myId == orgId && role == "organization"

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Организация") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (canEdit) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
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
                state.profile != null -> {
                    val prof = state.profile
                    prof?.avatarPath?.let { url ->
                        AsyncImage(
                            model = url.replace("127.0.0.1", "10.0.2.2"),
                            contentDescription = null,
                            modifier = Modifier.size(120.dp)
                        )
                    }
                    Text(prof?.fullName ?: prof?.nickname ?: "ID ${prof?.id}", style = MaterialTheme.typography.headlineSmall)
                    val joined = state.friendStatus == "friends"
                    val btnText = if (joined) "Выйти" else "Вступить"
                    TextButton(onClick = { if (joined) vm.leave() else vm.join() }) {
                        Text(btnText)
                    }
                }
            }
        }
    }
}
