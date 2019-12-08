package xyz.rtxux.utrip.android

import android.app.Application
import android.content.Context
import com.didichuxing.doraemonkit.DoraemonKit
import com.mapbox.mapboxsdk.Mapbox
import io.realm.Realm
import timber.log.Timber
import kotlin.properties.Delegates

class App : Application() {
    companion object {
        var CONTEXT: Context by Delegates.notNull()
    }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        DoraemonKit.install(this)
        CONTEXT = applicationContext
        Mapbox.getInstance(CONTEXT, getString(R.string.mapbox_access_token))
        Realm.init(CONTEXT)
    }
}