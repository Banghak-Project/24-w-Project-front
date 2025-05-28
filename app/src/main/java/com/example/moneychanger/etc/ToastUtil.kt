package com.example.moneychanger.etc

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.example.moneychanger.R

object ToastUtil {
    fun showDismissibleToast(context: Context, message: String) {
        val inflater = LayoutInflater.from(context)
        val layout: View = inflater.inflate(R.layout.custom_toast, null)

        val textView = layout.findViewById<TextView>(R.id.toast_text)
        textView.text = message

        val toast = Toast(context)
        toast.view = layout
        toast.duration = Toast.LENGTH_SHORT
        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 120)
        toast.show()

        Handler(Looper.getMainLooper()).postDelayed({
            toast.cancel()
        }, 1400)

        layout.setOnClickListener {
            toast.cancel()
        }
    }
}