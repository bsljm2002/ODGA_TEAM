package com.jongmyeong.odga

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast


class HomeFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view : View = inflater.inflate(R.layout.fragment_home, container, false)

        val btnMusic : ImageButton = view.findViewById(R.id.btnMusic)

        btnMusic.setOnClickListener(object :View.OnClickListener {
            override fun onClick(v: View?) {
                // 버튼 클릭시 화면 전환
                // val intent = Intent(activity, PhoneAdd::class.java)
                // startActivity(intent)
                // 다른 액티비티에서 전환할 때
                // activity?.finish()
                
                Toast.makeText(activity,"준비 중 입니다.",Toast.LENGTH_SHORT).show()
            }


        })


        return view
    }
}