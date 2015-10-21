package com.example.julien.imhome;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;

import java.util.ArrayList;

public class ContactActivity extends ListActivity{
	//tableau dans lequel je range mes contacts
	private ArrayList Mescontacts;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_contact);
        ListContact();
        ListView listview = (ListView) findViewById(R.id.contactList);
        listview.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, Mescontacts));
    }


    private void ListContact() {
    	// notre tableau de contact
    	Mescontacts = new ArrayList();
        // instance qui permet d'acceder au contenu d'autre application
    	ContentResolver ConnectApp = this.getContentResolver();
    	Uri uri = Contacts.People.CONTENT_URI;
         String[] projection = new String[] {Contacts.People.NAME, Contacts.People.NUMBER, Contacts.People._ID };
        // on récupere les contacts dans un curseur
         Cursor cur = ConnectApp.query(uri, projection, null, null, null);
         this.startManagingCursor(cur);

         if (cur.moveToFirst()) {
             do {
                 String name = cur.getString(cur.getColumnIndex(Contacts.People.NAME));
                 String num = cur.getString(cur.getColumnIndex(Contacts.People.NUMBER));
                 String id = cur.getString(cur.getColumnIndex(Contacts.People._ID));
                 Mescontacts.add(name+"=>"+num);


             } while (cur.moveToNext());
         }
     }

    }
