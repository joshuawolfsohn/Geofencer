package com.jpwolfso.geofencer.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "beacons")
public class Beacons {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "latitude")
    private Double latitude;

    @ColumnInfo(name = "longitude")
    private Double longitude;

    @ColumnInfo(name = "radius")
    private Double radius;

    @ColumnInfo(name = "active")
    private boolean active;

    public Beacons(int id, Double latitude, Double longitude, Double radius, boolean active) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getRadius() {
        return radius;
    }

    public boolean getActive() {
        return active;
    }
}
