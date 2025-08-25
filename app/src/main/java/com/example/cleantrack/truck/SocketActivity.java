package com.example.cleantrack.truck;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.cleantrack.R;
import com.example.cleantrack.truck.model.GeofenceTracker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import io.socket.client.IO;
import io.socket.client.Socket;
import com.example.cleantrack.truck.model.Truck;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URISyntaxException;
import java.util.Set;


public class SocketActivity extends AppCompatActivity {

    private Socket mSocket;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private GeofenceTracker tracker;

    private static final String roomId = "Trucks";
    private static  final String  role= "TruckDriver";
    private Truck truck;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Make sure this is your layout

        truck = new Truck("dhairya", 0.0, 0.0, true);
        tracker = GeofenceTracker.getInstance(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }

        setupSocket();


    }

    private void setupSocket() {

        IO.Options opts = new IO.Options();
        opts.forceNew = true;
        opts.reconnection = true;
        opts.timeout = 10000; // 10 second timeout
        opts.reconnectionAttempts = 5;
        opts.reconnectionDelay = 1000;
        opts.transports = new String[]{"websocket", "polling"};
        opts.query = "role=truck";

        try {


            mSocket = IO.socket("http://10.0.2.2:3030",opts); // Replace with your IP
            mSocket.connect();

            mSocket.on(Socket.EVENT_CONNECT, args -> {

//                mSocket.emit("join",roomId,role);
                Log.d("SocketActivity", "✅ Socket connected");
            });

            mSocket.on("messageFromServer", args -> {
                String response = args[0].toString();
                Log.d("SocketActivity", "📨 From Server: " + response);
            });

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMinUpdateIntervalMillis(500)
                .setMaxUpdateDelayMillis(1000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;

                for (android.location.Location location : locationResult.getLocations()) {
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();

                    truck.setLat(lat);
                    truck.setLog(lng);


                    try {
                        JSONObject data = new JSONObject();
                        data.put("truck_id", truck.getTruck_id());
                        data.put("lat", truck.getLat());
                        data.put("log", truck.getLog());
                        data.put("currentGeofences",new JSONArray(tracker.getActiveGeofences()));

                        if (mSocket != null && mSocket.connected()) {
                            mSocket.emit("updateLocation", data);
                            Log.d("SocketActivity", "📤 Sent: " + data.toString());
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSocket != null) {
            mSocket.disconnect();
        }
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }


}
