package com.narxoz.social.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.narxoz.social.repository.NotificationsRepository
import com.narxoz.social.repository.FriendsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val repo: NotificationsRepository = NotificationsRepository(),
    private val friendsRepo: FriendsRepository = FriendsRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsState(isLoading = true))
    val state: StateFlow<NotificationsState> = _state.asStateFlow()

    init { reload() }

    fun reload() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = null) }

        val notifsRes = repo.list()
        val incomingRes = friendsRepo.incoming()

        notifsRes
            .onSuccess { list ->
                val incomingMap = incomingRes.getOrNull()
                    ?.associateBy { it.fromUser?.id }
                    ?: emptyMap()

                val patched = list.map { n ->
                    if (n.type == "friend_request") {
                        val existingFriend = n.data?.friend
                        val uid = existingFriend?.id
                        val req = uid?.let { incomingMap[uid] }
                        val name = req?.fromUser?.fullName ?: req?.fromUser?.nickname
                        val reqId = req?.id

                        if (existingFriend != null && (reqId != null || !name.isNullOrBlank())) {
                            val patchedFriend = existingFriend.copy(
                                id = reqId ?: uid ?: existingFriend.id,
                                nickname = name ?: existingFriend.nickname
                            )
                            n.copy(data = n.data.copy(friend = patchedFriend))
                        } else n
                    } else n
                }

                _state.update { it.copy(isLoading = false, notifications = patched) }
            }
            .onFailure { e ->
                _state.update {
                    it.copy(isLoading = false, error = e.message ?: "Ошибка сети")
                }
            }
    }

    fun markRead(id: Int) = viewModelScope.launch {
        repo.markRead(id)
        _state.update { st ->
            st.copy(notifications = st.notifications.map { n ->
                if (n.id == id) n.copy(isRead = true) else n
            })
        }
    }
    fun respondToFriendRequest(friendId: Int, accepted: Boolean, notifId: Int) =
        viewModelScope.launch {
            friendsRepo.respond(friendId, accepted)
            markRead(notifId)
        }
}