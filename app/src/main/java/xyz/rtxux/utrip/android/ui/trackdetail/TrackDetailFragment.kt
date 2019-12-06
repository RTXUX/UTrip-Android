package xyz.rtxux.utrip.android.ui.trackdetail

import android.graphics.Color
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource

import xyz.rtxux.utrip.android.R
import xyz.rtxux.utrip.android.base.BaseVMFragment
import xyz.rtxux.utrip.android.base.MapViewLifeCycleBean
import xyz.rtxux.utrip.android.databinding.TrackDetailFragmentBinding
import xyz.rtxux.utrip.android.model.realm.MyPoint

class TrackDetailFragment : BaseVMFragment<TrackDetailViewModel, TrackDetailFragmentBinding>(true, TrackDetailViewModel::class.java) {
    override fun getLayoutResId(): Int = R.layout.track_detail_fragment
    private val args by navArgs<TrackDetailFragmentArgs>()
    private lateinit var mapboxMap: MapboxMap
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ret = super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)
        mBinding.viewModel = mViewModel
        mViewModel.loadTrack(args.trackId)
        initMap(savedInstanceState)
        return ret
    }


    fun initMap(savedInstanceState: Bundle?) {
        mBinding.trackMap.onCreate(savedInstanceState)
        lifecycle.addObserver(MapViewLifeCycleBean(mBinding.trackMap))
        mBinding.trackMap.getMapAsync {
            mapboxMap = it
            it.uiSettings.isAttributionEnabled = false
            it.uiSettings.isLogoEnabled = false
            it.setStyle(Style.MAPBOX_STREETS) {
                it.addSource(GeoJsonSource("line-source", generateFeatureCollection(mViewModel.myTrack.value!!.points)))
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
                    moveCamera()
                })
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.point_info_menu, menu)
        val btnDelete = menu.findItem(R.id.menu_btn_delete)
        btnDelete.isVisible = true
        btnDelete.setOnMenuItemClickListener {
            mViewModel.myTrack.removeObservers(this)
            mViewModel.deleteTrack()
            findNavController().navigateUp()
            true
        }
    }

    fun moveCamera() {
        LatLngBounds.Builder().includes(
            mViewModel.myTrack.value!!.points.map {
                LatLng(it.latitude, it.longitude)
            }
        ).build()?.let {
            mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(it, 50), 3000)
        }
    }

    override fun onDestroyView() {
        mBinding.trackMap.onDestroy()
        super.onDestroyView()
    }

    fun generateFeatureCollection(points: List<MyPoint>): FeatureCollection {
        return FeatureCollection.fromFeature(Feature.fromGeometry(LineString.fromLngLats(points.map {
            Point.fromLngLat(it.longitude, it.latitude)
        })))
    }
}
