package com.example.julien.imhome;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import java.util.List;

/**
 * Created by julie on 21/10/2015.
 */

public class WifiReceiver extends BroadcastReceiver {

    private final void createNotification(Context context, String message){
        //Récupération du notification Manager
        final NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        //Création de la notification avec spécification de l'icône de la notification et le texte qui apparait à la création de la notification
        final Notification notification = new Notification(R.drawable.ic_done_white_24dp, message, System.currentTimeMillis());

        //Définition de la redirection au moment du clic sur la notification. Dans notre cas la notification redirige vers notre application
        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);

        //Récupération du titre et description de la notification
        final String notificationTitle = "ImHome à envoyé un message";
        final String notificationDesc = message;

        //Notification & Vibration

        //notification.setLatestEventInfo(this, notificationTitle, notificationDesc, pendingIntent);
        notification.vibrate = new long[] {0,200,100,200,100,200};

        notificationManager.notify(0212, notification);
    }

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

                    AvertDataSource ads = new AvertDataSource(context);

                    try {
                        ads.open();
                        List<Avert> avertList = ads.getAllAvert();
                        for (Avert a : avertList) {
                            if (a.getHashcode() == info.getExtraInfo().hashCode()) {

                                SmsManager.getDefault().sendTextMessage(a.getContactNumber(), null, a.getMessageText(), null, null);
                                Toast.makeText(context, "ImHome : Message envoyé à " + a.getContactName(), Toast.LENGTH_LONG).show();
                               // createNotification(context,"Message envoyé à " + a.getContactName() );
                                ads.deleteAvert(a);
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
