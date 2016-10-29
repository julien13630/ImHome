package com.example.julien.imhome;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.julien.imhome.Adapter.AdapterMain;
import com.example.julien.imhome.Data.Avert;
import com.example.julien.imhome.Data.AvertDataSource;
import com.example.julien.imhome.Interface.BtnClickListener;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Avert> avertList;
    private float historicX = Float.NaN, historicY = Float.NaN;
    private static final int DELTA = 50;
    private AdapterMain adapter = null;
    private static LayoutInflater inflater = null;
    private ListView lvMain;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("ImHome");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, ContactActivity.class);
                startActivity(intent);
            }
        });

        lvMain = (ListView) findViewById(R.id.listMain);

        lvMain.setOnTouchListener(new View.OnTouchListener() {
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

        TextView tvEmptyText = (TextView)findViewById(R.id.tvEmptyList);
        tvEmptyText.setText("Pas de messages !");

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
            Intent intent = new Intent(MainActivity.this, WifiSettings.class);
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
        final AvertDataSource avertDT = new AvertDataSource(MainActivity.this);
        try {
            avertDT.open();
            avertList = avertDT.getAllAvert();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            avertDT.close();
        }

        BtnClickListener btnListener = new BtnClickListener() {
            @Override
            public void onBtnClick(int position) {
                //TODO la suppression
                avertDT.deleteAvert(avertList.get(position));
                avertList.remove(avertList.get(position));
                adapter.notifyDataSetChanged();
            }
        };




        adapter = new AdapterMain(MainActivity.this, 0, (ArrayList<Avert>) avertList, btnListener);
        lvMain.setAdapter(adapter);
    }

}
