package org.example.connections

import java.io.Serializable
import java.time.LocalDateTime

//сообщения, отправляемые сервером пользователю!
sealed class Intention(
    var time: LocalDateTime = LocalDateTime.now()
) : Serializable {

    data class Chat(
        val chatId: String,
        val chatName: String,
        val users: MutableList<User>
    ) : Intention()

    object Ping : Intention()
    data class ChatClosed(val chatId: String) : Intention()
    data class Message(
        val chatId: String,
        val user: User,
        val message: String
    ) : Intention()

    data class UserLeft(
        val chatId: String,
        val user: User
    ) : Intention()

    data class UserJoined(
        val chatId: String,
        val user: User
    ) : Intention()

    data class YourId(val userId: String): Intention()
}