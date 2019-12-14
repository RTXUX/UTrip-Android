package xyz.rtxux.utrip.android.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import xyz.rtxux.utrip.android.R
import xyz.rtxux.utrip.android.base.GlideApp
import xyz.rtxux.utrip.android.model.realm.MyTrack
import xyz.rtxux.utrip.android.ui.track.TrackFragmentDirections
import java.text.SimpleDateFormat
import java.util.*

class TrackListAdapter(
    data: OrderedRealmCollection<MyTrack>,
    val navController: NavController,
    val context: Context
) : RealmRecyclerViewAdapter<MyTrack, TrackListAdapter.TrackListViewHolder>(data, true) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackListViewHolder {
        return TrackListViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.track_list,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TrackListViewHolder, position: Int) {
        val track = getItem(position)!!
        holder.data = track
        holder.mTvName.text = track.name
        holder.mTvTime.text =
            SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Date(track.timestamp)).toString()
        GlideApp.with(context).load(track.headerImageUrl).centerCrop().into(holder.mIvMipmap)
            .clearOnDetach()
        holder.mCvWrapper.setOnClickListener {
            navController.navigate(TrackFragmentDirections.actionNavigationTrackToTrackDetailFragment(track.id))
        }
    }

    init {
        setHasStableIds(true)
    }

    class TrackListViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val mTvName: TextView = view.findViewById(R.id.tv_name)
        val mTvTime: TextView = view.findViewById(R.id.tv_time)
        var data: MyTrack? = null
        val mIvMipmap: ImageView = view.findViewById(R.id.iv_mipmap)
        val mCvWrapper: CardView = view.findViewById(R.id.cv_wrapper)
    }
}