package xyz.rtxux.utrip.android.ui.point

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import xyz.rtxux.utrip.android.model.UResult
import xyz.rtxux.utrip.android.model.repository.PointRepository
import xyz.rtxux.utrip.server.model.vo.PointVO

class PointInfoViewModel : ViewModel() {
    private val pointRepository by lazy { PointRepository() }
    val point: MutableLiveData<PointVO> = MutableLiveData()

    fun getPointVO(pointId: Int) {
        viewModelScope.launch {
            pointRepository.getPointVO(pointId).let {
                when (it) {
                    is UResult.Success -> {
                        point.value = it.data
                    }
                    is UResult.Error -> {

                    }
                }
            }
        }
    }
}
