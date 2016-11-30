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
                LocationSelectionFragment tab1 = new LocationSelectionFragment();
                return tab1;
            case 1:
                WifiSelectionFragment tab2 = new WifiSelectionFragment();
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
