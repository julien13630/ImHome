package com.dailyvery.apps.imhome.Adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast

import com.dailyvery.apps.imhome.Data.Avert
import com.dailyvery.apps.imhome.Data.AvertDataSource
import com.dailyvery.apps.imhome.Data.Wifi
import com.dailyvery.apps.imhome.Data.WifiDataSource
import com.dailyvery.apps.imhome.MyService
import com.dailyvery.apps.imhome.PlaceSelectionActivity
import com.dailyvery.apps.imhome.R

import java.sql.SQLException
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.LinkedHashMap

/**
 * Created by JulienSideness on 22/10/2015.
 */
class AdapterWifi(activity: Activity, textViewResourceId: Int, _lWifi: ArrayList<Wifi>, _listView: ListView) : ArrayAdapter<Wifi>(activity, textViewResourceId, _lWifi) {
    private var activity: Activity? = null
    private var lWifi: ArrayList<Wifi>? = null
    var selectedPosition: Int = 0
        private set
    private var myListView: ListView? = null

    init {
        try {
            this.activity = activity
            this.lWifi = _lWifi
            this.myListView = _listView
            sortWifi()
            val wds = WifiDataSource(activity)
            selectedPosition = -1
            inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun sortWifi() {
        Collections.sort(lWifi!!, Comparator { p1, p2 ->
            val b1 = p1.isFavorite
            val b2 = p2.isFavorite
            if (b1 && !b2) {
                return@Comparator -1
            }
            if (!b1 && b2) {
                1
            } else p1.ssid!!.compareTo(p2.ssid!!)
        })
    }

    override fun getCount(): Int {
        return lWifi!!.size
    }

    fun getItem(position: Wifi): Wifi {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    class ViewHolder {
        var display_ssid: TextView? = null
        var display_libelle: TextView? = null
        var display_checked: RadioButton? = null
        var display_layout: LinearLayout? = null
        var display_favorite_layout: LinearLayout? = null
        var display_favorite_iv: ImageView? = null

    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var vi = convertView
        val holder: ViewHolder
        try {
            if (convertView == null) {
                vi = inflater!!.inflate(R.layout.custom_wifi_layout, null)
                holder = ViewHolder()

                holder.display_ssid = vi!!.findViewById<View>(R.id.listWifiSSID) as TextView
                holder.display_libelle = vi.findViewById<View>(R.id.listWifiLibelle) as TextView
                holder.display_checked = vi.findViewById<View>(R.id.rb_Wifi) as RadioButton
                holder.display_layout = vi.findViewById<View>(R.id.listWifiLayout) as LinearLayout
                holder.display_favorite_iv = vi.findViewById<View>(R.id.iv_Favorite) as ImageView
                holder.display_favorite_layout = vi.findViewById<View>(R.id.ll_Favorite) as LinearLayout
                vi.tag = holder
            } else {
                holder = vi!!.tag as ViewHolder
            }

            holder.display_checked!!.tag = position
            holder.display_checked!!.isChecked = position == selectedPosition

            holder.display_checked!!.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    selectedPosition = compoundButton.tag as Int
                    notifyDataSetChanged()
                } else if (compoundButton.tag as Int == selectedPosition) {
                    compoundButton.isChecked = false
                    selectedPosition = -1
                }
            }

            holder.display_layout!!.tag = holder
            holder.display_layout!!.setOnClickListener { view ->
                val v = view.tag as ViewHolder
                v.display_checked!!.isChecked = true
            }

            if (lWifi!![position].isFavorite) {
                holder.display_favorite_iv!!.setImageResource(R.drawable.ic_etoile_pleine)
            } else {
                holder.display_favorite_iv!!.setImageResource(R.drawable.ic_etoile_vide)
            }
            holder.display_favorite_layout!!.tag = position
            holder.display_favorite_layout!!.setOnClickListener { view ->
                val w = lWifi!![view.tag as Int]

                if (w.isFavorite) {
                    w.isFavorite = false
                    val wds = WifiDataSource(activity!!.applicationContext)
                    w.label = w.ssid
                    try {
                        wds.open()
                        wds.update(w)
                        wds.close()
                    } catch (e: SQLException) {
                        e.printStackTrace()
                    }

                    sortWifi()
                    notifyDataSetChanged()
                } else {
                    askForWifiNickName(lWifi!![view.tag as Int])
                }
            }

            //On vérifie que le SSID n'est pas égal au libelle
            //Si oui, on ne set que le SSID à l'affichage
            if (lWifi!![position].ssid != lWifi!![position].label) {
                holder.display_ssid!!.text = lWifi!![position].ssid
                holder.display_ssid!!.visibility = View.VISIBLE
            } else {
                holder.display_ssid!!.text = ""
                holder.display_ssid!!.visibility = View.GONE
            }
            holder.display_libelle!!.text = lWifi!![position].label

        } catch (e: Exception) {

            e.printStackTrace()

        }

        return vi
    }

    private fun askForWifiNickName(wifi: Wifi) {
        var viewAlertDialog: View? = null
        viewAlertDialog = inflater!!.inflate(R.layout.alert_dialog_layout, null)
        //final CheckBox cbMessageReccurent = (CheckBox)viewAlertDialog.findViewById(R.id.cbMessageReccurent);
        val et = viewAlertDialog!!.findViewById<View>(R.id.etMessageToSend) as EditText

        et.setText(wifi.label, TextView.BufferType.EDITABLE)
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(activity!!.getString(R.string.tvPleaseEnterNickname))
                .setPositiveButton(activity!!.getString(R.string.validate)
                ) { dialog, id ->
                    wifi.isFavorite = true
                    val wds = WifiDataSource(activity!!.applicationContext)
                    wifi.label = et.text.toString()
                    try {
                        wds.open()
                        wds.update(wifi)
                        wds.close()
                    } catch (e: SQLException) {
                        e.printStackTrace()
                    }

                    sortWifi()
                    notifyDataSetChanged()
                    selectedPosition = lWifi!!.indexOf(wifi)
                    myListView!!.smoothScrollToPosition(lWifi!!.indexOf(wifi))
                }.setNegativeButton(activity!!.getString(R.string.cancel)

                ) { dialog, id -> }.setView(viewAlertDialog)

        builder.create().show()
        et.setSelectAllOnFocus(true)
    }

    companion object {
        private var inflater: LayoutInflater? = null
    }
}