package com.poc.dropme.Gadgets.SmsBroadcast;

import static android.content.ContentValues.TAG;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.PreciseDataConnectionState;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.poc.dropme.Gadgets.PrefManager;

public class SMSreceiverPassenger extends BroadcastReceiver {
    public static MessageListenerPassenger mListenerP;
    PrefManager mpref;

    @Override
    public void onReceive(Context context, Intent intent) {
        mpref =new PrefManager(context);

        if (mpref.getUsername().equalsIgnoreCase("Employee")){
            if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {

                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object[] pdus = (Object[])bundle.get("pdus");
                    final SmsMessage[] messages = new SmsMessage[pdus.length];
                    String smsStr = "";
                    String body = "";

                    if (messages.length > -1) {

                        for (int i = 0; i < messages.length; i++) {
                            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                            smsStr += "Sent From: " + messages[i].getOriginatingAddress();
                            smsStr += "\n\nDate: " + messages[i].getTimestampMillis();
                            smsStr += "\r\nMessage: ";
                            smsStr += messages[i].getMessageBody().toString();
                            body+= messages[i].getMessageBody().toString();
                            smsStr += "\r\n";

                        }
                        mListenerP.PassengerMessageReceived(body);

                        Log.i(TAG, "Message recieved: " + messages[0].getMessageBody());

                    }
                }
            }
        }

    }
    public static void bindListenerPassenger(MessageListenerPassenger listener){
        mListenerP = listener;
    }
}
