package com.dailyvery.apps.imhome

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.NetworkInfo
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.support.v7.preference.PreferenceManager
import android.telephony.SmsManager
import android.widget.Toast

import com.dailyvery.apps.imhome.Data.Avert
import com.dailyvery.apps.imhome.Data.AvertDataSource

import java.sql.SQLException
import java.util.Calendar
import java.util.Date

/**
 * Created by julie on 21/10/2015.
 */

class WifiReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val info = intent.getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO)
        if (info != null) {
            if (info.type == 1)
            //C'est du wifi
            {
                if (info.isConnected) {
                    val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                    val wifiInfo = wifiManager.connectionInfo
                    val ssid = wifiInfo.ssid

                    val ads = AvertDataSource(context)

                    try {
                        ads.open()
                        val avertList = ads.allAvert
                        if (avertList.size > 0) {
                            val notifID = avertList[0].hashcode
                            for (a in avertList) {
                                if (a.ssid.compareTo(ssid.substring(1, ssid.length - 1)) == 0) {

                                    //if(a.getFlagReccurence() == 0){
                                    MessageManager.instance.sendSMS(context, notifID, a)
                                    ads.deleteAvert(a, true)
                                    /*}else if(checkReccurence(a)){
                                        MessageManager.getInstance().sendSMS(context, notifID, a);
                                        Calendar cal = Calendar.getInstance();
                                        cal.setTime(a.getAddDate());
                                        cal.add(Calendar.DAY_OF_YEAR, 1);
                                        a.setAddDate(cal.getTime());
                                        ads.editAvert(a);
                                    }*/
                                }
                            }
                        }
                    } catch (e: SQLException) {
                        e.printStackTrace()
                    } finally {
                        ads.close()
                    }
                }
            }
        }
    }

    /**
     * On verifie si le message a ete envoye il y au moins un jour
     * @param a date de l'envoi du dernier message
     * @return boolean si le dernier message a ete envoye au moins hier
     */
    private fun checkReccurence(a: Avert): Boolean {
        if (a.flagReccurence == 1 && a.addDate != null) {
            val c1 = Calendar.getInstance() // today
            c1.add(Calendar.DAY_OF_YEAR, -1) // yesterday

            val c2 = Calendar.getInstance()
            c2.time = a.addDate // your date

            if (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                    && c1.get(Calendar.DAY_OF_YEAR) >= c2.get(Calendar.DAY_OF_YEAR)
                    && c1.get(Calendar.HOUR) >= c2.get(Calendar.HOUR)
                    && c1.get(Calendar.MINUTE) >= c2.get(Calendar.MINUTE)) {
                return true
            }
        }
        return false
    }
}
