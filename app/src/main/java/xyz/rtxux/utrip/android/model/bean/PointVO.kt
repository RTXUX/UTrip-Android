package xyz.rtxux.utrip.server.model.vo

import xyz.rtxux.utrip.server.model.dto.LocationBean

data class PointVO(
    val pointId: Int,
    val name: String,
    val description: String,
    val location: LocationBean,
    val userId: Int,
    val timestamp: Long,
    val associatedTrack: Int? = null,
    val images: List<Int>,
    val like: Int
)