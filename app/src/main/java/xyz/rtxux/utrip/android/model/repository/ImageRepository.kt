package xyz.rtxux.utrip.android.model.repository

import android.graphics.Bitmap
import com.google.gson.Gson
import com.qiniu.android.http.ResponseInfo
import org.json.JSONObject
import xyz.rtxux.utrip.android.base.AppException
import xyz.rtxux.utrip.android.base.BaseRepository
import xyz.rtxux.utrip.android.model.UResult
import xyz.rtxux.utrip.android.model.api.QiniuApiService
import xyz.rtxux.utrip.android.model.api.RetrofitClient
import xyz.rtxux.utrip.android.model.bean.ApiResponse
import xyz.rtxux.utrip.server.model.vo.ImagePreUploadVO
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ImageRepository : BaseRepository() {

    suspend fun preUploadImage(): UResult<ImagePreUploadVO> {
        return safeApiCall({ executeResponse(RetrofitClient.service.preUploadImage()) }, "获取上传凭据失败")
    }

    suspend fun uploadImage(bitmap: Bitmap, imagePreUploadVO: ImagePreUploadVO): UResult<Nothing> {
        return safeApiCall({
            val res = suspendCoroutine<JSONObject> { continuation ->
                val bos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
                QiniuApiService.uploadManager.put(
                    bos.toByteArray(),
                    imagePreUploadVO.key,
                    imagePreUploadVO.signature,
                    { key: String?, info: ResponseInfo?, res: JSONObject? ->
                        if (info!!.isOK) {
                            continuation.resume(res!!)
                        } else {
                            continuation.resumeWithException(AppException(3001, "上传失败", null))
                        }
                    },
                    null
                )
            }
            executeResponse<Nothing>(
                Gson().fromJson(
                    res.toString(),
                    ApiResponse::class.java
                ) as ApiResponse<Nothing>
            )
        }, "上传失败")
    }


}