package com.example.cleantrack.truck;
import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.cleantrack.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationService extends Service {

    private static final String TAG = "LocationService";
    private static final String CHANNEL_ID = "LocationServiceChannel";
    private static final int NOTIFICATION_ID = 1;

    // Action and Extra keys for broadcasting location updates
    public static final String ACTION_LOCATION_BROADCAST = "com.example.LOCATION_UPDATE";
    public static final String EXTRA_LATITUDE = "latitude";
    public static final String EXTRA_LONGITUDE = "longitude";
    public static boolean isRunning = false;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        Log.d(TAG, "Service onCreate");

        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Live Location Tracking")
                .setContentText("Your location is being updated in the background.")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true);

        startForeground(NOTIFICATION_ID, builder.build());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Notification permission denied");
        } else {
            NotificationManagerCompat.from(this)
                    .notify(NOTIFICATION_ID, builder.build());
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000) // 2-second interval
                .setMinUpdateIntervalMillis(2000) // Minimum 1-second interval
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        Log.d(TAG, "Service received Location: " + location.getLatitude() + ", " + location.getLongitude());
                        Intent broadcastIntent = new Intent(ACTION_LOCATION_BROADCAST);
                        broadcastIntent.putExtra(EXTRA_LATITUDE, location.getLatitude());
                        broadcastIntent.putExtra(EXTRA_LONGITUDE, location.getLongitude());
                        LocalBroadcastManager.getInstance(LocationService.this).sendBroadcast(broadcastIntent);
                    }
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service onStartCommand");
        requestLocationUpdates();
        return START_STICKY; // Service will restart if killed by system
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        Log.d(TAG, "Service onDestroy");
        fusedLocationClient.removeLocationUpdates(locationCallback); // Stop updates
        Log.d(TAG, "Location updates removed. LocationService stopped.");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // This service is not designed to be bound in this scenario, so return null.
        // Communication with MapsActivity is via Broadcasts.
        return null;
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Location permissions not granted. Service cannot request updates.");
            stopSelf();
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        Log.d(TAG, "Location updates requested by service.");
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Location Service Channel",
                NotificationManager.IMPORTANCE_LOW
        );
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(serviceChannel);
        }
    }
}