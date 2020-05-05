package org.example

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import java.time.format.DateTimeFormatter

inline fun <reified T> T.alsoPrintDebug(msg: String) =
    also { println("$msg...$this") }

val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd:MM:yyyy HH:mm:ss:SSS")

fun doNothing() = Unit

inline fun <reified T> Observable<T>.subscribeOnIo(): Observable<T> = this.subscribeOn(Schedulers.io())
inline fun <reified T> Single<T>.subscribeOnIo(): Single<T> = this.subscribeOn(Schedulers.io())
fun Completable.subscribeOnIo(): Completable = this.subscribeOn(Schedulers.io())
fun Completable.observeOnFx(): Completable = this.observeOn(JavaFxScheduler.platform())

inline fun <reified T> Completable.toObservableDefault(default: T) = this.toSingleDefault(default).toObservable()