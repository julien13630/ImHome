package com.example.julien.imhome;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.julien.imhome.Adapter.AdapterAvert;
import com.example.julien.imhome.Data.Avert;

import java.util.ArrayList;

public class ContactActivity extends Activity {

    // List d'Avert qui nous servira pour stocker les contact
    private ArrayList<Avert> avertList = new ArrayList<Avert>();
    private ArrayList<Avert> tmpAvertList = new ArrayList<Avert>();
    private FloatingActionButton fabOk;
    private float historicX = Float.NaN, historicY = Float.NaN;
    private static final int DELTA = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(ContactActivity.this,
                            new String[]{Manifest.permission.READ_CONTACTS}, 0);
                }else{
                    startActivityContactForResult();
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
                            Toast.makeText(getApplicationContext(), "Left", Toast.LENGTH_SHORT).show();
                            return true;
                        } else if (event.getX() - historicX > DELTA) {
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

    private void startActivityContactForResult(){
        //Contact activity
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            Uri contactData = data.getData();
            Cursor c =  getContentResolver().query(contactData, null, null, null, null);
            if (c.moveToFirst()) {

                String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String contactId =
                        c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));

                Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);

                String number = null;
                tmpAvertList.clear();

                while (phones.moveToNext()) {
                    //On verifie que le numéro n'est pas en double dans le contact recupere
                    String tmpNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).trim();
                    if(!tmpNumber.equals(number)){
                        number = tmpNumber;

                        // On creer un nouveau avert pour stocker le nom et le numero du contact
                        Avert tmpAvert = new Avert();
                        tmpAvert.setContactName(name);//Set le nom
                        tmpAvert.setContactNumber(number); //Set le numero
                        tmpAvertList.add(tmpAvert);

                        if (avertList.isEmpty()){
                            fabOk.setVisibility(View.INVISIBLE);
                        }else{
                            fabOk.setVisibility(View.VISIBLE);
                        }
                    }
                }
                phones.close();

                if(tmpAvertList.size() > 1 ){
                    AlertDialog.Builder builderSingle = new AlertDialog.Builder(ContactActivity.this);
                    builderSingle.setIcon(R.drawable.ic_add_white_24dp);
                    builderSingle.setTitle(" Selectionner un numero : ");

                    final ArrayAdapter<Avert> arrayAdapter = new ArrayAdapter<Avert>(
                            ContactActivity.this,
                            android.R.layout.select_dialog_singlechoice);
                    for (Avert item : tmpAvertList){
                        arrayAdapter.add(item);
                    }

                    builderSingle.setNegativeButton(
                            "Annuler",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                    builderSingle.setAdapter(
                            arrayAdapter,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    avertList.add(arrayAdapter.getItem(which)); // Stock le contact dans le tableau
                                    addToContactList(avertList);
                                }
                            });
                    builderSingle.show();
                }else if(tmpAvertList.size() == 1){
                    avertList.add(tmpAvertList.get(0));
                    addToContactList(avertList);
                }
            }
        }
    }

    private void addToContactList(ArrayList<Avert> avertList){

        AdapterAvert adapter = new AdapterAvert(ContactActivity.this, 0, avertList);
        ListView list = (ListView)findViewById(R.id.listContact);
        list.setAdapter(adapter);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startActivityContactForResult();
        }
    }
}
