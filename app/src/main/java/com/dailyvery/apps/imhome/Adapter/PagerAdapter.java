package com.dailyvery.apps.imhome.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.dailyvery.apps.imhome.LocationSelectionFragment;
import com.dailyvery.apps.imhome.WifiSelectionFragment;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                WifiSelectionFragment tab1 = new WifiSelectionFragment();
                return tab1;
            case 1:
                LocationSelectionFragment tab2 = new LocationSelectionFragment();
                return tab2;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
