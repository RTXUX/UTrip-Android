package xyz.rtxux.utrip.android.ui.profileedit

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.RadioGroup
import androidx.core.view.children
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputEditText
import xyz.rtxux.utrip.android.R
import xyz.rtxux.utrip.android.base.BaseCachingFragment
import xyz.rtxux.utrip.android.base.GlideApp
import xyz.rtxux.utrip.android.databinding.ProfileEditFragmentBinding
import xyz.rtxux.utrip.android.model.api.ApiService
import xyz.rtxux.utrip.android.model.api.RetrofitClient
import xyz.rtxux.utrip.android.ui.zoomview.ImageZoomActivity

class ProfileEditFragment :
    BaseCachingFragment<ProfileEditViewModel, ProfileEditFragmentBinding, ProfileEditFragment.ViewHolder>(
        ProfileEditViewModel::class.java
    ) {

    val navArg by navArgs<ProfileEditFragmentArgs>()

    class ViewHolder : BaseCachingFragment.ViewHolder<ProfileEditFragmentBinding>() {
        override fun clean() {
        }

    }



    private lateinit var editMenuItem: MenuItem

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.profile_edit_menu, menu)
        editMenuItem = menu.findItem(R.id.editToggle)
        editMenuItem.setOnMenuItemClickListener {
            if (mViewModel.editable.value == false) {
                mViewModel.editable.postValue(true)
                editMenuItem.icon = resources.getDrawable(R.drawable.ic_publish_white_24dp, context!!.theme)
            } else {
                mViewModel.editable.postValue(false)
                editMenuItem.icon =
                    resources.getDrawable(R.drawable.ic_edit_while_24dp, context!!.theme)
                updateProfile()
            }
            true
        }
//        mViewModel.editable.observe(viewHolder, Observer {
//            editMenuItem.icon = resources.getDrawable(if (it) R.drawable.ic_publish_white_24dp else R.drawable.ic_edit_while_24dp, context!!.theme)
//        })
        mViewModel.userProfileVO.observe(viewHolder.lifecycleOwner, Observer {
            editMenuItem.isVisible = it.userId == RetrofitClient.userId
        })
    }

    override fun getLayoutResId(): Int = R.layout.profile_edit_fragment
    override fun createViewHolder(): ViewHolder = ViewHolder()

    override fun initData() {
        viewHolder.mBinding.viewModel = mViewModel
        mViewModel.loadUserProfileVO(navArg.userId)
    }

    override fun initView(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        mViewModel.userProfileVO.observe(viewHolder.lifecycleOwner, Observer {
            GlideApp.with(context!!).load("${ApiService.API_BASE}/user/${it.userId}/avatar")
                .into(viewHolder.mBinding.ivFace)
        })
        mViewModel.gender.observe(viewHolder.lifecycleOwner, Observer {
            mViewModel.userProfileVO.postValue(mViewModel.userProfileVO.value?.apply {
                gender = it
            })
        })
        viewHolder.mBinding.ivFace.setOnClickListener {
            startActivity(Intent(context!!, ImageZoomActivity::class.java).apply {
                putExtra(
                    "url",
                    "${ApiService.API_BASE}/user/${mViewModel.userProfileVO.value!!.userId}/avatar"
                )
            })
        }
    }

    private fun updateProfile() {
        val binding = viewHolder.mBinding
//        val userProfile = mViewModel.userProfileVO.value!!.copy(
//            nickname = binding.tvNickname.text.toString(),
//            phone = binding.tvPhone.text.toString(),
//            email = binding.tvEmail.text.toString()
//        )
        mViewModel.updateUserProfile(mViewModel.userProfileVO.value!!)
    }


    object ProfileEditFragmentBindingAdapter {
        val genders = listOf(
            Pair("男", R.id.rb_male),
            Pair("女", R.id.rb_female)
        )

        @JvmStatic
        @BindingAdapter("app:editable")
        fun bindEditable(view: TextInputEditText, editable: Boolean) {
            if (editable) {
                view.inputType = 131073
//                view.isFocusable = true
            } else {
                view.inputType = InputType.TYPE_NULL
//                view.isFocusable = false
                view.setTextIsSelectable(true)
            }
        }

        @JvmStatic
        @BindingAdapter("app:editable")
        fun bindEditable(view: RadioGroup, editable: Boolean) {
            view.children.forEach {
                it.isClickable = editable
            }
        }

        @JvmStatic
        @BindingAdapter("app:checkedButton")
        fun setChecked(view: RadioGroup, gender: String?) {
            if (gender == null || gender == "") {
                view.clearCheck()
                return
            }
            val newId = genders.find { it.first == gender }?.second
            if (newId == null) {
                view.clearCheck()
                return
            }
            if (newId == view.checkedRadioButtonId) return
            view.check(newId)
        }

        @JvmStatic
        @InverseBindingAdapter(attribute = "app:checkedButton")
        fun getChecked(view: RadioGroup): String {
            return genders.find { it.second == view.checkedRadioButtonId }?.first ?: ""
        }

        @JvmStatic
        @BindingAdapter("app:checkedButtonAttrChanged")
        fun setCheckButtonAttrChanged(view: RadioGroup, attrChange: InverseBindingListener) {
            view.setOnCheckedChangeListener { _, _ ->
                attrChange.onChange()
            }
        }

    }
}
