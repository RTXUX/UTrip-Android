package xyz.rtxux.utrip.android.ui.trackdetail

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.NestedScrollView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.rtxux.utrip.android.App
import xyz.rtxux.utrip.android.R
import xyz.rtxux.utrip.android.base.BaseCachingFragment
import xyz.rtxux.utrip.android.base.GlideApp
import xyz.rtxux.utrip.android.base.MapViewLifeCycleBean
import xyz.rtxux.utrip.android.databinding.TrackDetailFragmentBinding
import xyz.rtxux.utrip.android.model.realm.MyPoint
import xyz.rtxux.utrip.android.ui.zoomview.ImageZoomActivity
import xyz.rtxux.utrip.android.utils.CommonUtils
import xyz.rtxux.utrip.android.utils.toast
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TrackDetailFragment :
    BaseCachingFragment<TrackDetailViewModel, TrackDetailFragmentBinding, TrackDetailFragment.ViewHolder>(
        TrackDetailViewModel::class.java
    ) {

    class ViewHolder : BaseCachingFragment.ViewHolder<TrackDetailFragmentBinding>() {
        lateinit var mapboxMap: MapboxMap
        lateinit var symbolManager: SymbolManager
        val symbolIdToPointId = mutableMapOf<Long, Int>()
        lateinit var bottomSheet: BottomSheetBehavior<NestedScrollView>
        override fun clean() {

        }

    }

    companion object {
        val ID_ICON_LOC = "ID_ICON_LOC"
    }

    class ActionModeCallback : ActionMode.Callback {

        var actionItemClickedCallback: ((ActionMode?, MenuItem?) -> Boolean)? = null

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return actionItemClickedCallback?.invoke(mode, item) ?: true
        }

        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.menuInflater?.inflate(R.menu.trackdetail_action_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode?) {

        }

    }

    lateinit var actionModeCallback: ActionModeCallback
    
    override fun getLayoutResId(): Int = R.layout.track_detail_fragment
    private val args by navArgs<TrackDetailFragmentArgs>()


    @SuppressLint("useChinaStyleVersion")
    fun initMap(savedInstanceState: Bundle?) {
        viewHolder.mBinding.trackMap.onCreate(savedInstanceState)
        viewHolder.lifecycleOwner.lifecycle.addObserver(MapViewLifeCycleBean(viewHolder.mBinding.trackMap))
        viewHolder.mBinding.trackMap.getMapAsync {
            viewHolder.mapboxMap = it
            it.uiSettings.isAttributionEnabled = false
            it.uiSettings.isLogoEnabled = false
            it.setStyle(
                Style.Builder().fromUri(Style.MAPBOX_STREETS).withImage(
                    ID_ICON_LOC,
                    context!!.getDrawable(R.drawable.ic_location_on_accent)!!
                )
            ) {
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
                mViewModel.myTrack.observe(viewHolder.lifecycleOwner, Observer { track ->
                    viewHolder.mapboxMap.getStyle {
                        it.getSourceAs<GeoJsonSource>("line-source")
                            ?.setGeoJson(
                                generateFeatureCollection(
                                    track.points.where().equalTo(
                                        "pathPoint",
                                        true
                                    ).findAll()
                                )
                            )
                    }
                    moveCamera()
                })
                viewHolder.symbolManager =
                    SymbolManager(viewHolder.mBinding.trackMap, viewHolder.mapboxMap, it)
                mViewModel.markerPoints.observe(viewHolder.lifecycleOwner, Observer {
                    viewHolder.symbolIdToPointId.clear()
                    viewHolder.symbolManager.deleteAll()
                    it.forEach {
                        viewHolder.symbolManager.create(
                            SymbolOptions().withIconImage(ID_ICON_LOC).withIconAnchor(Property.ICON_ANCHOR_BOTTOM).withLatLng(
                                LatLng(it.latitude, it.longitude)
                            )
                        ).apply {
                            viewHolder.symbolIdToPointId[id] = it.id
                        }
                    }
                })
                viewHolder.symbolManager.addClickListener {
                    mViewModel.setSelectedPoint(viewHolder.symbolIdToPointId[it.id]!!)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.trackdetail_menu, menu)
        val btnDelete = menu.findItem(R.id.menu_btn_delete)
        btnDelete.isVisible = true
        btnDelete.setOnMenuItemClickListener {
            mViewModel.myTrack.removeObservers(viewHolder.lifecycleOwner)
            mViewModel.deleteTrack()
            findNavController().navigateUp()
            true
        }
        val btnEdit = menu.findItem(R.id.menu_btn_edit)
        btnEdit.setOnMenuItemClickListener {
            view!!.startActionMode(actionModeCallback)
            true
        }
        menu.findItem(R.id.menu_btn_snapshot).apply {
            setOnMenuItemClickListener {
                viewHolder.mapboxMap.snapshot {
                    val file = File(App.imageCacheDir, "snp.png")
                    file.createNewFile()
                    val fos = FileOutputStream(file)
                    it.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    fos.close()
                    startActivity(Intent(context!!, ImageZoomActivity::class.java).apply {
                        putExtra("url", file.toURI().toString())
                    })
                }
                true
            }
        }
    }

    fun moveCamera() {
        LatLngBounds.Builder().includes(
            mViewModel.myTrack.value!!.points.map {
                LatLng(it.latitude, it.longitude)
            }
        ).build()?.let {
            viewHolder.mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(it, 50), 3000)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    fun generateFeatureCollection(points: List<MyPoint>): FeatureCollection {
        return FeatureCollection.fromFeature(Feature.fromGeometry(LineString.fromLngLats(points.map {
            Point.fromLngLat(it.longitude, it.latitude)
        })))
    }

    override fun createViewHolder(): ViewHolder = ViewHolder()
    override fun initData() {
        viewHolder.mBinding.viewModel = mViewModel
        mViewModel.loadTrack(args.trackId)
    }

    override fun initView(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        viewHolder.bottomSheet = BottomSheetBehavior.from(viewHolder.mBinding.bottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }
        initMap(savedInstanceState)
        actionModeCallback = ActionModeCallback()
        actionModeCallback.actionItemClickedCallback = { mode, item ->
            when (item?.itemId) {
                R.id.menu_btn_bulkadd -> {
                    bulkAdd()
                }
                R.id.menu_btn_change_name -> {
                    showSetNameDialog()
                }
            }
            true
        }
        viewHolder.mBinding.bsBtnDelete.setOnClickListener {
            mViewModel.deleteSelectedPoint()
        }
        mViewModel.selectedPoint.observe(viewHolder.lifecycleOwner, Observer {
            if (it != null) {
                viewHolder.bottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                viewHolder.bottomSheet.state = BottomSheetBehavior.STATE_HIDDEN
            }
        })
        viewHolder.bottomSheet.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) = Unit

            override fun onStateChanged(view: View, state: Int) {
                if (state == BottomSheetBehavior.STATE_COLLAPSED) {
                    mViewModel.setSelectedPoint(0)
                }
            }
        })
    }


    fun bulkAdd() {
        launch {
            val intent = Intent().apply {
                setType("image/*")
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                setAction(Intent.ACTION_GET_CONTENT)
            }
            val result = startActivityForResultKtx(Intent.createChooser(intent, "选择要导入的照片")).await()
            if (result.resultCode != RESULT_OK) {
                toast("选取失败")
                return@launch
            }
            val resultIntent = result.data!!
            val clipData = resultIntent.clipData
            val uris = mutableListOf<Uri>()
            clipData?.let {
                for (i in 0 until it.itemCount) {
                    uris.add(it.getItemAt(i).uri)
                }
            }
            resultIntent.data?.let {
                uris.add(it)
            }
            var failCount = 0
            withContext(Dispatchers.IO) {
                for (uri in uris) {
                    val fileDescriptor = context!!.contentResolver.openFileDescriptor(
                        uri,
                        "r"
                    )!!
                    val exifInterface = ExifInterface(fileDescriptor.fileDescriptor)
                    val latLong = FloatArray(2)
                    if (!exifInterface.getLatLong(latLong)) {
                        failCount++
                        continue
                    }

                    val bitmap = suspendCoroutine<Bitmap> { cont ->
                        GlideApp.with(context!!).asBitmap().load(uri)
                            .into(object : CustomTarget<Bitmap>() {
                                override fun onResourceReady(
                                    resource: Bitmap,
                                    transition: Transition<in Bitmap>?
                                ) {
                                    cont.resume(resource)
                                }

                                override fun onLoadCleared(placeholder: Drawable?) = Unit

                            })
                    }

                    val outCacheFile =
                        App.imageCacheDir.toPath().resolve("${CommonUtils.randomKey(24)}.png")
                            .toFile()
                        .apply { createNewFile() }
                    val fileOutputStream = FileOutputStream(outCacheFile)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                    bitmap.recycle()
                    fileOutputStream.close()
                    fileDescriptor.close()
                    val newPoint = MyPoint().apply {
                        timestamp = System.currentTimeMillis()
                        latitude = latLong[0].toDouble()
                        longitude = latLong[1].toDouble()
                        altitude = 0.0
                        pathPoint = false
                        imageUrls.add(outCacheFile.path)
                    }
                    mViewModel.persistNewPoint(newPoint)
                }
            }


        }
    }

    fun showSetNameDialog() {
        val builder = AlertDialog.Builder(context!!)
        val editText = EditText(context!!)
        editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
        builder.setView(editText)
        builder.setPositiveButton("确定") { _, _ ->
            val text = editText.text.toString()
            mViewModel.changeName(text)
        }
        builder.setNegativeButton("取消") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    object BindingAdapters {

        @JvmStatic
        @BindingAdapter("app:image_src")
        fun bindImageSrc(view: ImageView, url: String?) {
            GlideApp.with(view.context).load(url).into(view)
        }


    }
}
