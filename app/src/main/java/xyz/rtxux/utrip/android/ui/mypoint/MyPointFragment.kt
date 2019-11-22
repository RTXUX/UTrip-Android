package xyz.rtxux.utrip.android.ui.mypoint

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import xyz.rtxux.utrip.android.R
import xyz.rtxux.utrip.android.adapter.MyPointListAdapter
import xyz.rtxux.utrip.android.base.BaseVMFragment
import xyz.rtxux.utrip.android.databinding.MyPointFragmentBinding

class MyPointFragment :
    BaseVMFragment<MyPointViewModel, MyPointFragmentBinding>(true, MyPointViewModel::class.java) {
    override fun getLayoutResId(): Int = R.layout.my_point_fragment
    private lateinit var adapter: MyPointListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ret = super.onCreateView(inflater, container, savedInstanceState)
        mBinding.viewModel = mViewModel
        mViewModel.loadPoints()
        adapter = MyPointListAdapter(findNavController())
        mBinding.rvMyPointList.layoutManager = LinearLayoutManager(context)
        mBinding.rvMyPointList.adapter = adapter
        mViewModel.points.observe(this, Observer {
            adapter.data = it
        })
        return ret
    }


}
