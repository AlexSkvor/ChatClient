package org.example.ui

import org.example.connections.Intention
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class MessageForUi(
    val text: String,
    val userName: String,
    val time: LocalDateTime
) {
    override fun toString(): String = "$text\n$userName\t$strDate"

    private val strDate: String
        get() = timeFormatter.format(time)
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd:MM HH:mm:ss")

}

fun Intention.Message.toUi(): MessageForUi {
    return MessageForUi(
        text = this.message,
        userName = this.user.login,
        time = this.time
    )
}