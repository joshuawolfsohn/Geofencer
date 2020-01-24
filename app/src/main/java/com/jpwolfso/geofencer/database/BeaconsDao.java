package com.jpwolfso.geofencer.database;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface BeaconsDao {

    @Query("Select * from beacons")
    List<Beacons> getBeaconsList();

    @Query("Select latitude from beacons where id = :myid")
    Double getBeaconLat(Integer myid);

    @Query("Select longitude from beacons where id = :myid")
    Double getBeaconLong(Integer myid);

    @Query("Select radius from beacons where id = :myid")
    Double getBeaconRad(Integer myid);

    @Query("Select id from beacons where active = 1")
    List<Integer> getActiveBeacons();

    @Insert
    void insertBeacons(Beacons beacons);

    @Update
    void updateBeacons(Beacons beacons);

    @Query("Delete from beacons where latitude = :mylat and longitude = :mylong and radius = :myrad")
    void deleteBeacons(Double mylat, Double mylong, Double myrad);

}
