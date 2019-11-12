package xyz.rtxux.utrip.android.model.bean

data class ApiResponse<T>(
    val code: Int,
    val msg: String?,
    val data: T?
)