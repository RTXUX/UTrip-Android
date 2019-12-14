package xyz.rtxux.utrip.android.model.bean

data class ImagePreUploadVO(
    val id: Int,
    val key: String,
    val policy: String,
    val url: String,
    val signature: String
)