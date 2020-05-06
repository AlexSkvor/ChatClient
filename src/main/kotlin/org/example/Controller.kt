package org.example

import com.github.thomasnield.rxkotlinfx.itemSelections
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import org.example.mvi.*
import org.example.ui.ChatForUi
import org.example.ui.MessageForUi
import org.example.ui.UserForUi
import org.example.ui.toUi

class Controller : IntentionsProducer {

    @FXML
    private lateinit var chatsList: ListView<ChatForUi>

    @FXML
    private lateinit var chatMessagesList: ListView<MessageForUi>

    @FXML
    private lateinit var editTextServerAddress: TextField

    @FXML
    private lateinit var usersInChatList: ListView<UserForUi>

    @FXML
    private lateinit var editTextChatLogin: TextField

    @FXML
    private lateinit var messageText: TextArea

    @FXML
    private lateinit var newChatNameField: TextField

    private lateinit var combiner: Combiner

    private val loginRelay = PublishRelay.create<LoginIntent>()
    override fun getLoginIntent(): Observable<LoginIntent> = loginRelay.hide().filter { !combiner.lastState.started }

    fun initialize() {
        val repository = Repository()
        combiner = Combiner(repository)
        combiner.states(this)
            .subscribe { render(it) }
            .bind()
        chatsList.items = observableChats
        chatMessagesList.items = observableMessages
        usersInChatList.items = observableUsers
    }

    private fun render(state: State) {
        state.alsoPrintDebug("STATE")
        if (state.started) {
            renderChatList(state.chats, state.currentChat)
            renderUsersList(state.uiCurrentUsers)
            renderMessages(state.uiCurrentMessages)
        }
    }

    override fun joinUserIntent(): Observable<String> = usersInChatList.itemSelections
        .filter { !it.presentedInChat }
        .map { it.id }

    private val observableUsers = FXCollections.observableArrayList<UserForUi>()
    private fun renderUsersList(newList: List<UserForUi>) {
        observableUsers.clear()
        observableUsers.addAll(newList)
    }

    private val observableMessages = FXCollections.observableArrayList<MessageForUi>()
    private fun renderMessages(messages: List<MessageForUi>) {
        observableMessages.clear()
        observableMessages.addAll(messages)
    }

    override fun openChatIntent(): Observable<String> = chatsList.itemSelections.map { it.chatId }

    private val observableChats = FXCollections.observableArrayList<ChatForUi>()
    private fun renderChatList(chats: List<Chat>, selected: Chat?) {
        observableChats.clear()
        observableChats.addAll(chats.sortedBy { it.messages.lastOrNull()?.time }.map { it.toUi() })
        selected?.let {
            if (chatsList.selectionModel.selectedItem != it.toUi())
                chatsList.selectionModel.select(it.toUi())
        }
    }

    private val sendMessageRelay = PublishRelay.create<String>()
    override fun sendMessageIntent(): Observable<String> = sendMessageRelay.hide().filter { it.isNotEmpty() }

    @FXML
    private fun sendMessage(event: ActionEvent) {
        sendMessageRelay.accept(messageText.text)
        messageText.text = ""
    }

    private val leaveChatRelay = PublishRelay.create<Unit>()
    override fun leaveChatIntent(): Observable<Unit> = leaveChatRelay.hide()

    @FXML
    private fun leaveChat(event: ActionEvent) {
        if (combiner.lastState.currentChatId.isNotEmpty())
            leaveChatRelay.accept(Unit)
    }

    private val createChatRelay = PublishRelay.create<String>()
    override fun createChatIntent(): Observable<String> = createChatRelay.hide()

    @FXML
    private fun createChat(event: ActionEvent) {
        createChatRelay.accept(newChatNameField.text)
    }

    @FXML
    private fun tryConnect(event: ActionEvent) {
        val addressStr = editTextServerAddress.text
        val ip = addressStr.substringBefore(':')
        val port = addressStr.substringAfter(':').toIntOrNull()
            ?: return incorrectServerAddressFormat(addressStr)

        val name = editTextChatLogin.text
        if (name.isEmpty()) return nameIsEmpty()

        val login = LoginIntent(name = name, port = port, ip = ip)
        loginRelay.accept(login)
    }

    private fun incorrectServerAddressFormat(address: String) {
        address.alsoPrintDebug("Error parsing address $address")
    }

    private fun nameIsEmpty() {
        alsoPrintDebug("User name is empty!")
    }

    private fun Disposable.bind() {
        compositeDisposable.add(this)
    }

    fun clear() {
        if (::combiner.isInitialized) combiner.clear()
        compositeDisposable.clear()
    }

    private val compositeDisposable = CompositeDisposable()
}