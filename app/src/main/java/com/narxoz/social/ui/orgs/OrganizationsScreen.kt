package com.narxoz.social.ui.orgs

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.narxoz.social.R
import com.narxoz.social.ui.navigation.LocalNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizationsScreen(
    vm: OrganizationsViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val navController = LocalNavController.current

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title   = { Text("Организации") },
                actions = {
                    IconButton(onClick = vm::reload) {
                        Icon(Icons.Default.Refresh, null)
                    }
                }
            )
        }
    ) { inner ->
        when {
            state.isLoading -> Box(
                Modifier.fillMaxSize().padding(inner),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            state.error != null -> Box(
                Modifier.fillMaxSize().padding(inner),
                contentAlignment = Alignment.Center
            ) { Text(state.error!!) }

            else -> LazyColumn(
                contentPadding = PaddingValues(12.dp),
                modifier       = Modifier.padding(inner)
            ) {
                items(state.items) { org ->
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clickable { navController.navigate("organization/${'$'}{org.id}") },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = org.avatarUrl ?: "",
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    placeholder = painterResource(R.drawable.placeholder),
                                    error       = painterResource(R.drawable.placeholder)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(org.title,    style = MaterialTheme.typography.titleMedium)
                                    Text(org.subtitle, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            val btnText = if (org.joined) "Выйти" else "Вступить"
                            TextButton(onClick = { if (org.joined) vm.leave(org.id) else vm.join(org.id) }) {
                                Text(btnText)
                            }
                        }
                    }
                }
            }
        }
    }
}