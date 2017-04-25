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

                    AvertDataSource ads = new AvertDataSource(context);

                    try {
                        ads.open();
                        List<Avert> avertList = ads.getAllAvert();
                        if (avertList.size() > 0) {
                            int notifID = avertList.get(0).getHashcode();
                            for (Avert a : avertList) {
                                if (a.getSsid().compareTo(ssid.substring(1, ssid.length() - 1)) == 0) {

                                    if(a.getFlagReccurence() == 0){
                                        MessageManager.getInstance().sendSMS(context, notifID, a);
                                        ads.deleteAvert(a, true);
                                    }else if(checkReccurence(a)){
                                        MessageManager.getInstance().sendSMS(context, notifID, a);
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
