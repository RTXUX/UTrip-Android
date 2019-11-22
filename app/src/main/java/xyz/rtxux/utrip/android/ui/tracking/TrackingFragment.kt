package xyz.rtxux.utrip.android.ui.tracking

import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.mapbox.android.core.location.*
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
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
import xyz.rtxux.utrip.android.model.realm.MyPoint

class TrackingFragment : BaseVMFragment<TrackingViewModel, TrackingFragmentBinding>(
    true,
    TrackingViewModel::class.java
) {
    override fun getLayoutResId(): Int = R.layout.tracking_fragment

    private lateinit var mapboxMap: MapboxMap
    private lateinit var locationEngine: LocationEngine
    private lateinit var locationComponent: LocationComponent
    private val callback = object : LocationEngineCallback<LocationEngineResult> {
        override fun onFailure(exception: Exception) {

        }

        override fun onSuccess(result: LocationEngineResult?) {
            result!!
            locationComponent.forceLocationUpdate(result.lastLocation)
            mViewModel.postPoint(result)

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ret = super.onCreateView(inflater, container, savedInstanceState)
        mBinding.viewModel = mViewModel
        mViewModel.myTrack = mViewModel.createTrack()
        initMap(savedInstanceState)
        mBinding.stopTrackingButton.setOnClickListener {
            mapboxMap.locationComponent.isLocationComponentEnabled = false
            locationEngine.removeLocationUpdates(callback)
            findNavController().navigateUp()
        }
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
                it.addSource(
                    GeoJsonSource(
                        "line-source",
                        generateFeatureCollection(mViewModel.myTrack.value!!.points)
                    )
                )
                it.addLayer(
                    LineLayer("line-layer", "line-source").withProperties(
                        PropertyFactory.lineDasharray(arrayOf<Float>(0.01f, .2f)),
                        PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                        PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                        PropertyFactory.lineWidth(5f),
                        PropertyFactory.lineColor(Color.parseColor("#e55e5e"))
                    )
                )
                mViewModel.myTrack.observe(this, Observer { track ->
                    mapboxMap.getStyle {
                        it.getSourceAs<GeoJsonSource>("line-source")
                            ?.setGeoJson(generateFeatureCollection(track.points))
                    }
                })
            }
        }
    }

    fun generateFeatureCollection(points: List<MyPoint>): FeatureCollection {
        return FeatureCollection.fromFeature(Feature.fromGeometry(LineString.fromLngLats(points.map {
            Point.fromLngLat(it.longitude, it.latitude)
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
