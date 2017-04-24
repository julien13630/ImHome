package com.dailyvery.apps.imhome;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.dailyvery.apps.imhome.Data.Avert;
import com.dailyvery.apps.imhome.Data.AvertDataSource;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;


/**
 * Created by julien on 11/24/16.
 */

public class MyService extends Service
{
    private static final String TAG = "MyService";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 120000;
    private static final float LOCATION_DISTANCE = 50f;
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

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                double distanceDetection = prefs.getInt("localisation_distance", 100);
                distanceDetection = distanceDetection / 1000;

                //On compare
                if(distance(avertLatitude, avertLongitude, currentLatitude, currentLongitude) < distanceDetection){
                    AvertDataSource ads = new AvertDataSource(getApplicationContext());
                    try {
                        ads.open();
                        if(a.getFlagReccurence() == 0){
                            notifID = MessageManager.getInstance().sendSMS(getApplicationContext(), prefs, notifID, a);
                            ads.deleteAvert(a, true);
                            checkMessageLeft(ads);
                        }else if(checkReccurence(a)){
                            notifID = MessageManager.getInstance().sendSMS(getApplicationContext(), prefs, notifID, a);
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(a.getAddDate());
                            cal.add(Calendar.DAY_OF_YEAR, 1);
                            a.setAddDate(cal.getTime());
                            ads.editAvert(a);
                            checkMessageLeft(ads);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        avertList = ads.getAllAvert();
                        ads.close();
                        if(avertList.size() == 0){
                            stopSelf();
                        }
                    }
                //Si c'est faux
                }else{
                    Log.e(TAG, "On Y EST POOOOS !");
                }
            }
        }

        private void checkMessageLeft(AvertDataSource ads){
            List<Avert> list = ads.getAllAvert();
            boolean isGps = false;
            for(Avert item : list){
                if(item.getSsid() == null){
                    isGps = true;
                }
            }
            if(!isGps){
                stopService(new Intent(getBaseContext(), MyService.class));
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

    /**
     *  On verifie si le message a ete envoye il y au moins un jour
     * @param a date de l'envoi du dernier message
     * @return boolean si le dernier message a ete envoye au moins hier
     */
    private boolean checkReccurence(Avert a){
        if(a.getFlagReccurence() == 1 && a.getAddDate() != null){
            Calendar c1 = Calendar.getInstance(); // today
            c1.add(Calendar.DAY_OF_YEAR, -1); // yesterday

            Calendar c2 = Calendar.getInstance();
            c2.setTime(a.getAddDate()); // your date

            if (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                    && c1.get(Calendar.DAY_OF_YEAR) >= c2.get(Calendar.DAY_OF_YEAR)
                    && c1.get(Calendar.HOUR) >= c2.get(Calendar.HOUR)
                    && c1.get(Calendar.MINUTE) >= c2.get(Calendar.MINUTE)) {
                return true;
            }
        }
        return false;
    }
}
