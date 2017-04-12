package com.dailyvery.apps.imhome;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.dailyvery.apps.imhome.Data.Avert;

/**
 * Created by SidenessPC on 25/01/2017.
 */

public class MessageManager {

    private static MessageManager _INSTANCE;

    private MessageManager(){

    }

    public static MessageManager getInstance(){
        if(_INSTANCE == null){
            _INSTANCE = new MessageManager();
        }
        return _INSTANCE;
    }

    public int sendSMS(Context context, SharedPreferences prefs, int notifID, Avert a) {
        SmsManager.getDefault().sendTextMessage(a.getContactNumber(), null, a.getMessageText() + "\n" + context.getString(R.string.sentByImHome), null, null);
        Toast.makeText(context, context.getString(R.string.notifMessageSentTo) + a.getContactName(), Toast.LENGTH_LONG).show();
        if(prefs.getBoolean("notifications_new_message", true)){
            NotificationManager.getInstance().createNotification(context, context.getString(R.string.notifMessageSentTo) + a.getContactName(), notifID++);
        }
        return notifID;
    }

}
