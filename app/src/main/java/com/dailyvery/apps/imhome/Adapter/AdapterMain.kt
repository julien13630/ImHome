package com.dailyvery.apps.imhome.Adapter

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

import com.dailyvery.apps.imhome.Data.Avert
import com.dailyvery.apps.imhome.Interface.BtnClickListener
import com.dailyvery.apps.imhome.R

import java.util.ArrayList

/**
 * Created by JulienSideness on 23/10/2015.
 */
class AdapterMain(activity: Activity, textViewResourceId: Int, _lAvert: ArrayList<Avert>, listenerDelete: BtnClickListener, listenerEdit: BtnClickListener) : ArrayAdapter<Avert>(activity, textViewResourceId, _lAvert) {
    private var activity: Activity? = null
    private var lAvert: ArrayList<Avert>? = null
    private var mClickListenerDelete: BtnClickListener? = null
    private var mClickListenerEdit: BtnClickListener? = null
    private val colors = arrayOf("#FFFFFF", "#F4F4F4")

    init {
        try {
            this.activity = activity
            this.lAvert = _lAvert

            inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

            mClickListenerDelete = listenerDelete
            mClickListenerEdit = listenerEdit

        } catch (e: Exception) {

        }

    }

    override fun getCount(): Int {
        return lAvert!!.size
    }

    fun getItem(position: Avert): Avert {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class ViewHolder {
        var display_name: TextView? = null
        var display_wifi: TextView? = null
        var im_gps_wifi: ImageView? = null

    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var vi = convertView
        val holder: ViewHolder
        try {
            if (convertView == null) {
                vi = inflater!!.inflate(R.layout.custom_main_layout, null)
                holder = ViewHolder()

                holder.display_name = vi!!.findViewById<View>(R.id.listMainName) as TextView
                holder.display_wifi = vi.findViewById<View>(R.id.listMainWifi) as TextView
                holder.im_gps_wifi = vi.findViewById<View>(R.id.imGpsWifi) as ImageView


                vi.tag = holder
            } else {
                holder = vi!!.tag as ViewHolder
            }
            if (lAvert!![position].ssid != null) {
                holder.im_gps_wifi!!.setImageResource(R.drawable.ic_location_wifi)
            } else {
                holder.im_gps_wifi!!.setImageResource(R.drawable.ic_location_gps)
            }

            holder.display_name!!.text = lAvert!![position].contactName
            holder.display_wifi!!.text = lAvert!![position].label

            val colorPos = position % colors.size
            vi.setBackgroundColor(Color.parseColor(colors[colorPos]))

            val btDelete = vi.findViewById<View>(R.id.imDeleteMessage) as RelativeLayout
            btDelete.tag = position
            btDelete.setOnClickListener { v -> mClickListenerDelete!!.onBtnClick(v.tag as Int) }

            val btEdit = vi.findViewById<View>(R.id.imEditMessage) as RelativeLayout
            btEdit.tag = position
            btEdit.setOnClickListener { v -> mClickListenerEdit!!.onBtnClick(v.tag as Int) }

        } catch (e: Exception) {


        }

        return vi
    }

    companion object {
        private var inflater: LayoutInflater? = null
    }
}
