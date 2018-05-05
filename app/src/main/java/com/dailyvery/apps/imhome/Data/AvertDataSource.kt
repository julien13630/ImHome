package com.dailyvery.apps.imhome.Data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.provider.Settings
import android.util.Log

import java.sql.SQLException
import java.text.ParseException
import java.util.ArrayList
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

/**
 * Created by julie on 22/10/2015.
 */


class AvertDataSource(context: Context) {
    // Champs de la base de données
    private var database: SQLiteDatabase? = null
    private val dbHelper: MySQLiteHelper
    private val allColumns = arrayOf(MySQLiteHelper.COLUMN_A_ID, MySQLiteHelper.COLUMN_A_LIBELLE, MySQLiteHelper.COLUMN_A_SSID, MySQLiteHelper.COLUMN_A_MESSAGETEXT, MySQLiteHelper.COLUMN_A_HASHCODE, MySQLiteHelper.COLUMN_A_DATE, MySQLiteHelper.COLUMN_A_CONTACTNAME, MySQLiteHelper.COLUMN_A_CONTACTNUMBER, MySQLiteHelper.COLUMN_A_LATITUDE, MySQLiteHelper.COLUMN_A_LONGITUDE, MySQLiteHelper.COLUMN_A_FLAGRECCURENCE)

    // assurez-vous de la fermeture du curseur
    //TODO Lever une vraie exception
    val allAvert: List<Avert>
        get() {
            try {
                open()
                val averts = ArrayList<Avert>()

                val cursor = database!!.query(MySQLiteHelper.TABLE_AVERT,
                        allColumns, null, null, null, null, null)

                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    val avert = cursorToAvert(cursor)
                    averts.add(avert)
                    cursor.moveToNext()
                }
                cursor.close()
                close()
                return averts
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

    fun addAvert(libelle: String, ssid: String, messageText: String, hashcode: Int, date: Date,
                 contactName: String, contactNumber: String, latitude: Double, longitude: Double,
                 flagReccurence: Boolean): Avert {
        try {
            open()
            val values = ContentValues()
            values.put(MySQLiteHelper.COLUMN_A_ID, UUID.randomUUID().toString())
            values.put(MySQLiteHelper.COLUMN_A_LIBELLE, libelle)
            values.put(MySQLiteHelper.COLUMN_A_SSID, ssid)
            values.put(MySQLiteHelper.COLUMN_A_MESSAGETEXT, messageText)
            values.put(MySQLiteHelper.COLUMN_A_HASHCODE, hashcode)
            values.put(MySQLiteHelper.COLUMN_A_DATE, date.toString())
            values.put(MySQLiteHelper.COLUMN_A_CONTACTNAME, contactName)
            values.put(MySQLiteHelper.COLUMN_A_CONTACTNUMBER, contactNumber)
            values.put(MySQLiteHelper.COLUMN_A_LATITUDE, latitude)
            values.put(MySQLiteHelper.COLUMN_A_LONGITUDE, longitude)
            values.put(MySQLiteHelper.COLUMN_A_FLAGRECCURENCE, flagReccurence)

            val insertId = database!!.insert(MySQLiteHelper.TABLE_AVERT, null,
                    values)
            val cursor = database!!.query(MySQLiteHelper.TABLE_AVERT,
                    allColumns, MySQLiteHelper.COLUMN_A_CONTACTNUMBER + " = '" + contactNumber + "' AND " +
                    MySQLiteHelper.COLUMN_A_DATE + " = '" + date.toString() + "'", null, null, null, null)
            cursor.moveToFirst()
            val newAvert = cursorToAvert(cursor)
            cursor.close()
            close()
            return newAvert
        } catch (e: Exception) {
            //TODO Lever une vraie exception
            return Avert()
        }

    }

    fun addAvert(avert: Avert): Avert {
        try {
            open()
            val values = ContentValues()
            values.put(MySQLiteHelper.COLUMN_A_ID, UUID.randomUUID().toString())
            values.put(MySQLiteHelper.COLUMN_A_LIBELLE, avert.label)
            values.put(MySQLiteHelper.COLUMN_A_SSID, avert.ssid)
            values.put(MySQLiteHelper.COLUMN_A_MESSAGETEXT, avert.messageText)
            values.put(MySQLiteHelper.COLUMN_A_HASHCODE, avert.hashcode)
            values.put(MySQLiteHelper.COLUMN_A_DATE, avert.addDate.toString())
            values.put(MySQLiteHelper.COLUMN_A_CONTACTNAME, avert.contactName)
            values.put(MySQLiteHelper.COLUMN_A_CONTACTNUMBER, avert.contactNumber)
            values.put(MySQLiteHelper.COLUMN_A_LATITUDE, avert.latitude)
            values.put(MySQLiteHelper.COLUMN_A_LONGITUDE, avert.longitude)
            values.put(MySQLiteHelper.COLUMN_A_FLAGRECCURENCE, avert.flagReccurence)

            val insertId = database!!.insert(MySQLiteHelper.TABLE_AVERT, null,
                    values)

            close()
            return avert
        } catch (e: Exception) {
            //TODO Lever une vraie exception
            return Avert()
        }

    }

    fun deleteAvert(avert: Avert, automatic: Boolean) {
        try {
            open()
            val id = avert.id

            if (automatic && avert.flagReccurence == 1) {
                //TODO Disable avert
            } else {
                database!!.delete(MySQLiteHelper.TABLE_AVERT, MySQLiteHelper.COLUMN_A_ID + " = '" + id + "'", null)
            }

            close()
            println("Avert deleted : " + avert.contactName!!)
        } catch (e: Exception) {
            //TODO Lever une vraie exception
        }

    }

    fun editAvert(avert: Avert) {
        try {
            open()
            val id = avert.id

            val cv = ContentValues()
            cv.put(MySQLiteHelper.COLUMN_A_MESSAGETEXT, avert.messageText)
            cv.put(MySQLiteHelper.COLUMN_A_DATE, avert.addDate.toString())
            val flagReccurence = avert.flagReccurence

            database!!.update(MySQLiteHelper.TABLE_AVERT, cv, MySQLiteHelper.COLUMN_A_ID + " = '" + id + "'", null)

            close()
            println("Avert edited : " + avert.contactName!!)
        } catch (e: Exception) {
            //TODO Lever une vraie exception
        }

    }

    private fun cursorToAvert(cursor: Cursor): Avert {
        val avert = Avert()
        var tmpDate = Date()
        val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
        try {
            Log.d("LOL", cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_A_DATE)))
            tmpDate = dateFormat.parse(cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_A_DATE)))
        } catch (e: ParseException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }

        avert.id = cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_A_ID))
        avert.hashcode = cursor.getInt(cursor.getColumnIndex(MySQLiteHelper.COLUMN_A_HASHCODE))
        avert.label = cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_A_LIBELLE))
        avert.ssid = cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_A_SSID))
        avert.contactNumber = cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_A_CONTACTNUMBER))
        avert.messageText = cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_A_MESSAGETEXT))
        avert.addDate = tmpDate
        avert.contactName = cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_A_CONTACTNAME))
        avert.latitude = cursor.getDouble(cursor.getColumnIndex(MySQLiteHelper.COLUMN_A_LATITUDE))
        avert.longitude = cursor.getDouble(cursor.getColumnIndex(MySQLiteHelper.COLUMN_A_LONGITUDE))
        avert.flagReccurence = cursor.getInt(cursor.getColumnIndex(MySQLiteHelper.COLUMN_A_FLAGRECCURENCE))

        return avert
    }
}
