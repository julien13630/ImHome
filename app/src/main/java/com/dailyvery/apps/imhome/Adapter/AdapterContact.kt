package com.dailyvery.apps.imhome.Adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.dailyvery.apps.imhome.Data.Avert
import com.dailyvery.apps.imhome.Interface.BtnClickListener
import com.dailyvery.apps.imhome.R

import java.util.ArrayList

/**
 * Created by JulienSideness on 22/10/2015.
 */
class AdapterContact(activity: Activity, textViewResourceId: Int, _lAvert: ArrayList<Avert>, listenerDelete: BtnClickListener) : ArrayAdapter<Avert>(activity, textViewResourceId, _lAvert) {
    private var activity: Activity? = null
    private var mClickListenerDelete: BtnClickListener? = null
    private var lAvert: ArrayList<Avert>? = null

    init {
        try {
            this.activity = activity
            this.lAvert = _lAvert

            mClickListenerDelete = listenerDelete

            inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

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
        var display_number: TextView? = null

    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var vi = convertView
        val holder: ViewHolder
        try {
            if (convertView == null) {
                vi = inflater!!.inflate(R.layout.custom_contact_layout, null)
                holder = ViewHolder()

                holder.display_name = vi!!.findViewById<View>(R.id.listContactName) as TextView
                holder.display_number = vi.findViewById<View>(R.id.listContactNumber) as TextView


                vi.tag = holder
            } else {
                holder = vi!!.tag as ViewHolder
            }

            holder.display_name!!.text = lAvert!![position].contactName
            holder.display_number!!.text = lAvert!![position].contactNumber

            val btDelete = vi.findViewById<View>(R.id.btContactDeleteLayout) as RelativeLayout
            btDelete.tag = position
            btDelete.setOnClickListener { v -> mClickListenerDelete!!.onBtnClick(v.tag as Int) }

        } catch (e: Exception) {


        }

        return vi
    }

    companion object {
        private var inflater: LayoutInflater? = null
    }
}