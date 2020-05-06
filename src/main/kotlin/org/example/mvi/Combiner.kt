package org.example.mvi

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import org.example.connections.Intention
import org.example.connections.User
import org.example.connections.UserAction

class Combiner(
    private val repository: Repository
) {

    lateinit var lastState: State

    fun states(producer: IntentionsProducer): Observable<State> {

        val producerActions = subscribeProducerIntentions(producer)
        val serverActions = getServerActions()
        return Observable.merge(producerActions, serverActions).scan(State(), reducer)
            .distinctUntilChanged()
            .doOnNext { lastState = it }
    }

    private val reducer = BiFunction { oldState: State, it: PartialState ->
        when (it) {
            is PartialState.Finish -> oldState.copy(finished = true)
            is PartialState.OpenChatIntent -> oldState.copy(currentChatId = it.chatId)
            is PartialState.ChatClosed -> chatClosed(it.chatId, oldState)
            is PartialState.NewChat -> oldState.copy(chats = oldState.chats + Chat(it.chat, listOf()))
            is PartialState.Message -> message(it.message, oldState)
            is PartialState.ForgetChat -> chatClosed(it.chatId, oldState)
            is PartialState.UserLeft -> userLeft(it.action, oldState)
            is PartialState.UserJoined -> userJoined(it.chatId, it.user, oldState)
            is PartialState.LoggedIn -> oldState.copy(userId = it.userId)
        }
    }

    private fun userJoined(chatId: String, user: User, oldState: State): State {
        val message = Intention.Message(
            chatId = chatId, user = user,
            message = "Пользователь ${user.login} присоединился к чату!"
        )

        val state = oldState.copy(chats = oldState.chats.map {
            if (it.chat.chatId == chatId)
                it.copy(chat = it.chat.copy(
                    users = (it.chat.users + user)
                        .distinctBy { u -> u.uuid }
                        .toMutableList()
                )
                )
            else it
        })
        return message(message, state)
    }

    private fun userLeft(userLeft: Intention.UserLeft, oldState: State): State {
        val message = Intention.Message(
            chatId = userLeft.chatId, user = userLeft.user,
            message = "Пользователь ${userLeft.user.login} покинул данный чат!"
        ).also { it.time = userLeft.time }
        val state = oldState.copy(chats = oldState.chats.map {
            if (it.chat.chatId == userLeft.chatId)
                it.copy(chat = it.chat.copy(users = it.chat.users.filter { user ->
                    user.uuid == userLeft.user.uuid
                }.toMutableList()))
            else it
        })
        return message(message, state)
    }

    private fun message(message: Intention.Message, oldState: State): State {
        val chat = oldState.chats.firstOrNull { it.chat.chatId == message.chatId } ?: return oldState
        val newState = oldState.copy(chats = oldState.chats.filter { it.chat.chatId != message.chatId })
        return newState.copy(chats = newState.chats + chat.copy(messages = chat.messages + message))
    }

    private fun chatClosed(chatId: String, oldState: State): State {
        return oldState.copy(
            chats = oldState.chats.filter { c -> c.chat.chatId != chatId },
            currentChatId = if (oldState.currentChatId == chatId) "" else oldState.currentChatId
        )
    }

    private fun getServerActions(): Observable<PartialState> {
        val finishIntent = repository.serverFinish()
            .map { PartialState.Finish }

        val chatClosedIntent = repository.chatClosed()
            .map { PartialState.ChatClosed(it.chatId) }

        val chatCreatedIntent = repository.chatCreations()
            .map { PartialState.NewChat(it) }

        val messageIntent = repository.messageReceived()
            .map { PartialState.Message(it) }

        val userLeftIntent = repository.userLeft()
            .filter { ::lastState.isInitialized }
            .map { leftChat ->
                if (leftChat.user.uuid == lastState.userId) PartialState.ForgetChat(leftChat.chatId)
                else PartialState.UserLeft(leftChat)
            }

        val userJoinedIntent = repository.userJoined()
            .map { PartialState.UserJoined(it.chatId, it.user) }

        val list = listOf(
            finishIntent, chatClosedIntent, chatCreatedIntent,
            messageIntent, userLeftIntent, userJoinedIntent
        )
        return Observable.merge(list)
    }

    private fun subscribeProducerIntentions(producer: IntentionsProducer): Observable<PartialState> {

        fun Observable<out UserAction>.subscribeCommand() = this
            .flatMapCompletable { repository.pushCommand(it) }
            .subscribe({}, { it.printStackTrace() })
            .bind()

        producer.createChatIntent()
            .map { UserAction.CreateChat(it) }
            .subscribeCommand()

        producer.joinUserIntent()
            .filter { ::lastState.isInitialized }
            .map { id ->
                UserAction.JoinUser(chatId = lastState.currentChatId,
                    user = lastState.allUsers.first { it.uuid == id })
            }.subscribeCommand()

        producer.leaveChatIntent()
            .filter { ::lastState.isInitialized && lastState.currentChatId.isNotEmpty() }
            .map { UserAction.LeaveChat(lastState.currentChatId) }
            .subscribeCommand()

        producer.sendMessageIntent()
            .filter { ::lastState.isInitialized }
            .map { UserAction.Message(chatId = lastState.currentChatId, message = it) }
            .subscribeCommand()

        val logInIntent = producer.getLoginIntent()
            .switchMap { repository.login(it.name, it.ip, it.port) }
            .map { PartialState.LoggedIn(it.uuid) }

        val openChatIntent = producer.openChatIntent()
            .map { PartialState.OpenChatIntent(it) }

        return Observable.merge(logInIntent, openChatIntent)
    }

    private val disposable = CompositeDisposable()
    fun clear() {
        disposable.clear()
        repository.clear()
    }

    private fun Disposable.bind() {
        disposable.add(this)
    }
}