package xyz.rtxux.utrip.android.model.realm

import androidx.lifecycle.LiveData
import io.realm.RealmChangeListener
import io.realm.RealmModel
import io.realm.RealmResults
import io.realm.kotlin.addChangeListener
import io.realm.kotlin.removeChangeListener

class RealmLiveData<T : RealmModel>(val realmModel: T) : LiveData<T>() {

    init {
        value = realmModel
    }

    private val listener = RealmChangeListener<T> {
        postValue(it)
    }

    override fun onInactive() {
        realmModel.removeChangeListener(listener)
        super.onInactive()
    }

    override fun onActive() {
        super.onActive()
        realmModel.addChangeListener(listener)
    }
}

fun <T : RealmModel> T.asLiveData() = RealmLiveData<T>(this)

class RealmResultLiveData<T : RealmModel>(val result: RealmResults<T>) :
    LiveData<RealmResults<T>>() {

    init {
        value = result
    }

    private val listener = RealmChangeListener<RealmResults<T>> {
        postValue(it)
    }

    override fun onInactive() {
        result.removeChangeListener(listener)
        super.onInactive()
    }

    override fun onActive() {
        result.addChangeListener(listener)
        super.onActive()
    }
}

fun <T : RealmModel> RealmResults<T>.asLiveData() = RealmResultLiveData(this)