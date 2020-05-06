package org.example.mvi

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.example.alsoPrintDebug
import org.example.connections.Intention
import org.example.connections.Service
import org.example.connections.User
import org.example.connections.UserAction
import org.example.doNothing
import org.example.subscribeOnIo
import java.net.Socket
import java.util.concurrent.TimeUnit

class Repository {

    private lateinit var service: Service

    fun login(name: String, ip: String, port: Int): Observable<User> {
        @Suppress("BlockingMethodInNonBlockingContext")
        service = Service(Socket(ip, port))
        observeServiceChanges()
        return getId(name)
            .subscribeOnIo()
            .observeOnFx()
    }

    private fun getId(name: String) = idReceived().take(1)
        .flatMap {
            pushCommand(UserAction.Login(User(it, name)))
                .andThen(Observable.just(User(it, name)))
        }

    fun pushCommand(command: UserAction) = Completable.fromAction {
        if (::service.isInitialized) service.addTask(command)
    }

    private fun observeServiceChanges() {
        service.finish().subscribeOnIo().subscribe({
            serverFinishRelay.accept(it)
        }, {
            serverFinishRelay.accept(Unit)
            it.printStackTrace()
        }).bind()

        Observable.interval(100, TimeUnit.MILLISECONDS)
            .subscribeOnIo()
            .subscribe({ _ ->
                service.getAllReceivedMessages().forEach {
                    it.alsoPrintDebug("Received from server")
                    when (it) {
                        is Intention.Chat -> chatIntentionRelay.accept(it)
                        is Intention.ChatClosed -> chatClosedIntentionRelay.accept(it)
                        is Intention.Message -> messagesIntentionRelay.accept(it)
                        is Intention.UserLeft -> userLeftChatIntentionRelay.accept(it)
                        is Intention.UserJoined -> userJoinedIntentionRelay.accept(it)
                        is Intention.YourId -> idReceivedRelay.accept(it.userId)
                        Intention.Ping -> doNothing()
                    }
                }
            }, { it.printStackTrace() })
            .bind()
    }

    private val chatIntentionRelay: PublishRelay<Intention.Chat> = PublishRelay.create()
    fun chatCreations(): Observable<Intention.Chat> = chatIntentionRelay.hide().share().observeOnFx()

    private val chatClosedIntentionRelay: PublishRelay<Intention.ChatClosed> = PublishRelay.create()
    fun chatClosed(): Observable<Intention.ChatClosed> = chatClosedIntentionRelay.hide().share().observeOnFx()

    private val messagesIntentionRelay: PublishRelay<Intention.Message> = PublishRelay.create()
    fun messageReceived(): Observable<Intention.Message> = messagesIntentionRelay.hide().share().observeOnFx()

    private val userLeftChatIntentionRelay: PublishRelay<Intention.UserLeft> = PublishRelay.create()
    fun userLeft(): Observable<Intention.UserLeft> = userLeftChatIntentionRelay.hide().share().observeOnFx()

    private val userJoinedIntentionRelay: PublishRelay<Intention.UserJoined> = PublishRelay.create()
    fun userJoined(): Observable<Intention.UserJoined> = userJoinedIntentionRelay.hide().share().observeOnFx()

    private val idReceivedRelay: BehaviorRelay<String> = BehaviorRelay.create()
    private fun idReceived(): Observable<String> = idReceivedRelay.hide().share().observeOnFx()

    private val serverFinishRelay: PublishRelay<Unit> = PublishRelay.create()
    fun serverFinish(): Observable<Unit> = serverFinishRelay.hide().share().observeOnFx()

    private val disposable = CompositeDisposable()
    fun clear() {
        disposable.clear()
        if (::service.isInitialized)
            service.alive = false
    }

    private fun Disposable.bind() = disposable.add(this)
}