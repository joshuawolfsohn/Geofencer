package com.jpwolfso.geofencer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jpwolfso.geofencer.database.GeoDatabase;

import androidx.core.content.ContextCompat;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == Intent.ACTION_BOOT_COMPLETED && GeoDatabase.getInstance(context).beaconsDao().getActiveBeacons().size() > 0) {
            Intent myintent = new Intent(context, GeoService.class);
            ContextCompat.startForegroundService(context, myintent);
            //Toast.makeText(context, "Boot receiver took " + SystemClock.elapsedRealtime() / 1000 + " seconds.", Toast.LENGTH_SHORT).show();
        }
    }
}
