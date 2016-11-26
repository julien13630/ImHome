package com.dailyvery.apps.imhome.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

/**
 * Created by julie on 22/10/2015.
 */


public class AvertDataSource {
    // Champs de la base de données
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN_A_LIBELLE,
            MySQLiteHelper.COLUMN_A_SSID,
            MySQLiteHelper.COLUMN_A_MESSAGETEXT,
            MySQLiteHelper.COLUMN_A_HASHCODE,
            MySQLiteHelper.COLUMN_A_DATE,
            MySQLiteHelper.COLUMN_A_CONTACTNAME,
            MySQLiteHelper.COLUMN_A_CONTACTNUMBER,
            MySQLiteHelper.COLUMN_A_LATITUDE,
            MySQLiteHelper.COLUMN_A_LONGITUDE};


    public AvertDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        if (database == null || (database != null && !database.isOpen())) {
            database = dbHelper.getWritableDatabase();
        }
    }

    public void close() {
        if (database != null){
            database.close();
        }
    }

    public Avert addAvert(String libelle, String ssid,String messageText, int hashcode,  Date date,
                          String contactName, String contactNumber, double latitude, double longitude) {
        try{
            open();
            ContentValues values = new ContentValues();
            values.put(MySQLiteHelper.COLUMN_A_LIBELLE, libelle);
            values.put(MySQLiteHelper.COLUMN_A_SSID, ssid);
            values.put(MySQLiteHelper.COLUMN_A_MESSAGETEXT, messageText);
            values.put(MySQLiteHelper.COLUMN_A_HASHCODE, hashcode);
            values.put(MySQLiteHelper.COLUMN_A_DATE, date.toString());
            values.put(MySQLiteHelper.COLUMN_A_CONTACTNAME, contactName);
            values.put(MySQLiteHelper.COLUMN_A_CONTACTNUMBER, contactNumber);
            values.put(MySQLiteHelper.COLUMN_A_LATITUDE, latitude);
            values.put(MySQLiteHelper.COLUMN_A_LONGITUDE, longitude);

            long insertId = database.insert(MySQLiteHelper.TABLE_AVERT, null,
                    values);
            Cursor cursor = database.query(MySQLiteHelper.TABLE_AVERT,
                    allColumns, MySQLiteHelper.COLUMN_A_CONTACTNUMBER + " = '" + contactNumber + "' AND " +
                            MySQLiteHelper.COLUMN_A_DATE + " = '" + date.toString() + "'"  , null,
                    null, null, null);
            cursor.moveToFirst();
            Avert newAvert = cursorToAvert(cursor);
            cursor.close();
            close();
            return newAvert;
        }catch (Exception e){
            //TODO Lever une vraie exception
            return new Avert();
        }
    }

    public Avert addAvert(Avert avert) {
        try{
            open();
            ContentValues values = new ContentValues();
            values.put(MySQLiteHelper.COLUMN_A_LIBELLE, avert.getLabel());
            values.put(MySQLiteHelper.COLUMN_A_SSID, avert.getSsid());
            values.put(MySQLiteHelper.COLUMN_A_MESSAGETEXT, avert.getMessageText());
            values.put(MySQLiteHelper.COLUMN_A_HASHCODE, avert.getHashcode());
            values.put(MySQLiteHelper.COLUMN_A_DATE, avert.getAddDate().toString());
            values.put(MySQLiteHelper.COLUMN_A_CONTACTNAME, avert.getContactName());
            values.put(MySQLiteHelper.COLUMN_A_CONTACTNUMBER, avert.getContactNumber());
            values.put(MySQLiteHelper.COLUMN_A_LATITUDE, avert.getLatitude());
            values.put(MySQLiteHelper.COLUMN_A_LONGITUDE, avert.getLongitude());

            long insertId = database.insert(MySQLiteHelper.TABLE_AVERT, null,
                    values);

            close();
            return avert;
        }catch (Exception e){
            //TODO Lever une vraie exception
            return new Avert();
        }
    }

    public void deleteAvert(Avert avert) {
        try{
            open();
            String date = avert.getAddDate().toString();
            String contactNumber = avert.getContactNumber();
            int hashCode = avert.getHashcode();


            database.delete(MySQLiteHelper.TABLE_AVERT, MySQLiteHelper.COLUMN_A_CONTACTNUMBER + " = '" + contactNumber + "' AND " +
                    MySQLiteHelper.COLUMN_A_HASHCODE + " = '" + hashCode + "' AND " +
                    MySQLiteHelper.COLUMN_A_DATE + " = '" + date + "'", null);

            close();
            System.out.println("Avert deleted : " + avert.getContactName());
        }catch(Exception e) {
            //TODO Lever une vraie exception
        }
    }

    public void editAvert(Avert avert) {
        try{
            open();
            String date = avert.getAddDate().toString();
            String contactNumber = avert.getContactNumber();
            int hashCode = avert.getHashcode();

            ContentValues cv = new ContentValues();
            cv.put(MySQLiteHelper.COLUMN_A_MESSAGETEXT, avert.getMessageText());

            database.update(MySQLiteHelper.TABLE_AVERT, cv ,  MySQLiteHelper.COLUMN_A_CONTACTNUMBER + " = '" + contactNumber + "' AND " +
                    MySQLiteHelper.COLUMN_A_HASHCODE + " = '" + hashCode + "' AND " +
                    MySQLiteHelper.COLUMN_A_DATE + " = '" + date + "'", null);

            close();
            System.out.println("Avert edited : " + avert.getContactName());
        }catch(Exception e) {
            //TODO Lever une vraie exception
        }
    }

    public List<Avert> getAllAvert() {
        try{
            open();
            List<Avert> averts = new ArrayList<Avert>();

            Cursor cursor = database.query(MySQLiteHelper.TABLE_AVERT,
                    allColumns, null, null, null, null, null);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Avert avert = cursorToAvert(cursor);
                averts.add(avert);
                cursor.moveToNext();
            }
            // assurez-vous de la fermeture du curseur
            cursor.close();
            close();
            return averts;
        }catch (Exception e){
            //TODO Lever une vraie exception
            return new ArrayList<>();
        }
    }

    private Avert cursorToAvert(Cursor cursor) {
        Avert avert = new Avert();
        Date tmpDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy");
        try {
            tmpDate = dateFormat.parse(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_A_DATE)));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        avert.setHashcode(cursor.getInt(cursor.getColumnIndex(MySQLiteHelper.COLUMN_A_HASHCODE)));
        avert.setLabel(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_A_LIBELLE)));
        avert.setSsid(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_A_SSID)));
        avert.setContactNumber(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_A_CONTACTNUMBER)));
        avert.setMessageText(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_A_MESSAGETEXT)));
        avert.setAddDate(tmpDate);
        avert.setContactName(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_A_CONTACTNAME)));
        avert.setLatitude(cursor.getDouble(cursor.getColumnIndex(MySQLiteHelper.COLUMN_A_LATITUDE)));
        avert.setLongitude(cursor.getDouble(cursor.getColumnIndex(MySQLiteHelper.COLUMN_A_LONGITUDE)));

        return avert;
    }
}
