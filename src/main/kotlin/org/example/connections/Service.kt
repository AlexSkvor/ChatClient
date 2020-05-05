package org.example.connections

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.example.alsoPrintDebug
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket
import java.time.LocalDateTime

class Service(private val socket: Socket) : Thread() {

    var lastPingFromServer: LocalDateTime = LocalDateTime.now()

    private val server by lazy { Server(socket) }

    var alive: Boolean = true

    private val sendList: MutableList<UserAction> = mutableListOf()

    private val receivedMessages: MutableList<Intention> = mutableListOf()

    init {
        start()
    }

    private val finishRelay = PublishRelay.create<Unit>()

    fun finish(): Observable<Unit> = finishRelay.hide().share()

    //private val readerThread by lazy { ReaderThread() }

    private lateinit var readerThread: ReaderThread


    override fun run() {
        try {
            readerThread = ReaderThread()
            readerThread.start()
            while (alive) {
                if (lastPingFromServer.isBefore(LocalDateTime.now().minusMinutes(1))) {
                    alsoPrintDebug("Ping problem")
                    alive = false
                    break
                }
                sendAllUserActions()
                sleep(100)
            }
        } catch (e: Throwable) {
            alive = false
            e.printStackTrace()
            System.err.println("server left with error!")
        } finally {
            alsoPrintDebug("finally Service")
            try {
                finishRelay.accept(Unit)
                server.input.close()
                server.output.close()
                server.socket.close()
            } catch (e: Throwable) {
                e.printStackTrace()
                println("Could not close resources!")
            }
        }
    }

    @Synchronized
    fun addTask(task: UserAction) {
        sendList.add(task)
    }

    @Synchronized
    fun getAllReceivedMessages(): List<Intention> {
        val res = mutableListOf<Intention>()
        res.addAll(receivedMessages)
        receivedMessages.clear()
        return res
    }

    @Synchronized
    private fun getAllTasks(): List<UserAction> {
        val res = mutableListOf<UserAction>()
        res.addAll(sendList)
        sendList.clear()
        return res
    }

    private fun sendAllUserActions() {
        val tasks = getAllTasks()
        tasks.forEach { server.output.writeObject(it) }
    }

    @Synchronized
    private fun receiveMessage(task: Intention) {
        receivedMessages.add(task)
    }

    private inner class ReaderThread : Thread() {
        override fun run() {
            try {
                while (alive) {
                    val action = server.input.readObject()
                    action.alsoPrintDebug("AAAAAAAAAAAAAAAAAAAAA")
                    if (action !is Intention) continue
                    if (action is Intention.Ping) {
                        lastPingFromServer = LocalDateTime.now()
                        addTask(UserAction.Ping)
                    } else receiveMessage(action)
                    sleep(100)
                }
            } catch (e: Throwable) {
                alive = false
                e.printStackTrace()
            } finally {
                try {
                    finishRelay.accept(Unit)
                    server.input.close()
                    server.output.close()
                    server.socket.close()
                } catch (e: Throwable) {
                    e.printStackTrace()
                    println("Could not close resources!")
                }
            }
        }
    }

    private class Server(s: Socket) {

        val socket: Socket = s
        val input: ObjectInputStream
        val output: ObjectOutputStream

        init {
            alsoPrintDebug("1")
            output = ObjectOutputStream(socket.getOutputStream())
            alsoPrintDebug("2")
            output.flush()
            alsoPrintDebug("3")
            input = ObjectInputStream(socket.getInputStream())
            alsoPrintDebug("4")
        }
    }
}