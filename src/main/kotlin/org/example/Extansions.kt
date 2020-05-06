package org.example

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

inline fun <reified T> T.alsoPrintDebug(msg: String) =
    also { println("$msg...$this") }

fun doNothing() = Unit

inline fun <reified T> Observable<T>.subscribeOnIo(): Observable<T> = this.subscribeOn(Schedulers.io())