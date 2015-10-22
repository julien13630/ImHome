package com.example.julien.imhome.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by julie on 22/10/2015.
 */


public class AvertDataSource {
    // Champs de la base de données
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN_A_LIBELLE,
            MySQLiteHelper.COLUMN_A_SSID,
            MySQLiteHelper.COLUMN_A_HASHCODE,
            MySQLiteHelper.COLUMN_A_DATE,
            MySQLiteHelper.COLUMN_A_CONTACTNAME,
            MySQLiteHelper.COLUMN_A_CONTACTNUMBER};


    public AvertDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Avert addAvert(String libelle, String ssid, int hashcode,  Date date, String contactName, String contactNumber) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_A_LIBELLE, libelle);
        values.put(MySQLiteHelper.COLUMN_A_SSID, ssid);
        values.put(MySQLiteHelper.COLUMN_A_HASHCODE, hashcode);
        values.put(MySQLiteHelper.COLUMN_A_DATE, date.toString());
        values.put(MySQLiteHelper.COLUMN_A_CONTACTNAME, contactName);
        values.put(MySQLiteHelper.COLUMN_A_CONTACTNUMBER, contactNumber);

        long insertId = database.insert(MySQLiteHelper.TABLE_AVERT, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_AVERT,
                allColumns, MySQLiteHelper.COLUMN_A_CONTACTNUMBER + " = " + contactNumber + " AND " +
                        MySQLiteHelper.COLUMN_A_DATE + " = " + date.toString()  , null,
                null, null, null);
        cursor.moveToFirst();
        Avert newAvert = cursorToAvert(cursor);
        cursor.close();
        return newAvert;
    }

    public void deleteAvert(Avert avert) {
        Date date = avert.getAddDate();
        String contactNumber = avert.getContactNumber();
        System.out.println("Avert deleted : " + avert.toString());
        database.delete(MySQLiteHelper.TABLE_WIFI, MySQLiteHelper.COLUMN_A_CONTACTNUMBER + " = " + contactNumber + " AND " +
                MySQLiteHelper.COLUMN_A_DATE + " = " + date.toString(), null);
    }

    public List<Avert> getAllWifi() {
        List<Avert> averts = new ArrayList<Avert>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_WIFI,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Avert avert = cursorToAvert(cursor);
            averts.add(avert);
            cursor.moveToNext();
        }
        // assurez-vous de la fermeture du curseur
        cursor.close();
        return averts;
    }

    private Avert cursorToAvert(Cursor cursor) {
        Avert avert = new Avert();
        Date tmpDate = new Date();
        android.text.format.DateFormat.format(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_A_DATE)),tmpDate);

        avert.setHashcode(cursor.getInt(cursor.getColumnIndex(MySQLiteHelper.COLUMN_A_HASHCODE)));
        avert.setLibelle(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_A_LIBELLE)));
        avert.setSsid(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_A_SSID)));
        avert.setContactNumber(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_A_CONTACTNUMBER)));
        avert.setAddDate(tmpDate);
        avert.setContactName(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_A_CONTACTNAME)));
        return avert;
    }
}
