package xyz.rtxux.utrip.android.ui.explore

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import timber.log.Timber
import xyz.rtxux.utrip.android.model.UResult
import xyz.rtxux.utrip.android.model.bean.PointVO
import xyz.rtxux.utrip.android.model.repository.PointRepository

class ExploreViewModel : ViewModel() {

    var points: MutableLiveData<List<PointVO>> = MutableLiveData()
    val pointRepository by lazy { PointRepository() }

    fun loadPointAround(latitide: Double, longitude: Double) {
        viewModelScope.launch {
            pointRepository.getPointAround("WGS-84", latitide, longitude).apply {
                when (this) {
                    is UResult.Success -> {
                        points.postValue(data)
                    }
                    is UResult.Error -> {
                        Timber.d(exception)
                    }
                }
            }
        }
    }

}