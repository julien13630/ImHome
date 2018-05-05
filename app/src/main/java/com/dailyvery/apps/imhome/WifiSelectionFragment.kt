package com.dailyvery.apps.imhome

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.text.InputFilter
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast

import com.dailyvery.apps.imhome.Adapter.AdapterWifi
import com.dailyvery.apps.imhome.Data.Avert
import com.dailyvery.apps.imhome.Data.AvertDataSource
import com.dailyvery.apps.imhome.Data.Wifi
import com.dailyvery.apps.imhome.Data.WifiDataSource

import java.sql.SQLException
import java.util.ArrayList
import java.util.Calendar
import java.util.Collections
import java.util.Comparator
import java.util.Date

class WifiSelectionFragment : Fragment() {

    var wifiManager: WifiManager
    internal var listAndroidWifi: List<WifiConfiguration>? = null
    internal var arrayListWifi: ArrayList<Wifi>? = null
    private var wifiList: List<Wifi>? = null
    private var avertList: ArrayList<Avert>? = null
    private var arrayListWifiRegistered: ArrayList<Wifi>? = null
    internal var lvWifiRegistered: ListView
    private var bValidWifi: Button? = null
    private val timeSetListener: TimePickerDialog.OnTimeSetListener? = null
    private var dateReccurence: Date? = null

    private val listListenerRegistered = AdapterView.OnItemClickListener { parent, view, position, id -> showValidWifiDialog(arrayListWifiRegistered!![position]) }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //Ajouter les wifis système
        this.wifiManager = activity.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return if (wifiManager.isWifiEnabled) {
            inflater!!.inflate(R.layout.content_wifi_selection, container, false)
        } else {
            inflater!!.inflate(R.layout.content_wifi_selection_disabled, container, false)
        }

    }

    override fun setMenuVisibility(visible: Boolean) {
        super.setMenuVisibility(visible)
        if (visible) {
            if (!wifiManager.isWifiEnabled) {
                showDialogWifiChoice(getString(R.string.tvPleaseEnableWifi), this)
            }
        }
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inflaterDialog = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        if (wifiManager.isWifiEnabled) {
            initListsWifi(view)
        } else {
            val btActivateWifi = view!!.findViewById<View>(R.id.btActivateWifi) as Button
            btActivateWifi.setOnClickListener {
                wifiManager.isWifiEnabled = true
                activity.recreate()
            }
        }
    }

    /**
     * Initialise la liste des wifi a afficher
     */
    private fun initListsWifi(view: View?) {
        dateReccurence = null

        avertList = activity.intent.extras!!.getParcelableArrayList("avertList")

        val wds = WifiDataSource(activity.applicationContext)
        try {
            wds.open()
            wifiList = wds.allWifi

        } catch (e: SQLException) {
            e.printStackTrace()
        } finally {
            wds.close()
        }

        arrayListWifi = wifiList as ArrayList<Wifi>?
        arrayListWifiRegistered = ArrayList()

        listAndroidWifi = wifiManager.configuredNetworks
        addWifiToWifiRegistered()


        lvWifiRegistered = view!!.findViewById<View>(R.id.listRegistered) as ListView
        val adapterRegistered = AdapterWifi(activity, 0, arrayListWifiRegistered, lvWifiRegistered)

        lvWifiRegistered.adapter = adapterRegistered

        //lvWifiRegistered.setOnItemClickListener(listListenerRegistered);

        bValidWifi = view.findViewById<View>(R.id.BValidWifi) as Button
        bValidWifi!!.setOnClickListener { view ->
            val position = (lvWifiRegistered.adapter as AdapterWifi).selectedPosition
            if (position < 0) {
                Snackbar.make(view, getString(R.string.noWifiSelected), Snackbar.LENGTH_LONG).show()
            } else {
                showValidWifiDialog(arrayListWifiRegistered!![position])
            }
        }
    }


    private fun addWifiToWifiRegistered() {
        try {


            if ((listAndroidWifi == null) or (arrayListWifi == null))
                return
            for (i in listAndroidWifi!!.indices) {
                val temp = Wifi()
                //we need the substring to remove the double quotes from SSIDs
                temp.ssid = listAndroidWifi!![i].SSID.substring(1, listAndroidWifi!![i].SSID.length - 1)
                temp.hashcode = -1
                temp.label = listAndroidWifi!![i].SSID.substring(1, listAndroidWifi!![i].SSID.length - 1)
                var exists = false
                var j = 0
                while (j < arrayListWifi!!.size) {
                    if (arrayListWifi!![j].ssid.compareTo(temp.ssid) == 0) {
                        exists = true
                        arrayListWifiRegistered!!.add(arrayListWifi!![j])
                        j = arrayListWifi!!.size
                    }
                    j++
                }
                if (!exists) {
                    temp.isFavorite = false
                    arrayListWifiRegistered!!.add(temp)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * Affiche title a l'utilisateur lui donnant le choix d'activer le wifi
     * @param title Affiche dans le popup
     * @param wifi
     */
    private fun showDialogWifiChoice(title: String, wifi: WifiSelectionFragment) {
        val builderSingle = AlertDialog.Builder(activity)
        builderSingle.setIcon(R.drawable.ic_add_white_24dp)
        builderSingle.setTitle(title)

        builderSingle.setNegativeButton(
                getString(R.string.cancel)
        ) { dialog, which -> dialog.dismiss() }

        builderSingle.setPositiveButton(
                getString(R.string.validate)
        ) { dialog, which ->
            wifi.wifiManager.isWifiEnabled = true
            dialog.dismiss()
            activity.recreate()
        }
        builderSingle.show()
    }

    /**
     * Une fois qu'un wifi valide est valide on demande quel texte envoyer
     *
     * @param wifi
     * Le wifi a detecter pour envoyer le message
     */
    fun showValidWifiDialog(wifi: Wifi) {
        var viewAlertDialog: View? = null
        viewAlertDialog = inflaterDialog!!.inflate(R.layout.alert_dialog_layout, null)
        val et = viewAlertDialog!!.findViewById<View>(R.id.etMessageToSend) as EditText
        val ads = AvertDataSource(activity)
        et.setText(getString(R.string.defaultMessage), TextView.BufferType.EDITABLE)
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
        val filterArray = arrayOfNulls<InputFilter>(1)
        filterArray[0] = InputFilter.LengthFilter(160)
        et.filters = filterArray

        //cbMessageReccurent.setText(getString(R.string.cbRecurrence));
        //cbMessageReccurent.setChecked(false);

        val builder = AlertDialog.Builder(activity)
        builder.setMessage(getString(R.string.tvPleaseEnterMessageText))
                .setPositiveButton(getString(R.string.validate)

                ) { dialog, id ->
                    try {
                        ads.open()
                        for (a in avertList!!) {
                            if (dateReccurence != null) {
                                a.addDate = dateReccurence
                            }
                            a.messageText = et.text.toString()
                            a.ssid = wifi.ssid
                            a.label = wifi.label
                            a.hashcode = wifi.hashcode
                            a.setFlagReccurence(false)
                            //a.setFlagReccurence(cbMessageReccurent.isChecked());

                            ads.addAvert(a)

                        }
                    } catch (e: SQLException) {
                        e.printStackTrace()
                    } finally {
                        ads.close()
                    }

                    (activity as PlaceSelectionActivity).showAd()
                }.setNegativeButton(getString(R.string.cancel)

                ) { dialog, id ->
                    // User cancelled the dialog
                }.setView(viewAlertDialog)

        // Create the AlertDialog object and return it
        builder.create().show()
    }

    companion object {
        private var inflaterDialog: LayoutInflater? = null
    }
}
