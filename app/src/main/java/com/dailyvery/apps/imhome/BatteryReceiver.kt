package com.dailyvery.apps.imhome

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.widget.Toast

import com.dailyvery.apps.imhome.Data.Avert
import com.dailyvery.apps.imhome.Data.AvertDataSource

import java.sql.SQLException

/**
 * Created by Utilisateur on 23/01/2017.
 */

class BatteryReceiver : BroadcastReceiver() {

    //TODO Factoriser createNotification qui est utilise dans tous les receiver
    private fun createNotification(context: Context, message: String, notifID: Int) {
        val notificationManager = context.getSystemService(context.NOTIFICATION_SERVICE) as NotificationManager

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

    override fun onReceive(context: Context, intent: Intent) {
        val ads = AvertDataSource(context)
        try {
            ads.open()
            val avertList = ads.allAvert
            if (avertList.size > 0) {
                var notifID = avertList[0].hashcode
                for (a in avertList) {
                    SmsManager.getDefault().sendTextMessage(a.contactNumber, null, context.getString(R.string.messageCriticalBattery), null, null)
                    Toast.makeText(context, context.getString(R.string.notifMessageCriticalBattery) + " " + a.contactName + " " + context.getString(R.string.recipientAlerted), Toast.LENGTH_LONG).show()
                    createNotification(context, context.getString(R.string.notifMessageCriticalBattery) + " " + a.contactName, notifID++)
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            ads.close()
        }
    }
}
