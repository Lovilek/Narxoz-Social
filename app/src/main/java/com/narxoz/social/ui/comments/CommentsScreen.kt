package com.narxoz.social.ui.comments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.pullrefresh.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.narxoz.social.api.CommentDto
import com.narxoz.social.repository.AuthRepository

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun CommentsScreen(
    postId: Int,
    onBack: () -> Unit
) {
    val vm: CommentsViewModel = viewModel(factory = CommentsVmFactory(postId))
    val list by vm.comments.collectAsState()
    val loading by vm.state.collectAsState()

    /* ID текущего пользователя (нужен, чтобы показать «корзину» только своим) */
    val myId = AuthRepository.getUserId()

    var input by remember { mutableStateOf("") }

    val pullState = rememberPullRefreshState(
        refreshing = loading.isLoading,
        onRefresh  = { vm.reload() }
    )

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Комментарии") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { inner ->
        Column(Modifier.padding(inner)) {

            if (loading.error != null) {
                Text(
                    text = loading.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(12.dp)
                )
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            Box(Modifier.weight(1f).pullRefresh(pullState)) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(list) { comment ->
                        CommentItem(
                            c        = comment,
                            myId     = myId,
                            onDelete = { id -> vm.delete(id) }
                        )
                    }
                }
                PullRefreshIndicator(
                    refreshing = loading.isLoading,
                    state = pullState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Напишите комментарий…") }
                )
                IconButton(
                    enabled = input.isNotBlank(),
                    onClick = {
                        vm.send(input.trim())
                        input = ""
                    }
                ) { Icon(Icons.Default.Send, null) }
            }
        }
    }
}

@Composable
private fun CommentItem(
    c: CommentDto,
    myId: Int?,
    onDelete: (Int) -> Unit
) = Card(
    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainerLow),
    elevation = CardDefaults.cardElevation(1.dp)
) {
    Column(Modifier.padding(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(c.authorNickname, style = MaterialTheme.typography.labelSmall)

            if (myId != null && myId == c.authorId) {
                IconButton(onClick = { onDelete(c.id) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить")
                }
            }
        }
        Text(c.content, style = MaterialTheme.typography.bodyMedium)
    }
}