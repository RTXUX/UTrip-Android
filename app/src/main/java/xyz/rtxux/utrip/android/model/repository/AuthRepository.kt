package xyz.rtxux.utrip.android.model.repository

import xyz.rtxux.utrip.android.base.BaseRepository
import xyz.rtxux.utrip.android.model.UResult
import xyz.rtxux.utrip.android.model.api.RetrofitClient
import xyz.rtxux.utrip.android.model.bean.LoginVO
import xyz.rtxux.utrip.android.model.bean.RegisterDTO
import xyz.rtxux.utrip.android.model.bean.RegisterVO

class AuthRepository : BaseRepository() {
    suspend fun validate(): UResult<LoginVO> {
        return safeApiCall({ executeResponse(RetrofitClient.service.validateLogin()) }, "认证失败")

    }

    suspend fun login(username: String, password: String): UResult<LoginVO> {
        return safeApiCall(
            { executeResponse(RetrofitClient.service.login(username, password)) },
            "登录失败"
        )
    }

    suspend fun register(registerDTO: RegisterDTO): UResult<RegisterVO> {
        return safeApiCall(
            { executeResponse(RetrofitClient.service.register(registerDTO)) },
            "注册失败"
        )
    }

    suspend fun logout(): UResult<Any> {
        return safeApiCall({
            RetrofitClient.service.logout()
            RetrofitClient.clearCookie()
            UResult.Success(0)
        }, "登出失败")
    }
}