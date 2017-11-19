package com.github.bjoernpetersen.q.api.action

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Callable

fun <V> Callable<V>.asObservable(): Observable<V> = Observable.fromCallable(this)
    .subscribeOn(Schedulers.io())

fun <V> Observable<V>.onMainThread(): Observable<V> = observeOn(AndroidSchedulers.mainThread())
