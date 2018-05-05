package com.dailyvery.apps.imhome

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

import com.dailyvery.apps.imhome.Adapter.AdapterContact
import com.dailyvery.apps.imhome.Adapter.AdapterMain
import com.dailyvery.apps.imhome.Data.Avert
import com.dailyvery.apps.imhome.Data.AvertDataSource
import com.dailyvery.apps.imhome.Interface.BtnClickListener

import java.util.ArrayList

class ContactActivity : AppCompatActivity() {

    // List d'Avert qui nous servira pour stocker les contact
    private val avertList = ArrayList<Avert>()
    private val tmpAvertList = ArrayList<Avert>()
    private var adapter: AdapterContact? = null
    private var fabOk: Button? = null
    private var lvContact: ListView? = null
    private var iv_NoRecipient: ImageView? = null
    internal var btnListenerDelete: BtnClickListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        title = getString(R.string.tvContactPrevenir)

        val fab = findViewById<View>(R.id.fab) as Button
        fab.setOnClickListener { onAddButtonClick() }

        fabOk = findViewById<View>(R.id.fabOk) as Button
        fabOk!!.setOnClickListener {
            //Contact activity
            val intent = Intent(this@ContactActivity, PlaceSelectionActivity::class.java)
            intent.putExtra("avertList", avertList)
            startActivity(intent)
        }
        iv_NoRecipient = findViewById<View>(R.id.iv_NoRecipient) as ImageView
        setFabOkVisibility()

        lvContact = findViewById<View>(R.id.listContact) as ListView


        lvContact!!.emptyView = findViewById(R.id.emptyListContact)

        btnListenerDelete = BtnClickListener { position ->
            avertList.removeAt(position)
            adapter!!.notifyDataSetChanged()
            setFabOkVisibility()
        }



        if (avertList.size == 0) {
            onAddButtonClick()
        }
    }

    /**
     * Rend le bouton OK pour passer a la page suivant visible ou non
     */
    private fun setFabOkVisibility() {
        if (avertList.isEmpty()) {
            fabOk!!.visibility = View.GONE
            if (Math.random() < 0.5) {
                iv_NoRecipient!!.setImageResource(R.drawable.ic_person_girl)
            } else {
                iv_NoRecipient!!.setImageResource(R.drawable.ic_person_man)
            }
        } else {
            fabOk!!.visibility = View.VISIBLE

        }
    }


    /**
     * Ouvre la page des Contacts par defaut Android et demande de selectionner un contact
     */
    private fun startActivityContactForResult() {
        //Contact activity
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(intent, 1)
    }

    public override fun onActivityResult(reqCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(reqCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            val contactData = data.data
            val c = contentResolver.query(contactData!!, null, null, null, null)
            if (c!!.moveToFirst()) {

                val name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID))

                val phones = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null)

                var number: String? = null
                tmpAvertList.clear()

                while (phones!!.moveToNext()) {
                    //On verifie que le numéro n'est pas en double dans le contact recupere
                    val tmpNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).trim { it <= ' ' }
                    if (tmpNumber != number) {
                        number = tmpNumber

                        // On creer un nouveau avert pour stocker le nom et le numero du contact
                        val tmpAvert = Avert()
                        tmpAvert.contactName = name//Set le nom
                        tmpAvert.contactNumber = number //Set le numero
                        tmpAvertList.add(tmpAvert)
                    }
                }
                phones.close()

                if (tmpAvertList.size > 1) {
                    val builderSingle = AlertDialog.Builder(this@ContactActivity)
                    builderSingle.setIcon(R.drawable.ic_add_white_24dp)
                    builderSingle.setTitle(getString(R.string.tvSelectNumber))

                    val arrayAdapter = ArrayAdapter<Avert>(
                            this@ContactActivity,
                            android.R.layout.select_dialog_singlechoice)
                    for (item in tmpAvertList) {
                        arrayAdapter.add(item)
                    }

                    builderSingle.setNegativeButton(
                            getString(R.string.cancel)
                    ) { dialog, which -> dialog.dismiss() }

                    builderSingle.setAdapter(
                            arrayAdapter
                    ) { dialog, which ->
                        var exists = false
                        for (av in avertList) {
                            if (av.contactNumber == arrayAdapter.getItem(which)!!.contactNumber) {
                                exists = true
                            }
                        }
                        if (!exists) {
                            avertList.add(arrayAdapter.getItem(which)) // Stock le contact dans le tableau
                        } else {
                            Toast.makeText(applicationContext, getString(R.string.tstRecipientTwiceForbidden), Toast.LENGTH_LONG).show()
                        }
                        addToContactList(avertList)
                        setFabOkVisibility()
                    }
                    builderSingle.show()
                } else if (tmpAvertList.size == 1) {
                    var exists = false
                    for (av in avertList) {
                        if (av.contactNumber == tmpAvertList[0].contactNumber) {
                            exists = true
                        }
                    }
                    if (!exists) {
                        avertList.add(tmpAvertList[0]) // Stock le contact dans le tableau
                    } else {
                        Toast.makeText(applicationContext, getString(R.string.tstRecipientTwiceForbidden), Toast.LENGTH_LONG).show()
                    }
                    addToContactList(avertList)
                    setFabOkVisibility()
                } else {
                    Snackbar.make(window.decorView.findViewById(R.id.listContact), getString(R.string.tvNoNumber), Snackbar.LENGTH_LONG).show()
                }
            }
        } else if (avertList.size == 0) {
            finish()
        }
    }

    /**
     * Ajotue a la liste des Avert un avert donne
     *
     * @param avertList
     * Liste des Avert deja enregistres
     */
    private fun addToContactList(avertList: ArrayList<Avert>) {

        adapter = AdapterContact(this@ContactActivity, 0, avertList, btnListenerDelete)
        lvContact!!.adapter = adapter

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startActivityContactForResult()
        }
    }

    private fun onAddButtonClick() {
        if (ContextCompat.checkSelfPermission(applicationContext,
                        Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this@ContactActivity,
                    arrayOf(Manifest.permission.READ_CONTACTS), 0)
        } else {
            startActivityContactForResult()
        }
    }
}
