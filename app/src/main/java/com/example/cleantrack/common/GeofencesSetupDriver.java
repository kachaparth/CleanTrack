package com.example.cleantrack.common;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class GeofencesSetupDriver {
    private GeofencingClient geofencingClient;
    private  Context context;
    PendingIntent geofencePendingIntent;


    public GeofencesSetupDriver(Context context) {
        this.context = context;
        this.geofencingClient = LocationServices.getGeofencingClient(context);


    }

    public List<Geofence> buildDriverGeofences() {
        List<Geofence> geofenceList = new ArrayList<>();

        geofenceList.add(new Geofence.Builder()
                .setRequestId("G1")
                .setCircularRegion(
                        22.677460, 72.882672, 100  // ✅ Replace with dynamic coordinates if needed
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(
                        Geofence.GEOFENCE_TRANSITION_ENTER |
                                Geofence.GEOFENCE_TRANSITION_EXIT
                )
                .build());

        // You can add more geofences dynamically here (from server, DB, etc.)
        return geofenceList;
    }

    private GeofencingRequest getGeofencingRequest(List<Geofence> geofences) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger( Geofence.GEOFENCE_TRANSITION_ENTER |
                Geofence.GEOFENCE_TRANSITION_EXIT) ;
        builder.addGeofences(buildDriverGeofences());
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(context, GeofenceBroadcastReceiver.class);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;

        // ✅ For Android 12+ we must add MUTABLE flag
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_MUTABLE;
        }

        geofencePendingIntent = PendingIntent.getBroadcast(context, 0, intent, flags);
        return geofencePendingIntent;
    }

    public void registerDriverGeofences() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w("GeofencesSetupDriver", "registerDriverGeofences: Location permission not granted");
            return;
        }

        List<Geofence> geofences = buildDriverGeofences();

        geofencingClient.addGeofences(getGeofencingRequest(geofences), getGeofencePendingIntent())
                .addOnSuccessListener(unused -> {
                    Log.d("GeofencesSetupDriver", "Driver geofences successfully registered");
                    Toast.makeText(context, "Driver geofences added!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("GeofencesSetupDriver", "Failed to register driver geofences", e);
                    Toast.makeText(context, "Failed to add geofences", Toast.LENGTH_SHORT).show();
                });
    }

}
