package org.example.connections

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import org.example.alsoPrintDebug
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
                getAllTasks().forEach { server.output.writeObject(it) }
                sleep(100)
            }
        } catch (e: Throwable) {
            alive = false
            e.printStackTrace()
            System.err.println("connection lost")
        } finally {
            finishWork()
        }
    }

    private fun finishWork(){
        alsoPrintDebug("closing connection")
        try {
            finishRelay.accept(Unit)
            server.output.close()
            server.input.close()
            server.socket.close()
        } catch (e: Throwable) {
            e.printStackTrace()
            println("Could not close resources!")
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

    @Synchronized
    private fun receiveMessage(task: Intention) {
        receivedMessages.add(task)
    }

    private inner class ReaderThread : Thread() {
        override fun run() {
            try {
                while (alive) {
                    val action = server.input.readObject()
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
                finishWork()
            }
        }
    }
}