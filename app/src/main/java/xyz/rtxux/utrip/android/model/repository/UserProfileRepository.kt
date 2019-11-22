package xyz.rtxux.utrip.android.model.repository

import xyz.rtxux.utrip.android.base.BaseRepository
import xyz.rtxux.utrip.android.model.UResult
import xyz.rtxux.utrip.android.model.api.RetrofitClient
import xyz.rtxux.utrip.server.model.vo.UserProfileVO

class UserProfileRepository : BaseRepository() {

    suspend fun getUserProfileVO(userId: Int): UResult<UserProfileVO> {
        return safeApiCall(
            { executeResponse(RetrofitClient.service.getUserProfileVO(userId)) },
            "获取用户信息失败"
        )
    }

    suspend fun setAvatar(userId: Int, avatarId: Int): UResult<UserProfileVO> {
        return safeApiCall(
            { executeResponse(RetrofitClient.service.setAvatar(avatarId, RetrofitClient.userId)) },
            "设置头像失败"
        )
    }
}