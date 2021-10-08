package com.jongmyeong.odga

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PhoneInfoAdapter: RecyclerView.Adapter<PhoneInfoAdapter.ViewHolder>() {
    var datas = mutableListOf<PhoneBook>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.phone_list, parent, false)
        return ViewHolder(view)
    }
    override fun getItemCount(): Int{
        return datas.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val phoneBook = datas.get(position)
        holder.bind(phoneBook)
        holder.itemView.setOnClickListener {
            Log.d("클릭","${phoneBook}")
            val intent = Intent(it.context,PhonebookUpdateRemove::class.java)
            var id = phoneBook.id.toString()
            var name = phoneBook.fname
            var phone = phoneBook.fphone.toString()
            intent.putExtra("id", id)
            Log.d("dbid","${phoneBook.id}")
            intent.putExtra("name", name)
            intent.putExtra("phone", phone)

            it.context.startActivity(intent)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val txtName: TextView = itemView.findViewById(R.id.userName)
        private val txtPhone: TextView = itemView.findViewById(R.id.txtPhone)

        //        fun bind(item: PhoneInfoData) {
//            txtName.text = item.userName
//            txtPhone.text = item.phoneNum
//        }
        fun bind(phoneBook: PhoneBook) {
            txtName.text = "${phoneBook.fname}"
            txtPhone.text = "${phoneBook.fphone}"
        }


    }
}