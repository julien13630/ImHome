package com.example.julien.imhome;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

import com.example.julien.imhome.Adapter.AdapterAvert;
import com.example.julien.imhome.Data.Avert;

import java.util.ArrayList;

public class ContactActivity extends ListActivity {

    // List d'Avert qui nous servira pour stocker les contact
    private ArrayList<Avert> avertList = new ArrayList<Avert>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        //On set de titre de la page
        this.setTitle("Sélectionnez les contacts à prévenir");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Contact activity
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        });

       
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (1) :
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    Cursor c =  getContentResolver().query(contactData, null, null, null, null);
                    if (c.moveToFirst()) {

                        // On creer un nouveau avert pour stocker le nom et le numero du contact
                        Avert tmpAvert = new Avert();

                        String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        String contactId =
                                c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));

                        tmpAvert.setContactName(name);//Set le nom

                        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                        while (phones.moveToNext()) {
                            String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            // TODO Verifier qu'il y ai au moins un numero et pop up de selection si plusieurs

                            //Snackbar.make(ContactActivity.this.findViewById(android.R.id.content), number.toString(), Snackbar.LENGTH_LONG)
                            //        .show();

                            tmpAvert.setContactNumber(number); //Set le numero
                            avertList.add(tmpAvert); // Stock le contact dans le tableau
                        }
                        phones.close();

                        AdapterAvert adapter = new AdapterAvert(ContactActivity.this, 0, avertList);
                        ContactActivity.this.setListAdapter(adapter);


                    }
                }

            break;
        }
    }

}
