package xyz.rtxux.utrip.android.base

class AppException(
    val code: Int,
    val friendlyMessage: String?,
    override val cause: Throwable?
) : RuntimeException(friendlyMessage ?: cause?.message, cause)