package xyz.rtxux.utrip.android.ui.track

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import xyz.rtxux.utrip.android.R

class TrackFragment : Fragment() {

    private lateinit var trackViewModel: TrackViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        trackViewModel =
            ViewModelProviders.of(this).get(TrackViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_track, container, false)
        root.findViewById<FloatingActionButton>(R.id.beginTrack).setOnClickListener {
            findNavController().navigate(R.id.action_navigation_track_to_trackingFragment)
        }
        return root
    }
}