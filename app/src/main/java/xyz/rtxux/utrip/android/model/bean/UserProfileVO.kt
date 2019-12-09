package xyz.rtxux.utrip.android.model.bean


data class UserProfileVO(
    val userId: Int,
    val username: String,
    var nickname: String,
    var gender: String? = null,
    var email: String? = null,
    var phone: String? = null,
    var qq: String? = null
)