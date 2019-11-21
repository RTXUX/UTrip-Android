package xyz.rtxux.utrip.android.ui.tracking

import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.mapbox.android.core.location.*
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import xyz.rtxux.utrip.android.R
import xyz.rtxux.utrip.android.base.BaseVMFragment
import xyz.rtxux.utrip.android.databinding.TrackingFragmentBinding

class TrackingFragment : BaseVMFragment<TrackingViewModel, TrackingFragmentBinding>(
    true,
    TrackingViewModel::class.java
) {
    override fun getLayoutResId(): Int = R.layout.tracking_fragment

    private lateinit var viewModel: TrackingViewModel
    private lateinit var mapboxMap: MapboxMap
    private lateinit var locationEngine: LocationEngine
    private lateinit var locationComponent: LocationComponent
    private val callback = object : LocationEngineCallback<LocationEngineResult> {
        override fun onFailure(exception: Exception) {

        }

        override fun onSuccess(result: LocationEngineResult?) {
            result!!
            locationComponent.forceLocationUpdate(result.lastLocation)
            mViewModel.points.value?.add(
                MyPoint(
                    LatLng(result.lastLocation!!.latitude, result.lastLocation!!.longitude),
                    System.currentTimeMillis()
                )
            )
            mViewModel.points.postValue(mViewModel.points.value)
            mapboxMap.animateCamera {
                CameraPosition.Builder()
                    .target(LatLng(result.lastLocation!!.latitude, result.lastLocation!!.longitude))
                    .build()
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ret = super.onCreateView(inflater, container, savedInstanceState)
        mBinding.viewModel = mViewModel
        mViewModel.points.value = mutableListOf()
        initMap(savedInstanceState)

        return ret
    }

    fun initMap(savedInstanceState: Bundle?) {
        mBinding.trackingMap.onCreate(savedInstanceState)
        mBinding.trackingMap.getMapAsync {
            it.uiSettings.isAttributionEnabled = false
            it.uiSettings.isLogoEnabled = false
            mapboxMap = it
            it.setStyle(Style.MAPBOX_STREETS) {
                locationComponent = mapboxMap.locationComponent
                locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(
                        context!!,
                        it
                    ).build()
                )
                locationComponent.isLocationComponentEnabled = true
                locationComponent.cameraMode = CameraMode.TRACKING
                locationComponent.renderMode = RenderMode.COMPASS
                initLocationEngine()
                it.addSource(GeoJsonSource("line-source", generateFeatureCollection()))
                it.addLayer(
                    LineLayer("line-layer", "line-source").withProperties(
                        PropertyFactory.lineDasharray(arrayOf<Float>(0.01f, .2f)),
                        PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                        PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                        PropertyFactory.lineWidth(5f),
                        PropertyFactory.lineColor(Color.parseColor("#e55e5e"))
                    )
                )
                mViewModel.points.observe(this, Observer { points ->
                    it.getSourceAs<GeoJsonSource>("line-source")
                        ?.setGeoJson(generateFeatureCollection())
                })
            }
        }
    }

    fun generateFeatureCollection(): FeatureCollection {
        return FeatureCollection.fromFeature(Feature.fromGeometry(LineString.fromLngLats(mViewModel.points.value!!.map {
            Point.fromLngLat(it.latLng.longitude, it.latLng.latitude)
        })))
    }

    fun initLocationEngine() {

        locationEngine = LocationEngineProvider.getBestLocationEngine(context!!)
        val request = LocationEngineRequest.Builder(1000L)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY).setMaxWaitTime(5000L).build()
        locationEngine.requestLocationUpdates(request, callback, Looper.getMainLooper())
        locationEngine.getLastLocation(callback)
    }

    override fun onStart() {
        super.onStart()
        mBinding.trackingMap.onStart()
    }

    override fun onStop() {
        mBinding.trackingMap.onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        mBinding.trackingMap.onDestroy()
        super.onDestroyView()
    }

    override fun onPause() {
        mBinding.trackingMap.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mBinding.trackingMap.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mBinding.trackingMap.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mBinding.trackingMap.onLowMemory()
    }


}
