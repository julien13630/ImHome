package com.dailyvery.apps.imhome

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

/**
 * Created by SidenessPC on 25/01/2017.
 */

class NotificationManager private constructor() {

    fun createNotification(context: Context, message: String, notifID: Int) {
        val notificationManager = context.getSystemService(context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        // prepare intent which is triggered if the
        // notification is selected

        val intent = Intent(context, MainActivity::class.java)
        // use System.currentTimeMillis() to have a unique ID for the pending intent
        val pIntent = PendingIntent.getActivity(context, System.currentTimeMillis().toInt(), intent, 0)

        // build notification
        // the addAction re-use the same intent to keep the dailyvery short
        val n = Notification.Builder(context)
                .setContentTitle(context.getString(R.string.notifName))
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_done_white_24dp)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .notification

        notificationManager.notify(notifID, n)
    }

    fun cancel(context: Context, notifID: Int) {
        val notificationManager = context.getSystemService(context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.cancel(notifID)

    }

    companion object {

        private var _INSTANCE: NotificationManager? = null

        val instance: NotificationManager
            get() {
                if (_INSTANCE == null) {
                    _INSTANCE = NotificationManager()
                }
                return _INSTANCE
            }
    }

}
