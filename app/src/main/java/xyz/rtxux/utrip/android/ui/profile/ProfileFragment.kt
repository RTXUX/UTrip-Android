package xyz.rtxux.utrip.android.ui.profile

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.widget.LinearLayout
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.layout_dialog_imgtype.view.*
import kotlinx.coroutines.launch
import xyz.rtxux.utrip.android.R
import xyz.rtxux.utrip.android.base.BaseCachingFragment
import xyz.rtxux.utrip.android.base.GlideApp
import xyz.rtxux.utrip.android.databinding.FragmentProfileBinding
import xyz.rtxux.utrip.android.model.api.RetrofitClient
import xyz.rtxux.utrip.android.model.repository.AuthRepository
import xyz.rtxux.utrip.android.ui.auth.AuthActivity
import xyz.rtxux.utrip.android.ui.zoomview.ImageZoomActivity

class ProfileFragment :
    BaseCachingFragment<ProfileViewModel, FragmentProfileBinding, ProfileFragment.ViewHolder>(
        ProfileViewModel::class.java
    ) {

    class ViewHolder : BaseCachingFragment.ViewHolder<FragmentProfileBinding>() {
        override fun clean() {

        }

    }

    override fun getLayoutResId(): Int = R.layout.fragment_profile
    private val authRepository by lazy { AuthRepository() }


    fun initAvatarDialog() {
        viewHolder.mBinding.ivAvatar.setOnLongClickListener {
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
            true
        }
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
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
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
                        mViewModel.uploadAvatar(bitmap)
                    }
                }
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        val binding = viewHolder.mBinding
        binding.buttonLogout.setOnClickListener {
            launch { authRepository.logout() }
//            try {
//                Runtime.getRuntime().exec("pm clear ${App.CONTEXT.packageName} HERE")
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
            startActivity(Intent(context, AuthActivity::class.java))
            activity?.finish()
        }
        mViewModel.avatarUrl.observe(viewHolder.lifecycleOwner, Observer {
            GlideApp.with(context!!).load(it).into(binding.ivAvatar)
            binding.ivAvatar.setOnClickListener { view ->
                startActivity(Intent(context, ImageZoomActivity::class.java).apply {
                    putExtra("url", it)
                })
            }
        })
        binding.tvUsername.setOnClickListener {
            findNavController().navigate(
                ProfileFragmentDirections.actionNavigationProfileToProfileEditFragment(
                    RetrofitClient.userId
                )
            )
        }
        binding.layoutMyPoints.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.actionNavigationProfileToMyPointFragment())
        }
        initAvatarDialog()
    }

    override fun initData() {
        viewHolder.mBinding.viewModel = mViewModel
        mViewModel.loadUserProfileVO()
        mViewModel.loadAvatar()
    }

    override fun createViewHolder(): ViewHolder = ViewHolder()
}