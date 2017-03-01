package com.dailyvery.apps.imhome;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.dailyvery.apps.imhome.Data.Avert;
import com.dailyvery.apps.imhome.Data.AvertDataSource;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Utilisateur on 23/01/2017.
 */

public class BatteryReceiver extends BroadcastReceiver {

    //TODO Factoriser createNotification qui est utilise dans tous les receiver
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
        AvertDataSource ads = new AvertDataSource(context);
        try {
            ads.open();
            List<Avert> avertList = ads.getAllAvert();
            if (avertList.size() > 0) {
                int notifID = avertList.get(0).getHashcode();
                for (Avert a : avertList) {
                    SmsManager.getDefault().sendTextMessage(a.getContactNumber(), null, context.getString(R.string.messageCriticalBattery), null, null);
                    Toast.makeText(context, context.getString(R.string.notifMessageCriticalBattery) + " " + a.getContactName() + " " + context.getString(R.string.recipientAlerted), Toast.LENGTH_LONG).show();
                    createNotification(context, context.getString(R.string.notifMessageCriticalBattery) + " " + a.getContactName(), notifID++);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ads.close();
        }
    }
}
