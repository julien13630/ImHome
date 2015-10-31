package com.example.julien.imhome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.example.julien.imhome.Adapter.AdapterMain;
import com.example.julien.imhome.Data.Avert;
import com.example.julien.imhome.Data.AvertDataSource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Avert> avertList;
    private float historicX = Float.NaN, historicY = Float.NaN;
    private static final int DELTA = 50;
    private enum Direction {LEFT, RIGHT;}


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.setTitle("Messages en attente d'envoi :");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, ContactActivity.class);
                startActivity(intent);
            }
        });

        WifiReceiver wifi = new WifiReceiver();
        getDataSetList();

        ListView lvMain = (ListView) findViewById(R.id.listMain);

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

        AdapterMain adapter = new AdapterMain(MainActivity.this, 0, (ArrayList<Avert>)avertList);
        ListView list = (ListView)findViewById(R.id.listMain);
        list.setAdapter(adapter);
    }

}
