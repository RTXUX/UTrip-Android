package xyz.rtxux.utrip.android.base


import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.mapboxsdk.maps.MapView
import java.lang.ref.WeakReference

class MapViewLifeCycleBean(
    mapView: MapView?
) : DefaultLifecycleObserver {
    constructor() : this(null)

    private var mapViewWeakRef = WeakReference<MapView>(mapView)
    fun setMapView(mapView: MapView) {
        mapViewWeakRef = WeakReference(mapView)
    }

    override fun onCreate(owner: LifecycleOwner) {
        //mapViewWeakRef.get()?.onCreate(null)
    }

    override fun onResume(owner: LifecycleOwner) {
        mapViewWeakRef.get()?.onResume()
        Log.d(this.javaClass.simpleName, "Map Resumed")
    }

    override fun onPause(owner: LifecycleOwner) {
        mapViewWeakRef.get()?.onPause()
        Log.d(this.javaClass.simpleName, "Map Paused")
    }

    override fun onStart(owner: LifecycleOwner) {
        mapViewWeakRef.get()?.onStart()
        Log.d(this.javaClass.simpleName, "Map Started")
    }

    override fun onStop(owner: LifecycleOwner) {
        mapViewWeakRef.get()?.onStop()
        Log.d(this.javaClass.simpleName, "Map Stopped")
    }

    override fun onDestroy(owner: LifecycleOwner) {
//        Log.d(this.javaClass.simpleName, "Map Destroyed")
//        mapViewWeakRef.get()?.onDestroy()
    }
}