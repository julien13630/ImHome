package com.dailyvery.apps.imhome.Adapter;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.dailyvery.apps.imhome.Data.Wifi;
import com.dailyvery.apps.imhome.Data.WifiDataSource;
import com.dailyvery.apps.imhome.R;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by JulienSideness on 22/10/2015.
 */
public class AdapterWifi extends ArrayAdapter<Wifi> {
    private Activity activity;
    private ArrayList<Wifi> lWifi;
    private int selectedIndex;
    private static LayoutInflater inflater = null;

    public AdapterWifi (Activity activity, int textViewResourceId,ArrayList<Wifi> _lWifi) {
        super(activity, textViewResourceId, _lWifi);
        try {
            this.activity = activity;
            this.lWifi = _lWifi;
            WifiDataSource wds = new WifiDataSource(activity);
            selectedIndex = -1;
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        } catch (Exception e) {
                e.printStackTrace();
        }
    }

    public int getCount() {
        return lWifi.size();
    }

    public int getSelectedPosition()
    {
        return  selectedIndex;
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
        public RadioButton display_checked;
        public LinearLayout display_layout;
        public LinearLayout display_favorite_layout;
        public ImageView display_favorite_iv;

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
                holder.display_checked = (RadioButton) vi.findViewById(R.id.rb_Wifi);
                holder.display_layout = (LinearLayout) vi.findViewById(R.id.listWifiLayout);
                holder.display_favorite_iv = (ImageView) vi.findViewById(R.id.iv_Favorite);
                holder.display_favorite_layout = (LinearLayout) vi.findViewById((R.id.ll_Favorite));
                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }

            holder.display_checked.setTag(position);
            holder.display_checked.setChecked(position == selectedIndex);

            holder.display_checked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        selectedIndex = (int)compoundButton.getTag();
                        notifyDataSetChanged();
                    }
                    else if((int) compoundButton.getTag() == selectedIndex)
                    {
                        compoundButton.setChecked(false);
                        selectedIndex = -1;
                    }
                }
            });

            holder.display_layout.setTag(holder);
            holder.display_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ViewHolder v =(ViewHolder)view.getTag();
                    v.display_checked.setChecked(true);

                }
            });

            if (lWifi.get(position).isFavorite())
            {
                holder.display_favorite_iv.setImageResource(R.drawable.ic_etoile_pleine);
            }
            else
            {
                holder.display_favorite_iv.setImageResource(R.drawable.ic_etoile_vide);
            }
            holder.display_favorite_layout.setTag(position);
            holder.display_favorite_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    lWifi.get((int)view.getTag()).setFavorite(!lWifi.get((int)view.getTag()).isFavorite());
                    WifiDataSource wds = new WifiDataSource(activity.getApplicationContext());

                    try {
                        wds.open();
                        wds.update(lWifi.get((int)view.getTag()));
                        wds.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    notifyDataSetChanged();

                }
            });

            //On vérifie que le SSID n'est pas égal au libelle
            //Si oui, on ne set que le SSID à l'affichage
            if (!lWifi.get(position).getSsid().equals(lWifi.get(position).getLabel())){
                holder.display_libelle.setText(lWifi.get(position).getLabel());
            }
            holder.display_ssid.setText(lWifi.get(position).getSsid());

        } catch (Exception e) {

           e.printStackTrace();

        }
        return vi;
    }
}