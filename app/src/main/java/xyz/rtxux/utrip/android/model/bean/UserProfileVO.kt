package xyz.rtxux.utrip.server.model.vo

data class UserProfileVO(
    val userId: Int,
    val username: String,
    val nickname: String,
    val gender: String? = null,
    val avatarUrl: String
)