package com.onelinegaming.posedetectiondemo

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.onelinegaming.squidrunner.addTo
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit


class GameViewModel : ViewModel() {

    val disposables = CompositeDisposable()

    val canRun: MutableLiveData<Boolean> = MutableLiveData(true)
    val time: MutableLiveData<Int> = MutableLiveData(0)
    val gameStartLeft: MutableLiveData<Int> = MutableLiveData(0)
    val updateFrame: SingleLiveEvent<Any> = SingleLiveEvent()

    init {
        Observable.interval(1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.newThread())
            .repeat()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                gameStartLeft.value = it.toInt()
                if (it == 4L)
                    startGame()
            }.addTo(disposables)

    }

    private fun startGame() {
        canRun.value = true
        Observable.timer(5, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.newThread())
            .repeat()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                canRun.value = canRun.value?.not()
                Log.w("Squid", "timer")
            }.addTo(disposables)

        Observable.interval(1, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.newThread())
            .repeat()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                time.value = it.toInt()
            }.addTo(disposables)

        Observable.interval(20, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.newThread())
            .repeat()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                updateFrame.value = null
            }.addTo(disposables)
    }

}