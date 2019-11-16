package xyz.rtxux.utrip.android.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style
import xyz.rtxux.utrip.android.R

class ExploreFragment : Fragment() {

    private lateinit var mainMap: MapView

    private lateinit var exploreViewModel: ExploreViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        exploreViewModel =
            ViewModelProviders.of(this).get(ExploreViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_explore, container, false)
        mainMap = root.findViewById<MapView>(R.id.mainMap)
        mainMap.onCreate(savedInstanceState)
        mainMap.getMapAsync {
            it.setStyle(Style.MAPBOX_STREETS)
            it.uiSettings.isLogoEnabled = false
            it.uiSettings.isAttributionEnabled = false
        }
        root.findViewById<FloatingActionButton>(R.id.publishButton)
            .setOnClickListener { onFabClicked() }
        return root
    }

    fun onFabClicked() {
        findNavController().navigate(ExploreFragmentDirections.actionNavigationExploreToPublishPointFragment())
    }

    override fun onStart() {
        super.onStart()
        mainMap.onStart()
    }

    override fun onStop() {
        mainMap.onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        mainMap.onDestroy()
        super.onDestroyView()
    }

    override fun onPause() {
        mainMap.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mainMap.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mainMap.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mainMap.onLowMemory()
    }
}