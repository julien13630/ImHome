package com.dailyvery.apps.imhome.Data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase

import java.sql.SQLException
import java.util.ArrayList

/**
 * Created by julie on 22/10/2015.
 */
class WifiDataSource(context: Context) {
    // Champs de la base de données
    private var database: SQLiteDatabase? = null
    private val dbHelper: MySQLiteHelper
    private val allColumns = arrayOf(MySQLiteHelper.COLUMN_W_LIBELLE, MySQLiteHelper.COLUMN_W_SSID, MySQLiteHelper.COLUMN_W_HASHCODE, MySQLiteHelper.COLUMN_W_FAVORIT)

    // assurez-vous de la fermeture du curseur
    val allWifi: List<Wifi>
        get() {
            val wifis = ArrayList<Wifi>()

            val cursor = database!!.query(MySQLiteHelper.TABLE_WIFI,
                    allColumns, null, null, null, null, null)

            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val wifi = cursorToWifi(cursor)
                wifis.add(wifi)
                cursor.moveToNext()
            }
            cursor.close()
            return wifis
        }


    init {
        dbHelper = MySQLiteHelper(context)
    }

    @Throws(SQLException::class)
    fun open() {
        database = dbHelper.writableDatabase
    }

    fun close() {
        dbHelper.close()
    }

    fun addWifi(libelle: String, ssid: String, hashcode: Int, favorit: Boolean): Wifi {
        val values = ContentValues()
        values.put(MySQLiteHelper.COLUMN_W_LIBELLE, libelle)
        values.put(MySQLiteHelper.COLUMN_W_SSID, ssid)
        values.put(MySQLiteHelper.COLUMN_W_HASHCODE, hashcode)
        values.put(MySQLiteHelper.COLUMN_W_FAVORIT, favorit)
        val insertId = database!!.insert(MySQLiteHelper.TABLE_WIFI, null,
                values)

        val cursor = database!!.query(MySQLiteHelper.TABLE_WIFI,
                allColumns, MySQLiteHelper.COLUMN_W_SSID + " = " + ssid.replace("'".toRegex(), "\''") + "'", null, null, null, null)
        cursor.moveToFirst()
        val newWifi = cursorToWifi(cursor)
        cursor.close()

        return newWifi
    }

    fun addWifi(wifi: Wifi): Wifi {
        val values = ContentValues()
        values.put(MySQLiteHelper.COLUMN_W_LIBELLE, wifi.label)
        values.put(MySQLiteHelper.COLUMN_W_SSID, wifi.ssid)
        values.put(MySQLiteHelper.COLUMN_W_HASHCODE, wifi.hashcode)
        values.put(MySQLiteHelper.COLUMN_W_FAVORIT, wifi.isFavorite)
        val insertId = database!!.insert(MySQLiteHelper.TABLE_WIFI, null,
                values)
        val cursor = database!!.query(MySQLiteHelper.TABLE_WIFI,
                allColumns, MySQLiteHelper.COLUMN_W_SSID + " = '" + wifi.ssid!!.replace("'".toRegex(), "\''") + "'", null, null, null, null)
        cursor.moveToFirst()
        val newWifi = cursorToWifi(cursor)
        cursor.close()
        return newWifi
    }

    fun update(wifi: Wifi): Boolean {
        val values = ContentValues()
        values.put(MySQLiteHelper.COLUMN_W_LIBELLE, wifi.label)
        values.put(MySQLiteHelper.COLUMN_W_SSID, wifi.ssid)
        values.put(MySQLiteHelper.COLUMN_W_HASHCODE, wifi.hashcode)
        values.put(MySQLiteHelper.COLUMN_W_FAVORIT, wifi.isFavorite)

        addWifi(wifi)
        try {
            database!!.update(MySQLiteHelper.TABLE_WIFI, values, MySQLiteHelper.COLUMN_W_SSID + " = '" + wifi.ssid!!.replace("'".toRegex(), "\''") + "'", null)
        } catch (ex: Exception) {
            ex.printStackTrace()
            return false
        }


        return true

    }

    fun deleteComment(comment: Wifi) {
        val id = comment.hashcode
        println("Comment deleted with hascode: $id")
        database!!.delete(MySQLiteHelper.TABLE_WIFI, MySQLiteHelper.COLUMN_W_HASHCODE
                + " = " + id, null)
    }

    private fun cursorToWifi(cursor: Cursor): Wifi {
        val wifi = Wifi()
        wifi.isFavorite = cursor.getInt(cursor.getColumnIndex(MySQLiteHelper.COLUMN_W_FAVORIT)) != 0
        wifi.hashcode = cursor.getInt(cursor.getColumnIndex(MySQLiteHelper.COLUMN_W_HASHCODE))
        wifi.label = cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_W_LIBELLE))
        wifi.ssid = cursor.getString(cursor.getColumnIndex(MySQLiteHelper.COLUMN_W_SSID))
        return wifi
    }
}
