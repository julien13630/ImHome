package com.dailyvery.apps.imhome;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dailyvery.apps.imhome.Data.Avert;
import com.dailyvery.apps.imhome.Data.AvertDataSource;
import com.dailyvery.apps.imhome.Data.Wifi;
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

    MapView mMapView;
    private GoogleMap googleMap;
    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = PlaceSelectionActivity.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private Marker marker;
    private LatLng location;
    private Button btValider;
    private ArrayList<Avert> avertList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_location_selection, container, false);

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
                            Address address = list.get(0);
                            if (marker != null) {
                                marker.remove();
                            }

                            location = new LatLng(latLng.latitude, latLng.longitude);

                            MarkerOptions options = new MarkerOptions()
                                    .title(address.getLocality())
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
                showValidLocationDialog(marker.getPosition());
            }
        });
        //Tant qu'on a pas de marker, on n'active pas le bouton
        btValider.setEnabled(false);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //setUpMapIfNeeded();
        mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
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
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            // Blank for a moment...
        }
        else {
            handleNewLocation(location);
        };
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
        // For showing a move to my location button
        googleMap.setMyLocationEnabled(true);

        LatLng myPosition = new LatLng(location.getLatitude(), location.getLongitude());

        CameraPosition cameraPosition = new CameraPosition.Builder().target(myPosition).zoom(12).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        mMapView.onResume(); // needed to get the map to display immediately
    }

    /**
     * Une fois qu'une location est valide on demande quel texte envoyer
     *
     * @param location
     *            La location a detecter pour envoyer le message
     */
    public void showValidLocationDialog(final LatLng location)
    {
        final EditText et = new EditText(getActivity());
        final AvertDataSource ads = new AvertDataSource(getActivity());
        et.setText("Je suis arrivé :)", TextView.BufferType.EDITABLE);

        //On limite le text a 160 caractères
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(160);
        et.setFilters(filterArray);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Saisissez le texte à envoyer : ")
                .setPositiveButton("Valider", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                try {
                                    ads.open();
                                    for (Avert a : avertList) {
                                        a.setAddDate(new Date());
                                        a.setMessageText(et.getText().toString());
                                        a.setLatitude(location.latitude);
                                        a.setLongitude(location.longitude);
                                        ads.addAvert(a);

                                    }
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                } finally {
                                    ads.close();
                                }

                                getContext().startService(new Intent(getActivity().getApplicationContext(), MyService.class));

                                Intent intent=new Intent(getActivity(),MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);

                            }
                        }

                ).setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                }

        ).setView(et);

        // Create the AlertDialog object and return it
        builder.create().

        show();
    }
}
