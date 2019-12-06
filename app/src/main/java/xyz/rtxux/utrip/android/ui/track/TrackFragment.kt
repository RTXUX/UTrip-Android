package xyz.rtxux.utrip.android.ui.track

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import xyz.rtxux.utrip.android.R
import xyz.rtxux.utrip.android.adapter.TrackListAdapter
import xyz.rtxux.utrip.android.base.BaseVMFragment
import xyz.rtxux.utrip.android.databinding.FragmentTrackBinding

class TrackFragment :
    BaseVMFragment<TrackViewModel, FragmentTrackBinding>(true, TrackViewModel::class.java) {
    override fun getLayoutResId(): Int = R.layout.fragment_track


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ret = super.onCreateView(inflater, container, savedInstanceState)
        mViewModel.getTrackList()
        mBinding.beginTrack.setOnClickListener {
            findNavController().navigate(TrackFragmentDirections.actionNavigationTrackToTrackingFragment())
        }
        mBinding.rvTrackList.layoutManager = LinearLayoutManager(context)
        mBinding.rvTrackList.adapter = TrackListAdapter(mViewModel.trackList.value!!, findNavController())

        return ret
    }
}