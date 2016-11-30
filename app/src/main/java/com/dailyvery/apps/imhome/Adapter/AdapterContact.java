package com.dailyvery.apps.imhome.Adapter;

import android.app.Activity;
import android.content.Context;
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
 * Created by JulienSideness on 22/10/2015.
 */
public class AdapterContact extends ArrayAdapter<Avert> {
    private Activity activity;
    private BtnClickListener mClickListenerDelete = null;
    private ArrayList<Avert> lAvert;
    private static LayoutInflater inflater = null;

    public AdapterContact(Activity activity, int textViewResourceId, ArrayList<Avert> _lAvert, BtnClickListener listenerDelete) {
        super(activity, textViewResourceId, _lAvert);
        try {
            this.activity = activity;
            this.lAvert = _lAvert;

            mClickListenerDelete = listenerDelete;

            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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
        public TextView display_number;

    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
        try {
            if (convertView == null) {
                vi = inflater.inflate(R.layout.custom_contact_layout, null);
                holder = new ViewHolder();

                holder.display_name = (TextView) vi.findViewById(R.id.listContactName);
                holder.display_number = (TextView) vi.findViewById(R.id.listContactNumber);


                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }

            holder.display_name.setText(lAvert.get(position).getContactName());
            holder.display_number.setText(lAvert.get(position).getContactNumber());

            ImageButton btDelete = (ImageButton)vi.findViewById(R.id.imDeleteContact);
            btDelete.setTag(position);
            btDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickListenerDelete.onBtnClick((Integer) v.getTag());
                }
            });

        } catch (Exception e) {


        }
        return vi;
    }
}