package xyz.rtxux.utrip.android

import android.app.Application
import android.content.Context
import com.mapbox.mapboxsdk.Mapbox
import io.realm.Realm
import kotlin.properties.Delegates

class App : Application() {
    companion object {
        var CONTEXT: Context by Delegates.notNull()
    }

    override fun onCreate() {
        super.onCreate()
        CONTEXT = applicationContext
        Mapbox.getInstance(CONTEXT, getString(R.string.mapbox_access_token))
        Realm.init(CONTEXT)
    }
}