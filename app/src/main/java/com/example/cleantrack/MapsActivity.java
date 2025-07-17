package com.example.cleantrack; // 📦 This is your app’s package name (same as in Manifest)

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.fragment.app.FragmentActivity; // 🧱 Needed because Map is in a Fragment
import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory; // 🎥 For moving/zooming the map
import com.google.android.gms.maps.GoogleMap; // 🌍 The main map object
import com.google.android.gms.maps.OnMapReadyCallback; // 📞 Callback triggered when the map is ready
import com.google.android.gms.maps.SupportMapFragment; // 🧩 The map UI component
import com.google.android.gms.maps.model.LatLng; // 📍 Represents a coordinate (latitude, longitude)
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Objects;



public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private BroadcastReceiver locationUpdateReceiver;
    private GeofencingClient geofencingClient;
    PendingIntent geofencePendingIntent;

    Geofence geofence = new Geofence.Builder()
            .setRequestId("CleanTrackGeofence")
            .setCircularRegion(
                    22.678722,   // latitude
                    72.880417,   // longitude
                100    // radius in meters
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(
                    Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT
            )
            .build();

    private GeofencingRequest getgeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger( Geofence.GEOFENCE_TRANSITION_ENTER |
                Geofence.GEOFENCE_TRANSITION_EXIT) ;
        builder.addGeofence(geofence);
        return builder.build();
    }

//    private GeofencingRequest getgeofencingRequest = new GeofencingRequest.Builder()
//            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
//            .addGeofence(geofence)
//            .build();

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }



    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps); // 🖼️ Tells Android: use activity_maps.xml for UI
        locationUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Objects.equals(intent.getAction(), LocationService.ACTION_LOCATION_BROADCAST)) {
                    double latitude = intent.getDoubleExtra(LocationService.EXTRA_LATITUDE, 0.0);
                    double longitude = intent.getDoubleExtra(LocationService.EXTRA_LONGITUDE, 0.0);
                    Log.d(TAG, "Received live Location Update: " + latitude + ", " + longitude);
                    updateMarkerPosition(latitude, longitude); // Update marker on map
                }
            }
        };
        // 🔁 Get the map fragment from the layout and tell it to notify us when it’s ready
        requestAllPermissions(); // Handle permissions and start service

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map); // `map` is the ID in activity_maps.xml

        if (mapFragment != null) {
            mapFragment.getMapAsync(this); // 🔔 Trigger the map load callback when map is ready
        }
        if (geofencePendingIntent == null) {
            Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);

            geofencePendingIntent = PendingIntent.getBroadcast(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE  // ✅ Add FLAG_MUTABLE or FLAG_IMMUTABLE here
            );
        }




        geofencingClient = LocationServices.getGeofencingClient(this);
        FusedLocationProviderClient fusedClient = LocationServices.getFusedLocationProviderClient(this);
        fusedClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                float[] distance = new float[1];
                Location.distanceBetween(
                        location.getLatitude(), location.getLongitude(),
                        22.677661, 72.882078, // geofence center
                        distance
                );

                if (distance[0] > 100) { // not inside
                    geofencingClient.addGeofences(getgeofencingRequest(), getGeofencePendingIntent());
                } else {
                    Log.d("GEOFENCE", "User is already inside geofence, not adding");
                }
            }
        });


        geofencingClient.addGeofences(getgeofencingRequest(),getGeofencePendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences added
                        // ...
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to add geofences
                        // ...
                    }
                });

    }



    private void requestAllPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                requestLocationPermissionsAndStartService();
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if(isGranted){
                    requestLocationPermissionsAndStartService();
                } else{
                    Toast.makeText(MapsActivity.this, "Notification Permission Denied", Toast.LENGTH_SHORT);
                }
            });

    private void enableMyLocationUI() {
        if (mMap == null) {
            Log.d(TAG, "Map not loaded");
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap; // 🔗 Save map reference for future use (zoom, markers, etc.)

        // 🌐 Create a LatLng object for a specific location (Ahmedabad here)
        enableMyLocationUI();
        LatLng DDU = new LatLng(22.6796337, 72.8801478);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DDU, 19));
        Log.d(TAG, "Camera moved to default location: " + DDU.latitude + ", " + DDU.longitude);
        LocalBroadcastManager.getInstance(this).registerReceiver(locationUpdateReceiver,
                new IntentFilter(LocationService.ACTION_LOCATION_BROADCAST));
    }

    private void updateMarkerPosition(double latitude, double longitude) {
        Log.d(TAG, "updateMarkerPosition() called with Lat: " + latitude + ", Lng: " + longitude);

        if (mMap == null) {
            Log.e(TAG, "updateMarkerPosition: mMap is NULL. Map is not ready yet!");
            return;
        }

        LatLng newLocation = new LatLng(latitude, longitude);

        if (latitude == 0.0 && longitude == 0.0) {
            Log.w(TAG, "updateMarkerPosition: Received (0,0) coordinates, ignoring marker update.");
            return;
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLng(newLocation));
        Log.d(TAG, "updateMarkerPosition: Animated camera to new location.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MapsActivity onDestroy");

        if (locationUpdateReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(locationUpdateReceiver);
            Log.d(TAG, "BroadcastReceiver unregistered.");
        } else {
            Log.w(TAG, "BroadcastReceiver was null in onDestroy.");
        }


        Intent serviceIntent = new Intent(this, LocationService.class);
        stopService(serviceIntent);
        Log.d(TAG, "LocationService stop requested from MapsActivity onDestroy.");
    }

    private final ActivityResultLauncher<String> fineLocationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "Permissions Granted");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        requestBackgroundLocationPermission();
                    }
                } else {
                    Log.d(TAG, "Not Granted");
                    Toast.makeText(MapsActivity.this, "Location Permission Denied", Toast.LENGTH_SHORT);
                }
            });

    private final ActivityResultLauncher<String> backgroundLocationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "All Permissions Granted");
                    enableMyLocationUI();
                    startLocationService();
                } else {
                    Log.d(TAG, "Background permission not Granted");
                    Toast.makeText(MapsActivity.this, "Background Permission Denied", Toast.LENGTH_SHORT);
                }
            });

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void requestBackgroundLocationPermission() {
        if ((ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
                PackageManager.PERMISSION_GRANTED)){
            Log.d(TAG, "Background Location Permission Granted");
            startLocationService();
        } else if ((ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION))) {
            Log.d(TAG, "Background Location permission rationale");
            showRationaleDialog("Background location is required so we can continue to provide updates when the app is not visible.",
                    () -> backgroundLocationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION));
        } else {
            backgroundLocationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }
    }

    private void requestLocationPermissionsAndStartService() {
        Log.d(TAG, "requestLocationPermissionsAndStartService() called.");
        if ((ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED)){
            Log.d(TAG, "Fine Location Permission Granted");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestBackgroundLocationPermission();
            }
        } else if ((ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACCESS_FINE_LOCATION))) {
            Log.d(TAG, "Fine Location permission rationale");
            showRationaleDialog("Location service is required for providing location-based services.",
                    () -> fineLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION));
        } else {
            fineLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void showRationaleDialog(String s, Runnable onOk) {
        new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage(s)
                .setPositiveButton("Ok", ((dialog, which) -> onOk.run()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startLocationService() {
        if(!LocationService.isRunning){
            Intent serviceIntent = new Intent(MapsActivity.this, LocationService.class);
            ContextCompat.startForegroundService(this, serviceIntent);
            Toast.makeText(this, "Location service starting...", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "LocationService start requested.");
        }
    }
}
