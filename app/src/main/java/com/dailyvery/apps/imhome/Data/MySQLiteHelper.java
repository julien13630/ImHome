package com.dailyvery.apps.imhome.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by julie on 22/10/2015.
 */

    public class MySQLiteHelper extends SQLiteOpenHelper {

        public static final String TABLE_WIFI = "wifi";
        public static final String COLUMN_W_LIBELLE = "libelle";
        public static final String COLUMN_W_SSID = "ssid";
        public static final String COLUMN_W_HASHCODE = "hashcode";
        public static final String COLUMN_W_FAVORIT = "favorite";

        public static final String TABLE_AVERT = "avert";
        public static final String COLUMN_A_ID = "id";
        public static final String COLUMN_A_CONTACTNAME = "contactname";
        public static final String COLUMN_A_CONTACTNUMBER = "contactnumber";
        public static final String COLUMN_A_MESSAGETEXT = "messagetext";
        public static final String COLUMN_A_HASHCODE = "hashcode";
        public static final String COLUMN_A_SSID = "ssid";
        public static final String COLUMN_A_LIBELLE = "libelle";
        public static final String COLUMN_A_LATITUDE = "latitude";
        public static final String COLUMN_A_LONGITUDE = "longitude";
        public static final String COLUMN_A_DATE = "adddate";
        public static final String COLUMN_A_FLAGRECCURENCE = "flagnumber";

        public static final String TABLE_LOCATION = "location";
        public static final String COLUMN_L_ADDRESS = "address";
        public static final String COLUMN_L_NICK = "nick";
        public static final String COLUMN_L_LAT = "lat";
        public static final String COLUMN_L_LONG = "long";

        private static final String DATABASE_NAME = "ImHome.db";
        private static final int DATABASE_VERSION = 5;

        // Commande sql pour la création de la base de données
        private static final String DATABASE_CREATE_WIFI = "CREATE TABLE `wifi` (" +
                " `libelle` MEDIUMTEXT NULL DEFAULT NULL," +
                " `ssid` MEDIUMTEXT NULL DEFAULT NULL," +
                " `hashcode` INTEGER NOT NULL DEFAULT NULL, " +
                " `favorite` bit NULL DEFAULT NULL, " +
                " PRIMARY KEY (`ssid`)" +
                " );" ;

        // Commande sql pour la création de la base de données
        private static final String DATABASE_CREATE_AVERT = "CREATE TABLE `avert` ("+
            "`id` MEDIUMTEXT NULL DEFAULT NULL,"+
            "`libelle` MEDIUMTEXT NULL DEFAULT NULL,"+
            "`ssid` MEDIUMTEXT NULL DEFAULT NULL,"+
            "`messagetext` MEDIUMTEXT NULL DEFAULT NULL,"+
            "`hashcode` INTEGER NOT NULL DEFAULT NULL,"+
            "`adddate` DATE NULL DEFAULT NULL,"+
            "`contactname` MEDIUMTEXT NOT NULL DEFAULT 'NULL',"+
            "`contactnumber` MEDIUMTEXT NOT NULL DEFAULT 'NULL',"+
            "`latitude` DOUBLE NULL DEFAULT NULL,"+
            "`longitude` DOUBLE NULL DEFAULT NULL,"+
            "`flagnumber` INTEGER NOT NULL DEFAULT NULL"+
            " );";

    // Commande sql pour la création de la base de données
    private static final String DATABASE_CREATE_LOCATION = "CREATE TABLE `location` (" +
            " `address` MEDIUMTEXT NULL DEFAULT NULL," +
            " `nick` MEDIUMTEXT NULL DEFAULT NULL," +
            " `lat` DOUBLE NULL DEFAULT NULL," +
            " `long` DOUBLE NULL DEFAULT NULL" +
            " );" ;


        public MySQLiteHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            database.execSQL(DATABASE_CREATE_WIFI);
            database.execSQL(DATABASE_CREATE_AVERT);
            database.execSQL(DATABASE_CREATE_LOCATION);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(MySQLiteHelper.class.getName(),
                    "Upgrading database from version " + oldVersion + " to "
                            + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_WIFI);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_AVERT);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);

            onCreate(db);
        }
    }

