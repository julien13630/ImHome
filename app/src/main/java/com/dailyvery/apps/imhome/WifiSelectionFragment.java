package com.dailyvery.apps.imhome;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.dailyvery.apps.imhome.Adapter.AdapterWifi;
import com.dailyvery.apps.imhome.Data.Avert;
import com.dailyvery.apps.imhome.Data.AvertDataSource;
import com.dailyvery.apps.imhome.Data.Wifi;
import com.dailyvery.apps.imhome.Data.WifiDataSource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
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
    private Button bValidWifi;
    private static LayoutInflater inflaterDialog = null;
    private TimePickerDialog.OnTimeSetListener timeSetListener = null;
    private Date dateReccurence;

    private AdapterView.OnItemClickListener listListenerRegistered = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            showValidWifiDialog(arrayListWifiRegistered.get(position));
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Ajouter les wifis système
        this.wifiManager = (WifiManager)getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()){
            return inflater.inflate(R.layout.content_wifi_selection, container, false);
        }else{
            return inflater.inflate(R.layout.content_wifi_selection_disabled, container, false);
        }

    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            if (!wifiManager.isWifiEnabled()){
                showDialogWifiChoice(getString(R.string.tvPleaseEnableWifi), this);
            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inflaterDialog = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (wifiManager.isWifiEnabled()){
            initListsWifi(view);
        }else{
            Button btActivateWifi = (Button) view.findViewById(R.id.btActivateWifi);
            btActivateWifi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    wifiManager.setWifiEnabled(true);
                    getActivity().recreate();
                }
            });
        }
    }

    /**
     * Initialise la liste des wifi a afficher
     */
    private void initListsWifi(View view) {
        dateReccurence = null;

        avertList = getActivity().getIntent().getExtras().getParcelableArrayList("avertList");

        WifiDataSource wds = new WifiDataSource(getActivity().getApplicationContext());
        try {
            wds.open();
            wifiList = wds.getAllWifi();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            wds.close();
        }

        arrayListWifi = (ArrayList<Wifi>)wifiList;
        arrayListWifiRegistered = new ArrayList<>();

        listAndroidWifi = wifiManager.getConfiguredNetworks();
        addWifiToWifiRegistered();


        lvWifiRegistered = (ListView)view.findViewById(R.id.listRegistered);
        AdapterWifi adapterRegistered = new AdapterWifi(getActivity(), 0, arrayListWifiRegistered,lvWifiRegistered );

        lvWifiRegistered.setAdapter(adapterRegistered);

        //lvWifiRegistered.setOnItemClickListener(listListenerRegistered);

        bValidWifi = (Button) view.findViewById(R.id.BValidWifi);
        bValidWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = ((AdapterWifi)lvWifiRegistered.getAdapter()).getSelectedPosition();
                if (position < 0)
                {
                    Snackbar.make(view, getString(R.string.noWifiSelected),Snackbar.LENGTH_LONG).show();
                }
                else
                {
                    showValidWifiDialog(arrayListWifiRegistered.get(position));
                }

            }
        });
    }


    private void addWifiToWifiRegistered() {
        try {


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
                    arrayListWifiRegistered.add(arrayListWifi.get(j));
                    j = arrayListWifi.size();
                }
            }
            if(!exists){
                temp.setFavorite(false);
                arrayListWifiRegistered.add(temp);
            }
        }

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

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
                getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.setPositiveButton(
                getString(R.string.validate),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        wifi.wifiManager.setWifiEnabled(true);
                        dialog.dismiss();
                        getActivity().recreate();
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
        View viewAlertDialog = null;
        viewAlertDialog = inflaterDialog.inflate(R.layout.alert_dialog_layout, null);
        final EditText et = (EditText) viewAlertDialog.findViewById(R.id.etMessageToSend);
        final AvertDataSource ads = new AvertDataSource(getActivity());
        et.setText(getString(R.string.defaultMessage), TextView.BufferType.EDITABLE);
        /*final CheckBox cbMessageReccurent = (CheckBox)viewAlertDialog.findViewById(R.id.cbMessageReccurent);

        final Calendar c = Calendar.getInstance();
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
                }else{
                    cbMessageReccurent.setText(getString(R.string.cbRecurrence));
                }
            }
        });*/

        //On limite le text a 160 caractères
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(160);
        et.setFilters(filterArray);

        //cbMessageReccurent.setText(getString(R.string.cbRecurrence));
        //cbMessageReccurent.setChecked(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.tvPleaseEnterMessageText))
                .setPositiveButton(getString(R.string.validate), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            ads.open();
                            for (Avert a : avertList) {
                                if(dateReccurence != null){
                                    a.setAddDate(dateReccurence);
                                }
                                a.setMessageText(et.getText().toString());
                                a.setSsid(wifi.getSsid());
                                a.setLabel(wifi.getLabel());
                                a.setHashcode(wifi.getHashcode());
                                a.setFlagReccurence(false);
                                //a.setFlagReccurence(cbMessageReccurent.isChecked());

                                ads.addAvert(a);

                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            ads.close();
                        }

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
