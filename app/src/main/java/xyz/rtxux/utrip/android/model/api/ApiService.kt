package xyz.rtxux.utrip.android.model.api

import retrofit2.http.*
import xyz.rtxux.utrip.android.model.bean.ApiResponse
import xyz.rtxux.utrip.android.model.bean.LoginVO
import xyz.rtxux.utrip.android.model.bean.RegisterDTO
import xyz.rtxux.utrip.android.model.bean.RegisterVO

interface ApiService {
    companion object {
        const val API_BASE = "http://192.168.0.152:8080"

    }

    @GET("/auth/validate")
    suspend fun validateLogin(): ApiResponse<LoginVO>

    @POST("/login")
    @FormUrlEncoded
    suspend fun login(@Field("username") username: String, @Field("password") password: String): ApiResponse<LoginVO>

    @POST("/auth/register")
    suspend fun register(@Body registerDTO: RegisterDTO): ApiResponse<RegisterVO>
}