package xyz.rtxux.utrip.android.ui.tracking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mapbox.android.core.location.LocationEngineResult
import io.realm.Realm
import xyz.rtxux.utrip.android.model.realm.MyPoint
import xyz.rtxux.utrip.android.model.realm.MyTrack
import xyz.rtxux.utrip.android.model.realm.asLiveData

class TrackingViewModel : ViewModel() {
    val realm by lazy {
        Realm.getDefaultInstance()
    }

    var myTrack: LiveData<MyTrack> = MutableLiveData()
    var id = 0

    fun createTrack(): LiveData<MyTrack> {
        realm.beginTransaction()
        val myTrack = MyTrack().apply {
            id =
                this@TrackingViewModel.realm.where(MyTrack::class.java).max("id")?.let { it.toInt() + 1 }
                    ?: 1
            timestamp = System.currentTimeMillis()
            name = "轨迹${id}"
        }
        id = myTrack.id
        val data = realm.copyToRealm(myTrack).asLiveData()
        realm.commitTransaction()
        return data
    }

    fun postPoint(result: LocationEngineResult) {
        if (realm.isClosed) return
        realm.executeTransactionAsync {
            val myPoint = MyPoint().apply {
                id = it.where(MyPoint::class.java).max("id")?.let { it.toInt() + 1 } ?: 1
                timestamp = System.currentTimeMillis()
                latitude = result.lastLocation!!.latitude
                longitude = result.lastLocation!!.longitude
                altitude = result.lastLocation!!.altitude
                pathPoint = true
            }
            it.where(MyTrack::class.java).equalTo("id", id).findFirst()?.points?.add(myPoint)
        }
    }

    override fun onCleared() {
        realm.close()
        myTrack = MutableLiveData()
        super.onCleared()
    }
}

