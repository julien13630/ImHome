package com.dailyvery.apps.imhome;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.dailyvery.apps.imhome.Adapter.AdapterWifi;
import com.dailyvery.apps.imhome.Data.Avert;
import com.dailyvery.apps.imhome.Data.AvertDataSource;
import com.dailyvery.apps.imhome.Data.Wifi;
import com.dailyvery.apps.imhome.Data.WifiDataSource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WifiSelectionFragment extends Fragment {

    public WifiManager wifiManager;
    List<WifiConfiguration> listAndroidWifi;
    ArrayList<Wifi> arrayListWifi;
    private List<Wifi> wifiList;
    private ArrayList<Avert> avertList;
    private ArrayList<Wifi> arrayListWifiRegistered;
    ListView lvWifiRegistered;
    ListView lvWifi;
    private float historicX = Float.NaN, historicY = Float.NaN;
    private static final int DELTA = 50;

    private AdapterView.OnItemClickListener listListenerFavorite = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showValidWifiDialog(wifiList.get(position));


        }
    };

    private AdapterView.OnItemClickListener listListenerRegistered = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            showValidWifiDialog(arrayListWifiRegistered.get(position));


        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_wifi_selection, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        avertList = getActivity().getIntent().getExtras().getParcelableArrayList("avertList");

        WifiDataSource wds = new WifiDataSource(getActivity());
        try {
            wds.open();
            wifiList = wds.getAllWifi();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            wds.close();
        }

        arrayListWifi = (ArrayList<Wifi>)wifiList;
        arrayListWifiRegistered = new ArrayList<Wifi>();

        //Ajouter les wifis système
        this.wifiManager = (WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()){
            //wifiManager.setWifiEnabled(true);
            showDialogWifiChoice("Veuillez activer le wifi", this);
        }else {
            initListsWifi(view);
        }
    }

    /**
     * Initialise la liste des wifi a afficher
     */
    private void initListsWifi(View view) {
        listAndroidWifi = wifiManager.getConfiguredNetworks();
        addWifiToWifiRegistered();

        AdapterWifi adapter = new AdapterWifi(getActivity(), 0, arrayListWifi);
        lvWifi = (ListView)view.findViewById(R.id.listFavorite);
        lvWifi.setAdapter(adapter);

        AdapterWifi adapterRegistered = new AdapterWifi(getActivity(), 0, arrayListWifiRegistered);
        lvWifiRegistered = (ListView)view.findViewById(R.id.listRegistered);
        lvWifiRegistered.setAdapter(adapterRegistered);

        lvWifi.setOnItemClickListener(listListenerFavorite);
        lvWifiRegistered.setOnItemClickListener(listListenerRegistered);

        /*lvWifi.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return detectSwipeDirection(event);
            }
        });

        lvWifiRegistered.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return detectSwipeDirection(event);
            }
        });*/
    }


    private void addWifiToWifiRegistered() {
        if (listAndroidWifi == null | arrayListWifi == null )
            return;
        for(int i = 0 ; i < listAndroidWifi.size() ; i++){
            Wifi temp = new Wifi();
            //we need the substring to remove the double quotes from SSIDs
            temp.setSsid(listAndroidWifi.get(i).SSID.substring(1, listAndroidWifi.get(i).SSID.length() - 1));
            temp.setHashcode(-1);
            temp.setLabel(listAndroidWifi.get(i).SSID.substring(1, listAndroidWifi.get(i).SSID.length() - 1));
            boolean exists = false;
            for(int j = 0 ; j < arrayListWifi.size() ; j++){
                if(arrayListWifi.get(j).getSsid().compareTo(temp.getSsid()) == 0){
                    exists = true;
                    j = arrayListWifi.size();
                }
            }
            if(!exists){
                arrayListWifiRegistered.add(temp);
            }
        }
    }

    /**
     * Detecte dans quelle direction l'utilisateur swipe l'ecran
     *
     * @param event
     *            Le MotionEvent levee lorsque l'utilisateur swipe l'ecran
     * @return boolean
     *            return true si on a bien detecte un mouvement a droite ou gauche
     */
    /*private boolean detectSwipeDirection(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                historicX = event.getX();
                historicY = event.getY();
                break;

            case MotionEvent.ACTION_UP:
                if (event.getX() - historicX < -DELTA) {
                    Toast.makeText(getApplicationContext(), "Left", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (event.getX() - historicX > DELTA) {
                    Toast.makeText(getApplicationContext(), "Right", Toast.LENGTH_SHORT).show();
                    return true;
                }
                break;
            default:
                return false;
        }
        return false;
    }*/

    /**
     * Affiche title a l'utilisateur lui donnant le choix d'activer le wifi
     * @param title Affiche dans le popup
     * @param wifi
     */
    private void showDialogWifiChoice(String title, final WifiSelectionFragment wifi) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
        builderSingle.setIcon(R.drawable.ic_add_white_24dp);
        builderSingle.setTitle(title);

        builderSingle.setNegativeButton(
                "Annuler",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.setPositiveButton(
                "Valider",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        wifi.wifiManager.setWifiEnabled(true);
                        initListsWifi(getView());
                        dialog.dismiss();
                    }
                });
        builderSingle.show();
    }

    /**
     * Une fois qu'un wifi valide est valide on demande quel texte envoyer
     *
     * @param wifi
     *            Le wifi a detecter pour envoyer le message
     */
    public void showValidWifiDialog(final Wifi wifi)
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
                                a.setSsid(wifi.getSsid());
                                a.setLabel(wifi.getLabel());
                                a.setHashcode(wifi.getHashcode());

                                ads.addAvert(a);

                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            ads.close();
                        }

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