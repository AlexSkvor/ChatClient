package org.example.mvi

import org.example.connections.Intention
import org.example.connections.User

sealed class PartialState(private val log: String) {

    data class OpenChatIntent(val chatId: String) : PartialState("OpenChatIntent chatId $chatId")
    data class ChatClosed(val chatId: String) : PartialState("ChatClosed chatId $chatId")
    data class NewChat(val chat: Intention.Chat) : PartialState("NewChat $chat")
    data class Message(val message: Intention.Message) : PartialState("Message $message")
    data class ForgetChat(val chatId: String) : PartialState("ForgetChat $chatId")
    data class UserLeft(val action: Intention.UserLeft) : PartialState("UserLeft $action")
    data class UserJoined(val chatId: String, val user: User) : PartialState("UserJoined chat $chatId and user $user")
    data class LoggedIn(val userId: String) : PartialState("LoggedIn $userId")
    object Finish : PartialState("Finish")

    override fun toString(): String = log
}