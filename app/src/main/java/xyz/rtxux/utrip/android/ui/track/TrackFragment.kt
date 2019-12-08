package xyz.rtxux.utrip.android.ui.track

import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import xyz.rtxux.utrip.android.R
import xyz.rtxux.utrip.android.adapter.TrackListAdapter
import xyz.rtxux.utrip.android.base.BaseCachingFragment
import xyz.rtxux.utrip.android.databinding.FragmentTrackBinding

class TrackFragment :
    BaseCachingFragment<TrackViewModel, FragmentTrackBinding, TrackFragment.ViewHolder>(
        TrackViewModel::class.java
    ) {

    class ViewHolder : BaseCachingFragment.ViewHolder<FragmentTrackBinding>() {
        override fun clean() {

        }

    }

    override fun getLayoutResId(): Int = R.layout.fragment_track


    override fun createViewHolder(): ViewHolder = ViewHolder()

    override fun initData() {
        viewHolder.mBinding.viewModel = mViewModel
        mViewModel.getTrackList()
    }

    override fun initView(savedInstanceState: Bundle?) {
        viewHolder.mBinding.beginTrack.setOnClickListener {
            findNavController().navigate(TrackFragmentDirections.actionNavigationTrackToTrackingFragment())
        }
        viewHolder.mBinding.rvTrackList.layoutManager = LinearLayoutManager(context)
        viewHolder.mBinding.rvTrackList.adapter =
            TrackListAdapter(mViewModel.trackList.value!!, findNavController())
    }
}