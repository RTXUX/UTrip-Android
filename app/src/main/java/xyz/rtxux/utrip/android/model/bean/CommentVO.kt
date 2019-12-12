package xyz.rtxux.utrip.android.model.bean

data class CommentVO(
    val id: Int,
    val userId: Int,
    val pointId: Int,
    val userNickname: String,
    val timestamp: Long,
    val content: String
)