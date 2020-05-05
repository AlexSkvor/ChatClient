package org.example.mvi

import org.example.ui.MessageForUi
import org.example.ui.UserForUi
import org.example.ui.toUi

data class State(
    val userId: String = "",
    val currentChatId: String = "",
    val chats: List<Chat> = emptyList(),
    val finished: Boolean = false
) {

    val started: Boolean
        get() = userId.isNotEmpty()

    val currentChat: Chat?
        get() = chats.firstOrNull { it.chat.chatId == currentChatId }

    val uiCurrentMessages: List<MessageForUi>
        get() = currentChat?.messages?.sortedBy { it.time }?.map { it.toUi() }.orEmpty()

    val uiCurrentUsers: List<UserForUi>
        get() = chats.asSequence()
            .map { it.chat.users }
            .flatten()
            .distinctBy { it.uuid }
            .map { user -> user.toUi(currentChat?.chat?.users?.map { it.uuid }.orEmpty()) }
            .sortedBy { it.presentedInChat }.toList()

    val allUsers = chats.map { it.chat.users }.flatten().distinctBy { it.uuid }
}