package com.dailyvery.apps.imhome.Adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
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
    private BtnClickListener mClickListener = null;
    private String[] colors = new String[] { "#FFFFFF", "#F4F4F4" };

    public AdapterMain (Activity activity, int textViewResourceId,ArrayList<Avert> _lAvert, BtnClickListener listener) {
        super(activity, textViewResourceId, _lAvert);
        try {
            this.activity = activity;
            this.lAvert = _lAvert;

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            mClickListener = listener;

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


                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }

            holder.display_name.setText(lAvert.get(position).getContactName());
            holder.display_wifi.setText(lAvert.get(position).getLabel());

            int colorPos = position % colors.length;
            vi.setBackgroundColor(Color.parseColor(colors[colorPos]));

            ImageButton btDelete = (ImageButton)vi.findViewById(R.id.imDeleteMessage);
            btDelete.setTag(position);
            btDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickListener.onBtnClick((Integer) v.getTag());
                }
            });

        } catch (Exception e) {


        }
        return vi;
    }
}
