package xyz.rtxux.utrip.server.model.vo

data class ImagePreUploadVO(
    val id: Int,
    val key: String,
    val policy: String,
    val url: String,
    val signature: String
)