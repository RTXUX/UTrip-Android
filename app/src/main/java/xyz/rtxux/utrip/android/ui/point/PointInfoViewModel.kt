package xyz.rtxux.utrip.android.ui.point

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import xyz.rtxux.utrip.android.model.UResult
import xyz.rtxux.utrip.android.model.bean.CommentVO
import xyz.rtxux.utrip.android.model.bean.PointVO
import xyz.rtxux.utrip.android.model.bean.UserProfileVO
import xyz.rtxux.utrip.android.model.repository.PointRepository
import xyz.rtxux.utrip.android.model.repository.UserProfileRepository

class PointInfoViewModel : ViewModel() {
    private val pointRepository by lazy { PointRepository() }
    val point: MutableLiveData<PointVO> = MutableLiveData()
    val userProfile: MutableLiveData<UserProfileVO> = MutableLiveData()
    val deleted: MutableLiveData<Boolean> = MutableLiveData(false)
    val comments: MutableLiveData<MutableList<CommentVO>> = MutableLiveData()
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

    fun deletePoint() {
        if (point.value == null) return
        viewModelScope.launch {
            val result = pointRepository.deletePoint(point.value!!.pointId).also {
                when (it) {
                    is UResult.Success -> {
                        deleted.postValue(true)
                    }
                }
            }
        }
    }

    fun fetchComment() {
        val pointId = point.value?.pointId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            pointRepository.getComment(pointId).apply {
                when (this) {
                    is UResult.Success -> {
                        comments.postValue(data.toMutableList())
                    }
                    is UResult.Error -> {
                        Timber.d(exception)
                    }
                }
            }
        }

    }

    fun postComment(content: String) {
        val pointId = point.value?.pointId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            pointRepository.postComment(pointId, content).apply {
                when (this) {
                    is UResult.Success -> {
                        val data = this.data
                        comments.postValue(comments.value?.apply {
                            add(data)
                        })
                    }
                    is UResult.Error -> {
                        Timber.e(exception)
                    }
                }
            }
        }
    }

    fun deleteComment(position: Int) {
        val pointId = point.value?.pointId ?: return
        val comment = comments.value?.get(position) ?: return
        viewModelScope.launch(Dispatchers.IO) {
            pointRepository.deleteComment(pointId, comment.id).apply {
                when (this) {
                    is UResult.Success -> {
                        comments.postValue(comments.value?.apply {
                            removeAt(position)
                        })
                    }
                    is UResult.Error -> {
                        Timber.e(exception)
                    }
                }
            }
        }
    }
}
