package com.dailyvery.apps.imhome;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.dailyvery.apps.imhome.Adapter.PagerAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.vungle.publisher.EventListener;
import com.vungle.publisher.VunglePub;

public class PlaceSelectionActivity extends AppCompatActivity {

    InterstitialAd mInterstitialAd;

    // get the VunglePub instance
    final VunglePub vunglePub = VunglePub.getInstance();

    private void onLevelComplete() {
        vunglePub.playAd();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_selection);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tbPlaceSelection);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.tvSelectDestination));

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-7386174591450774/2590892840");

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                finishNewMessage();
            }
        });

        requestNewInterstitial();

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.GPS)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.Wifi)));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        final EventListener vungleListener = new EventListener(){

            @Deprecated
            @Override
            public void onVideoView(boolean isCompletedView, int watchedMillis, int videoDurationMillis) {
                // This method is deprecated and will be removed. Please do not use it.
                // Please use onAdEnd instead.
            }

            @Override
            public void onAdStart() {
                // Called before playing an ad
            }

            @Override
            public void onAdEnd(boolean wasSuccessfulView, boolean wasCallToActionClicked) {
                finishNewMessage();
            }

            @Override
            public void onAdPlayableChanged(boolean isAdPlayable) {
                // Called when the playability state changes. if isAdPlayable is true, you can now
                // play an ad.
                // If false, you cannot yet play an ad.
            }

            @Override
            public void onAdUnavailable(String reason) {
                // Called when VunglePub.playAd() was called, but no ad was available to play
            }

        };

        vunglePub.addEventListeners(vungleListener);
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();

        mInterstitialAd.loadAd(adRequest);
    }

    public void showAd(){
        if (vunglePub.isAdPlayable()) {
            onLevelComplete();
        } else if(mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else{
            finishNewMessage();
        }
    }

    private void finishNewMessage() {
        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        vunglePub.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        vunglePub.onResume();
    }


}
