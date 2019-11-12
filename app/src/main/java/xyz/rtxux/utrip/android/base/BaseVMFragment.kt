package xyz.rtxux.utrip.android.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders

abstract class BaseVMFragment<VM : ViewModel, VB : ViewDataBinding>(
    useBinding: Boolean = false,
    viewModelClass: Class<VM>
) : Fragment() {
    private val _useBinding = useBinding
    private val _viewModelClass = viewModelClass
    protected lateinit var mViewModel: VM
    protected lateinit var mBinding: VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mViewModel = ViewModelProviders.of(this).get(_viewModelClass)
        if (_useBinding) {
            mBinding = DataBindingUtil.inflate(inflater, getLayoutResId(), container, false)
            mBinding.lifecycleOwner = this
            return mBinding.root
        } else {
            return inflater.inflate(getLayoutResId(), container, false)
        }
    }

    abstract fun getLayoutResId(): Int
}