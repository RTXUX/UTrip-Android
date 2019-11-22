package xyz.rtxux.utrip.android.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import xyz.rtxux.utrip.android.R
import xyz.rtxux.utrip.android.base.GlideApp
import xyz.rtxux.utrip.android.model.api.ApiService
import xyz.rtxux.utrip.android.ui.mypoint.MyPointFragmentDirections
import xyz.rtxux.utrip.server.model.vo.PointVO
import java.text.SimpleDateFormat
import java.util.*

class MyPointListAdapter(
    val navController: NavController
) : RecyclerView.Adapter<MyPointListAdapter.ViewHolder>() {

    var data: List<PointVO> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.my_point_list, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val point = data[position]
        holder.tvName.text = point.name
        holder.tvTime.text =
            SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(Date(point.timestamp)).toString()
        holder.tvLocation.text = "(${point.location.latitude} ${point.location.longitude})"
        if (!point.images.isEmpty()) {
            GlideApp.with(holder.view.context)
                .load("${ApiService.API_BASE}/image/${point.images[0]}").into(holder.ivPreview)
        }
        holder.layout.setOnClickListener {
            navController.navigate(
                MyPointFragmentDirections.actionMyPointFragmentToPointInfoFragment(
                    point.pointId
                )
            )
        }
    }


    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val ivPreview = view.findViewById<ImageView>(R.id.iv_preview)
        val tvName = view.findViewById<TextView>(R.id.tv_name)
        val tvTime = view.findViewById<TextView>(R.id.tv_time)
        val layout = view.findViewById<ConstraintLayout>(R.id.layout_point_entry)
        val tvLocation = view.findViewById<TextView>(R.id.tv_location)
    }
}