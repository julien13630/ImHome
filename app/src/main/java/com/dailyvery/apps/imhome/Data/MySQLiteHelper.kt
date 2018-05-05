package com.dailyvery.apps.imhome.Data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

/**
 * Created by julie on 22/10/2015.
 */

class MySQLiteHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(database: SQLiteDatabase) {
        database.execSQL(DATABASE_CREATE_WIFI)
        database.execSQL(DATABASE_CREATE_AVERT)
        database.execSQL(DATABASE_CREATE_LOCATION)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.w(MySQLiteHelper::class.java.name,
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_WIFI")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_AVERT")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOCATION")

        onCreate(db)
    }

    companion object {

        val TABLE_WIFI = "wifi"
        val COLUMN_W_LIBELLE = "libelle"
        val COLUMN_W_SSID = "ssid"
        val COLUMN_W_HASHCODE = "hashcode"
        val COLUMN_W_FAVORIT = "favorite"

        val TABLE_AVERT = "avert"
        val COLUMN_A_ID = "id"
        val COLUMN_A_CONTACTNAME = "contactname"
        val COLUMN_A_CONTACTNUMBER = "contactnumber"
        val COLUMN_A_MESSAGETEXT = "messagetext"
        val COLUMN_A_HASHCODE = "hashcode"
        val COLUMN_A_SSID = "ssid"
        val COLUMN_A_LIBELLE = "libelle"
        val COLUMN_A_LATITUDE = "latitude"
        val COLUMN_A_LONGITUDE = "longitude"
        val COLUMN_A_DATE = "adddate"
        val COLUMN_A_FLAGRECCURENCE = "flagnumber"

        val TABLE_LOCATION = "location"
        val COLUMN_L_ADDRESS = "address"
        val COLUMN_L_NICK = "nick"
        val COLUMN_L_LAT = "lat"
        val COLUMN_L_LONG = "long"

        private val DATABASE_NAME = "ImHome.db"
        private val DATABASE_VERSION = 5

        // Commande sql pour la création de la base de données
        private val DATABASE_CREATE_WIFI = "CREATE TABLE `wifi` (" +
                " `libelle` MEDIUMTEXT NULL DEFAULT NULL," +
                " `ssid` MEDIUMTEXT NULL DEFAULT NULL," +
                " `hashcode` INTEGER NOT NULL DEFAULT NULL, " +
                " `favorite` bit NULL DEFAULT NULL, " +
                " PRIMARY KEY (`ssid`)" +
                " );"

        // Commande sql pour la création de la base de données
        private val DATABASE_CREATE_AVERT = "CREATE TABLE `avert` (" +
                "`id` MEDIUMTEXT NULL DEFAULT NULL," +
                "`libelle` MEDIUMTEXT NULL DEFAULT NULL," +
                "`ssid` MEDIUMTEXT NULL DEFAULT NULL," +
                "`messagetext` MEDIUMTEXT NULL DEFAULT NULL," +
                "`hashcode` INTEGER NOT NULL DEFAULT NULL," +
                "`adddate` DATE NULL DEFAULT NULL," +
                "`contactname` MEDIUMTEXT NOT NULL DEFAULT 'NULL'," +
                "`contactnumber` MEDIUMTEXT NOT NULL DEFAULT 'NULL'," +
                "`latitude` DOUBLE NULL DEFAULT NULL," +
                "`longitude` DOUBLE NULL DEFAULT NULL," +
                "`flagnumber` INTEGER NOT NULL DEFAULT NULL" +
                " );"

        // Commande sql pour la création de la base de données
        private val DATABASE_CREATE_LOCATION = "CREATE TABLE `location` (" +
                " `address` MEDIUMTEXT NULL DEFAULT NULL," +
                " `nick` MEDIUMTEXT NULL DEFAULT NULL," +
                " `lat` DOUBLE NULL DEFAULT NULL," +
                " `long` DOUBLE NULL DEFAULT NULL" +
                " );"
    }
}

