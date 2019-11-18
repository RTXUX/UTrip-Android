package xyz.rtxux.utrip.android.ui.explore

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.rtxux.utrip.android.R
import xyz.rtxux.utrip.android.model.UResult
import xyz.rtxux.utrip.android.model.repository.PointRepository
import xyz.rtxux.utrip.android.utils.toast
import xyz.rtxux.utrip.server.model.vo.PointVO
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

class ExploreFragment : Fragment() {

    private lateinit var mainMap: MapView
    private lateinit var mapboxMap: MapboxMap
    private lateinit var exploreViewModel: ExploreViewModel
    private lateinit var locationComponent: LocationComponent
    private lateinit var symbolManager: SymbolManager
    private var idle = true
    private var points: List<PointVO> = listOf()
    private val pointRepository by lazy { PointRepository() }
    private val symbolToPointVO = mutableMapOf<Symbol, PointVO>()
    private val pointVOToSymbol = mutableMapOf<PointVO, Symbol>()
    private val markerLock: ReadWriteLock = ReentrantReadWriteLock()

    companion object {
        private val ID_ICON_LOC = "LOC_ICON_1"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        exploreViewModel =
            ViewModelProviders.of(this).get(ExploreViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_explore, container, false)
        mainMap = root.findViewById<MapView>(R.id.mainMap)
        mainMap.onCreate(savedInstanceState)
        mainMap.getMapAsync {
            mapboxMap = it
            it.setStyle(
                Style.Builder().fromUri(Style.MAPBOX_STREETS).withImage(
                    ID_ICON_LOC,
                    context!!.getDrawable(R.drawable.ic_location_on_accent)!!
                ), { onMapInit(it) })
            it.uiSettings.isLogoEnabled = false
            it.uiSettings.isAttributionEnabled = false
        }
        root.findViewById<FloatingActionButton>(R.id.publishButton)
            .setOnClickListener { onFabClicked() }
        return root
    }

    fun onFabClicked() {
        findNavController().navigate(ExploreFragmentDirections.actionNavigationExploreToPublishPointFragment())
    }

    @SuppressLint("CheckResult")
    fun onMapInit(style: Style) {
        RxPermissions(this).request(Manifest.permission.ACCESS_FINE_LOCATION).subscribe { granted ->
            if (!granted) {
                toast("需要定位权限")
                activity?.finish()
            }
            symbolManager = SymbolManager(mainMap, mapboxMap, style)
            symbolManager.addClickListener {
                val pointVO = symbolToPointVO[it]!!
                findNavController().navigate(
                    ExploreFragmentDirections.actionNavigationExploreToPointInfoFragment(
                        pointVO.pointId
                    )
                )
            }
            locationComponent = mapboxMap.locationComponent
            locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(
                    context!!,
                    style
                ).build()
            )
            locationComponent.isLocationComponentEnabled = true
            locationComponent.cameraMode = CameraMode.TRACKING
            locationComponent.renderMode = RenderMode.COMPASS
            mapboxMap.addOnCameraIdleListener {
                if (idle) return@addOnCameraIdleListener
                Log.i("Camera", mapboxMap.cameraPosition.toString())
                exploreViewModel.viewModelScope.launch {
                    pointRepository.getPointAround(
                        "WGS-84",
                        mapboxMap.cameraPosition.target.latitude,
                        mapboxMap.cameraPosition.target.longitude
                    ).let {
                        when (it) {
                            is UResult.Success -> {
                                withContext(Dispatchers.Main) {
                                    try {
                                        markerLock.writeLock().lock()
                                        points = it.data
                                        updateMarkers()
                                    } finally {
                                        markerLock.writeLock().unlock()
                                    }
                                }
                            }
                            is UResult.Error -> {
                                toast(it.exception.message!!)
                            }
                        }
                    }
                }
            }
            mapboxMap.addOnCameraMoveStartedListener {
                idle = false
            }
        }
    }

    fun updateMarkers() {
        try {
            markerLock.writeLock().lock()
            for (point in points) {
                if (!(point in pointVOToSymbol)) {
                    symbolManager.create(
                        SymbolOptions().withIconImage(ID_ICON_LOC).withIconAnchor(ICON_ANCHOR_BOTTOM).withLatLng(
                            LatLng(point.location.latitude, point.location.longitude)
                        )
                    ).apply {
                        symbolToPointVO.put(this, point)
                        pointVOToSymbol.put(point, this)
                    }
                } else {

                }
            }
            with(symbolToPointVO.iterator()) {
                forEach {
                    val pointVO = it.value
                    if (!(pointVO in points)) {
                        symbolManager.delete(it.key)
                        remove()
                        pointVOToSymbol.remove(pointVO)
                    }
                }
            }
        } finally {
            markerLock.writeLock().unlock()
        }

    }

    override fun onStart() {
        super.onStart()
        mainMap.onStart()
    }

    override fun onStop() {
        mainMap.onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        locationComponent.isLocationComponentEnabled = false
        mainMap.onDestroy()
        super.onDestroyView()
    }

    override fun onPause() {
        mainMap.onPause()
        symbolManager.deleteAll()
        symbolToPointVO.clear()
        pointVOToSymbol.clear()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mainMap.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mainMap.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mainMap.onLowMemory()
    }
}