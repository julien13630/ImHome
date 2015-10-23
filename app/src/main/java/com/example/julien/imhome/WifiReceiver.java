package com.example.julien.imhome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.example.julien.imhome.Adapter.AdapterWifi;
import com.example.julien.imhome.Data.Avert;
import com.example.julien.imhome.Data.AvertDataSource;
import com.example.julien.imhome.Data.Wifi;
import com.example.julien.imhome.Data.WifiDataSource;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by julie on 21/10/2015.
 */

public class WifiReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if(info != null) {
            if (info.getType() == 1) //C'est du wifi
            {
                if(info.isConnected()) {
                    // Do your work.

                    // e.g. To check the Network Name or other info:
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    String ssid = wifiInfo.getSSID();
                    Toast.makeText(context, ssid, Toast.LENGTH_LONG);
                    AvertDataSource ads = new AvertDataSource(context);
                    try {
                        ads.open();
                        for (Avert a : ads.getAllAvert()) {
                            if (a.getHashcode() == info.getExtraInfo().hashCode()) {

                                SmsManager.getDefault().sendTextMessage(a.getContactNumber(), null, a.getMessageText(), null, null);
                            }

                        }

                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        ads.close();
                    }
                }
            }
        }
    }
}
