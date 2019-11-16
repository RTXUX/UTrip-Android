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
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.layout_dialog_imgtype.view.*
import xyz.rtxux.utrip.android.R
import xyz.rtxux.utrip.android.base.BaseVMFragment
import xyz.rtxux.utrip.android.databinding.PublishPointFragmentBinding
import xyz.rtxux.utrip.android.utils.CommonUtils

class PublishPointFragment : BaseVMFragment<PublishPointViewModel, PublishPointFragmentBinding>(
    true,
    PublishPointViewModel::class.java
) {
    override fun getLayoutResId(): Int = R.layout.publish_point_fragment
    private val imageList = mutableListOf<Bitmap>()
    private lateinit var viewModel: PublishPointViewModel
    private lateinit var mapboxMap: MapboxMap
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ret = super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProviders.of(this).get(PublishPointViewModel::class.java)
        mBinding.viewModel = viewModel
        initMap(savedInstanceState)
        initView()
        return ret
    }

    fun initMap(savedInstanceState: Bundle?) {
        mBinding.pickMap.onCreate(savedInstanceState)
        mBinding.pickMap.getMapAsync {
            it.uiSettings.isAttributionEnabled = false
            it.uiSettings.isLogoEnabled = false
            mapboxMap = it
            it.setStyle(Style.MAPBOX_STREETS) {
                val locationComponent = mapboxMap.locationComponent
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

    fun initView() {
        mBinding.layoutAddimg2.setOnClickListener {
            mBinding.layoutAddimg.performClick()
        }
        mBinding.layoutAddimg.setOnClickListener {
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
        imageList.add(bitmap)
        val refreshSv = {
            if (imageList.size >= 5) {
                mBinding.layoutAddimg2.visibility = View.GONE
            }
            if (imageList.size < 5) {
                mBinding.layoutAddimg2.visibility = View.VISIBLE
            }
            if (imageList.isEmpty()) {
                mBinding.svImg.visibility = View.GONE
                mBinding.layoutAddimg.visibility = View.VISIBLE
            } else {
                mBinding.svImg.visibility = View.VISIBLE
                mBinding.layoutAddimg.visibility = View.GONE
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
                mBinding.layoutImg.removeView(this)
                imageList.remove(bitmap)
                refreshSv()
                true
            }
        }
        mBinding.layoutImg.addView(
            imageView,
            LinearLayout.LayoutParams(
                CommonUtils.dp2px(63f),
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        )
        refreshSv()
    }

    fun pickPhoto() {
        startActivityForResult(
            Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            ), 1
        )
    }

    override fun onStart() {
        super.onStart()
        mBinding.pickMap.onStart()
    }

    override fun onStop() {
        mBinding.pickMap.onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        mBinding.pickMap.onDestroy()
        super.onDestroyView()
    }

    override fun onPause() {
        mBinding.pickMap.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mBinding.pickMap.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mBinding.pickMap.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mBinding.pickMap.onLowMemory()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.publish_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.buttonConfirmPublish -> {
                this.findNavController().navigateUp()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            1 -> {
                if (resultCode == RESULT_OK && data != null) {
                    val selectedImage = data.data
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
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }
}
