package com.onelinegaming.squidrunner

import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

fun <T: Disposable> T.addTo(compositeDisposable: CompositeDisposable): T
        = apply { compositeDisposable.add(this) }