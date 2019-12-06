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
import xyz.rtxux.utrip.android.base.BaseVMFragment2
import xyz.rtxux.utrip.android.databinding.LoginFragmentBinding
import xyz.rtxux.utrip.android.model.UResult
import xyz.rtxux.utrip.android.model.repository.AuthRepository
import xyz.rtxux.utrip.android.ui.main.MainActivity

class LoginFragment :
    BaseVMFragment2<LoginViewModel, LoginFragmentBinding>(LoginViewModel::class.java) {

    override fun getLayoutResId(): Int = R.layout.login_fragment

    private val authRepository by lazy { AuthRepository() }

    override fun initView(savedInstanceState: Bundle?) {
        val binding = mBinding!!
        binding.button.setOnClickListener {
            mViewModel.viewModelScope.launch {
                val response = authRepository.login(binding.username!!, binding.password!!)
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
        binding.registerButton.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
        }
    }

    override fun initData() {
        val binding = mBinding!!
        binding.viewModel = mViewModel
    }

}
