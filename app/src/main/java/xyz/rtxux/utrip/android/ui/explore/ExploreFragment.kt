package xyz.rtxux.utrip.android.ui.explore

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
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
import xyz.rtxux.utrip.android.base.BaseCachingFragment
import xyz.rtxux.utrip.android.base.MapViewLifeCycleBean
import xyz.rtxux.utrip.android.databinding.FragmentExploreBinding
import xyz.rtxux.utrip.android.model.UResult
import xyz.rtxux.utrip.android.model.repository.PointRepository
import xyz.rtxux.utrip.android.utils.toast
import xyz.rtxux.utrip.server.model.vo.PointVO
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

class ExploreFragment :
    BaseCachingFragment<ExploreViewModel, FragmentExploreBinding, ExploreFragment.ViewHolder>(
        ExploreViewModel::class.java
    ) {

    class ViewHolder : BaseCachingFragment.ViewHolder<FragmentExploreBinding>() {
        lateinit var mapboxMap: MapboxMap
        lateinit var locationComponent: LocationComponent
        var symbolManager: SymbolManager? = null
        var idle = true
        var points: List<PointVO> = listOf()
        val pointRepository by lazy { PointRepository() }
        val symbolToPointVO = mutableMapOf<Symbol, PointVO>()
        val pointVOToSymbol = mutableMapOf<PointVO, Symbol>()
        val markerLock: ReadWriteLock = ReentrantReadWriteLock()
        lateinit var mapViewLifecycleObserver: MapViewLifeCycleBean
        override fun clean() {

        }

    }
    @SuppressLint("NotChinaMapView")

    companion object {
        private val ID_ICON_LOC = "LOC_ICON_1"
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
            viewHolder.symbolManager =
                SymbolManager(viewHolder.mBinding.mainMap, viewHolder.mapboxMap, style)
            viewHolder.symbolManager!!.addClickListener {
                val pointVO = viewHolder.symbolToPointVO[it]!!
                findNavController().navigate(
                    ExploreFragmentDirections.actionNavigationExploreToPointInfoFragment(
                        pointVO.pointId
                    )
                )
            }
            viewHolder.locationComponent = viewHolder.mapboxMap.locationComponent
            viewHolder.locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(
                    context!!,
                    style
                ).build()
            )
            viewHolder.locationComponent.isLocationComponentEnabled = true
            viewHolder.locationComponent.cameraMode = CameraMode.TRACKING
            viewHolder.locationComponent.renderMode = RenderMode.COMPASS
            viewHolder.mapboxMap.addOnCameraIdleListener {
                if (viewHolder.idle) return@addOnCameraIdleListener
                viewHolder.idle = true
                Log.i("Camera", viewHolder.mapboxMap.cameraPosition.toString())
                mViewModel.viewModelScope.launch {
                    viewHolder.pointRepository.getPointAround(
                        "WGS-84",
                        viewHolder.mapboxMap.cameraPosition.target.latitude,
                        viewHolder.mapboxMap.cameraPosition.target.longitude
                    ).let {
                        when (it) {
                            is UResult.Success -> {
                                withContext(Dispatchers.Main) {
                                    try {
                                        viewHolder.markerLock.writeLock().lock()
                                        viewHolder.points = it.data
                                        updateMarkers()
                                    } finally {
                                        viewHolder.markerLock.writeLock().unlock()
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
            viewHolder.mapboxMap.addOnCameraMoveStartedListener {
                viewHolder.idle = false
            }
        }
    }

    fun updateMarkers() {
        try {
            viewHolder.markerLock.writeLock().lock()
            for (point in viewHolder.points) {
                if (!(point in viewHolder.pointVOToSymbol)) {
                    viewHolder.symbolManager!!.create(
                        SymbolOptions().withIconImage(ID_ICON_LOC).withIconAnchor(ICON_ANCHOR_BOTTOM).withLatLng(
                            LatLng(point.location.latitude, point.location.longitude)
                        )
                    ).apply {
                        viewHolder.symbolToPointVO.put(this, point)
                        viewHolder.pointVOToSymbol.put(point, this)
                    }
                } else {

                }
            }
            with(viewHolder.symbolToPointVO.iterator()) {
                forEach {
                    val pointVO = it.value
                    if (!(pointVO in viewHolder.points)) {
                        viewHolder.symbolManager!!.delete(it.key)
                        remove()
                        viewHolder.pointVOToSymbol.remove(pointVO)
                    }
                }
            }
        } finally {
            viewHolder.markerLock.writeLock().unlock()
        }

    }

    override fun createViewHolder(): ViewHolder = ViewHolder()

    override fun getLayoutResId(): Int = R.layout.fragment_explore

    override fun initData() {
        viewHolder.mBinding.viewModel = mViewModel
    }

    override fun initView(savedInstanceState: Bundle?) {
        viewHolder.mBinding.mainMap.onCreate(savedInstanceState)
        viewHolder.mapViewLifecycleObserver =
            MapViewLifeCycleBean(viewHolder.mBinding.mainMap).apply {
                viewHolder.lifecycle.addObserver(
                    this
                )
            }
        viewHolder.mBinding.mainMap.getMapAsync {
            viewHolder.mapboxMap = it
            it.setStyle(
                Style.Builder().fromUri(Style.MAPBOX_STREETS).withImage(
                    ID_ICON_LOC,
                    context!!.getDrawable(R.drawable.ic_location_on_accent)!!
                ), { onMapInit(it) })
            it.uiSettings.isLogoEnabled = false
            it.uiSettings.isAttributionEnabled = false
        }
        viewHolder.mBinding.publishButton.setOnClickListener {
            onFabClicked()
        }
    }


}