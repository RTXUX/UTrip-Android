package xyz.rtxux.utrip.android.ui.point

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.Observer
import com.bumptech.glide.request.RequestOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.style.layers.Property
import com.tbruyelle.rxpermissions2.RxPermissions
import com.youth.banner.BannerConfig
import com.youth.banner.loader.ImageLoader
import xyz.rtxux.utrip.android.R
import xyz.rtxux.utrip.android.base.BaseVMFragment
import xyz.rtxux.utrip.android.base.GlideApp
import xyz.rtxux.utrip.android.databinding.PointInfoFragmentBinding
import xyz.rtxux.utrip.android.model.api.ApiService
import xyz.rtxux.utrip.android.utils.toast
import java.text.SimpleDateFormat
import java.util.*

class PointInfoFragment : BaseVMFragment<PointInfoViewModel, PointInfoFragmentBinding>(
    true,
    PointInfoViewModel::class.java
) {
    companion object {
        private val ID_ICON_LOC = "LOC_ICON_1"
    }

    private lateinit var mapboxMap: MapboxMap
    override fun getLayoutResId(): Int = R.layout.point_info_fragment
    private val pointId: Int by lazy { arguments!!["pointId"] as Int }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val res = super.onCreateView(inflater, container, savedInstanceState)
        mBinding.viewModel = mViewModel
        mBinding.infoMap.mParentView = mBinding.layoutScroll
        mBinding.infoMap.onCreate(savedInstanceState)
        initView()
        initData()
        return res
    }

    fun initView() {

        mBinding.infoMap.getMapAsync {
            mapboxMap = it
            it.setStyle(
                Style.Builder().fromUri(Style.MAPBOX_STREETS).withImage(
                    ID_ICON_LOC,
                    context!!.getDrawable(R.drawable.ic_location_on_accent)!!
                ), { onMapInit(it) })
            it.uiSettings.isLogoEnabled = false
            it.uiSettings.isAttributionEnabled = false
        }
        mBinding.banner.setImageLoader(object : ImageLoader() {
            override fun displayImage(context: Context?, path: Any?, imageView: ImageView?) {
                GlideApp.with(context!!).load(path)
                    .apply(RequestOptions().placeholder(R.drawable.loading)).into(imageView!!)
            }
        })
        mBinding.banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR)
        mViewModel.point.observe(this, Observer {
            mBinding.tvTime.text =
                SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Date(it.timestamp)).toString()
            mBinding.banner.setImages(it.images.map {
                "${ApiService.API_BASE}/image/${it}"
            })
            mBinding.banner.start()
        })
        mViewModel.userProfile.observe(this, Observer {
            GlideApp.with(context!!).load(it.avatarUrl).into(mBinding.ivAvatar)
        })
    }

    fun initData() {
        mViewModel.getPointVO(pointId)
    }

    @SuppressLint("CheckResult")
    fun onMapInit(style: Style) {
        RxPermissions(this).request(Manifest.permission.ACCESS_FINE_LOCATION).subscribe { granted ->
            if (!granted) {
                toast("需要定位权限")
                activity?.finish()
            }
            val symbolManager = SymbolManager(mBinding.infoMap, mapboxMap, style)
            mViewModel.point.observe(this, Observer {
                val latLng = LatLng(it.location.latitude, it.location.longitude)
                symbolManager.create(
                    SymbolOptions()
                        .withLatLng(latLng)
                        .withIconAnchor(Property.ICON_ANCHOR_BOTTOM)
                        .withIconImage(ID_ICON_LOC)
                )
                mapboxMap.animateCamera {
                    CameraPosition.Builder().target(latLng).build()
                }
            })
            val locationComponent = mapboxMap.locationComponent
            locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(
                    context!!,
                    style
                ).build()
            )
            locationComponent.isLocationComponentEnabled = true
            locationComponent.cameraMode = CameraMode.NONE
            locationComponent.renderMode = RenderMode.COMPASS

        }
    }

    override fun onStart() {
        super.onStart()
        mBinding.infoMap.onStart()
    }

    override fun onStop() {
        mBinding.infoMap.onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        mBinding.infoMap.onDestroy()
        super.onDestroyView()
    }

    override fun onPause() {
        mBinding.infoMap.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mBinding.infoMap.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mBinding.infoMap.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mBinding.infoMap.onLowMemory()
    }

}
