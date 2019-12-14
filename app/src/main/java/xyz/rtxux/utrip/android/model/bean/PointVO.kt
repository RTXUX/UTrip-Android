package xyz.rtxux.utrip.android.model.bean

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