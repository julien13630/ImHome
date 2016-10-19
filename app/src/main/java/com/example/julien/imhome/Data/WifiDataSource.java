package com.example.julien.imhome.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by julie on 22/10/2015.
 */
public class WifiDataSource {
    // Champs de la base de données
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN_W_LIBELLE,
            MySQLiteHelper.COLUMN_W_SSID,
            MySQLiteHelper.COLUMN_W_HASHCODE,
            MySQLiteHelper.COLUMN_W_FAVORIT };


    public WifiDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Wifi addWifi(String libelle, String ssid, int hashcode, boolean favorit) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_W_LIBELLE, libelle);
        values.put(MySQLiteHelper.COLUMN_W_SSID, ssid);
        values.put(MySQLiteHelper.COLUMN_W_HASHCODE, hashcode);
        values.put(MySQLiteHelper.COLUMN_W_FAVORIT, favorit);
        long insertId = database.insert(MySQLiteHelper.TABLE_WIFI, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_WIFI,
                allColumns, MySQLiteHelper.COLUMN_W_HASHCODE + " = " + hashcode, null,
                null, null, null);
        cursor.moveToFirst();
        Wifi newWifi = cursorToWifi(cursor);
        cursor.close();
        return newWifi;
    }

    public Wifi addWifi(Wifi wifi) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_W_LIBELLE, wifi.getLabel());
        values.put(MySQLiteHelper.COLUMN_W_SSID, wifi.getSsid());
        values.put(MySQLiteHelper.COLUMN_W_HASHCODE, wifi.getHashcode());
        values.put(MySQLiteHelper.COLUMN_W_FAVORIT, wifi.isFavorite());
        long insertId = database.insert(MySQLiteHelper.TABLE_WIFI, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_WIFI,
                allColumns, MySQLiteHelper.COLUMN_W_HASHCODE + " = " + wifi.getHashcode(), null,
                null, null, null);
        cursor.moveToFirst();
        Wifi newWifi = cursorToWifi(cursor);
        cursor.close();
        return newWifi;
    }

    public void deleteComment(Wifi comment) {
        int id = comment.getHashcode();
        System.out.println("Comment deleted with hascode: " + id);
        database.delete(MySQLiteHelper.TABLE_WIFI, MySQLiteHelper.COLUMN_W_HASHCODE
                + " = " + id, null);
    }

    public List<Wifi> getAllWifi() {
        List<Wifi> wifis = new ArrayList<Wifi>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_WIFI,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Wifi wifi = cursorToWifi(cursor);
            wifis.add(wifi);
            cursor.moveToNext();
        }
        // assurez-vous de la fermeture du curseur
        cursor.close();
        return wifis;
    }

    private Wifi cursorToWifi(Cursor cursor) {
        Wifi wifi = new Wifi();
        wifi.setFavorite(cursor.getInt(cursor.getColumnIndex(MySQLiteHelper.COLUMN_W_FAVORIT))!= 0);
        wifi.setHashcode(cursor.getInt(cursor.getColumnIndex(MySQLiteHelper.COLUMN_W_HASHCODE)));
        wifi.setLabel(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_W_LIBELLE)));
        wifi.setSsid(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_W_SSID)));
        return wifi;
    }
}
