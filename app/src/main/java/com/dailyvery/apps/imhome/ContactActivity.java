package com.dailyvery.apps.imhome;

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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dailyvery.apps.imhome.Adapter.AdapterContact;
import com.dailyvery.apps.imhome.Adapter.AdapterMain;
import com.dailyvery.apps.imhome.Data.Avert;
import com.dailyvery.apps.imhome.Data.AvertDataSource;
import com.dailyvery.apps.imhome.Interface.BtnClickListener;

import java.util.ArrayList;

public class ContactActivity extends AppCompatActivity {

    // List d'Avert qui nous servira pour stocker les contact
    private ArrayList<Avert> avertList = new ArrayList<>();
    private ArrayList<Avert> tmpAvertList = new ArrayList<>();
    private AdapterContact adapter = null;
    private FloatingActionButton fabOk;
    private ListView lvContact;
    BtnClickListener btnListenerDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.app_name));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddButtonClick();
            }
        });

        fabOk = (FloatingActionButton) findViewById(R.id.fabOk);
        fabOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Contact activity
                Intent intent = new Intent(ContactActivity.this, PlaceSelectionActivity.class);
                intent.putExtra("avertList", avertList);
                startActivity(intent);
            }
        });

        setFabOkVisibility();

        lvContact = (ListView)findViewById(R.id.listContact);

        TextView tvEmptyText = (TextView)findViewById(R.id.tvEmptyListContact);
        tvEmptyText.setText(getString(R.string.tvNoRecipient));

        lvContact.setEmptyView(findViewById(R.id.emptyListContact));

        btnListenerDelete = new BtnClickListener() {
            @Override
            public void onBtnClick(int position) {
                avertList.remove(position);
                adapter.notifyDataSetChanged();
            }
        };

        if(avertList.size() == 0){
            onAddButtonClick();
        }
    }

    /**
     * Rend le bouton OK pour passer a la page suivant visible ou non
     */
    private void setFabOkVisibility() {
        if (avertList.isEmpty()){
            fabOk.setVisibility(View.INVISIBLE);
        }else{
            fabOk.setVisibility(View.VISIBLE);
        }
    }


    /**
     * Ouvre la page des Contacts par defaut Android et demande de selectionner un contact
     */
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
                    }
                }
                phones.close();

                if(tmpAvertList.size() > 1 ){
                    AlertDialog.Builder builderSingle = new AlertDialog.Builder(ContactActivity.this);
                    builderSingle.setIcon(R.drawable.ic_add_white_24dp);
                    builderSingle.setTitle(getString(R.string.tvSelectNumber));

                    final ArrayAdapter<Avert> arrayAdapter = new ArrayAdapter<Avert>(
                            ContactActivity.this,
                            android.R.layout.select_dialog_singlechoice);
                    for (Avert item : tmpAvertList){
                        arrayAdapter.add(item);
                    }

                    builderSingle.setNegativeButton(
                            getString(R.string.cancel),
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
                                    boolean exists = false;
                                    for(Avert av : avertList){
                                        if(av.getContactNumber().equals(arrayAdapter.getItem(which).getContactNumber())){
                                            exists = true;
                                        }
                                    }
                                    if(!exists){
                                        avertList.add(arrayAdapter.getItem(which)); // Stock le contact dans le tableau
                                    }else{
                                        Toast.makeText(getApplicationContext(), getString(R.string.tstRecipientTwiceForbidden), Toast.LENGTH_LONG).show();
                                    }
                                    addToContactList(avertList);
                                    setFabOkVisibility();
                                }
                            });
                    builderSingle.show();
                }else if(tmpAvertList.size() == 1){
                    boolean exists = false;
                    for(Avert av : avertList){
                        if(av.getContactNumber().equals(tmpAvertList.get(0).getContactNumber())){
                            exists = true;
                        }
                    }
                    if(!exists){
                        avertList.add(tmpAvertList.get(0)); // Stock le contact dans le tableau
                    }else{
                        Toast.makeText(getApplicationContext(), getString(R.string.tstRecipientTwiceForbidden), Toast.LENGTH_LONG).show();
                    }
                    addToContactList(avertList);
                    setFabOkVisibility();
                }
            }
        }
    }

    /**
     * Ajotue a la liste des Avert un avert donne
     *
     * @param avertList
     *            Liste des Avert deja enregistres
     */
    private void addToContactList(ArrayList<Avert> avertList){

        adapter = new AdapterContact(ContactActivity.this, 0, avertList, btnListenerDelete);
        lvContact.setAdapter(adapter);

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

    private void onAddButtonClick(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(ContactActivity.this,
                    new String[]{Manifest.permission.READ_CONTACTS}, 0);
        }else{
            startActivityContactForResult();
        }
    }
}
