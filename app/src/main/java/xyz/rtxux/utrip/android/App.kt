package xyz.rtxux.utrip.android

import android.app.Application
import android.content.Context
import com.mapbox.mapboxsdk.Mapbox
import io.realm.Realm
import leakcanary.LeakCanary
import kotlin.properties.Delegates

class App : Application() {
    companion object {
        var CONTEXT: Context by Delegates.notNull()
    }

    override fun onCreate() {
        super.onCreate()
        CONTEXT = applicationContext
        LeakCanary.config =
            LeakCanary.config.copy(retainedVisibleThreshold = 3, dumpHeapWhenDebugging = true)
        Mapbox.getInstance(CONTEXT, getString(R.string.mapbox_access_token))
        Realm.init(CONTEXT)
    }
}