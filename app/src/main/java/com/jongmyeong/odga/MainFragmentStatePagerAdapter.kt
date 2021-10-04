package com.jongmyeong.odga

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class MainFragmentStatePagerAdapter(fm : FragmentManager, val fragmentCount : Int) : FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        when(position){
            0 -> return HomeFragment()
            1 -> return BluetoothFragment()
            2 -> return PhoneBookFragment()

        }
        throw IllegalStateException("position $position is invalid for this viewpager")
    }

    override fun getCount(): Int = fragmentCount // 자바에서는 { return fragmentCount }
}