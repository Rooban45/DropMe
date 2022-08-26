package com.poc.dropme.Gadgets;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.maps.model.Polyline;
import com.poc.dropme.Driver.DriverHome;
import com.poc.dropme.Gadgets.SmsBroadcast.MessageListener;
import com.poc.dropme.Gadgets.SmsBroadcast.MessageListenerPassenger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    public static LocationCall mListenerP;
    private static final String TAG = "GeofenceBroadcastReceiv";


    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        Toast.makeText(context, "triggered", Toast.LENGTH_SHORT).show();

        Log.d(TAG, "onReceive: 1111111111111111111111111111111111111111111111111");

      //  NotificationHelper notificationHelper = new NotificationHelper(context);

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);


        if (geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofence event...");
            return;
        }

        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
        for (Geofence geofence: geofenceList) {
            Log.d(TAG, "onReceive: " + geofence.getRequestId());

        }
        Location location = geofencingEvent.getTriggeringLocation();



        int transitionType = geofencingEvent.getGeofenceTransition();

        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:

                mListenerP.enteredDwell("GEOFENCE_TRANSITION_ENTER");
                Toast.makeText(context, "GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_SHORT).show();
             //   notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_ENTER", "", PassengerHome.class);
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Toast.makeText(context, "GEOFENCE_TRANSITION_DWELL", Toast.LENGTH_SHORT).show();
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Toast.makeText(context, "GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public static void bindListenergeo(LocationCall locationCall){
        mListenerP = locationCall;


    }


}
