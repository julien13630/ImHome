package com.dailyvery.apps.imhome

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.support.v4.content.ContextCompat
import android.support.v7.preference.PreferenceManager
import android.util.Log

import com.dailyvery.apps.imhome.Data.Avert
import com.dailyvery.apps.imhome.Data.AvertDataSource

import java.sql.SQLException
import java.util.Calendar


/**
 * Created by julien on 11/24/16.
 */

class MyService : Service() {
    private var mLocationManager: LocationManager? = null
    private var mRunning = false
    private var avertList: List<Avert>? = null

    internal var mLocationListeners = arrayOf(LocationListener(LocationManager.GPS_PROVIDER), LocationListener(LocationManager.NETWORK_PROVIDER))

    private inner class LocationListener(provider: String) : android.location.LocationListener {
        internal var mLastLocation: Location

        init {
            Log.e(TAG, "LocationListener $provider")
            mLastLocation = Location(provider)
        }

        override fun onLocationChanged(location: Location) {
            Log.e(TAG, "onLocationChanged: $location")
            mLastLocation.set(location)
            if (avertList == null || avertList!!.size == 0) {
                //Rustine pour empecher un plantage si jamais le service ne s'est pas correctement eteint
                return
            }
            val notifID = avertList!![0].hashcode
            for (a in avertList!!) {
                // On récupère les positions a comparer
                val avertLatitude = a.latitude
                val avertLongitude = a.longitude
                val currentLatitude = mLastLocation.latitude
                val currentLongitude = mLastLocation.longitude

                val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                var distanceDetection = prefs.getInt("localisation_distance", 100).toDouble()
                distanceDetection = distanceDetection / 1000

                //On compare
                if (distance(avertLatitude, avertLongitude, currentLatitude, currentLongitude) < distanceDetection) {
                    val ads = AvertDataSource(applicationContext)
                    try {
                        ads.open()
                        //if(a.getFlagReccurence() == 0){
                        MessageManager.instance.sendSMS(applicationContext, notifID, a)
                        ads.deleteAvert(a, true)
                        checkMessageLeft(ads)
                        /*}else if(checkReccurence(a)){
                            MessageManager.getInstance().sendSMS(getApplicationContext(), notifID, a);
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(a.getAddDate());
                            cal.add(Calendar.DAY_OF_YEAR, 1);
                            a.setAddDate(cal.getTime());
                            ads.editAvert(a);
                            checkMessageLeft(ads);
                        }*/
                    } catch (e: SQLException) {
                        e.printStackTrace()
                    } finally {
                        avertList = ads.allAvert
                        ads.close()
                        if (avertList!!.size == 0) {
                            stopSelf()
                        }
                    }
                    //Si c'est faux
                } else {
                    Log.e(TAG, "On Y EST POOOOS !")
                }
            }
        }

        private fun checkMessageLeft(ads: AvertDataSource) {
            val list = ads.allAvert
            var isGps = false
            for (item in list) {
                if (item.ssid == null) {
                    isGps = true
                }
            }
            if (!isGps) {
                stopService(Intent(baseContext, MyService::class.java))
            }
        }

        /** calculates the distance between two locations in kilometers  */
        private fun distance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {

            val earthRadius = 6371.0

            val dLat = Math.toRadians(lat2 - lat1)
            val dLng = Math.toRadians(lng2 - lng1)

            val sindLat = Math.sin(dLat / 2)
            val sindLng = Math.sin(dLng / 2)

            val a = Math.pow(sindLat, 2.0) + (Math.pow(sindLng, 2.0)
                    * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)))

            val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

            return earthRadius * c // output distance, in MILES
        }

        override fun onProviderDisabled(provider: String) {
            Log.e(TAG, "onProviderDisabled: $provider")
        }

        override fun onProviderEnabled(provider: String) {
            Log.e(TAG, "onProviderEnabled: $provider")
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            Log.e(TAG, "onStatusChanged: $provider")
        }
    }

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (!mRunning) {
            mRunning = true
            Log.e(TAG, "onStartCommand - start")
            super.onStartCommand(intent, flags, startId)
        } else {
            Log.e(TAG, "onStartCommand - already started")
        }
        val avertDT = AvertDataSource(applicationContext)
        try {
            avertDT.open()
            avertList = avertDT.allAvert
        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            avertDT.close()
        }
        return Service.START_STICKY
    }

    override fun onCreate() {
        Log.e(TAG, "onCreate")
        initializeLocationManager()
        try {
            mLocationManager!!.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL.toLong(), LOCATION_DISTANCE,
                    mLocationListeners[1])
        } catch (ex: java.lang.SecurityException) {
            Log.i(TAG, "fail to request location update, ignore", ex)
        } catch (ex: IllegalArgumentException) {
            Log.d(TAG, "network provider does not exist, " + ex.message)
        }

        try {
            mLocationManager!!.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL.toLong(), LOCATION_DISTANCE,
                    mLocationListeners[0])
        } catch (ex: java.lang.SecurityException) {
            Log.i(TAG, "fail to request location update, ignore", ex)
        } catch (ex: IllegalArgumentException) {
            Log.d(TAG, "gps provider does not exist " + ex.message)
        }

        mRunning = false
    }

    override fun onDestroy() {
        Log.e(TAG, "onDestroy")
        super.onDestroy()
        if (mLocationManager != null) {
            for (i in mLocationListeners.indices) {
                try {
                    if (ContextCompat.checkSelfPermission(applicationContext,
                                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    }
                    mLocationManager!!.removeUpdates(mLocationListeners[i])
                } catch (ex: Exception) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex)
                }

            }
        }
    }

    private fun initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager")
        if (mLocationManager == null) {
            mLocationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
    }

    /**
     * On verifie si le message a ete envoye il y au moins un jour
     * @param a date de l'envoi du dernier message
     * @return boolean si le dernier message a ete envoye au moins hier
     */
    private fun checkReccurence(a: Avert): Boolean {
        if (a.flagReccurence == 1 && a.addDate != null) {
            val c1 = Calendar.getInstance() // today
            c1.add(Calendar.DAY_OF_YEAR, -1) // yesterday

            val c2 = Calendar.getInstance()
            c2.time = a.addDate // your date

            if (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                    && c1.get(Calendar.DAY_OF_YEAR) >= c2.get(Calendar.DAY_OF_YEAR)
                    && c1.get(Calendar.HOUR) >= c2.get(Calendar.HOUR)
                    && c1.get(Calendar.MINUTE) >= c2.get(Calendar.MINUTE)) {
                return true
            }
        }
        return false
    }

    companion object {
        private val TAG = "MyService"
        private val LOCATION_INTERVAL = 120000
        private val LOCATION_DISTANCE = 50f
    }
}
