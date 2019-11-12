package xyz.rtxux.utrip.android.base

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import retrofit2.HttpException
import xyz.rtxux.utrip.android.model.UResult
import xyz.rtxux.utrip.android.model.bean.ApiResponse
import java.io.IOException

abstract class BaseRepository {
    suspend fun <T : Any> safeApiCall(
        call: suspend () -> UResult<T>,
        errorMessage: String
    ): UResult<T> {
        return try {
            call()
        } catch (e: HttpException) {
            val response = Gson().fromJson<ApiResponse<Nothing>>(
                e.response()?.errorBody()?.charStream(),
                ApiResponse::class.java
            )
            executeResponse(response)
        } catch (e: Exception) {
            UResult.Error(IOException(errorMessage, e))
        }
    }

    suspend fun <T : Any> executeResponse(
        response: ApiResponse<T>, successBlock: (suspend CoroutineScope.() -> Unit)? = null,
        errorBlock: (suspend CoroutineScope.() -> Unit)? = null
    ): UResult<T> {
        return coroutineScope {
            if (response.code != 0) {
                errorBlock?.let { it() }
                UResult.Error(AppException(response.code, response.msg, null))
            } else {
                successBlock?.let { it() }
                UResult.Success(response.data!!)
            }
        }
    }
}