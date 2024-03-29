package xyz.rtxux.utrip.android.ui.publish

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.layout_dialog_imgtype.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.rtxux.utrip.android.R
import xyz.rtxux.utrip.android.base.BaseCachingFragment
import xyz.rtxux.utrip.android.base.MapViewLifeCycleBean
import xyz.rtxux.utrip.android.databinding.PublishPointFragmentBinding
import xyz.rtxux.utrip.android.model.UResult
import xyz.rtxux.utrip.android.model.bean.LocationBean
import xyz.rtxux.utrip.android.model.bean.PointDTO
import xyz.rtxux.utrip.android.model.repository.ImageRepository
import xyz.rtxux.utrip.android.model.repository.PointRepository
import xyz.rtxux.utrip.android.utils.CommonUtils
import xyz.rtxux.utrip.android.utils.toast

class PublishPointFragment :
    BaseCachingFragment<PublishPointViewModel, PublishPointFragmentBinding, PublishPointFragment.ViewHolder>(
    PublishPointViewModel::class.java
) {

    class ViewHolder : BaseCachingFragment.ViewHolder<PublishPointFragmentBinding>() {
        val imageList = mutableListOf<Bitmap>()
        lateinit var mapboxMap: MapboxMap

        override fun clean() {

        }

    }

    private val imageRepository by lazy { ImageRepository() }
    private val pointRepository by lazy { PointRepository() }
    override fun getLayoutResId(): Int = R.layout.publish_point_fragment



    fun initMap(savedInstanceState: Bundle?) {
        val binding = viewHolder.mBinding
        binding.pickMap.onCreate(savedInstanceState)
        viewHolder.lifecycleOwner.lifecycle.addObserver(MapViewLifeCycleBean(binding.pickMap))
        binding.pickMap.getMapAsync {
            it.uiSettings.isAttributionEnabled = false
            it.uiSettings.isLogoEnabled = false
            viewHolder.mapboxMap = it
            it.setStyle(Style.MAPBOX_STREETS) {
                val locationComponent = viewHolder.mapboxMap.locationComponent
                locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(
                        context!!,
                        it
                    ).build()
                )
                locationComponent.isLocationComponentEnabled = true
                locationComponent.cameraMode = CameraMode.TRACKING
                locationComponent.renderMode = RenderMode.NORMAL
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        initMap(savedInstanceState)
        val binding = viewHolder.mBinding
        binding.layoutAddimg2.setOnClickListener {
            binding.layoutAddimg.performClick()
        }
        binding.layoutAddimg.setOnClickListener {
            val dialog = Dialog(context!!)
            val dialogView = layoutInflater.inflate(R.layout.layout_dialog_imgtype, null)
            dialog.setContentView(dialogView)
            dialogView.tv_alarm.setOnClickListener {
                pickPhoto()
                dialog.dismiss()
            }
            dialogView.tv_close.setOnClickListener {
                dialog.dismiss()
            }
            val dialogWindow = dialog.window!!
            dialogWindow.setGravity(Gravity.BOTTOM)
            val lp = dialogWindow.attributes
            lp.width = LinearLayout.LayoutParams.MATCH_PARENT
            dialogWindow.attributes = lp
            dialog.show()

        }
    }

    fun addImg(bitmap: Bitmap) {
        val binding = viewHolder.mBinding
        val imageList = viewHolder.imageList
        imageList.add(bitmap)
        val refreshSv = {
            if (imageList.size >= 5) {
                binding.layoutAddimg2.visibility = View.GONE
            }
            if (imageList.size < 5) {
                binding.layoutAddimg2.visibility = View.VISIBLE
            }
            if (imageList.isEmpty()) {
                binding.svImg.visibility = View.GONE
                binding.layoutAddimg.visibility = View.VISIBLE
            } else {
                binding.svImg.visibility = View.VISIBLE
                binding.layoutAddimg.visibility = View.GONE
            }
        }
        val imageView = ImageView(context!!).apply {
            setImageBitmap(bitmap)
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
            setOnLongClickListener {
                binding.layoutImg.removeView(this)
                imageList.remove(bitmap)
                refreshSv()
                true
            }
        }
        binding.layoutImg.addView(
            imageView,
            LinearLayout.LayoutParams(
                CommonUtils.dp2px(63f),
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        )
        refreshSv()
    }

    fun pickPhoto() {
//        startActivityForResult(
//            Intent(
//                Intent.ACTION_PICK,
//                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//            ), 1
//        )
        launch {
            val result = startActivityForResultKtx(
                Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
            ).await()
            if (result.resultCode == RESULT_OK && result.data != null) {
                val selectedImage = result.data.data
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                selectedImage?.let {
                    val cursor = activity!!.contentResolver.query(
                        selectedImage,
                        filePathColumn,
                        null,
                        null,
                        null
                    )
                    if (cursor != null) {
                        cursor.moveToFirst()
                        val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                        val picturePath = cursor.getString(columnIndex)
                        cursor.close()
                        val opts = BitmapFactory.Options()
                        opts.inJustDecodeBounds = true
                        BitmapFactory.decodeFile(picturePath, opts)
                        //debug(msg = "" + opts.outWidth + " " + opts.outHeight)
                        val less = if (opts.outWidth > opts.outHeight)
                            opts.outHeight
                        else
                            opts.outWidth
                        var i = 1
                        if (less > 720) {
                            i = 2
                            while (less / i > 720) {
                                i += 2
                            }
                        }
                        opts.inSampleSize = i
                        opts.inJustDecodeBounds = false
                        val bitmap = BitmapFactory.decodeFile(picturePath, opts)
                        // debug(msg = "" + opts.outWidth + " " + opts.outHeight)
                        addImg(bitmap)

                    }
                }
            }
        }
    }

//    override fun onStart() {
//        super.onStart()
//        mBinding.pickMap.onStart()
//    }
//
//    override fun onStop() {
//        mBinding.pickMap.onStop()
//        super.onStop()
//    }
//
//    override fun onDestroyView() {
//        mBinding.pickMap.onDestroy()
//        super.onDestroyView()
//    }
//
//    override fun onPause() {
//        mBinding.pickMap.onPause()
//        super.onPause()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        mBinding.pickMap.onResume()
//    }
//
//    override fun onSaveInstanceState(outState: Bundle) {
//        mBinding.pickMap.onSaveInstanceState(outState)
//        super.onSaveInstanceState(outState)
//    }
//
//    override fun onLowMemory() {
//        super.onLowMemory()
//        mBinding.pickMap.onLowMemory()
//    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.publish_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val binding = viewHolder.mBinding
        when (item.itemId) {
            R.id.buttonConfirmPublish -> {
                mViewModel.viewModelScope.launch {
                    val imageIds = mutableListOf<Int>()
                    withContext((Dispatchers.IO)) {
                        for (image in viewHolder.imageList) {
                            launch {
                                val imagePreUploadVO =
                                    (imageRepository.preUploadImage() as UResult.Success).data
                                imageRepository.uploadImage(image, imagePreUploadVO)
                                imageIds.add(imagePreUploadVO.id)
                            }
                        }
                    }
                    val target = viewHolder.mapboxMap.cameraPosition.target
                    val pointDTO = PointDTO(
                        name = binding.name!!,
                        description = binding.content!!,
                        images = imageIds,
                        associatedTrack = null,
                        location = LocationBean("WGS-84", target.latitude, target.longitude)
                    )
                    val res = pointRepository.createPoint(pointDTO)
                    when (res) {
                        is UResult.Success -> {
                            toast("发布成功")
                            withContext(Dispatchers.Main) {
                                this@PublishPointFragment.findNavController().navigateUp()
                            }
                        }
                        is UResult.Error -> {
                            toast(res.exception.message!!)
                        }
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun initData() {
        viewHolder.mBinding.viewModel = mViewModel
    }

    override fun createViewHolder(): ViewHolder = ViewHolder()

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        when (requestCode) {
//            1 -> {
//                if (resultCode == RESULT_OK && data != null) {
//                    val selectedImage = data.data
//                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
//                    selectedImage?.let {
//                        val cursor = activity!!.contentResolver.query(
//                            selectedImage,
//                            filePathColumn,
//                            null,
//                            null,
//                            null
//                        )
//                        if (cursor != null) {
//                            cursor.moveToFirst()
//                            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
//                            val picturePath = cursor.getString(columnIndex)
//                            cursor.close()
//                            val opts = BitmapFactory.Options()
//                            opts.inJustDecodeBounds = true
//                            BitmapFactory.decodeFile(picturePath, opts)
//                            //debug(msg = "" + opts.outWidth + " " + opts.outHeight)
//                            val less = if (opts.outWidth > opts.outHeight)
//                                opts.outHeight
//                            else
//                                opts.outWidth
//                            var i = 1
//                            if (less > 720) {
//                                i = 2
//                                while (less / i > 720) {
//                                    i += 2
//                                }
//                            }
//                            opts.inSampleSize = i
//                            opts.inJustDecodeBounds = false
//                            val bitmap = BitmapFactory.decodeFile(picturePath, opts)
//                            // debug(msg = "" + opts.outWidth + " " + opts.outHeight)
//                            addImg(bitmap)
//
//                        }
//                    }
//                }
//            }
//            else -> {
//                super.onActivityResult(requestCode, resultCode, data)
//            }
//        }
//    }
}
