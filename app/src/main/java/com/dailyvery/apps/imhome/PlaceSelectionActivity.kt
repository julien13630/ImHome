package com.dailyvery.apps.imhome

import android.content.Intent
import android.content.SharedPreferences
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.preference.PreferenceManager
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Button
import android.widget.LinearLayout

import com.dailyvery.apps.imhome.Adapter.PagerAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd

class PlaceSelectionActivity : AppCompatActivity() {

    internal var mInterstitialAd: InterstitialAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_selection)
        val toolbar = findViewById<View>(R.id.tbPlaceSelection) as Toolbar
        setSupportActionBar(toolbar)
        title = getString(R.string.tvSelectDestination)

        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = "ca-app-pub-7386174591450774/2590892840"

        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
            }
        }

        requestNewInterstitial()

        val tabLayout = findViewById<View>(R.id.tab_layout) as TabLayout
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.GPS)))
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.Wifi)))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL

        val viewPager = findViewById<View>(R.id.pager) as ViewPager
        val adapter = PagerAdapter(supportFragmentManager, tabLayout.tabCount)
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.setOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })

        val ll_tuto = findViewById<View>(R.id.tuto_layout) as LinearLayout

        val prefs = PreferenceManager.getDefaultSharedPreferences(baseContext)
        val previouslyDisplayed = prefs.getBoolean("TutoAlreadyDisplayed", false)
        if (previouslyDisplayed) {
            ll_tuto.visibility = View.GONE
        } else {
            val bOk = findViewById<View>(R.id.bTutoUnderstood) as Button
            bOk.setOnClickListener {
                val edit = prefs.edit()
                edit.putBoolean("TutoAlreadyDisplayed", java.lang.Boolean.TRUE)
                edit.commit()
                ll_tuto.visibility = View.GONE
            }


        }
    }

    private fun requestNewInterstitial() {
        val adRequest = AdRequest.Builder().addTestDevice("28D1BA9946A1BB6526982E1CBE179939").build()

        mInterstitialAd.loadAd(adRequest)
    }

    fun showAd() {
        if (mInterstitialAd.isLoaded) {
            mInterstitialAd.show()
        } else {
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }
}
