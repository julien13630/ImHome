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
import java.util.Calendar;
import java.util.Date;
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
                .setContentTitle(context.getString(R.string.notifName))
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

                                    if(a.getFlagReccurence() == 0){
                                        notifID = sendSMS(context, prefs, notifID, a);
                                        ads.deleteAvert(a, true);
                                    }else if(checkReccurence(a)){
                                        notifID = sendSMS(context, prefs, notifID, a);
                                        Calendar cal = Calendar.getInstance();
                                        cal.setTime(a.getAddDate());
                                        cal.add(Calendar.DAY_OF_YEAR, 1);
                                        a.setAddDate(cal.getTime());
                                        ads.editAvert(a);
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

    private int sendSMS(Context context, SharedPreferences prefs, int notifID, Avert a) {
        SmsManager.getDefault().sendTextMessage(a.getContactNumber(), null, a.getMessageText(), null, null);
        Toast.makeText(context, context.getString(R.string.notifMessageSentTo) + a.getContactName(), Toast.LENGTH_LONG).show();
        if(prefs.getBoolean("notifications_new_message", true)){
            createNotification(context, context.getString(R.string.notifMessageSentTo) + a.getContactName(), notifID++);
        }
        return notifID;
    }

    /**
     *  On verifie si le message a ete envoye il y au moins un jour
     * @param a date de l'envoi du dernier message
     * @return boolean si le dernier message a ete envoye au moins hier
     */
    private boolean checkReccurence(Avert a){
        if(a.getFlagReccurence() == 1 && a.getAddDate() != null){
            Calendar c1 = Calendar.getInstance(); // today
            c1.add(Calendar.DAY_OF_YEAR, -1); // yesterday

            Calendar c2 = Calendar.getInstance();
            c2.setTime(a.getAddDate()); // your date

            if (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                    && c1.get(Calendar.DAY_OF_YEAR) >= c2.get(Calendar.DAY_OF_YEAR)
                    && c1.get(Calendar.HOUR) >= c2.get(Calendar.HOUR)
                    && c1.get(Calendar.MINUTE) >= c2.get(Calendar.MINUTE)) {
                return true;
            }
        }
        return false;
    }
}
