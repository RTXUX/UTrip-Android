package xyz.rtxux.utrip.android.model.api

import com.qiniu.android.storage.Configuration
import com.qiniu.android.storage.UploadManager

object QiniuApiService {
    private val configuration by lazy {
        Configuration.Builder().build()
    }

    val uploadManager by lazy {
        UploadManager(configuration, 5)
    }
}