package com.example.julien.imhome;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

import com.example.julien.imhome.Adapter.AdapterWifi;
import com.example.julien.imhome.Data.Wifi;
import com.example.julien.imhome.Data.WifiDataSource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WifiSettings extends ListActivity {

    private List<Wifi> wifiList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_settings);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                ConnectivityManager cm = (ConnectivityManager) WifiSettings.this.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = cm.getActiveNetworkInfo();

                if (info != null) {
                    if (info.isConnected()) {

                        if (info.getType() != 1) //C'est pas du wifi
                        {
                            showNoWifiDialog();
                            return;
                        }
                        // e.g. To check the Network Name or other info:

                        showAddWifiDialog(info);
                    }
                }


            }
        });

        WifiDataSource wds = new WifiDataSource(WifiSettings.this);
        try {
            wds.open();
           wifiList = wds.getAllWifi();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            wds.close();
        }

        ArrayList<Wifi> arrayListWifi = (ArrayList<Wifi>)wifiList;
        AdapterWifi adapter = new AdapterWifi(WifiSettings.this, 0, arrayListWifi);
        WifiSettings.this.setListAdapter(adapter);
    }

    /**
     * PopUp pour ajouter un Wifi
     *
     * @param info
     *
     * @return boolean
     *            return true si on a bien detecte un mouvement a droite ou gauche
     */
    public void showAddWifiDialog(final NetworkInfo info)
    {
        WifiManager wifiManager = (WifiManager)WifiSettings.this.getSystemService(Context.WIFI_SERVICE);
        final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        final String ssid = wifiInfo.getSSID();

        AlertDialog.Builder builder = new AlertDialog.Builder(WifiSettings.this);
        builder.setMessage("Voulez-vous enregister le Wifi suivant : " + ssid)
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        WifiDataSource wds = new WifiDataSource(WifiSettings.this);
                        try {
                            wds.open();
                            for (Wifi w : wds.getAllWifi()) {
                                if (w.getHashcode() == info.getExtraInfo().hashCode()) {
                                    showWifiAlreadyAddedDialog();
                                    wds.close();
                                    return;
                                }

                            }
                            wds.addWifi(ssid, ssid, info.getExtraInfo().hashCode(), false);
                            wifiList = wds.getAllWifi();

                            ArrayList<Wifi> arrayListWifi = (ArrayList<Wifi>)wifiList;
                            AdapterWifi adapter = new AdapterWifi(WifiSettings.this, 0, arrayListWifi);
                            WifiSettings.this.setListAdapter(adapter);

                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            wds.close();
                        }

                    }
                })
                .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        // Create the AlertDialog object and return it
        builder.create().show();
    }

    /**
     * PopUp pour ajouter un Wifi si aucun wifi enregistre
     *
     */
    public void showNoWifiDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(WifiSettings.this);
        builder.setMessage("Vous devez vous connecter à un reseau Wifi pour l'ajouter");


        // Create the AlertDialog object and return it
        builder.create().show();
    }

    /**
     * Affiche que le Wifi est deja enregistre
     */
    public void showWifiAlreadyAddedDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(WifiSettings.this);
        builder.setMessage("Ce réseau Wifi est déjà enregistré");


        // Create the AlertDialog object and return it
        builder.create().show();
    }

}
