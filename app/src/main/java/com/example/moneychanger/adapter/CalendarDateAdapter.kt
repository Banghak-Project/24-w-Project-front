package com.example.moneychanger.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.moneychanger.R
import com.example.moneychanger.databinding.CalendarDateTileBinding
import java.time.LocalDate
import java.time.YearMonth

class CalendarDateAdapter(
    private val dateList: List<LocalDate?>,
    private val recordCountMap: Map<LocalDate, Int>, // ← 이게 핵심
    private val selectedDate: LocalDate?,
    private val onDateClick: (LocalDate) -> Unit
) : RecyclerView.Adapter<CalendarDateAdapter.DateViewHolder>() {

    inner class DateViewHolder(val binding: CalendarDateTileBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(date: LocalDate?) {
            if (date != null) {
                binding.dateText.text = date.dayOfMonth.toString()
                binding.dateTile.setOnClickListener {
                    onDateClick(date) // 클릭 시 CalendarActivity로 전달
                }
            } else {
                binding.dateText.text = ""
                binding.dateText.setOnClickListener(null)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CalendarDateTileBinding.inflate(inflater, parent, false)
        return DateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        val date = dateList[position]
        holder.bind(date)

        if (date != null) {
            holder.binding.dateText.text = date.dayOfMonth.toString()
            holder.binding.dateText.visibility = View.VISIBLE

            val count = recordCountMap[date] ?: 0
            val context = holder.binding.root.context

            // ✅ 선택된 날짜일 때 → 무조건 강조 배경
            if (date == selectedDate) {
                holder.binding.circleView.visibility = View.VISIBLE
                holder.binding.circleView.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.main)
                )
                holder.binding.dateText.setTextColor(
                    ContextCompat.getColor(holder.binding.root.context, R.color.white)
                )
            }
            // ✅ 선택된 날짜가 아니고, count > 0 → 회색 동그라미
            else if (count > 0) {
                holder.binding.circleView.visibility = View.VISIBLE
                holder.binding.circleView.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.blue_01)
                )
            }
            // ✅ 선택도 아니고, 데이터도 없음 → 숨김
            else {
                holder.binding.circleView.visibility = View.INVISIBLE
            }

            // ✅ count >= 2면 extraCount 표시
            if (count > 0) {
                holder.binding.extraCount.visibility = View.VISIBLE
                holder.binding.countText.text = count.toString()
            } else {
                holder.binding.extraCount.visibility = View.INVISIBLE
            }

        } else {
            holder.binding.dateText.visibility = View.INVISIBLE
            holder.binding.circleView.visibility = View.INVISIBLE
            holder.binding.extraCount.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int = dateList.size
}
