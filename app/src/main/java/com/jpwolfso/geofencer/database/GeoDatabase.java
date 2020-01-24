package com.jpwolfso.geofencer.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Beacons.class}, exportSchema = false, version = 1)
public abstract class GeoDatabase extends RoomDatabase {

    private static final String DB_NAME = "geo_db";
    private static GeoDatabase instance;

    public static synchronized GeoDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    GeoDatabase.class,
                    DB_NAME)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }

    public abstract BeaconsDao beaconsDao();
}
