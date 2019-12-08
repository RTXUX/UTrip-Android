package xyz.rtxux.utrip.android.ui.mypoint

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import xyz.rtxux.utrip.android.R
import xyz.rtxux.utrip.android.adapter.MyPointListAdapter
import xyz.rtxux.utrip.android.base.BaseCachingFragment
import xyz.rtxux.utrip.android.databinding.MyPointFragmentBinding

class MyPointFragment :
    BaseCachingFragment<MyPointViewModel, MyPointFragmentBinding, MyPointFragment.ViewHolder>(
        MyPointViewModel::class.java
    ) {

    class ViewHolder : BaseCachingFragment.ViewHolder<MyPointFragmentBinding>() {
        lateinit var adapter: MyPointListAdapter
        override fun clean() {

        }

    }
    
    override fun getLayoutResId(): Int = R.layout.my_point_fragment

    override fun initView(savedInstanceState: Bundle?) {
        val binding = viewHolder.mBinding
        mViewModel.loadPoints()
        viewHolder.adapter = MyPointListAdapter(findNavController())
        binding.rvMyPointList.layoutManager = LinearLayoutManager(context)
        binding.rvMyPointList.adapter = viewHolder.adapter
        mViewModel.points.observe(viewHolder, Observer {
            viewHolder.adapter.data = it
        })
    }

    override fun initData() {
        val binding = viewHolder.mBinding
        binding.viewModel = mViewModel
    }

    override fun createViewHolder(): ViewHolder = ViewHolder()


}
