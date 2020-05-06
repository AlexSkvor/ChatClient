package org.example.connections

import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket

class Server(s: Socket) {
    val socket: Socket = s
    val input: ObjectInputStream
    val output: ObjectOutputStream

    init {
        output = ObjectOutputStream(socket.getOutputStream())
        output.flush()
        input = ObjectInputStream(socket.getInputStream())
    }
}