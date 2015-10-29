package com.example.julien.imhome;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.julien.imhome.Adapter.AdapterWifi;
import com.example.julien.imhome.Data.Avert;
import com.example.julien.imhome.Data.AvertDataSource;
import com.example.julien.imhome.Data.Wifi;
import com.example.julien.imhome.Data.WifiDataSource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WifiSelectionActivity extends ListActivity {

    private List<Wifi> wifiList;
    private ArrayList<Avert> avertList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_settings);

        avertList = getIntent().getExtras().getParcelableArrayList("avertList");

        WifiDataSource wds = new WifiDataSource(WifiSelectionActivity.this);
        try {
            wds.open();
            wifiList = wds.getAllWifi();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            wds.close();
        }

        ArrayList<Wifi> arrayListWifi = (ArrayList<Wifi>)wifiList;

        //Ajouter les wifis système
        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for(int i = 0 ; i < list.size() ; i++){
            Wifi temp = new Wifi();
            temp.setSsid(list.get(i).SSID);
            temp.setHashcode(-1);
            temp.setLibelle(list.get(i).SSID);
            boolean exists = false;
            for(int j = 0 ; j < arrayListWifi.size() ; j++){
                if(arrayListWifi.get(j).getSsid().compareTo(temp.getSsid()) == 0){
                    exists = true;
                    j = arrayListWifi.size();
                }
            }
            if(!exists){
                arrayListWifi.add(temp);
            }
        }

        AdapterWifi adapter = new AdapterWifi(WifiSelectionActivity.this, 0, arrayListWifi);
        WifiSelectionActivity.this.setListAdapter(adapter);
    }

    public void showValidWifiDialog(final Wifi w)
    {
        final EditText et = new EditText(WifiSelectionActivity.this);
        final AvertDataSource ads = new AvertDataSource(WifiSelectionActivity.this);
        et.setText("Je suis arrivé :)", TextView.BufferType.EDITABLE);

        //On limite le text a 160 caractères
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(160);
        et.setFilters(filterArray);

        AlertDialog.Builder builder = new AlertDialog.Builder(WifiSelectionActivity.this);
        builder.setMessage("Saisissez le texte à envoyer : ")
                .setPositiveButton("Valider", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            ads.open();
                            for (Avert a : avertList) {
                                a.setAddDate(new Date());
                                a.setMessageText(et.getText().toString());
                                a.setSsid(w.getSsid());
                                a.setLibelle(w.getLibelle());
                                a.setHashcode(w.getHashcode());

                                ads.addAvert(a);

                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            ads.close();
                        }

                        Intent intent=new Intent(WifiSelectionActivity.this,MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);

                        }
                    }

                    ).setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                }
                            }

                    ).setView(et);

                    // Create the AlertDialog object and return it
                    builder.create().

                    show();
                }

        @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        showValidWifiDialog(wifiList.get(position));
    }
}
