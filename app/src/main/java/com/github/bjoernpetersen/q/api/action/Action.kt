package com.github.bjoernpetersen.q.api.action

import android.content.Context
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.Callable

interface Action<V> : Callable<V> {
  fun asObservable(): Observable<V> = Observable.fromCallable(this)
  fun defaultAction(context: Context): Disposable
}