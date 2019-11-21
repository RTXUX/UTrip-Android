package xyz.rtxux.utrip.android.ui.tracking

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mapbox.mapboxsdk.geometry.LatLng

class TrackingViewModel : ViewModel() {
    val points: MutableLiveData<MutableList<MyPoint>> = MutableLiveData()
}

data class MyPoint(
    val latLng: LatLng,
    val timestamp: Long
)