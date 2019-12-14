package xyz.rtxux.utrip.android.ui.explore

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.Observer
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
import xyz.rtxux.utrip.android.R
import xyz.rtxux.utrip.android.base.BaseCachingFragment
import xyz.rtxux.utrip.android.base.MapViewLifeCycleBean
import xyz.rtxux.utrip.android.databinding.FragmentExploreBinding
import xyz.rtxux.utrip.android.model.bean.PointVO
import xyz.rtxux.utrip.android.utils.toast
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

        val symbolToPointVO = mutableMapOf<Symbol, PointVO>()
        val pointVOToSymbol = mutableMapOf<PointVO, Symbol>()
        val markerLock: ReadWriteLock = ReentrantReadWriteLock()
        lateinit var mapViewLifecycleObserver: MapViewLifeCycleBean
        override fun clean() {

        }

    }

    var idle = true
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
                if (view == null) return@addOnCameraIdleListener
                if (idle) return@addOnCameraIdleListener
                idle = true
                mViewModel.loadPointAround(
                    viewHolder.mapboxMap.cameraPosition.target.latitude,
                    viewHolder.mapboxMap.cameraPosition.target.longitude
                )
            }
            viewHolder.mapboxMap.addOnCameraMoveStartedListener {
                idle = false
            }
            mViewModel.points.observe(viewHolder.lifecycleOwner, Observer {
                updateMarkers(it)
            })

        }
    }

    fun updateMarkers(points: List<PointVO>) {
        try {
            viewHolder.markerLock.writeLock().lock()
            for (point in points) {
                if (!(point in viewHolder.pointVOToSymbol)) {
                    viewHolder.symbolManager!!.create(
                        SymbolOptions().withIconImage(ID_ICON_LOC).withIconAnchor(ICON_ANCHOR_BOTTOM).withLatLng(
                            LatLng(point.location.latitude, point.location.longitude)
                        )
                    ).apply {
                        viewHolder.symbolToPointVO.put(this, point)
                        viewHolder.pointVOToSymbol.put(point, this)
                    }
                }
            }
            with(viewHolder.symbolToPointVO.iterator()) {
                forEach {
                    val pointVO = it.value
                    if (!(pointVO in points)) {
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
        val viewHolder = this.viewHolder
        viewHolder.mBinding.mainMap.onCreate(savedInstanceState)
        viewHolder.mapViewLifecycleObserver =
            MapViewLifeCycleBean(viewHolder.mBinding.mainMap).apply {
                viewHolder.lifecycleOwner.lifecycle.addObserver(
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