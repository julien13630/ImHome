package com.dailyvery.apps.imhome.Data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

import java.sql.SQLException
import java.util.ArrayList

/**
 * Created by justefani on 21/12/2017.
 */

class LocationDataSource(context: Context) {
    // Champs de la base de données
    private var database: SQLiteDatabase? = null
    private val dbHelper: MySQLiteHelper
    private val allColumns = arrayOf(MySQLiteHelper.COLUMN_L_ADDRESS, MySQLiteHelper.COLUMN_L_NICK, MySQLiteHelper.COLUMN_L_LAT, MySQLiteHelper.COLUMN_L_LONG)

    // assurez-vous de la fermeture du curseur
    //TODO Lever une vraie exception
    val allLocations: List<Location>
        get() {
            try {
                open()
                val locations = ArrayList<Location>()

                val cursor = database!!.query(MySQLiteHelper.TABLE_LOCATION,
                        allColumns, null, null, null, null, null)

                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    val location = cursorToLocation(cursor)
                    locations.add(location)
                    cursor.moveToNext()
                }
                cursor.close()
                close()
                return locations
            } catch (e: Exception) {
                return ArrayList()
            }

        }


    init {
        dbHelper = MySQLiteHelper(context)
    }

    @Throws(SQLException::class)
    fun open() {
        if (database == null || database != null && !database!!.isOpen) {
            database = dbHelper.writableDatabase
        }
    }

    fun close() {
        if (database != null) {
            database!!.close()
        }
    }

    fun addLocation(location: Location): Location {
        try {
            open()
            val values = ContentValues()
            values.put(MySQLiteHelper.COLUMN_L_ADDRESS, location.address)
            values.put(MySQLiteHelper.COLUMN_L_NICK, location.nick)
            values.put(MySQLiteHelper.COLUMN_L_LAT, location.lat)
            values.put(MySQLiteHelper.COLUMN_L_LONG, location.long)

            val insertId = database!!.insert(MySQLiteHelper.TABLE_LOCATION, null,
                    values)

            close()
            return location
        } catch (e: Exception) {
            //TODO Lever une vraie exception
            return Location()
        }

    }

    fun deleteLocation(location: Location) {
        try {
            open()
            val address = location.address

            database!!.delete(MySQLiteHelper.TABLE_LOCATION, MySQLiteHelper.COLUMN_L_ADDRESS + " = '" + address + "'", null)

            close()
            println("Location deleted : " + location.address!!)
        } catch (e: Exception) {
            //TODO Lever une vraie exception
        }

    }

    fun editLocation(location: Location) {
        try {
            open()
            val address = location.address

            val cv = ContentValues()
            cv.put(MySQLiteHelper.COLUMN_L_ADDRESS, location.address)

            database!!.update(MySQLiteHelper.TABLE_LOCATION, cv, MySQLiteHelper.COLUMN_L_ADDRESS + " = '" + address + "'", null)

            close()
            println("Location edited : " + location.address!!)
        } catch (e: Exception) {
            //TODO Lever une vraie exception
        }

    }

    private fun cursorToLocation(cursor: Cursor): Location {
        val location = Location()

        location.address = cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_L_ADDRESS))
        location.nick = cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_L_NICK))
        location.lat = cursor.getDouble(cursor.getColumnIndex(MySQLiteHelper.COLUMN_L_LAT))
        location.long = cursor.getDouble(cursor.getColumnIndex(MySQLiteHelper.COLUMN_L_LONG))

        return location
    }
}
