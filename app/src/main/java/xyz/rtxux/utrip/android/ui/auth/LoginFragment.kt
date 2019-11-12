package xyz.rtxux.utrip.android.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.rtxux.utrip.android.R
import xyz.rtxux.utrip.android.base.BaseVMFragment
import xyz.rtxux.utrip.android.databinding.LoginFragmentBinding
import xyz.rtxux.utrip.android.model.UResult
import xyz.rtxux.utrip.android.model.repository.AuthRepository
import xyz.rtxux.utrip.android.ui.main.MainActivity

class LoginFragment :
    BaseVMFragment<LoginViewModel, LoginFragmentBinding>(true, LoginViewModel::class.java) {

    override fun getLayoutResId(): Int = R.layout.login_fragment

    private val authRepository by lazy { AuthRepository() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ret = super.onCreateView(inflater, container, savedInstanceState)
        mBinding.viewModel = mViewModel
        mBinding.button.setOnClickListener {
            mViewModel.viewModelScope.launch {
                val response = authRepository.login(mBinding.username!!, mBinding.password!!)
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
        mBinding.registerButton.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
        }
        return ret
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

}
