package org.example.ui

import org.example.connections.User

data class UserForUi(
    val name: String,
    val id: String,
    val presentedInChat: Boolean
) {
    override fun toString(): String = "$name $presentedInChatStr"

    private val presentedInChatStr: String
        get() = if (presentedInChat) "(+)" else "(-)"
}

fun User.toUi(presentedIds: List<String>): UserForUi =
    UserForUi(login, uuid, uuid in presentedIds)