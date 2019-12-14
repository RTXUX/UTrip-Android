package xyz.rtxux.utrip.android.ui.point

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.widget.ImageView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.vanniktech.emoji.EmojiPopup
import com.youth.banner.BannerConfig
import com.youth.banner.loader.ImageLoader
import xyz.rtxux.utrip.android.R
import xyz.rtxux.utrip.android.base.BaseCachingFragment
import xyz.rtxux.utrip.android.base.GlideApp
import xyz.rtxux.utrip.android.base.MapViewLifeCycleBean
import xyz.rtxux.utrip.android.databinding.PointInfoFragmentBinding
import xyz.rtxux.utrip.android.model.api.ApiService
import xyz.rtxux.utrip.android.model.api.RetrofitClient
import xyz.rtxux.utrip.android.ui.zoomview.ImageZoomActivity
import xyz.rtxux.utrip.android.utils.toast
import java.text.SimpleDateFormat
import java.util.*

class PointInfoFragment :
    BaseCachingFragment<PointInfoViewModel, PointInfoFragmentBinding, PointInfoFragment.ViewHolder>(
    PointInfoViewModel::class.java
) {
    companion object {
        private val ID_ICON_LOC = "LOC_ICON_1"
    }

    class ViewHolder : BaseCachingFragment.ViewHolder<PointInfoFragmentBinding>() {
        lateinit var mapboxMap: MapboxMap
        lateinit var menu: Menu
        lateinit var commentAdapter: CommentAdapter
        override fun clean() {

        }

    }


    override fun getLayoutResId(): Int = R.layout.point_info_fragment
    private val args by navArgs<PointInfoFragmentArgs>()

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.point_info_menu, menu)
        viewHolder.menu = menu
    }

    override fun initView(savedInstanceState: Bundle?) {
        val binding = viewHolder.mBinding
        initCommentRecyclerView()
        viewHolder.lifecycleOwner.lifecycle.addObserver(MapViewLifeCycleBean(binding.infoMap))
        binding.viewModel = mViewModel
        binding.infoMap.mParentView = binding.layoutScroll
        binding.infoMap.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        binding.infoMap.getMapAsync {
            viewHolder.mapboxMap = it
            it.setStyle(
                Style.Builder().fromUri(Style.MAPBOX_STREETS).withImage(
                    ID_ICON_LOC,
                    context!!.getDrawable(R.drawable.ic_location_on_accent)!!
                ), { onMapInit(it) })
            it.uiSettings.isLogoEnabled = false
            it.uiSettings.isAttributionEnabled = false
        }
        binding.banner.setImageLoader(object : ImageLoader() {
            override fun displayImage(context: Context?, path: Any?, imageView: ImageView?) {
                GlideApp.with(context!!).load(path)
                    .apply(RequestOptions().placeholder(R.drawable.loading)).into(imageView!!)
            }
        })
        binding.banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR)
        mViewModel.point.observe(this, Observer {
            binding.tvTime.text =
                SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Date(it.timestamp)).toString()
            val imageUrls = it.images.map {
                "${ApiService.API_BASE}/image/${it}"
            }
            binding.banner.setImages(imageUrls)
            binding.banner.setOnBannerListener { position ->
                startActivity(Intent(context, ImageZoomActivity::class.java).apply {
                    putExtra("url", imageUrls[position])
                })
            }
            binding.banner.start()
        })
//        mViewModel.userProfile.observe(this, Observer {
//            GlideApp.with(context!!).load(it.avatarUrl).into(binding.ivAvatar)
//        })
        mViewModel.point.observe(viewHolder.lifecycleOwner, Observer {
            GlideApp.with(context!!).load("${ApiService.API_BASE}/user/${it.userId}/avatar")
                .into(binding.ivAvatar)
            viewHolder.mBinding.ivAvatar.setOnClickListener { _ ->
                findNavController().navigate(
                    PointInfoFragmentDirections.actionPointInfoFragmentToProfileEditFragment(
                        it.userId
                    )
                )
            }
        })
        mViewModel.point.observe(viewHolder.lifecycleOwner, Observer {
            if (it.userId == RetrofitClient.userId) {
                val deleteButton = viewHolder.menu.findItem(R.id.menu_btn_delete)
                deleteButton.isVisible = true
                deleteButton.setOnMenuItemClickListener {
                    mViewModel.deletePoint()
                    true
                }
            } else {
                val deleteButton = viewHolder.menu.findItem(R.id.menu_btn_delete)
                deleteButton.isVisible = false
            }
        })
        mViewModel.deleted.observe(viewHolder.lifecycleOwner, Observer {
            if (it == true) {
                toast("删除成功")
                findNavController().navigateUp()
            }

        })
        mViewModel.point.observe(viewHolder.lifecycleOwner, Observer {
            mViewModel.fetchComment()
        })

    }

    override fun initData() {
        val binding = viewHolder.mBinding
        binding.viewModel = mViewModel
        mViewModel.getPointVO(args.pointId)
    }

    @SuppressLint("CheckResult")
    fun onMapInit(style: Style) {
        RxPermissions(this).request(Manifest.permission.ACCESS_FINE_LOCATION).subscribe { granted ->
            if (!granted) {
                toast("需要定位权限")
                activity?.finish()
            }
            val symbolManager =
                SymbolManager(viewHolder.mBinding.infoMap, viewHolder.mapboxMap, style)
            mViewModel.point.observe(viewHolder.lifecycleOwner, Observer {
                val latLng = LatLng(it.location.latitude, it.location.longitude)
                symbolManager.create(
                    SymbolOptions()
                        .withLatLng(latLng)
                        .withIconAnchor(Property.ICON_ANCHOR_BOTTOM)
                        .withIconImage(ID_ICON_LOC)
                )
                viewHolder.mapboxMap.animateCamera {
                    CameraPosition.Builder().target(latLng).build()
                }
            })
            val locationComponent = viewHolder.mapboxMap.locationComponent
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

    override fun createViewHolder(): ViewHolder = ViewHolder()

    private fun initCommentRecyclerView() {
        val adapter = CommentAdapter()
        viewHolder.commentAdapter = adapter
        viewHolder.mBinding.rvComment.layoutManager = LinearLayoutManager(context!!)
        viewHolder.mBinding.rvComment.adapter = adapter
        mViewModel.comments.observe(viewHolder.lifecycleOwner, Observer {
            adapter.comments = it
        })
        adapter.deleteListener = {
            mViewModel.deleteComment(it)
        }
        viewHolder.mBinding.layoutSend.setOnClickListener {
            if (viewHolder.mBinding.inputText.text.toString().isBlank()) {
                viewHolder.mBinding.inputText.error = "空"
            } else {
                mViewModel.postComment(viewHolder.mBinding.inputText.text.toString())
                viewHolder.mBinding.inputText.text?.clear()
            }
        }
        val emojiPopup = EmojiPopup.Builder.fromRootView(viewHolder.mBinding.root)
            .build(viewHolder.mBinding.inputText)
        viewHolder.mBinding.layoutEmoji.setOnClickListener {
            if (emojiPopup.isShowing) {
                emojiPopup.dismiss()
            } else {
                emojiPopup.toggle()
            }
        }
    }

}
