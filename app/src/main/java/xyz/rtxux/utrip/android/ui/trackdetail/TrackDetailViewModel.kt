package xyz.rtxux.utrip.android.ui.trackdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.realm.Realm
import xyz.rtxux.utrip.android.model.realm.MyTrack
import xyz.rtxux.utrip.android.model.realm.asLiveData

class TrackDetailViewModel : ViewModel() {
    private val realm by lazy {
        Realm.getDefaultInstance()
    }

    var myTrack: LiveData<MyTrack> = MutableLiveData()

    fun loadTrack(trackId: Int) {
        myTrack = realm.where(MyTrack::class.java).equalTo("id", trackId).findFirst()?.asLiveData()?:myTrack
    }

    fun deleteTrack() {
        realm.executeTransaction { myTrack.value?.deleteFromRealm() }
        myTrack = MutableLiveData()
    }
}
