package com.example.cleantrack; // 📦 This is your app’s package name (same as in Manifest)

import androidx.fragment.app.FragmentActivity; // 🧱 Needed because Map is in a Fragment
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory; // 🎥 For moving/zooming the map
import com.google.android.gms.maps.GoogleMap; // 🌍 The main map object
import com.google.android.gms.maps.OnMapReadyCallback; // 📞 Callback triggered when the map is ready
import com.google.android.gms.maps.SupportMapFragment; // 🧩 The map UI component
import com.google.android.gms.maps.model.LatLng; // 📍 Represents a coordinate (latitude, longitude)
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions; // 📌 To place a marker on the map

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private static final int PERMISSION_REQUEST_CODE = 1001;

    private GoogleMap mMap;
    private Marker currentLocationMarker;
    private BroadcastReceiver locationUpdateReceiver;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps); // 🖼️ Tells Android: use activity_maps.xml for UI

        // 🔁 Get the map fragment from the layout and tell it to notify us when it’s ready
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map); // `map` is the ID in activity_maps.xml

        if (mapFragment != null) {
            mapFragment.getMapAsync(this); // 🔔 Trigger the map load callback when map is ready
        }

        locationUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (LocationService.ACTION_LOCATION_BROADCAST.equals(intent.getAction())) {
                    double latitude = intent.getDoubleExtra(LocationService.EXTRA_LATITUDE, 0.0);
                    double longitude = intent.getDoubleExtra(LocationService.EXTRA_LONGITUDE, 0.0);
                    Log.d(TAG, "Received live Location Update: " + latitude + ", " + longitude);
                    updateMarkerPosition(latitude, longitude); // Update marker on map
                }
            }
        };

        registerReceiver(locationUpdateReceiver,
                new IntentFilter(LocationService.ACTION_LOCATION_BROADCAST),
                Context.RECEIVER_NOT_EXPORTED);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
//            // For API 33 and above, use the new overload
//            registerReceiver(locationUpdateReceiver,
//                    new IntentFilter(LocationService.ACTION_LOCATION_BROADCAST),
//                    Context.RECEIVER_NOT_EXPORTED);
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // API 31+ (Android 12)
//            // For API 31 & 32, use this overload
//            registerReceiver(locationUpdateReceiver,
//                    new IntentFilter(LocationService.ACTION_LOCATION_BROADCAST),
//                    null, // No permission needed if not exported
//                    null, // No scheduler needed
//                    Context.RECEIVER_NOT_EXPORTED);
//        }// Register receiver

        requestLocationPermissionsAndStartService(); // Handle permissions and start service
    }

    // 🔧 This method is called once the map is ready to use
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap; // 🔗 Save map reference for future use (zoom, markers, etc.)

        // 🌐 Create a LatLng object for a specific location (Ahmedabad here)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            Log.d(TAG, "My Location layer enabled.");
        } else {
            Log.w(TAG, "My Location layer not enabled because location permission not granted.");
        }
        LatLng DDU = new LatLng(22.6796337, 72.8801478);

        // 📌 Add a marker at the location with a title
//        mMap.addMarker(new MarkerOptions().position(DDU).title("My Marker"));

        // 🔍 Move the camera to the location with a zoom level (0 = far, 21 = street)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DDU, 17));
        Log.d(TAG, "Camera moved to default location: " + DDU.latitude + ", " + DDU.longitude);
    }

    private void updateMarkerPosition(double latitude, double longitude) {
        Log.d(TAG, "updateMarkerPosition() called with Lat: " + latitude + ", Lng: " + longitude);

        if (mMap == null) {
            Log.e(TAG, "updateMarkerPosition: mMap is NULL. Map is not ready yet!");
            return;
        }

        LatLng newLocation = new LatLng(latitude, longitude);

        // Check if the LatLng is valid (not 0,0 which can happen with default extras)
        if (latitude == 0.0 && longitude == 0.0) {
            Log.w(TAG, "updateMarkerPosition: Received (0,0) coordinates, ignoring marker update.");
            return;
        }


        if (currentLocationMarker == null) {
            currentLocationMarker = mMap.addMarker(new MarkerOptions().position(newLocation).title("Current Device Location"));
            Log.d(TAG, "updateMarkerPosition: Marker was NULL, ADDED new marker at " + newLocation.latitude + ", " + newLocation.longitude);
        } else {
            currentLocationMarker.setPosition(newLocation);
            Log.d(TAG, "updateMarkerPosition: Marker existed, UPDATED position to " + newLocation.latitude + ", " + newLocation.longitude);
        }

        // Always animate camera to the new location so you can see the marker
        mMap.animateCamera(CameraUpdateFactory.newLatLng(newLocation));
        Log.d(TAG, "updateMarkerPosition: Animated camera to new location.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MapsActivity onDestroy");

        if (locationUpdateReceiver != null) {
            unregisterReceiver(locationUpdateReceiver);
            Log.d(TAG, "BroadcastReceiver unregistered.");
        } else {
            Log.w(TAG, "BroadcastReceiver was null in onDestroy.");
        }


        Intent serviceIntent = new Intent(this, LocationService.class);
        stopService(serviceIntent);
        Log.d(TAG, "LocationService stop requested from MapsActivity onDestroy.");
    }

    private void requestLocationPermissionsAndStartService() {
        Log.d(TAG, "requestLocationPermissionsAndStartService() called.");

        boolean fineLocationGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean backgroundLocationGranted = true; // Default true for older Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            backgroundLocationGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }

        Log.d(TAG, "Fine Location Granted: " + fineLocationGranted);
        Log.d(TAG, "Background Location Granted (if applicable): " + backgroundLocationGranted);

        if (!fineLocationGranted || !backgroundLocationGranted) {
            Log.d(TAG, "Permissions NOT fully granted. Requesting permissions...");
            String[] permissionsToRequest;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissionsToRequest = new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                };
                Log.d(TAG, "Requesting for API Q+ (Fine and Background)");
            } else {
                permissionsToRequest = new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                };
                Log.d(TAG, "Requesting for pre-API Q (Fine only)");
            }
            // Add a log for the permissions array being requested
            for (String perm : permissionsToRequest) {
                Log.d(TAG, "  Permission to request: " + perm);
            }

            ActivityCompat.requestPermissions(this, permissionsToRequest, PERMISSION_REQUEST_CODE);
            Log.d(TAG, "ActivityCompat.requestPermissions() called.");

        } else {
            Log.d(TAG, "All required permissions ALREADY granted. Starting Location Service.");
            startLocationService();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // Always call super

        Log.d(TAG, "onRequestPermissionsResult() called. Request Code: " + requestCode);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            Log.d(TAG, "  Matching PERMISSION_REQUEST_CODE.");

            boolean fineLocationResult = false;
            boolean backgroundLocationResult = true; // Default true for pre-Q or if not requested

            Log.d(TAG, "  Permissions in result array:");
            for (int i = 0; i < permissions.length; i++) {
                Log.d(TAG, "    " + permissions[i] + ": " + (grantResults[i] == PackageManager.PERMISSION_GRANTED ? "GRANTED" : "DENIED"));
                if (permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    fineLocationResult = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && permissions[i].equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    backgroundLocationResult = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                }
            }
            Log.d(TAG, "  Final Fine Location Result: " + fineLocationResult);
            Log.d(TAG, "  Final Background Location Result: " + backgroundLocationResult);


            if (fineLocationResult && backgroundLocationResult) {
                Toast.makeText(this, "Location permissions granted!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Permissions GRANTED. Starting Location Service.");
                startLocationService();
            } else {
                Toast.makeText(this, "Location permissions denied. Continuous background tracking may not work.", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Permissions DENIED. Location Service NOT started.");
            }
        } else {
            Log.d(TAG, "  Request Code MISMATCH. Not handling this permission result.");
        }
    }

    private void startLocationService() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, serviceIntent);
        } else {
            startService(serviceIntent);
        }
        Toast.makeText(this, "Location service starting...", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "LocationService start requested.");
    }
}
