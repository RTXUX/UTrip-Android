package xyz.rtxux.utrip.server.model.dto


data class PointDTO(
    val name: String,
    val description: String,
    val images: List<Int>,
    val associatedTrack: Int?,
    val location: LocationBean
)