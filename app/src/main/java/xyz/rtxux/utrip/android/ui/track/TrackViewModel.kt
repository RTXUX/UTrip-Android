package xyz.rtxux.utrip.android.ui.track

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.realm.Realm
import io.realm.RealmResults
import xyz.rtxux.utrip.android.model.realm.MyTrack

class TrackViewModel : ViewModel() {
    val realm by lazy {
        Realm.getDefaultInstance()
    }

    val trackList: MutableLiveData<RealmResults<MyTrack>> = MutableLiveData()

    fun getTrackList() {
        trackList.value = realm.where(MyTrack::class.java).findAll()
    }

    override fun onCleared() {
        realm.close()
        trackList.value = null
    }
}