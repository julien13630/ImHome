package com.dailyvery.apps.imhome.Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by justefani on 21/12/2017.
 */

public class LocationDataSource {
    // Champs de la base de données
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN_L_ADDRESS,
            MySQLiteHelper.COLUMN_L_NICK, MySQLiteHelper.COLUMN_L_LAT, MySQLiteHelper.COLUMN_L_LONG};


    public LocationDataSource(Context context) {
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

    public Location addLocation(Location location) {
        try{
            open();
            ContentValues values = new ContentValues();
            values.put(MySQLiteHelper.COLUMN_L_ADDRESS, location.getAddress());
            values.put(MySQLiteHelper.COLUMN_L_NICK, location.getNick());
            values.put(MySQLiteHelper.COLUMN_L_LAT, location.getLat());
            values.put(MySQLiteHelper.COLUMN_L_LONG, location.getLong());

            long insertId = database.insert(MySQLiteHelper.TABLE_LOCATION, null,
                    values);

            close();
            return location;
        }catch (Exception e){
            //TODO Lever une vraie exception
            return new Location();
        }
    }

    public void deleteLocation(Location location) {
        try{
            open();
            String address = location.getAddress();

            database.delete(MySQLiteHelper.TABLE_LOCATION, MySQLiteHelper.COLUMN_L_ADDRESS + " = '" + address + "'", null);

            close();
            System.out.println("Location deleted : " + location.getAddress());
        }catch(Exception e) {
            //TODO Lever une vraie exception
        }
    }

    public void editLocation(Location location) {
        try{
            open();
            String address = location.getAddress();

            ContentValues cv = new ContentValues();
            cv.put(MySQLiteHelper.COLUMN_L_ADDRESS, location.getAddress());

            database.update(MySQLiteHelper.TABLE_LOCATION, cv ,  MySQLiteHelper.COLUMN_L_ADDRESS + " = '" + address + "'", null);

            close();
            System.out.println("Location edited : " + location.getAddress());
        }catch(Exception e) {
            //TODO Lever une vraie exception
        }
    }

    public List<Location> getAllLocations() {
        try{
            open();
            List<Location> locations = new ArrayList<Location>();

            Cursor cursor = database.query(MySQLiteHelper.TABLE_LOCATION,
                    allColumns, null, null, null, null, null);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Location location = cursorToLocation(cursor);
                locations.add(location);
                cursor.moveToNext();
            }
            // assurez-vous de la fermeture du curseur
            cursor.close();
            close();
            return locations;
        }catch (Exception e){
            //TODO Lever une vraie exception
            return new ArrayList<>();
        }
    }

    private Location cursorToLocation(Cursor cursor) {
        Location location = new Location();

        location.setAddress(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_L_ADDRESS)));
        location.setNick(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_L_NICK)));
        location.setLat(cursor.getDouble(cursor.getColumnIndex(MySQLiteHelper.COLUMN_L_LAT)));
        location.setLong(cursor.getDouble(cursor.getColumnIndex(MySQLiteHelper.COLUMN_L_LONG)));

        return location;
    }
}
