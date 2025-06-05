package com.example.moneychanger.adapter;

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.moneychanger.R

class EmailAdapter(
        private val emails: List<String>
) : RecyclerView.Adapter<EmailAdapter.EmailViewHolder>() {

class EmailViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val emailText: TextView = view.findViewById(R.id.textViewIdResult)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmailViewHolder {
        val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.list_id, parent, false)
        return EmailViewHolder(view)
        }

        override fun onBindViewHolder(holder: EmailViewHolder, position: Int) {
        holder.emailText.text = emails[position]
        }

        override fun getItemCount(): Int = emails.size
}


