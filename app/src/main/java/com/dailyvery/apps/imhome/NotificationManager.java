package com.dailyvery.apps.imhome;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Created by SidenessPC on 25/01/2017.
 */

public class NotificationManager {

    private static NotificationManager _INSTANCE;

    private NotificationManager(){

    }

    public static NotificationManager getInstance(){
        if(_INSTANCE == null){
            _INSTANCE = new NotificationManager();
        }
        return _INSTANCE;
    }

    public void createNotification(Context context, String message, int notifID){
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

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

}
