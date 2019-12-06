package xyz.rtxux.utrip.android.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.rtxux.utrip.android.R
import xyz.rtxux.utrip.android.base.BaseVMFragment2
import xyz.rtxux.utrip.android.databinding.RegisterFragmentBinding
import xyz.rtxux.utrip.android.model.UResult
import xyz.rtxux.utrip.android.model.bean.RegisterDTO
import xyz.rtxux.utrip.android.model.repository.AuthRepository

class RegisterFragment : BaseVMFragment2<RegisterViewModel, RegisterFragmentBinding>(
    RegisterViewModel::class.java
) {
    override fun getLayoutResId(): Int = R.layout.register_fragment

    private val authRepository by lazy { AuthRepository() }

    override fun initView(savedInstanceState: Bundle?) {
        val binding = mBinding!!
        binding.button2.setOnClickListener {
            mViewModel.viewModelScope.launch {
                val response =
                    authRepository.register(RegisterDTO(binding.username!!, binding.password!!))
                withContext(Dispatchers.Main) {
                    when (response) {
                        is UResult.Success -> {
                            Toast.makeText(activity, "注册成功", Toast.LENGTH_SHORT).show()
                            findNavController().navigateUp()
                        }
                        is UResult.Error -> {
                            Toast.makeText(activity, response.exception.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }
    }

    override fun initData() {
        
    }


}
