package com.jpwolfso.geofencer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.jpwolfso.geofencer.database.GeoDatabase;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class GeoService extends Service {

    static boolean isRunning;
    LocationManager locationManager;
    LocationProvider locationProvider;
    Location location;
    List<GeoPoint> list;
    Polyline line;
    Notification.Builder notificationBuilder;
    Notification notification;
    NotificationManager notificationManager;

    LocationListener locationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onLocationChanged(Location location) {


            for (int i = 0; i < GeoDatabase.getInstance(getApplicationContext()).beaconsDao().getActiveBeacons().size(); i++) {
                int myindex = GeoDatabase.getInstance(getApplicationContext()).beaconsDao().getActiveBeacons().get(i);
                Double latitude = GeoDatabase.getInstance(getApplicationContext()).beaconsDao().getBeaconLat(myindex);
                Double longitude = GeoDatabase.getInstance(getApplicationContext()).beaconsDao().getBeaconLong(myindex);
                Double radius = GeoDatabase.getInstance(getApplicationContext()).beaconsDao().getBeaconRad(myindex);

                GeoPoint cpoint = new GeoPoint(latitude, longitude);
                GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

                geofenceAction(geoPoint, cpoint, radius);
            }

        }
    };


    public GeoService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);

        try {
            location = locationManager.getLastKnownLocation(locationProvider.getName());
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        } catch (SecurityException e) {
            Log.e("jpwolfso", String.valueOf(e));
        }

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        isRunning = true;
        notificationManager = ((NotificationManager) getSystemService(NOTIFICATION_SERVICE));
        notificationManager.createNotificationChannel(new NotificationChannel("mygeoservice", "Geofence Listener", NotificationManager.IMPORTANCE_DEFAULT));
        notificationBuilder = new Notification.Builder(this, "mygeoservice")
                .setContentTitle("Geofence Listener Service")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentText("Getting ready...");
        notification = notificationBuilder.build();

        startForeground(567, notification);

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    protected void geofenceAction(GeoPoint myGeoPoint, GeoPoint geofencePoint, Double geofenceRadius) {

        list = new ArrayList<>();
        list.add(myGeoPoint);
        list.add(geofencePoint);

        line = new Polyline();
        line.setPoints(list);

        if (line.getDistance() <= geofenceRadius) {
            notificationManager.notify(567, notificationBuilder.setContentText("You are inside the geofence.").build());
        } else {
            notificationManager.notify(567, notificationBuilder.setContentText("You are outside the geofence.").build());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        isRunning = false;

        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }

    }
}
