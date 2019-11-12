package xyz.rtxux.utrip.android.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.amap.api.maps.SupportMapFragment
import xyz.rtxux.utrip.android.R

class ExploreFragment : Fragment() {

    private lateinit var exploreViewModel: ExploreViewModel
    private lateinit var mMapView: SupportMapFragment
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        exploreViewModel =
            ViewModelProviders.of(this).get(ExploreViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_explore, container, false)
        mMapView = childFragmentManager.findFragmentById(R.id.mainMap) as SupportMapFragment
        mMapView.onCreateView(inflater, container, savedInstanceState)
        return root
    }

    override fun onDestroyView() {
        mMapView.onDestroyView()
        mMapView.onDestroy()
        super.onDestroyView()
    }

    override fun onPause() {
        mMapView.onPause()
        super.onPause()
    }

    override fun onResume() {
        mMapView.onResume()
        super.onResume()
    }
}