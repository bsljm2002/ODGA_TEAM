package com.jongmyeong.odga

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class PhoneBookFragment : Fragment() {

    lateinit var phoneInfoAdapter: PhoneInfoAdapter
    lateinit var recyclerView1 : RecyclerView
    private var mContext: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view : View = inflater.inflate(R.layout.fragment_phone_book, container, false)
        recyclerView1 = view.findViewById(R.id.phoneInfo) as RecyclerView
        phoneInfoAdapter = PhoneInfoAdapter()
        phoneInfoAdapter?.notifyDataSetChanged()
        recyclerView1?.adapter = phoneInfoAdapter
        recyclerView1?.layoutManager= LinearLayoutManager(requireContext())


        val button : ImageButton = view.findViewById(R.id.btnAdd)
        button.setOnClickListener(object :View.OnClickListener {
            override fun onClick(v: View?) {
                val intent = Intent(activity, PhoneAdd::class.java)
                startActivity(intent)
                // 다른 액티비티에서 전환할 때
//                activity?.finish()
            }


        })

        initRecycler()

        return view
    }

    fun initRecycler() {

        val DB_NAME = "sqlite.sql"
        val DB_VERSION = 1
        val helper = SqliteHelper(requireContext(), DB_NAME, DB_VERSION)
        val phones = helper.selectPhoneBook()

        phoneInfoAdapter?.datas.clear()
        phoneInfoAdapter?.datas.addAll(phones)
        Log.d("db","${phones}")
        phoneInfoAdapter?.notifyDataSetChanged()

        recyclerView1.addItemDecoration(
            DividerItemDecoration(
                activity,
                DividerItemDecoration.VERTICAL
            )
        )
    }
}