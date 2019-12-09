package xyz.rtxux.utrip.android.model

/**
 * Created by luyao
 * on 2019/10/12 11:08
 */
sealed class UResult<out T : Any> {

    data class Success<out T : Any>(val data: T) : UResult<T>()
    data class Error(val exception: Exception) : UResult<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$exception]"
        }
    }


}