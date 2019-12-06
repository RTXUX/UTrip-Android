package xyz.rtxux.utrip.android.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

abstract class BaseVMFragment2<VM : ViewModel, VB : ViewDataBinding>(
    viewModelClass: Class<VM>
) : Fragment(), CoroutineScope by MainScope() {
    private val _viewModelClass = viewModelClass
    protected lateinit var mViewModel: VM
    protected var mBinding: VB? = null
    protected var mBindingWeakRef = WeakReference<VB>(null)
    protected val pendingActivityResult =
        ConcurrentHashMap<Int, CompletableDeferred<ActivityResult>>()
    private var _requestCode = 1000
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mViewModel = ViewModelProviders.of(this).get(_viewModelClass)
        mBinding = mBindingWeakRef.get()
//        return if (mBinding!=null) {
//            mBinding!!.root
//        } else {
//            val binding = DataBindingUtil.inflate<VB>(inflater, getLayoutResId(), container, false)
//            mBinding = binding
//            mBindingWeakRef = WeakReference(binding)
//            initData()
//            initView(savedInstanceState)
//            binding.root
//        }
        return mBinding?.root ?: run {
            val binding = DataBindingUtil.inflate<VB>(inflater, getLayoutResId(), container, false)
            mBinding = binding
            mBindingWeakRef = WeakReference(binding)
            initData()
            initView(savedInstanceState)
            binding.root
        }
    }

    abstract fun getLayoutResId(): Int

    abstract fun initView(savedInstanceState: Bundle?)

    abstract fun initData()

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    fun startActivityForResultKtx(
        intent: Intent,
        options: Bundle? = null
    ): CompletableDeferred<ActivityResult> {
        val result = CompletableDeferred<ActivityResult>()
        val requestCode = _requestCode++
        pendingActivityResult.put(requestCode, result)
        startActivityForResult(intent, requestCode, options)
        return result
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = pendingActivityResult.get(requestCode)
        if (result != null) {
            result.complete(ActivityResult(resultCode, data))
            pendingActivityResult.remove(requestCode)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    data class ActivityResult(val resultCode: Int, val data: Intent?)

}

