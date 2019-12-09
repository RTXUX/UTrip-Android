package xyz.rtxux.utrip.android.ui.profileedit

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import timber.log.Timber
import xyz.rtxux.utrip.android.model.UResult
import xyz.rtxux.utrip.android.model.bean.UserProfileVO
import xyz.rtxux.utrip.android.model.repository.UserProfileRepository

class ProfileEditViewModel : ViewModel() {
    val editable = MutableLiveData<Boolean>(false)
    val userProfileVO = MutableLiveData<UserProfileVO>()
    val gender = MediatorLiveData<String>().apply {
        addSource(userProfileVO) {
            if (it.gender != value) postValue(it.gender)
        }
    }
    val userProfileRepository by lazy { UserProfileRepository() }
    fun loadUserProfileVO(userId: Int) {
        viewModelScope.launch {
            userProfileRepository.getUserProfileVO(userId).apply {
                when (this) {
                    is UResult.Success -> {
                        userProfileVO.postValue(data)
                    }
                    is UResult.Error -> {
                        Timber.d("Failed to load User Profile")
                    }
                }
            }
        }

    }

    fun updateUserProfile(userProfileVO: UserProfileVO) {
        viewModelScope.launch {
            userProfileRepository.updateProfile(userProfileVO).apply {
                when (this) {
                    is UResult.Success -> {
                        this@ProfileEditViewModel.userProfileVO.postValue(data)
                    }
                    is UResult.Error -> {
                        Timber.d("Failed to load User Profile")
                    }
                }
            }
        }
    }

}
