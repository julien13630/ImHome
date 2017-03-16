package com.dailyvery.apps.imhome;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.dailyvery.apps.imhome.Adapter.AdapterMain;
import com.dailyvery.apps.imhome.Data.Avert;
import com.dailyvery.apps.imhome.Data.AvertDataSource;
import com.dailyvery.apps.imhome.Interface.BtnClickListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Avert> avertList;
    private AdapterMain adapter = null;
    private static LayoutInflater inflater = null;
    private ListView lvMain;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.tvMessagesEnAttente));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean previouslyStarted = prefs.getBoolean(getString(R.string.pref_previously_started), false);
        if(!previouslyStarted) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(getString(R.string.pref_previously_started), Boolean.TRUE);
            edit.commit();
            Intent intent = new Intent(MainActivity.this, SplashScreenActivity.class);
            startActivity(intent);
            finish();
        }

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-7386174591450774~4697714843");

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ContactActivity.class);
                startActivity(intent);
            }
        });

        lvMain = (ListView) findViewById(R.id.listMain);

        lvMain.setEmptyView(findViewById(R.id.emptyListMain));

        getDataSetList();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.SEND_SMS}, 0);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_SMS}, 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getDataSetList();
    }

    /**
     * Récupère la liste des Avert a afficher
     */
    private void getDataSetList(){
        AvertDataSource avertDT = new AvertDataSource(MainActivity.this);
        try {
            avertDT.open();
            avertList = avertDT.getAllAvert();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            avertDT.close();
        }

        BtnClickListener btnListenerDelete = new BtnClickListener() {
            @Override
            public void onBtnClick(int position) {
                final Avert avertToRestore = avertList.get(position);
                AvertDataSource avertDT = new AvertDataSource(MainActivity.this);
                avertDT.deleteAvert(avertList.get(position), false);
                avertList.remove(avertList.get(position));
                adapter.notifyDataSetChanged();
                avertDT.close();

                Snackbar.make(findViewById(android.R.id.content), getResources().getString(R.string.deleted), Snackbar.LENGTH_LONG)
                        .setAction(getResources().getString(R.string.cancel), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                AvertDataSource avertDT = new AvertDataSource(MainActivity.this);
                                avertDT.addAvert(avertToRestore);
                                avertList.add(avertToRestore);
                                adapter.notifyDataSetChanged();
                                avertDT.close();
                            }
                        })
                        .setActionTextColor(Color.YELLOW)
                        .show();
            }
        };

        BtnClickListener btnListenerEdit = new BtnClickListener() {
            @Override
            public void onBtnClick(final int position) {
                AvertDataSource avertDT = new AvertDataSource(MainActivity.this);

                final EditText et = new EditText(MainActivity.this);
                et.setText(avertDT.getAllAvert().get(position).getMessageText(), TextView.BufferType.EDITABLE);

                avertDT.close();

                //On limite le text a 160 caractères
                InputFilter[] filterArray = new InputFilter[1];
                filterArray[0] = new InputFilter.LengthFilter(160);
                et.setFilters(filterArray);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(getString(R.string.tvPleaseEnterMessageTextNew))
                        .setPositiveButton(getString(R.string.validate), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        AvertDataSource avertDT = new AvertDataSource(MainActivity.this);
                                        avertList.get(position).setMessageText(et.getText().toString());
                                        avertDT.editAvert(avertList.get(position));
                                        adapter.notifyDataSetChanged();
                                        avertDT.close();
                                    }
                                }

                        ).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        }

                ).setView(et);

                // Create the AlertDialog object and return it
                builder.create().show();
            }
        };

        adapter = new AdapterMain(MainActivity.this, 0, (ArrayList<Avert>) avertList, btnListenerDelete, btnListenerEdit);
        lvMain.setAdapter(adapter);
    }

}
