package xyz.rtxux.utrip.android.ui.mypoint

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import xyz.rtxux.utrip.android.model.UResult
import xyz.rtxux.utrip.android.model.api.RetrofitClient
import xyz.rtxux.utrip.android.model.repository.PointRepository
import xyz.rtxux.utrip.server.model.vo.PointVO

class MyPointViewModel : ViewModel() {

    private val pointRepository by lazy { PointRepository() }

    val points: MutableLiveData<List<PointVO>> = MutableLiveData()

    fun loadPoints() {
        viewModelScope.launch {
            val result = pointRepository.findByUser(RetrofitClient.userId).also {
                when (it) {
                    is UResult.Success -> {
                        points.postValue(it.data)
                    }
                }
            }
        }
    }
}
