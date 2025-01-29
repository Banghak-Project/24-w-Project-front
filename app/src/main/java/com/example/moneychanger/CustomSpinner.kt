package com.example.moneychanger

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.TextView

class CustomSpinner(private val context: Context, private val items: List<String>) {
    private var popupWindow: PopupWindow? = null
    private var selectedItem: String = items[0]

    fun show(anchor: View, callback: (String) -> Unit) {
        if (popupWindow == null) {
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.custom_spinner_dropdown, null)
            val listView = view.findViewById<ListView>(R.id.custom_spinner_list)

            // 커스텀 ArrayAdapter 적용
            val adapter = object : ArrayAdapter<String>(context, R.layout.custom_spinner_item, items) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.custom_spinner_item, parent, false)
                    val textView = view.findViewById<TextView>(R.id.custom_spinner_item)
                    textView.text = getItem(position)
                    return view
                }

                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.custom_spinner_item, parent, false)
                    val textView = view.findViewById<TextView>(R.id.custom_spinner_item)
                    textView.text = getItem(position)
                    return view
                }
            }

            listView.adapter = adapter
            listView.setOnItemClickListener { _, _, position, _ ->
                selectedItem = items[position]
                callback(selectedItem)
                popupWindow?.dismiss()
            }

            popupWindow = PopupWindow(view, anchor.width, 400, true) // 높이 제한하여 스크롤 가능
            popupWindow?.isOutsideTouchable = true
        }

        popupWindow?.showAsDropDown(anchor)
    }

    fun getSelectedItem(): String {
        return selectedItem
    }
}

