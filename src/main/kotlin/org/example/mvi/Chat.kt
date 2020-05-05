package org.example.mvi

import org.example.connections.Intention

data class Chat(
    val chat: Intention.Chat,
    val messages: List<Intention.Message>
)