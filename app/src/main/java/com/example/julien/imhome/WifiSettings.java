package com.example.julien.imhome;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.example.julien.imhome.Data.Wifi;
import com.example.julien.imhome.Data.WifiDataSource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WifiSettings extends AppCompatActivity {

    private List<Wifi> wifiList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
    }

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
                            //TODO Recharger la liste ICI
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

    public void showNoWifiDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(WifiSettings.this);
        builder.setMessage("Vous devez vous connecter à un reseau Wifi pour l'ajouter");


        // Create the AlertDialog object and return it
        builder.create().show();
    }

    public void showWifiAlreadyAddedDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(WifiSettings.this);
        builder.setMessage("Ce réseau Wifi est déjà enregistré");


        // Create the AlertDialog object and return it
        builder.create().show();
    }

}
