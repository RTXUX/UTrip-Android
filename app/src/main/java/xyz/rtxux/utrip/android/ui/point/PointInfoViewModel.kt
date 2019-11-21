package xyz.rtxux.utrip.android.ui.point

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import xyz.rtxux.utrip.android.model.UResult
import xyz.rtxux.utrip.android.model.repository.PointRepository
import xyz.rtxux.utrip.android.model.repository.UserProfileRepository
import xyz.rtxux.utrip.server.model.vo.PointVO
import xyz.rtxux.utrip.server.model.vo.UserProfileVO

class PointInfoViewModel : ViewModel() {
    private val pointRepository by lazy { PointRepository() }
    val point: MutableLiveData<PointVO> = MutableLiveData()
    val userProfile: MutableLiveData<UserProfileVO> = MutableLiveData()
    private val userProfileRepository by lazy { UserProfileRepository() }
    fun getPointVO(pointId: Int) {
        viewModelScope.launch {
            pointRepository.getPointVO(pointId).let {
                when (it) {
                    is UResult.Success -> {
                        point.value = it.data
                        getUserProfileVO(it.data.userId)
                    }
                    is UResult.Error -> {

                    }
                }
            }
        }
    }

    fun getUserProfileVO(userId: Int) {
        viewModelScope.launch {
            userProfileRepository.getUserProfileVO(userId).let {
                when (it) {
                    is UResult.Success -> {
                        userProfile.postValue(it.data)
                    }
                    is UResult.Error -> {

                    }
                }
            }
        }
    }
}
