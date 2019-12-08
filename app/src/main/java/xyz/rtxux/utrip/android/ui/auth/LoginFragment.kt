package xyz.rtxux.utrip.android.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.rtxux.utrip.android.R
import xyz.rtxux.utrip.android.base.BaseCachingFragment
import xyz.rtxux.utrip.android.databinding.LoginFragmentBinding
import xyz.rtxux.utrip.android.model.UResult
import xyz.rtxux.utrip.android.model.repository.AuthRepository
import xyz.rtxux.utrip.android.ui.main.MainActivity

class LoginFragment :
    BaseCachingFragment<LoginViewModel, LoginFragmentBinding, LoginFragment.ViewHolder>(
        LoginViewModel::class.java
    ) {

    class ViewHolder : BaseCachingFragment.ViewHolder<LoginFragmentBinding>() {
        override fun clean() {

        }

    }

    override fun getLayoutResId(): Int = R.layout.login_fragment

    private val authRepository by lazy { AuthRepository() }

    override fun initView(savedInstanceState: Bundle?) {
        viewHolder.mBinding.button.setOnClickListener {
            mViewModel.viewModelScope.launch {
                val response = authRepository.login(
                    viewHolder.mBinding.username!!,
                    viewHolder.mBinding.password!!
                )
                when (response) {
                    is UResult.Success -> {
                        withContext(Dispatchers.Main) {
                            startActivity(Intent(activity, MainActivity::class.java))
                            activity?.finish()
                        }
                    }
                    is UResult.Error -> {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(activity, response.exception.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }
        viewHolder.mBinding.registerButton.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
        }
    }

    override fun initData() {
        viewHolder.mBinding.viewModel = mViewModel
    }

    override fun createViewHolder(): ViewHolder = ViewHolder()

}
