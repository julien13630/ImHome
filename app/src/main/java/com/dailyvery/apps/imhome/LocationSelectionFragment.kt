package com.dailyvery.apps.imhome

import android.Manifest
import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

import com.dailyvery.apps.imhome.Data.Avert
import com.dailyvery.apps.imhome.Data.AvertDataSource
import com.dailyvery.apps.imhome.Data.LocationDataSource
import com.dailyvery.apps.imhome.SearchBar.DelayAutoCompleteTextView
import com.dailyvery.apps.imhome.SearchBar.GeoAutoCompleteAdapter
import com.dailyvery.apps.imhome.SearchBar.GeoSearchResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

import java.io.IOException
import java.sql.SQLException
import java.util.ArrayList
import java.util.Date

class LocationSelectionFragment : Fragment(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private var mMapView: MapView? = null
    private var locationManager: LocationManager? = null
    private var pbLoading: ProgressBar? = null
    private var googleMap: GoogleMap? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var marker: Marker? = null
    private var address: Address? = null
    private var location: LatLng? = null
    private var btValider: Button? = null
    private var avertList: ArrayList<Avert>? = null
    private val timeSetListener: TimePickerDialog.OnTimeSetListener? = null
    private var dateReccurence: Date? = null
    private var firstLocation: Boolean = false
    private var lds: LocationDataSource? = null

    private val THRESHOLD = 2
    private var geo_autocomplete: DelayAutoCompleteTextView? = null
    private var geo_autocomplete_clear: ImageView? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView: View

        firstLocation = true

        lds = LocationDataSource(activity)

        if (ContextCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)

            rootView = inflater!!.inflate(R.layout.fragment_location_denied, container, false)

            val btActivateGps = rootView.findViewById(R.id.btActivateGps) as Button
            btActivateGps.setOnClickListener { activity.recreate() }
        } else {
            rootView = inflater!!.inflate(R.layout.fragment_location_selection, container, false)

            inflaterDialog = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            pbLoading = rootView.findViewById(R.id.pbLoading) as ProgressBar

            dateReccurence = null

            avertList = activity.intent.extras!!.getParcelableArrayList("avertList")

            mMapView = rootView.findViewById(R.id.mapView) as MapView
            mMapView!!.onCreate(savedInstanceState)

            mGoogleApiClient = GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build()

            try {
                MapsInitializer.initialize(activity.applicationContext)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            mMapView!!.getMapAsync { mMap ->
                googleMap = mMap

                googleMap!!.setOnMapClickListener(GoogleMap.OnMapClickListener { latLng ->
                    val geocoder = Geocoder(activity)
                    val list: List<Address>
                    try {
                        list = geocoder.getFromLocation(latLng.latitude,
                                latLng.longitude, 1)
                    } catch (e: IOException) {
                        return@OnMapClickListener
                    }

                    address = list[0]
                    if (marker != null) {
                        marker!!.remove()
                    }

                    location = LatLng(latLng.latitude, latLng.longitude)

                    val properAddress = String.format("%s, %s",
                            if (address!!.maxAddressLineIndex > 0) address!!.getAddressLine(0) else "",
                            address!!.locality)

                    val options = MarkerOptions()
                            .title(properAddress)
                            .position(location!!)

                    marker = googleMap!!.addMarker(options)
                    //btValider.setEnabled(true);
                })
            }

            btValider = rootView.findViewById(R.id.btValiderDestination) as Button
            btValider!!.setOnClickListener {
                if (marker != null) {
                    showValidLocationDialog(marker!!.position, address)
                } else {
                    Snackbar.make(view!!, getString(R.string.noMarkerPlaced), Snackbar.LENGTH_LONG).show()
                }
            }
            //Tant qu'on a pas de marker, on n'active pas le bouton
            //btValider.setEnabled(false);

            val btFavLocations = rootView.findViewById(R.id.btFavoriteLocations) as Button
            btFavLocations.setOnClickListener {
                try {
                    lds!!.open()
                    val listLocations: List<com.dailyvery.apps.imhome.Data.Location>?
                    listLocations = lds!!.allLocations
                    lds!!.close()
                    if (listLocations != null && listLocations.size > 0) {
                        displayFavorites(listLocations)
                    } else {
                        Snackbar.make(view!!, R.string.tvNoFavLocation, Snackbar.LENGTH_LONG).show()
                    }
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }

            geo_autocomplete_clear = rootView.findViewById(R.id.geo_autocomplete_clear) as ImageView

            geo_autocomplete = rootView.findViewById(R.id.geo_autocomplete) as DelayAutoCompleteTextView
            geo_autocomplete!!.threshold = THRESHOLD
            geo_autocomplete!!.setAdapter(GeoAutoCompleteAdapter(context)) // 'this' is Activity instance

            geo_autocomplete!!.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, id ->
                val result = adapterView.getItemAtPosition(position) as GeoSearchResult
                geo_autocomplete!!.setText(result.address)

                val geo = Geocoder(context)
                var gotAddresses: List<Address>? = null
                try {

                    gotAddresses = geo.getFromLocationName(result.address, 1)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                address = gotAddresses!![0]

                val properAddress = String.format("%s, %s",
                        if (address!!.maxAddressLineIndex > 0) address!!.getAddressLine(0) else "",
                        address!!.locality)

                val location = LatLng(address!!.latitude, address!!.longitude)

                setNewMarker(properAddress, location)

                val inputManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                inputManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS)
            }

            geo_autocomplete!!.addTextChangedListener(object : TextWatcher {

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun afterTextChanged(s: Editable) {
                    if (s.length > 0) {
                        geo_autocomplete_clear!!.visibility = View.VISIBLE
                    } else {
                        geo_autocomplete_clear!!.visibility = View.INVISIBLE
                    }
                }
            })

            geo_autocomplete_clear!!.setOnClickListener {
                // TODO Auto-generated method stub
                geo_autocomplete!!.setText("")
            }
        }

        return rootView
    }

    private fun setNewMarker(properAddress: String, location: LatLng) {
        if (marker != null) {
            marker!!.remove()
        }

        val options = MarkerOptions()
                .title(properAddress)
                .position(location)

        marker = googleMap!!.addMarker(options)

        val cameraPosition = CameraPosition.Builder()
                .target(location) // Center Set
                .zoom(18.0f)                // Zoom
                .build()                   // Creates a CameraPosition from the builder
        googleMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        btValider!!.isEnabled = true
    }

    override fun onResume() {
        super.onResume()
        //setUpMapIfNeeded();
        if (ContextCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && mGoogleApiClient != null) {
            mGoogleApiClient!!.connect()
        } else if (ContextCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            activity.recreate()
        }
    }

    override fun onPause() {
        super.onPause()
        if (ContextCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (mGoogleApiClient != null && mGoogleApiClient!!.isConnected) {
                mGoogleApiClient!!.disconnect()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (ContextCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && mMapView != null) {
            mMapView!!.onDestroy()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        if (ContextCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && mMapView != null) {
            mMapView!!.onLowMemory()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

        } else {
            //TODO Empecher la map de faire quelque chose pour eviter les bugs
        }
    }

    override fun onConnectionSuspended(i: Int) {

    }

    override fun onConnected(bundle: Bundle?) {
        statusCheck()
    }

    private fun getLocation() {
        if (context != null && ContextCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Nothing
        } else {
            val location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
            if (location == null) {
                pbLoading!!.visibility = View.VISIBLE
                mMapView!!.visibility = View.GONE
            } else {
                handleNewLocation(location)
            }
        }
    }

    private fun displayFavorites(listLocations: List<com.dailyvery.apps.imhome.Data.Location>) {
        val builderSingle = AlertDialog.Builder(activity)
        builderSingle.setTitle("Sélectionnez un lieu favori")

        val arrayAdapter = ArrayAdapter<String>(activity, android.R.layout.select_dialog_singlechoice)
        for (location in listLocations) {
            arrayAdapter.add(location.address)
        }

        builderSingle.setNegativeButton(getString(R.string.cancel)) { dialog, which -> dialog.dismiss() }

        builderSingle.setAdapter(arrayAdapter) { dialog, which ->
            val location = listLocations[which]
            val strName = location.address
            val latlng = LatLng(location.lat, location.long)
            setNewMarker(strName, latlng)
            dialog.dismiss()
        }
        builderSingle.show()
    }

    fun statusCheck() {
        val manager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        } else {
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (locationManager != null) {
                        getLocation()
                    }
                }

                override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
                    if (locationManager != null) {
                        getLocation()
                    }
                }

                override fun onProviderEnabled(provider: String) {
                    if (locationManager != null) {
                        getLocation()
                    }
                }

                override fun onProviderDisabled(provider: String) {
                    locationManager!!.removeUpdates(this)
                    try {
                        finalize()
                    } catch (t: Throwable) {
                        Toast.makeText(context, "TEST TEST", Toast.LENGTH_SHORT).show()
                    }

                }
            }
            locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (ContextCompat.checkSelfPermission(context,
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, listener)
                locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, listener)
                getLocation()
            }
        }
    }

    private fun buildAlertMessageNoGps() {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(getString(R.string.enable_location))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes)) { dialog, id -> startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
                .setNegativeButton(getString(R.string.no)) { dialog, id -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(activity, CONNECTION_FAILURE_RESOLUTION_REQUEST)
            } catch (e: IntentSender.SendIntentException) {
                e.printStackTrace()
            }

        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.errorCode)
        }
    }

    private fun handleNewLocation(location: Location) {
        if (ContextCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
        }

        pbLoading!!.visibility = View.GONE
        mMapView!!.visibility = View.VISIBLE

        // For showing a move to my location button
        googleMap!!.isMyLocationEnabled = true

        val myPosition = LatLng(location.latitude, location.longitude)

        if (firstLocation) {
            val cameraPosition = CameraPosition.Builder().target(myPosition).zoom(12f).build()
            googleMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            firstLocation = false
        }

        mMapView!!.onResume() // needed to get the map to display immediately
    }

    /**
     * Une fois qu'une location est valide on demande quel texte envoyer
     *
     * @param location
     * La location a detecter pour envoyer le message
     * @param address
     */
    fun showValidLocationDialog(location: LatLng, address: Address?) {
        var viewAlertDialog: View? = null
        viewAlertDialog = inflaterDialog!!.inflate(R.layout.alert_dialog_layout, null)
        //final CheckBox cbMessageReccurent = (CheckBox)viewAlertDialog.findViewById(R.id.cbMessageReccurent);
        val et = viewAlertDialog!!.findViewById(R.id.etMessageToSend) as EditText
        val cb = viewAlertDialog.findViewById(R.id.cbAddFavorite) as CheckBox
        val ads = AvertDataSource(activity)
        et.setText(getString(R.string.defaultMessage), TextView.BufferType.EDITABLE)

        val builder = AlertDialog.Builder(activity)
        builder.setMessage(getString(R.string.tvPleaseEnterMessageText))
                .setPositiveButton(getString(R.string.validate)) { dialog, id ->
                    try {
                        ads.open()
                        for (a in avertList!!) {
                            if (dateReccurence != null) {
                                a.addDate = dateReccurence
                            }

                            var properAddress = String.format("%s, %s",
                                    address!!.thoroughfare,
                                    address.locality)

                            if (cb.isChecked) {
                                lds!!.open()
                                val locToSave = com.dailyvery.apps.imhome.Data.Location()
                                locToSave.address = properAddress
                                locToSave.nick = ""
                                locToSave.lat = location.latitude
                                locToSave.long = location.longitude
                                lds!!.addLocation(locToSave)
                                lds!!.close()
                            }

                            if (properAddress.length > 25) {
                                properAddress = properAddress.substring(0, 25) + "..."
                            }

                            a.messageText = et.text.toString()
                            a.latitude = location.latitude
                            a.longitude = location.longitude
                            a.setFlagReccurence(false)
                            a.label = properAddress
                            //a.setFlagReccurence(cbMessageReccurent.isChecked());

                            ads.addAvert(a)

                            locationManager = null

                        }
                    } catch (e: SQLException) {
                        e.printStackTrace()
                    } finally {
                        ads.close()
                    }

                    context.startService(Intent(activity.applicationContext, MyService::class.java))

                    (activity as PlaceSelectionActivity).showAd()
                }.setNegativeButton(getString(R.string.cancel)

                ) { dialog, id ->
                    // User cancelled the dialog
                }.setView(viewAlertDialog)

        // Create the AlertDialog object and return it
        builder.create().show()
    }

    companion object {
        val TAG = PlaceSelectionActivity::class.java!!.getSimpleName()
        private val CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000
        private var inflaterDialog: LayoutInflater? = null
    }
}
