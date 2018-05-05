package com.dailyvery.apps.imhome

import android.Manifest
import android.annotation.TargetApi
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceManager
import android.support.v7.widget.Toolbar
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView

import com.dailyvery.apps.imhome.Adapter.AdapterMain
import com.dailyvery.apps.imhome.Data.Avert
import com.dailyvery.apps.imhome.Data.AvertDataSource
import com.dailyvery.apps.imhome.Interface.BtnClickListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

import java.sql.SQLException
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private var avertList: MutableList<Avert>? = null
    private var adapter: AdapterMain? = null
    private var lvMain: ListView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        title = getString(R.string.tvMessagesEnAttente)

        val prefs = PreferenceManager.getDefaultSharedPreferences(baseContext)
        val previouslyStarted = prefs.getBoolean(getString(R.string.pref_previously_started), false)
        if (!previouslyStarted) {
            val edit = prefs.edit()
            edit.putBoolean(getString(R.string.pref_previously_started), java.lang.Boolean.TRUE)
            edit.commit()
            val intent = Intent(this@MainActivity, SplashScreenActivity::class.java)
            startActivity(intent)
            finish()
        }

        MobileAds.initialize(applicationContext, "ca-app-pub-7386174591450774~4697714843")

        val mAdView = findViewById<View>(R.id.adView) as AdView
        val adRequest = AdRequest.Builder().addTestDevice("28D1BA9946A1BB6526982E1CBE179939").build()
        mAdView.loadAd(adRequest)


        val BtnAdd = findViewById<View>(R.id.BtnAddMessage) as Button
        BtnAdd.setOnClickListener {
            val intent = Intent(this@MainActivity, ContactActivity::class.java)
            startActivity(intent)
        }

        val ImgBtnAdd = findViewById<View>(R.id.iv_NoPendingMessage) as ImageView
        ImgBtnAdd.setOnClickListener {
            val intent = Intent(this@MainActivity, ContactActivity::class.java)
            startActivity(intent)
        }

        lvMain = findViewById<View>(R.id.listMain) as ListView

        lvMain!!.emptyView = findViewById(R.id.emptyListMain)

        getDataSetList()

        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(Manifest.permission.SEND_SMS), 0)
        }
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(Manifest.permission.READ_SMS), 0)
        }

        if (avertList!!.size > 0) {
            askIgnoreBatteryOptimizations()
        }
    }

    private fun askIgnoreBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent()
            val packageName = packageName
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_settings) {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()

        getDataSetList()
    }

    /**
     * Récupère la liste des Avert a afficher
     */
    private fun getDataSetList() {
        val avertDT = AvertDataSource(this@MainActivity)
        try {
            avertDT.open()
            avertList = avertDT.allAvert
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            avertDT.close()
        }

        val btnListenerDelete = BtnClickListener { position ->
            val avertToRestore = avertList!![position]
            val avertDT = AvertDataSource(this@MainActivity)
            avertDT.deleteAvert(avertList!![position], false)
            avertList!!.remove(avertList!![position])
            adapter!!.notifyDataSetChanged()
            avertDT.close()

            Snackbar.make(findViewById(android.R.id.content), resources.getString(R.string.deleted), Snackbar.LENGTH_LONG)
                    .setAction(resources.getString(R.string.cancel)) {
                        val avertDT = AvertDataSource(this@MainActivity)
                        avertDT.addAvert(avertToRestore)
                        avertList!!.add(avertToRestore)
                        adapter!!.notifyDataSetChanged()
                        avertDT.close()
                    }
                    .setActionTextColor(Color.YELLOW)
                    .show()
        }

        val btnListenerEdit = BtnClickListener { position ->
            val avertDT = AvertDataSource(this@MainActivity)

            val et = EditText(this@MainActivity)
            et.setText(avertDT.allAvert[position].messageText, TextView.BufferType.EDITABLE)

            avertDT.close()

            //On limite le text a 160 caractères
            val filterArray = arrayOfNulls<InputFilter>(1)
            filterArray[0] = InputFilter.LengthFilter(160)
            et.filters = filterArray

            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setMessage(getString(R.string.tvPleaseEnterMessageTextNew))
                    .setPositiveButton(getString(R.string.validate)

                    ) { dialog, id ->
                        val avertDT = AvertDataSource(this@MainActivity)
                        avertList!![position].messageText = et.text.toString()
                        avertDT.editAvert(avertList!![position])
                        adapter!!.notifyDataSetChanged()
                        avertDT.close()
                    }.setNegativeButton(getString(R.string.cancel)

                    ) { dialog, id ->
                        // User cancelled the dialog
                    }.setView(et)

            // Create the AlertDialog object and return it
            builder.create().show()
        }

        adapter = AdapterMain(this@MainActivity, 0, avertList as ArrayList<Avert>?, btnListenerDelete, btnListenerEdit)
        lvMain!!.adapter = adapter
    }

    companion object {
        private val inflater: LayoutInflater? = null
    }

}
