package xyz.rtxux.utrip.android.model.api

import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import xyz.rtxux.utrip.android.App

object RetrofitClient {
    private const val TIME_OUT = 5;

    private val cookieJar by lazy {
        PersistentCookieJar(
            SetCookieCache(), SharedPrefsCookiePersistor(
                App.CONTEXT
            )
        )
    }

    fun clearCookie() {
        cookieJar.clear()
    }

    val certificatePinner by lazy {
        CertificatePinner.Builder()
            .add("api.utrip.rtxux.xyz", "sha256/bupIWki8bCz32yHk1gJuwSC2ZhujBXLzNch2WYG+OWQ=")
            .build()
    }

    var userId: Int = 0

    val service by lazy {
        Retrofit.Builder()
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(ApiService.API_BASE)
            .build()
            .create(ApiService::class.java)
    }

    val client: OkHttpClient
        get() {
            val builder = OkHttpClient.Builder()
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            builder.certificatePinner(certificatePinner)
            builder.addInterceptor(logging)
            builder.addInterceptor { chain ->
                val request =
                    chain.request().newBuilder().addHeader("X-Requested-With", "XMLHTTPRequest")
                        .build()
                chain.proceed(request)
            }
            builder.cookieJar(cookieJar)
            return builder.build()
        }


}