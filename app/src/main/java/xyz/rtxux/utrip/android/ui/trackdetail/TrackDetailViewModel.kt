package xyz.rtxux.utrip.android.ui.trackdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.realm.Realm
import io.realm.RealmResults
import xyz.rtxux.utrip.android.model.realm.MyPoint
import xyz.rtxux.utrip.android.model.realm.MyTrack
import xyz.rtxux.utrip.android.model.realm.asLiveData

class TrackDetailViewModel : ViewModel() {
    private val realm by lazy {
        Realm.getDefaultInstance()
    }

    var myTrack: LiveData<MyTrack> = MutableLiveData()
    var trackId: Int = 0
    var markerPoints: LiveData<RealmResults<MyPoint>> = MutableLiveData()

    var selectedPoint: MutableLiveData<MyPoint> = MutableLiveData()

    fun loadTrack(trackId: Int) {
        myTrack = realm.where(MyTrack::class.java).equalTo("id", trackId).findFirst()?.asLiveData()?:myTrack
        myTrack.value?.let {
            markerPoints = it.points.where().equalTo("pathPoint", false).findAll().asLiveData()
        }
        this.trackId = trackId
    }

    fun deleteTrack() {
        realm.executeTransaction {
            myTrack.value?.points?.deleteAllFromRealm()
            myTrack.value?.deleteFromRealm()
        }
        myTrack = MutableLiveData()
    }

    fun setSelectedPoint(id: Int) {
        selectedPoint.value?.let { point ->
            realm.executeTransaction {
                it.copyToRealmOrUpdate(point)
            }
        }
        myTrack.value?.let {
            (selectedPoint as MutableLiveData).postValue(
                it.points.where().equalTo(
                    "id",
                    id
                ).findFirst()?.let {
                    realm.copyFromRealm(it)
                }
            )
        }
    }

    fun deleteSelectedPoint() {
        val point = selectedPoint.value ?: return
        realm.executeTransaction { realm ->
            myTrack.value?.let {
                it.points.where().equalTo("id", point.id).findFirst()?.deleteFromRealm()
            }
        }
        selectedPoint.postValue(null)
    }

    fun persistNewPoint(point: MyPoint) {
        val realm = Realm.getDefaultInstance()
        Realm.getDefaultInstance().executeTransaction {
            //            myTrack.value?.let { myTrack ->
//                myTrack.points.add(point)
//            }
            point.apply {
                id = it.where(MyPoint::class.java).max("id")?.let { it.toInt() + 1 } ?: 1
            }
            it.where(MyTrack::class.java).equalTo("id", trackId).findFirst()?.points?.add(point)
        }
        realm.close()
    }

    override fun onCleared() {
        super.onCleared()
        realm.close()
    }
}
