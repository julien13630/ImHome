package com.dailyvery.apps.imhome

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.support.v7.preference.PreferenceManager
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast

import com.dailyvery.apps.imhome.Data.Avert

import java.util.Random
import java.util.Timer
import java.util.TimerTask

/**
 * Created by SidenessPC on 25/01/2017.
 */

class MessageManager private constructor() {

    private val SENT = "SMS_SENT"

    private var mTimer: Timer? = null

    private var broadcastReceiver: BroadcastReceiver? = null

    init {
        mTimer = Timer()
        broadcastReceiver = null
    }

    fun sendSMS(context: Context, notifID: Int, a: Avert) {

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        val sentPI = PendingIntent.getBroadcast(context, 0, Intent(SENT), 0)

        if (broadcastReceiver == null) {
            broadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(arg0: Context, arg1: Intent) {
                    when (resultCode) {
                        Activity.RESULT_OK -> {

                            if (prefs.getBoolean("notifications_new_message", true)) {
                                NotificationManager.instance.cancel(context, notifID)
                                NotificationManager.instance.createNotification(context, context.getString(R.string.notifMessageSentTo) + a.contactName, notifID)
                            }
                            mTimer!!.cancel()
                            mTimer = Timer()
                        }

                        SmsManager.RESULT_ERROR_GENERIC_FAILURE, SmsManager.RESULT_ERROR_NO_SERVICE, SmsManager.RESULT_ERROR_NULL_PDU, SmsManager.RESULT_ERROR_RADIO_OFF -> {
                            NotificationManager.instance.cancel(context, notifID)
                            NotificationManager.instance.createNotification(context, "ECHEC", notifID)
                            //try again in 1 minute
                            mTimer!!.schedule(object : TimerTask() {
                                override fun run() {
                                    this.cancel() //no need to run again, if it fails, this exact code will run again
                                    //context.getApplicationContext().unregisterReceiver(broadcastReceiver);
                                    //Log.e("MessageManager", String.valueOf(result));
                                    sendSMS(context, notifID, a)
                                }
                            }, 60000, 60000)
                            return
                        }
                    }
                }
            }
        }

        // ---when the SMS has been sent---
        context.applicationContext.registerReceiver(broadcastReceiver, IntentFilter(SENT))

        SmsManager.getDefault().sendTextMessage(a.contactNumber, null, a.messageText + "\n" + context.getString(R.string.sentByImHome), sentPI, null)
    }

    companion object {

        private var _INSTANCE: MessageManager? = null

        val instance: MessageManager
            get() {
                if (_INSTANCE == null) {
                    _INSTANCE = MessageManager()
                }
                return _INSTANCE
            }
    }
}
