package com.dailyvery.apps.imhome;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.dailyvery.apps.imhome.Data.Avert;
import com.dailyvery.apps.imhome.Data.AvertDataSource;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by julie on 21/10/2015.
 */

public class WifiReceiver extends BroadcastReceiver {

    private final void createNotification(Context context, String message, int notifID){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        // prepare intent which is triggered if the
        // notification is selected

        Intent intent = new Intent(context, MainActivity.class);
        // use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, 0);

        // build notification
        // the addAction re-use the same intent to keep the dailyvery short
        Notification n  = new Notification.Builder(context)
                .setContentTitle("ImHome Message Envoyé")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_done_white_24dp)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .getNotification();

        notificationManager.notify(notifID, n);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if(info != null) {
            if (info.getType() == 1) //C'est du wifi
            {
                if(info.isConnected()) {
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    String ssid = wifiInfo.getSSID();

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

                    AvertDataSource ads = new AvertDataSource(context);

                    try {
                        ads.open();
                        List<Avert> avertList = ads.getAllAvert();
                        if (avertList.size() > 0) {
                            int notifID = avertList.get(0).getHashcode();
                            for (Avert a : avertList) {
                                if (a.getSsid().compareTo(ssid.substring(1, ssid.length() - 1)) == 0) {

                                    SmsManager.getDefault().sendTextMessage(a.getContactNumber(), null, a.getMessageText(), null, null);
                                    Toast.makeText(context, "ImHome : Message envoyé à " + a.getContactName(), Toast.LENGTH_LONG).show();
                                    if(prefs.getBoolean("notifications_new_message", true)){
                                        createNotification(context, "Message envoyé à " + a.getContactName(), notifID++);
                                    }
                                    if(a.getFlagReccurence() == 0){
                                        ads.deleteAvert(a, true);
                                    }
                                }
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
