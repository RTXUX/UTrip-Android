package xyz.rtxux.utrip.android

import android.app.Application
import android.content.Context
import com.mapbox.mapboxsdk.Mapbox
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import io.realm.Realm
import timber.log.Timber
import java.io.File
import kotlin.properties.Delegates

class App : Application() {
    companion object {
        var CONTEXT: Context by Delegates.notNull()
        lateinit var imageCacheDir: File
    }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        CONTEXT = applicationContext
        Mapbox.getInstance(CONTEXT, getString(R.string.mapbox_access_token))
        Realm.init(CONTEXT)
        imageCacheDir = filesDir.toPath().resolve("img_cache").toFile()
        imageCacheDir.mkdirs()
        EmojiManager.install(GoogleEmojiProvider())
    }
}