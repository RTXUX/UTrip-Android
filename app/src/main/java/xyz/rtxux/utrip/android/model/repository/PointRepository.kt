package xyz.rtxux.utrip.android.model.repository

import xyz.rtxux.utrip.android.base.BaseRepository
import xyz.rtxux.utrip.android.base.Dummy
import xyz.rtxux.utrip.android.model.UResult
import xyz.rtxux.utrip.android.model.api.RetrofitClient
import xyz.rtxux.utrip.android.model.bean.CommentVO
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

    suspend fun findByUser(userId: Int): UResult<List<PointVO>> {
        return safeApiCall(
            { executeResponse(RetrofitClient.service.findPointByUser(userId)) },
            "未能获取点"
        )
    }

    suspend fun getComment(pointId: Int): UResult<List<CommentVO>> {
        return safeApiCall(
            { executeResponse(RetrofitClient.service.getComment(pointId)) },
            "获取评论失败"
        )
    }

    suspend fun postComment(pointId: Int, content: String): UResult<CommentVO> {
        return safeApiCall(
            { executeResponse(RetrofitClient.service.postComment(pointId, content)) },
            "发布评论失败"
        )
    }

    suspend fun deleteComment(pointId: Int, commentId: Int): UResult<Unit> {
        return safeApiCall(
            { executeResponse(RetrofitClient.service.deleteComment(pointId, commentId)) },
            "删除评论失败"
        )
    }
}