package com.dailyvery.apps.imhome;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.dailyvery.apps.imhome.Data.Avert;
import com.dailyvery.apps.imhome.Data.AvertDataSource;
import com.google.android.gms.maps.model.LatLng;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by julien on 11/24/16.
 */

public class MyService extends Service
{
    private static final String TAG = "MyService";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    private boolean mRunning = false;
    private List<Avert> avertList;

    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;

        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            int notifID = avertList.get(0).getHashcode();
            for(Avert a : avertList){
                // On récupère les positions a comparer
                double avertLatitude = a.getLatitude();
                double avertLongitude = a.getLongitude();
                double currentLatitude = mLastLocation.getLatitude();
                double currentLongitude = mLastLocation.getLongitude();

                //On compare
                if(distance(avertLatitude, avertLongitude, currentLatitude, currentLongitude) < 0.1){
                    AvertDataSource ads = new AvertDataSource(getApplicationContext());
                    try {
                        ads.open();
                        Log.e(TAG, "ON Y EST !!");
                        SmsManager.getDefault().sendTextMessage(a.getContactNumber(), null, a.getMessageText(), null, null);
                        createNotification(getApplicationContext(), "Message envoyé à " + a.getContactName(), notifID++);
                        ads.deleteAvert(a);
                        avertList = ads.getAllAvert();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        ads.close();
                    }
                //Si c'est faux
                }else{
                    Log.e(TAG, "On Y EST POOOOS !");
                }
            }
        }

        /** calculates the distance between two locations in kilometers */
        private double distance(double lat1, double lng1, double lat2, double lng2) {

            double earthRadius = 6371;

            double dLat = Math.toRadians(lat2-lat1);
            double dLng = Math.toRadians(lng2-lng1);

            double sindLat = Math.sin(dLat / 2);
            double sindLng = Math.sin(dLng / 2);

            double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                    * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

            double dist = earthRadius * c;

            return dist; // output distance, in MILES
        }

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
                    .setContentTitle("ImHome Message Envoyé")
                    .setContentText(message)
                    .setSmallIcon(R.drawable.ic_done_white_24dp)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .getNotification();

            notificationManager.notify(notifID, n);
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(!mRunning){
            mRunning = true;
            Log.e(TAG, "onStartCommand - start");
            super.onStartCommand(intent, flags, startId);
        }else{
            Log.e(TAG, "onStartCommand - already started");
        }
        final AvertDataSource avertDT = new AvertDataSource(getApplicationContext());
        try {
            avertDT.open();
            avertList = avertDT.getAllAvert();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            avertDT.close();
        }
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
        mRunning = false;
    }

    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {

                    }
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}
