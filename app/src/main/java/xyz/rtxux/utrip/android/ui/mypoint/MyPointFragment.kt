package xyz.rtxux.utrip.android.ui.mypoint

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import xyz.rtxux.utrip.android.R
import xyz.rtxux.utrip.android.adapter.MyPointListAdapter
import xyz.rtxux.utrip.android.base.BaseVMFragment2
import xyz.rtxux.utrip.android.databinding.MyPointFragmentBinding

class MyPointFragment :
    BaseVMFragment2<MyPointViewModel, MyPointFragmentBinding>(MyPointViewModel::class.java) {
    override fun getLayoutResId(): Int = R.layout.my_point_fragment
    private lateinit var adapter: MyPointListAdapter

    override fun initView(savedInstanceState: Bundle?) {
        val binding = mBinding!!
        mViewModel.loadPoints()
        adapter = MyPointListAdapter(findNavController())
        binding.rvMyPointList.layoutManager = LinearLayoutManager(context)
        binding.rvMyPointList.adapter = adapter
        mViewModel.points.observe(this, Observer {
            adapter.data = it
        })
    }

    override fun initData() {
        val binding = mBinding!!
        binding.viewModel = mViewModel
    }


}
