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
import xyz.rtxux.utrip.android.base.BaseCachingFragment
import xyz.rtxux.utrip.android.base.MapViewLifeCycleBean
import xyz.rtxux.utrip.android.databinding.TrackingFragmentBinding
import xyz.rtxux.utrip.android.model.realm.MyPoint

class TrackingFragment :
    BaseCachingFragment<TrackingViewModel, TrackingFragmentBinding, TrackingFragment.ViewHolder>(
    TrackingViewModel::class.java
) {
    class ViewHolder : BaseCachingFragment.ViewHolder<TrackingFragmentBinding>() {
        lateinit var mapboxMap: MapboxMap
        lateinit var locationEngine: LocationEngine
        lateinit var locationComponent: LocationComponent
        val callback = object : LocationEngineCallback<LocationEngineResult> {
            override fun onFailure(exception: Exception) {

            }

            override fun onSuccess(result: LocationEngineResult?) {
                result!!
                locationComponent.forceLocationUpdate(result.lastLocation)
                mBinding.viewModel?.postPoint(result)
            }

        }

        override fun clean() {

        }

    }

    override fun getLayoutResId(): Int = R.layout.tracking_fragment



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ret = super.onCreateView(inflater, container, savedInstanceState)


        return ret
    }

    fun initMap(savedInstanceState: Bundle?) {
        viewHolder.mBinding.trackingMap.onCreate(savedInstanceState)
        viewHolder.lifecycleOwner.lifecycle.addObserver(MapViewLifeCycleBean(viewHolder.mBinding.trackingMap))
        viewHolder.mBinding.trackingMap.getMapAsync {
            it.uiSettings.isAttributionEnabled = false
            it.uiSettings.isLogoEnabled = false
            viewHolder.mapboxMap = it
            it.setStyle(Style.MAPBOX_STREETS) {
                viewHolder.locationComponent = viewHolder.mapboxMap.locationComponent
                viewHolder.locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(
                        context!!,
                        it
                    ).build()
                )
                viewHolder.locationComponent.isLocationComponentEnabled = true
                viewHolder.locationComponent.cameraMode = CameraMode.TRACKING
                viewHolder.locationComponent.renderMode = RenderMode.COMPASS
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
                mViewModel.myTrack.observe(viewHolder.lifecycleOwner, Observer { track ->
                    viewHolder.mapboxMap.getStyle {
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

        viewHolder.locationEngine = LocationEngineProvider.getBestLocationEngine(context!!)
        val request = LocationEngineRequest.Builder(1000L)
            .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY).setMaxWaitTime(5000L).build()
        viewHolder.locationEngine.requestLocationUpdates(
            request,
            viewHolder.callback,
            Looper.getMainLooper()
        )
        viewHolder.locationEngine.getLastLocation(viewHolder.callback)
    }



    override fun onSaveInstanceState(outState: Bundle) {
        viewHolder.mBinding.trackingMap.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        viewHolder.mBinding.trackingMap.onLowMemory()
    }

    override fun createViewHolder(): ViewHolder = ViewHolder()

    override fun initData() {
        viewHolder.mBinding.viewModel = mViewModel
        mViewModel.myTrack = mViewModel.createTrack()
    }

    override fun initView(savedInstanceState: Bundle?) {
        initMap(savedInstanceState)
        viewHolder.mBinding.stopTrackingButton.setOnClickListener {
            viewHolder.mapboxMap.locationComponent.isLocationComponentEnabled = false
            viewHolder.locationEngine.removeLocationUpdates(viewHolder.callback)
            findNavController().navigateUp()
        }
    }


}
