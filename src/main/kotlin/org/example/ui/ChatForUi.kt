package org.example.ui

import org.example.mvi.Chat

data class ChatForUi(
    val chatName: String,
    val chatId: String,
    val usersNumber: Int
) {
    override fun toString(): String = "$chatName ($usersNumber)"
}

fun Chat.toUi() = ChatForUi(
    chatName = this.chat.chatName,
    chatId = this.chat.chatId,
    usersNumber = this.chat.users.count()
)