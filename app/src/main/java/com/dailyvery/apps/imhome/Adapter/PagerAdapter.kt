package com.dailyvery.apps.imhome.Adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

import com.dailyvery.apps.imhome.LocationSelectionFragment
import com.dailyvery.apps.imhome.WifiSelectionFragment

class PagerAdapter(fm: FragmentManager, internal var mNumOfTabs: Int) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment? {

        when (position) {
            0 -> {
                return LocationSelectionFragment()
            }
            1 -> {
                return WifiSelectionFragment()
            }
            else -> return null
        }
    }

    override fun getCount(): Int {
        return mNumOfTabs
    }
}
