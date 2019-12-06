package xyz.rtxux.utrip.android.ui.profileedit

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.InputType
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.textfield.TextInputEditText

import xyz.rtxux.utrip.android.R
import xyz.rtxux.utrip.android.base.BaseVMFragment
import xyz.rtxux.utrip.android.databinding.ProfileEditFragmentBinding

class ProfileEditFragment : BaseVMFragment<ProfileEditViewModel, ProfileEditFragmentBinding>(true, ProfileEditViewModel::class.java) {

    val inputs = mutableListOf<TextInputEditText>()

    private lateinit var editMenuItem: MenuItem
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ret = super.onCreateView(inflater, container, savedInstanceState)
        mBinding.viewModel = mViewModel
        inputs.apply {
            add(mBinding.editNickname)
            add(mBinding.editPhone)
            add(mBinding.editQq)
        }
        setHasOptionsMenu(true)
        mViewModel.editable.observe(this, Observer {
            for (input in inputs) {
                input.inputType = if (it) InputType.TYPE_CLASS_TEXT else InputType.TYPE_NULL
            }
        })
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

}
