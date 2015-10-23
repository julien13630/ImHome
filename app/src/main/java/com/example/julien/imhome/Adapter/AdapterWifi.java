package com.example.julien.imhome.Adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.example.julien.imhome.Data.Wifi;
import com.example.julien.imhome.R;

import java.util.ArrayList;

/**
 * Created by JulienSideness on 22/10/2015.
 */
public class AdapterWifi extends ArrayAdapter<Wifi> {
    private Activity activity;
    private ArrayList<Wifi> lWifi;
    private static LayoutInflater inflater = null;

    public AdapterWifi (Activity activity, int textViewResourceId,ArrayList<Wifi> _lWifi) {
        super(activity, textViewResourceId, _lWifi);
        try {
            this.activity = activity;
            this.lWifi = _lWifi;

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        } catch (Exception e) {

        }
    }

    public int getCount() {
        return lWifi.size();
    }

    public Wifi getItem(Wifi position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder {
        public TextView display_ssid;
        public TextView display_libelle;

    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
        try {
            if (convertView == null) {
                vi = inflater.inflate(R.layout.custom_wifi_layout, null);
                holder = new ViewHolder();

                holder.display_ssid = (TextView) vi.findViewById(R.id.listWifiSSID);
                holder.display_libelle = (TextView) vi.findViewById(R.id.listWifiLibelle);


                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }

            //On vérifie que le SSID n'est pas égal au libelle
            //Si oui, on ne set que le SSID à l'affichage
            if (!lWifi.get(position).getSsid().equals(lWifi.get(position).getLibelle())){
                holder.display_libelle.setText(lWifi.get(position).getLibelle());
            }
            holder.display_ssid.setText(lWifi.get(position).getSsid());



        } catch (Exception e) {


        }
        return vi;
    }
}