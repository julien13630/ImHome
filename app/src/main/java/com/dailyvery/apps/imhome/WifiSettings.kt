package com.dailyvery.apps.imhome

import android.app.AlertDialog
import android.app.ListActivity
import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.view.View

import com.dailyvery.apps.imhome.Adapter.AdapterWifi
import com.dailyvery.apps.imhome.Data.Wifi
import com.dailyvery.apps.imhome.Data.WifiDataSource

import java.sql.SQLException
import java.util.ArrayList

class WifiSettings : ListActivity() {

    private var wifiList: List<Wifi>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_settings)


        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        fab.setOnClickListener(View.OnClickListener {
            val cm = this@WifiSettings.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val info = cm.activeNetworkInfo

            if (info != null) {
                if (info.isConnected) {

                    if (info.type != 1)
                    //C'est pas du wifi
                    {
                        showNoWifiDialog()
                        return@OnClickListener
                    }
                    // e.g. To check the Network Name or other info:

                    showAddWifiDialog(info)
                }
            }
        })

        val wds = WifiDataSource(this@WifiSettings)
        try {
            wds.open()
            wifiList = wds.allWifi

        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            wds.close()
        }

        val arrayListWifi = wifiList as ArrayList<Wifi>?
        val adapter = AdapterWifi(this@WifiSettings, 0, arrayListWifi, this@WifiSettings.listView)
        this@WifiSettings.listAdapter = adapter
    }

    /**
     * PopUp pour ajouter un Wifi
     *
     * @param info
     *
     * @return boolean
     * return true si on a bien detecte un mouvement a droite ou gauche
     */
    fun showAddWifiDialog(info: NetworkInfo?) {
        val wifiManager = this@WifiSettings.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val ssid = wifiInfo.ssid

        val builder = AlertDialog.Builder(this@WifiSettings)
        builder.setMessage("Voulez-vous enregister le Wifi suivant : $ssid")
                .setPositiveButton("Oui", DialogInterface.OnClickListener { dialog, id ->
                    val wds = WifiDataSource(this@WifiSettings)
                    try {
                        wds.open()
                        for (w in wds.allWifi) {
                            if (w.hashcode == info!!.extraInfo.hashCode()) {
                                showWifiAlreadyAddedDialog()
                                wds.close()
                                return@OnClickListener
                            }

                        }
                        wds.addWifi(ssid, ssid, info!!.extraInfo.hashCode(), false)
                        wifiList = wds.allWifi

                        val arrayListWifi = wifiList as ArrayList<Wifi>?
                        val adapter = AdapterWifi(this@WifiSettings, 0, arrayListWifi, this@WifiSettings.listView)
                        this@WifiSettings.listAdapter = adapter

                    } catch (e: SQLException) {
                        e.printStackTrace()
                    } finally {
                        wds.close()
                    }
                })
                .setNegativeButton("Non") { dialog, id ->
                    // User cancelled the dialog
                }

        // Create the AlertDialog object and return it
        builder.create().show()
    }

    /**
     * PopUp pour ajouter un Wifi si aucun wifi enregistre
     *
     */
    fun showNoWifiDialog() {
        val builder = AlertDialog.Builder(this@WifiSettings)
        builder.setMessage("Vous devez vous connecter à un reseau Wifi pour l'ajouter")


        // Create the AlertDialog object and return it
        builder.create().show()
    }

    /**
     * Affiche que le Wifi est deja enregistre
     */
    fun showWifiAlreadyAddedDialog() {
        val builder = AlertDialog.Builder(this@WifiSettings)
        builder.setMessage("Ce réseau Wifi est déjà enregistré")


        // Create the AlertDialog object and return it
        builder.create().show()
    }

}
