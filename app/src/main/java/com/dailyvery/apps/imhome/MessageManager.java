package com.dailyvery.apps.imhome;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.dailyvery.apps.imhome.Data.Avert;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by SidenessPC on 25/01/2017.
 */

public class MessageManager {

    private static MessageManager _INSTANCE;

    final private String SENT = "SMS_SENT";

    private Timer mTimer;

    private BroadcastReceiver broadcastReceiver;

    private MessageManager(){
        mTimer = new Timer();
        broadcastReceiver = null;
    }

    public static MessageManager getInstance(){
        if(_INSTANCE == null){
            _INSTANCE = new MessageManager();
        }
        return _INSTANCE;
    }

    public void sendSMS(final Context context, final int notifID, final Avert a) {

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, new Intent(SENT), 0);

        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver()
            {
                @Override
                public void onReceive(Context arg0,Intent arg1)
                {
                    switch(getResultCode())
                    {
                        case Activity.RESULT_OK:

                            if(prefs.getBoolean("notifications_new_message", true)){
                                NotificationManager.getInstance().cancel(context, notifID);
                                NotificationManager.getInstance().createNotification(context, context.getString(R.string.notifMessageSentTo) + a.getContactName(), notifID);
                            }
                            mTimer.cancel();
                            mTimer = new Timer();
                            break;

                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            NotificationManager.getInstance().cancel(context, notifID);
                            NotificationManager.getInstance().createNotification(context, "ECHEC", notifID);
                            //try again in 1 minute
                            mTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    this.cancel(); //no need to run again, if it fails, this exact code will run again
                                    //context.getApplicationContext().unregisterReceiver(broadcastReceiver);
                                    //Log.e("MessageManager", String.valueOf(result));
                                    sendSMS(context, notifID, a);
                                }
                            }, 60000, 60000);
                            return;
                    }
                }
            };
        }

        // ---when the SMS has been sent---
        context.getApplicationContext().registerReceiver(broadcastReceiver, new IntentFilter(SENT));

        SmsManager.getDefault().sendTextMessage(a.getContactNumber(), null, a.getMessageText() + "\n" + context.getString(R.string.sentByImHome), sentPI, null);
    }
}
