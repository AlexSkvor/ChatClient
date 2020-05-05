package org.example.connections

import org.example.formatter
import java.io.Serializable
import java.time.LocalDateTime
import java.util.*

//сообщения, отправляемые пользователем на сервер!
sealed class UserAction(
    open val chatId: String, //empty string -> main chat!
    val time: LocalDateTime = LocalDateTime.now(),
    private val uuid: String = UUID.randomUUID().toString()
) : Serializable {

    data class Message(
        override val chatId: String,
        val message: String
    ) : UserAction(chatId)

    data class Login(
        val user: User
    ) : UserAction("")

    object Ping : UserAction("")
    data class CloseChat(override val chatId: String) : UserAction(chatId)
    data class JoinUser(override val chatId: String, val user: User) : UserAction(chatId)
    data class CreateChat(val chatName: String) : UserAction("")
    data class LeaveChat(override val chatId: String) : UserAction(chatId)

    val strDate: String
        get() = formatter.format(time)
}