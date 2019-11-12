package xyz.rtxux.utrip.android.model.bean


data class RegisterDTO(
    val username: String,
    val password: String,
    val nickname: String? = null
)