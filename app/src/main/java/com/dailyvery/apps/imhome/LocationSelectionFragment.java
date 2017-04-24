package com.dailyvery.apps.imhome;

import android.Manifest;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dailyvery.apps.imhome.Data.Avert;
import com.dailyvery.apps.imhome.Data.AvertDataSource;
import com.dailyvery.apps.imhome.SearchBar.DelayAutoCompleteTextView;
import com.dailyvery.apps.imhome.SearchBar.GeoAutoCompleteAdapter;
import com.dailyvery.apps.imhome.SearchBar.GeoSearchResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LocationSelectionFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private MapView mMapView;
    private LocationManager locationManager;
    private ProgressBar pbLoading;
    private GoogleMap googleMap;
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = PlaceSelectionActivity.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private Marker marker;
    private Address address;
    private LatLng location;
    private Button btValider;
    private ArrayList<Avert> avertList;
    private static LayoutInflater inflaterDialog = null;
    private TimePickerDialog.OnTimeSetListener timeSetListener = null;
    private Date dateReccurence;
    private boolean firstLocation;

    private Integer THRESHOLD = 2;
    private DelayAutoCompleteTextView geo_autocomplete;
    private ImageView geo_autocomplete_clear;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView;

        firstLocation = true;

        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);

            rootView = inflater.inflate(R.layout.fragment_location_denied, container, false);

            Button btActivateGps = (Button) rootView.findViewById(R.id.btActivateGps);
            btActivateGps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().recreate();
                }
            });
        }else{
            rootView = inflater.inflate(R.layout.fragment_location_selection, container, false);

            inflaterDialog = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            pbLoading = (ProgressBar) rootView.findViewById(R.id.pbLoading);

            dateReccurence = null;

            avertList = getActivity().getIntent().getExtras().getParcelableArrayList("avertList");

            mMapView = (MapView) rootView.findViewById(R.id.mapView);
            mMapView.onCreate(savedInstanceState);

            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            try {
                MapsInitializer.initialize(getActivity().getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }

            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap mMap) {
                    googleMap = mMap;

                    googleMap.setOnMapClickListener(new
                        GoogleMap.OnMapClickListener() {
                            @Override
                            public void onMapClick (LatLng latLng){
                                Geocoder geocoder =
                                        new Geocoder(getActivity());
                                List<Address> list;
                                try {
                                    list = geocoder.getFromLocation(latLng.latitude,
                                            latLng.longitude, 1);
                                } catch (IOException e) {
                                    return;
                                }
                                address = list.get(0);
                                if (marker != null) {
                                    marker.remove();
                                }

                                location = new LatLng(latLng.latitude, latLng.longitude);

                                String properAddress = String.format("%s, %s",
                                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                                        address.getLocality());

                                MarkerOptions options = new MarkerOptions()
                                        .title(properAddress)
                                        .position(location);

                                marker = googleMap.addMarker(options);
                                btValider.setEnabled(true);
                            }
                        });
                }
            });

            btValider = (Button)rootView.findViewById(R.id.btValiderDestination);
            btValider.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showValidLocationDialog(marker.getPosition(), address);
                }
            });
            //Tant qu'on a pas de marker, on n'active pas le bouton
            btValider.setEnabled(false);

            geo_autocomplete_clear = (ImageView) rootView.findViewById(R.id.geo_autocomplete_clear);

            geo_autocomplete = (DelayAutoCompleteTextView) rootView.findViewById(R.id.geo_autocomplete);
            geo_autocomplete.setThreshold(THRESHOLD);
            geo_autocomplete.setAdapter(new GeoAutoCompleteAdapter(getContext())); // 'this' is Activity instance

            geo_autocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    GeoSearchResult result = (GeoSearchResult) adapterView.getItemAtPosition(position);
                    geo_autocomplete.setText(result.getAddress());

                    Geocoder geo = new Geocoder(getContext());
                    List<Address> gotAddresses = null;
                    try {

                        gotAddresses = geo.getFromLocationName(result.getAddress(), 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (marker != null) {
                        marker.remove();
                    }

                    address = gotAddresses.get(0);

                    String properAddress = String.format("%s, %s",
                            address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                            address.getLocality());

                    LatLng location = new LatLng(address.getLatitude(), address.getLongitude());

                    MarkerOptions options = new MarkerOptions()
                            .title(properAddress)
                            .position(location);

                    marker = googleMap.addMarker(options);

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(location) // Center Set
                            .zoom(18.0f)                // Zoom
                            .build();                   // Creates a CameraPosition from the builder
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    InputMethodManager inputManager = (InputMethodManager)
                            getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

                    inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);

                    btValider.setEnabled(true);
                }
            });

            geo_autocomplete.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(s.length() > 0)
                    {
                        geo_autocomplete_clear.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        geo_autocomplete_clear.setVisibility(View.INVISIBLE);
                    }
                }
            });

            geo_autocomplete_clear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    geo_autocomplete.setText("");
                }
            });
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //setUpMapIfNeeded();
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }else if(ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            getActivity().recreate();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && mMapView != null) {
            mMapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && mMapView != null) {
            mMapView.onLowMemory();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

        }else{
            //TODO Empecher la map de faire quelque chose pour eviter les bugs
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        statusCheck();
    }

    private void getLocation(){
        if (getContext() != null && ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //Nothing
        }else {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (location == null) {
                pbLoading.setVisibility(View.VISIBLE);
                mMapView.setVisibility(View.GONE);
            } else {
                handleNewLocation(location);
            }
        }
    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }else{
            LocationListener listener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if(locationManager != null){
                        getLocation();
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    if(locationManager != null){
                        getLocation();
                    }
                }

                @Override
                public void onProviderEnabled(String provider) {
                    if(locationManager != null){
                        getLocation();
                    }
                }

                @Override
                public void onProviderDisabled(String provider) {
                    locationManager.removeUpdates(this);
                    try{
                        finalize();
                    }catch (Throwable t){
                        Toast.makeText(getContext(), "TEST TEST", Toast.LENGTH_SHORT).show();
                    }
                }
            };
            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
                getLocation();
            }
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getString(R.string.enable_location))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    private void handleNewLocation(Location location) {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        pbLoading.setVisibility(View.GONE);
        mMapView.setVisibility(View.VISIBLE);

        // For showing a move to my location button
        googleMap.setMyLocationEnabled(true);

        LatLng myPosition = new LatLng(location.getLatitude(), location.getLongitude());

        if(firstLocation){
            CameraPosition cameraPosition = new CameraPosition.Builder().target(myPosition).zoom(12).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            firstLocation = false;
        }

        mMapView.onResume(); // needed to get the map to display immediately
    }

    /**
     * Une fois qu'une location est valide on demande quel texte envoyer
     *
     * @param location
     *            La location a detecter pour envoyer le message
     * @param address
     */
    public void showValidLocationDialog(final LatLng location, final Address address)
    {
        View viewAlertDialog = null;
        viewAlertDialog = inflaterDialog.inflate(R.layout.alert_dialog_layout, null);
        //final CheckBox cbMessageReccurent = (CheckBox)viewAlertDialog.findViewById(R.id.cbMessageReccurent);
        final EditText et = (EditText) viewAlertDialog.findViewById(R.id.etMessageToSend);
        final AvertDataSource ads = new AvertDataSource(getActivity());
        et.setText(getString(R.string.defaultMessage), TextView.BufferType.EDITABLE);
        /*final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, minute);
                cal.add(Calendar.DAY_OF_YEAR, - 1);
                dateReccurence = cal.getTime();

                cbMessageReccurent.setText(getString(R.string.cbRecurrenceSet) + cal.get(Calendar.HOUR_OF_DAY) +
                                    "h" + cal.get(Calendar.MINUTE) + "min");
            }
        };

        final TimePickerDialog tpd = new TimePickerDialog(getActivity(), timeSetListener,
                hour, minute, DateFormat.is24HourFormat(getActivity()));
        tpd.setCancelable(false);
        tpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                cbMessageReccurent.setChecked(false);
                cbMessageReccurent.setText(getString(R.string.cbRecurrence));
            }
        });

        cbMessageReccurent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(checked){
                    tpd.show();
                }else {
                    cbMessageReccurent.setText(getString(R.string.cbRecurrence));
                }
            }
        });

        //On limite le text a 160 caractères
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(160);
        et.setFilters(filterArray);

        cbMessageReccurent.setText(getString(R.string.cbRecurrence));
        cbMessageReccurent.setChecked(false);*/

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.tvPleaseEnterMessageText))
                .setPositiveButton(getString(R.string.validate), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                try {
                                    ads.open();
                                    for (Avert a : avertList) {
                                        if (dateReccurence != null){
                                            a.setAddDate(dateReccurence);
                                        }

                                        String properAddress = String.format("%s, %s",
                                                address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                                                address.getLocality());
                                        properAddress = properAddress.substring(0, 25) + "...";

                                        a.setMessageText(et.getText().toString());
                                        a.setLatitude(location.latitude);
                                        a.setLongitude(location.longitude);
                                        a.setFlagReccurence(false);
                                        a.setLabel(properAddress);
                                        //a.setFlagReccurence(cbMessageReccurent.isChecked());

                                        ads.addAvert(a);

                                        locationManager = null;

                                    }
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                } finally {
                                    ads.close();
                                }

                                getContext().startService(new Intent(getActivity().getApplicationContext(), MyService.class));

                                ((PlaceSelectionActivity)getActivity()).showAd();

                            }
                        }

                ).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                }

        ).setView(viewAlertDialog);

        // Create the AlertDialog object and return it
        builder.create().

        show();
    }
}
