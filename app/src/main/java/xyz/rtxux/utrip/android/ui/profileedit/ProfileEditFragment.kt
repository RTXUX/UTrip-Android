package xyz.rtxux.utrip.android.ui.profileedit

import android.os.Bundle
import android.text.InputType
import android.view.*
import androidx.lifecycle.Observer
import com.google.android.material.textfield.TextInputEditText
import xyz.rtxux.utrip.android.R
import xyz.rtxux.utrip.android.base.BaseCachingFragment
import xyz.rtxux.utrip.android.databinding.ProfileEditFragmentBinding

class ProfileEditFragment :
    BaseCachingFragment<ProfileEditViewModel, ProfileEditFragmentBinding, ProfileEditFragment.ViewHolder>(
        ProfileEditViewModel::class.java
    ) {

    class ViewHolder : BaseCachingFragment.ViewHolder<ProfileEditFragmentBinding>() {
        val inputs = mutableListOf<TextInputEditText>()
        override fun clean() {
        }

    }



    private lateinit var editMenuItem: MenuItem
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ret = super.onCreateView(inflater, container, savedInstanceState)


        return ret
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.profile_edit_menu, menu)
        editMenuItem = menu.findItem(R.id.editToggle)
        editMenuItem.setOnMenuItemClickListener {
            if (mViewModel.editable.value == false) {
                mViewModel.editable.postValue(true)
                editMenuItem.icon = resources.getDrawable(R.drawable.ic_publish_white_24dp, context!!.theme)
            }
            true
        }
    }

    override fun getLayoutResId(): Int = R.layout.profile_edit_fragment
    override fun createViewHolder(): ViewHolder = ViewHolder()

    override fun initData() {
        viewHolder.mBinding.viewModel = mViewModel
    }

    override fun initView(savedInstanceState: Bundle?) {
        viewHolder.inputs.apply {
            add(viewHolder.mBinding.editNickname)
            add(viewHolder.mBinding.editPhone)
            add(viewHolder.mBinding.editQq)
        }
        setHasOptionsMenu(true)
        mViewModel.editable.observe(viewHolder, Observer {
            for (input in viewHolder.inputs) {
                input.inputType = if (it) InputType.TYPE_CLASS_TEXT else InputType.TYPE_NULL
            }
        })
    }

}
