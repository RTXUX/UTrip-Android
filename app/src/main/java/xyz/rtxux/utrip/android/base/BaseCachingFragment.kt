package xyz.rtxux.utrip.android.base

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.Closeable
import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class BaseCachingFragment<VM : ViewModel, VB : ViewDataBinding, VH : BaseCachingFragment.ViewHolder<VB>>(
    private val _VMClass: Class<VM>
) : Fragment(), CoroutineScope by MainScope() {
    protected val pendingActivityResult =
        ConcurrentHashMap<Int, CompletableDeferred<ActivityResult>>()
    private var _requestCode = 1000
    protected lateinit var mViewModel: VM

    abstract class ViewHolder<VB : ViewDataBinding> : Closeable, LifecycleOwner {
        lateinit var mBinding: VB
        private var closed = false
        val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

        init {
            lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
        }

        protected fun finalize() {
            //runBlocking {  MainScope().launch { lifecycleRegistry.currentState = Lifecycle.State.DESTROYED }.join() }
            close()
        }

        protected abstract fun clean()
        override fun close() {
            synchronized(this) {
                try {
                    if (!closed) {
                        if (Looper.getMainLooper().isCurrentThread) {
                            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
                        } else runBlocking {
                            MainScope().launch {
                                lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
                            }.join()
                        }
                        clean()
                    }
                } finally {
                    closed = true
                }
            }
        }

        override fun getLifecycle(): Lifecycle {
            return lifecycleRegistry
        }
    }

    companion object {
        val referenceQueue = ReferenceQueue<Any>()

        init {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    while (true) {
                        val ref = referenceQueue.remove(1000)
                        if (!isActive) throw InterruptedException()
                        if (ref != null) Timber.d("Object Collected")
                    }
                } catch (e: InterruptedException) {
                    Timber.d("Monitor Canceled")
                    return@launch
                }
            }
        }
    }

    class ViewLifecycleObserver(val lifecycleRegistry: LifecycleRegistry) : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onStart() {
            lifecycleRegistry.currentState = Lifecycle.State.STARTED
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun onResume() {
            lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun onPause() {
            lifecycleRegistry.currentState = Lifecycle.State.STARTED
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onStop() {
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
        }

        fun finalize() {
            Timber.d("Finalized")
        }
    }

    protected abstract fun createViewHolder(): VH

    protected var viewHolder by autoCleared<VH>()

    protected var ref: WeakReference<VH> = WeakReference<VH>(null)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val cRef = ref.get()
        if (cRef != null) {
            Timber.d("Reusing previously created view.")
            viewHolder = cRef
            viewLifecycleOwner.lifecycle.addObserver(ViewLifecycleObserver(viewHolder.lifecycleRegistry))
            //viewHolder.mBinding.lifecycleOwner = viewHolder
            return viewHolder.mBinding.root
        } else {
            mViewModel = ViewModelProviders.of(this).get(_VMClass)
            viewHolder = createViewHolder()
            viewHolder.mBinding =
                DataBindingUtil.inflate(inflater, getLayoutResId(), container, false)
            ref = WeakReference(viewHolder, referenceQueue)
            initData()
            initView(savedInstanceState)
            viewHolder.lifecycleRegistry.currentState = Lifecycle.State.CREATED
            viewLifecycleOwner.lifecycle.addObserver(ViewLifecycleObserver(viewHolder.lifecycleRegistry))
            viewHolder.mBinding.lifecycleOwner = viewHolder
            return viewHolder.mBinding.root
        }
    }

    abstract fun getLayoutResId(): Int

    abstract fun initData()

    abstract fun initView(savedInstanceState: Bundle?)

    override fun onDestroy() {
        cancel()
        ref.get()?.close()
        //ref.clear()
        super.onDestroy()
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

class AutoClearedValue<T : Any>(fragment: Fragment) : ReadWriteProperty<Fragment, T> {
    private var _value: T? = null
    private val observer: LifecycleObserver = object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            Timber.d("Clearing view reference")
            _value = null
        }
    }

    init {
        fragment.viewLifecycleOwnerLiveData.observe(fragment, Observer {
            it.lifecycle.addObserver(observer)
        })
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        return _value ?: throw IllegalStateException(
            "should never call auto-cleared-value get when it might not be available"
        )
    }

    override fun setValue(thisRef: Fragment, property: KProperty<*>, value: T) {
        _value = value
    }
}

fun <T : Any> Fragment.autoCleared() = AutoClearedValue<T>(this)