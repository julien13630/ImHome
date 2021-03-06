package com.dailyvery.apps.imhome.Adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dailyvery.apps.imhome.Data.Avert;
import com.dailyvery.apps.imhome.Interface.BtnClickListener;
import com.dailyvery.apps.imhome.R;

import java.util.ArrayList;

/**
 * Created by JulienSideness on 23/10/2015.
 */
public class AdapterMain extends ArrayAdapter<Avert> {
    private Activity activity;
    private ArrayList<Avert> lAvert;
    private static LayoutInflater inflater = null;
    private BtnClickListener mClickListenerDelete = null;
    private BtnClickListener mClickListenerEdit = null;
    private String[] colors = new String[] { "#FFFFFF", "#F4F4F4" };

    public AdapterMain (Activity activity, int textViewResourceId,ArrayList<Avert> _lAvert, BtnClickListener listenerDelete, BtnClickListener listenerEdit) {
        super(activity, textViewResourceId, _lAvert);
        try {
            this.activity = activity;
            this.lAvert = _lAvert;

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            mClickListenerDelete = listenerDelete;
            mClickListenerEdit = listenerEdit;

        } catch (Exception e) {

        }
    }

    public int getCount() {
        return lAvert.size();
    }

    public Avert getItem(Avert position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder {
        public TextView display_name;
        public TextView display_wifi;
        public ImageView im_gps_wifi;

    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
        try {
            if (convertView == null) {
                vi = inflater.inflate(R.layout.custom_main_layout, null);
                holder = new ViewHolder();

                holder.display_name = (TextView) vi.findViewById(R.id.listMainName);
                holder.display_wifi = (TextView) vi.findViewById(R.id.listMainWifi);
                holder.im_gps_wifi = (ImageView) vi.findViewById(R.id.imGpsWifi);


                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }
            if(lAvert.get(position).getSsid() != null){
                holder.im_gps_wifi.setImageResource(R.drawable.ic_location_wifi);
            }else{
                holder.im_gps_wifi.setImageResource(R.drawable.ic_location_gps);
            }

            holder.display_name.setText(lAvert.get(position).getContactName());
            holder.display_wifi.setText(lAvert.get(position).getLabel());

            int colorPos = position % colors.length;
            vi.setBackgroundColor(Color.parseColor(colors[colorPos]));

            RelativeLayout btDelete = (RelativeLayout)vi.findViewById(R.id.imDeleteMessage);
            btDelete.setTag(position);
            btDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickListenerDelete.onBtnClick((Integer) v.getTag());
                }
            });

            RelativeLayout btEdit = (RelativeLayout)vi.findViewById(R.id.imEditMessage);
            btEdit.setTag(position);
            btEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickListenerEdit.onBtnClick((Integer) v.getTag());
                }
            });

        } catch (Exception e) {


        }
        return vi;
    }
}
