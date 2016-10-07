package com.example.julien.imhome;

import android.Manifest;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.example.julien.imhome.Adapter.AdapterAvert;
import com.example.julien.imhome.Data.Avert;

import java.util.ArrayList;

public class ContactActivity extends Activity {

    // List d'Avert qui nous servira pour stocker les contact
    private ArrayList<Avert> avertList = new ArrayList<Avert>();
    private FloatingActionButton fabOk;
    private float historicX = Float.NaN, historicY = Float.NaN;
    private static final int DELTA = 50;
    private enum Direction {LEFT, RIGHT;}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/

        //On set de titre de la page
        //this.setTitle("Sélectionnez les contacts à prévenir");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(ContactActivity.this,
                            new String[]{Manifest.permission.READ_CONTACTS}, 0);
                }
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.READ_CONTACTS)
                        == PackageManager.PERMISSION_GRANTED) {
                    //Contact activity
                    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    startActivityForResult(intent, 1);
                }
            }
        });

        fabOk = (FloatingActionButton) findViewById(R.id.fabOk);
        fabOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Contact activity
                Intent intent = new Intent(ContactActivity.this, WifiSelectionActivity.class);
                intent.putExtra("avertList", avertList);
                startActivity(intent);
            }
        });

        if (avertList.isEmpty()){
            fabOk.setVisibility(View.INVISIBLE);
        }else{
            fabOk.setVisibility(View.VISIBLE);
        }

        ListView lvContact = (ListView) findViewById(R.id.listContact);

        lvContact.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        historicX = event.getX();
                        historicY = event.getY();
                        break;

                    case MotionEvent.ACTION_UP:
                        if (event.getX() - historicX < -DELTA) {
                            //FunctionDeleteRowWhenSlidingLeft();
                            Toast.makeText(getApplicationContext(), "Left", Toast.LENGTH_SHORT).show();
                            return true;
                        } else if (event.getX() - historicX > DELTA) {
                            //FunctionDeleteRowWhenSlidingRight();
                            Toast.makeText(getApplicationContext(), "Right", Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        break;
                    default:
                        return false;
                }
                return false;
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

                            if (avertList.isEmpty()){
                                fabOk.setVisibility(View.INVISIBLE);
                            }else{
                                fabOk.setVisibility(View.VISIBLE);
                            }
                        }
                        phones.close();

                        AdapterAvert adapter = new AdapterAvert(ContactActivity.this, 0, avertList);
                        ListView list = (ListView)findViewById(R.id.listContact);
                        list.setAdapter(adapter);

                    }
                }

            break;
        }
    }

}
