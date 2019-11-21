package xyz.rtxux.utrip.android.model.api

import retrofit2.http.*
import xyz.rtxux.utrip.android.model.bean.ApiResponse
import xyz.rtxux.utrip.android.model.bean.LoginVO
import xyz.rtxux.utrip.android.model.bean.RegisterDTO
import xyz.rtxux.utrip.android.model.bean.RegisterVO
import xyz.rtxux.utrip.server.model.dto.PointDTO
import xyz.rtxux.utrip.server.model.vo.ImagePreUploadVO
import xyz.rtxux.utrip.server.model.vo.PointVO
import xyz.rtxux.utrip.server.model.vo.UserProfileVO

interface ApiService {
    companion object {
        const val API_BASE = "http://api.utrip.rtxux.xyz:4399"

    }

    @GET("/auth/validate")
    suspend fun validateLogin(): ApiResponse<LoginVO>

    @POST("/login")
    @FormUrlEncoded
    suspend fun login(@Field("username") username: String, @Field("password") password: String): ApiResponse<LoginVO>

    @POST("/auth/register")
    suspend fun register(@Body registerDTO: RegisterDTO): ApiResponse<RegisterVO>

    @POST("/image/upload")
    suspend fun preUploadImage(): ApiResponse<ImagePreUploadVO>

    @POST("/point")
    suspend fun createPoint(@Body pointDTO: PointDTO): ApiResponse<PointVO>

    @GET("/point/around")
    suspend fun getPointAround(
        @Query("coordinateType") coordinateType: String, @Query("latitude") latitude: Double, @Query(
            "longitude"
        ) longitude: Double
    ): ApiResponse<List<PointVO>>

    @GET("/point/{id}")
    suspend fun getPointVO(@Path("id") pointId: Int): ApiResponse<PointVO>

    @GET("/user/{id}/profile")
    suspend fun getUserProfileVO(@Path("id") userId: Int): ApiResponse<UserProfileVO>
}