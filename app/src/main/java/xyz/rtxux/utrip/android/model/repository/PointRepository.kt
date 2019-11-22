package xyz.rtxux.utrip.android.model.repository

import xyz.rtxux.utrip.android.base.BaseRepository
import xyz.rtxux.utrip.android.base.Dummy
import xyz.rtxux.utrip.android.model.UResult
import xyz.rtxux.utrip.android.model.api.RetrofitClient
import xyz.rtxux.utrip.server.model.dto.PointDTO
import xyz.rtxux.utrip.server.model.vo.PointVO

class PointRepository : BaseRepository() {

    suspend fun createPoint(pointDTO: PointDTO): UResult<PointVO> {
        return safeApiCall(
            { executeResponse(RetrofitClient.service.createPoint(pointDTO)) },
            "发布失败"
        )
    }

    suspend fun getPointAround(
        coordinateType: String,
        latitude: Double,
        longitude: Double
    ): UResult<List<PointVO>> {
        return safeApiCall({
            executeResponse(
                RetrofitClient.service.getPointAround(
                    coordinateType,
                    latitude,
                    longitude
                )
            )
        }, "获取附近点失败")
    }

    suspend fun getPointVO(pointId: Int): UResult<PointVO> {
        return safeApiCall(
            { executeResponse(RetrofitClient.service.getPointVO(pointId)) },
            "获取信息失败"
        )
    }

    suspend fun deletePoint(pointId: Int): UResult<Dummy> {
        return safeApiCall(
            { executeResponse(RetrofitClient.service.deletePoint(pointId)) },
            "删除点失败"
        )
    }
}