package org.example.mvi

import io.reactivex.Observable

interface IntentionsProducer {

    fun getLoginIntent(): Observable<LoginIntent>
    fun openChatIntent(): Observable<String> //chatId
    fun sendMessageIntent(): Observable<String> //text of message for current chat
    fun leaveChatIntent(): Observable<Unit> //leave current chat
    fun createChatIntent(): Observable<String> //chatName
    fun joinUserIntent(): Observable<String> //userName (add this user(s) to current chat)

}