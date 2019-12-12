package xyz.rtxux.utrip.android.ui.point

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.comment_item.view.*
import xyz.rtxux.utrip.android.R
import xyz.rtxux.utrip.android.base.GlideApp
import xyz.rtxux.utrip.android.model.api.ApiService
import xyz.rtxux.utrip.android.model.api.RetrofitClient
import xyz.rtxux.utrip.android.model.bean.CommentVO
import xyz.rtxux.utrip.android.utils.toTimeStr

class CommentAdapter : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {

    var comments: List<CommentVO> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var deleteListener: ((Int) -> Unit)? = null

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val ivFace = view.iv_face
        val tvNickname = view.tv_nickname
        val tvMessage = view.tv_message
        val tvTime = view.tv_time
        val delete = view.layout_delete
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.comment_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return comments.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        GlideApp.with(holder.view.context)
            .load("${ApiService.API_BASE}/user/${comments[position].userId}/avatar")
            .into(holder.ivFace)
        holder.tvNickname.text = comments[position].userNickname
        holder.tvMessage.text = comments[position].content
        holder.tvTime.text = comments[position].timestamp.toTimeStr()
        holder.delete.setOnClickListener {
            deleteListener?.invoke(position)
        }
        holder.delete.visibility =
            if (RetrofitClient.userId == comments[position].userId) View.VISIBLE else View.GONE
    }
}