package xyz.rtxux.utrip.android.ui.auth

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
import xyz.rtxux.utrip.android.databinding.RegisterFragmentBinding
import xyz.rtxux.utrip.android.model.UResult
import xyz.rtxux.utrip.android.model.bean.RegisterDTO
import xyz.rtxux.utrip.android.model.repository.AuthRepository

class RegisterFragment : BaseVMFragment<RegisterViewModel, RegisterFragmentBinding>(
    true,
    RegisterViewModel::class.java
) {
    override fun getLayoutResId(): Int = R.layout.register_fragment

    private val authRepository by lazy { AuthRepository() }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ret = super.onCreateView(inflater, container, savedInstanceState)
        mBinding.button2.setOnClickListener {
            mViewModel.viewModelScope.launch {
                val response =
                    authRepository.register(RegisterDTO(mBinding.username!!, mBinding.password!!))
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
        return ret
    }


}
