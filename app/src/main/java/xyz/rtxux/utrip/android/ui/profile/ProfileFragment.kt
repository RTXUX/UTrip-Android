package xyz.rtxux.utrip.android.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import xyz.rtxux.utrip.android.R
import xyz.rtxux.utrip.android.base.BaseVMFragment
import xyz.rtxux.utrip.android.base.GlideApp
import xyz.rtxux.utrip.android.databinding.FragmentProfileBinding
import xyz.rtxux.utrip.android.model.repository.AuthRepository
import xyz.rtxux.utrip.android.ui.auth.AuthActivity

class ProfileFragment :
    BaseVMFragment<ProfileViewModel, FragmentProfileBinding>(true, ProfileViewModel::class.java) {
    override fun getLayoutResId(): Int = R.layout.fragment_profile
    private val authRepository by lazy { AuthRepository() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ret = super.onCreateView(inflater, container, savedInstanceState)
        mBinding.viewModel = mViewModel
        mViewModel.loadUserProfileVO()
        mBinding.buttonLogout.setOnClickListener {
            launch { authRepository.logout() }
            startActivity(Intent(context, AuthActivity::class.java))
            activity?.finish()
        }
        mViewModel.userProfileVO.observe(this, Observer {
            GlideApp.with(context!!).load(it.avatarUrl).into(mBinding.ivAvatar)
        })
        mBinding.layoutMyPoints.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.actionNavigationProfileToMyPointFragment())
        }
        return ret
    }
}