package xyz.rtxux.utrip.android.ui.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import xyz.rtxux.utrip.android.model.UResult
import xyz.rtxux.utrip.android.model.api.RetrofitClient
import xyz.rtxux.utrip.android.model.repository.UserProfileRepository
import xyz.rtxux.utrip.server.model.vo.UserProfileVO

class ProfileViewModel : ViewModel() {
    private val userProfileRepository by lazy { UserProfileRepository() }

    val userProfileVO: MutableLiveData<UserProfileVO> = MutableLiveData()

    fun loadUserProfileVO() {
        viewModelScope.launch {
            val result = userProfileRepository.getUserProfileVO(RetrofitClient.userId).also {
                when (it) {
                    is UResult.Success -> {
                        userProfileVO.postValue(it.data)
                    }
                    is UResult.Error -> {

                    }
                }
            }
        }
    }

}