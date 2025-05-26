package com.example.moneychanger.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.moneychanger.R
import com.example.moneychanger.network.notice.NoticeResponseDto

class NoticeAdapter(
    private val items: List<NoticeResponseDto>
) : RecyclerView.Adapter<NoticeAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: LinearLayout = view.findViewById(R.id.notice_container)
        val date: TextView = view.findViewById(R.id.date)
        val title: TextView = view.findViewById(R.id.notice_title)
        val detailContainer: LinearLayout = view.findViewById(R.id.notice_detail_container)
        val content: TextView = view.findViewById(R.id.notice_detail_content)
        val arrow: ImageView = view.findViewById(R.id.notice_arrow) // 화살표 추가

        fun bind(item: NoticeResponseDto, position: Int) {
            date.text = item.date
            title.text = item.title
            content.text = item.content
            detailContainer.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
            arrow.rotation = if (item.isExpanded) 90F else 270F

            container.setOnClickListener {
                item.isExpanded = !item.isExpanded
                detailContainer.visibility = if (item.isExpanded) View.VISIBLE else View.GONE
                arrow.rotation = if (item.isExpanded) 90F else 270F
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_notice, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount() = items.size
}
