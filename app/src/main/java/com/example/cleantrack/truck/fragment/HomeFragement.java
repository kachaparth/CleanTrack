package com.example.cleantrack.truck.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.cleantrack.R;
import com.example.cleantrack.truck.model.GeofenceTracker;
import com.example.cleantrack.truck.model.Truck;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.socket.client.IO;
import io.socket.client.Socket;

public class HomeFragement extends Fragment {


    private String TruckId;
    private ToggleButton toggleStatus;
    private ListView listGeofences;
    private ArrayAdapter<String> geofenceAdapter;
    private Socket mSocket;
    private GeofenceTracker tracker;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private LocationCallback locationCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tracker = GeofenceTracker.getInstance(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_fragement, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toggleStatus = view.findViewById(R.id.toggle_status);
        toggleStatus.setChecked(true);
        listGeofences = view.findViewById(R.id.list_geofences);

        tracker = GeofenceTracker.getInstance(requireContext());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        toggleStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                Toast.makeText(getContext(), "Truck is ACTIVE", Toast.LENGTH_SHORT).show();
                 // Load geofences when active
                startTracking();
            } else {
                Toast.makeText(getContext(), "Truck is DEACTIVE", Toast.LENGTH_SHORT).show();
               stopTracking();
            }
        });

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }
    private void stopTracking() {
        if (mSocket != null) mSocket.disconnect();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        if (geofenceAdapter != null) {
            geofenceAdapter.clear();
            geofenceAdapter.notifyDataSetChanged();
        }
    }


    private void startTracking()
    {
        setupSocket();
        checkPermissionAndStartLocation();
        startLocationUpdates();
    }

    private void setupSocket() {
        IO.Options opts = new IO.Options();
        opts.forceNew = true;
        opts.reconnection = true;
        opts.timeout = 10000;
        opts.reconnectionAttempts = 5;
        opts.reconnectionDelay = 1000;
        opts.transports = new String[]{"websocket", "polling"};
        opts.query = "role=truck";

        try {
            mSocket = IO.socket("http://10.121.105.112:3030", opts);
            mSocket.connect();

            mSocket.on(Socket.EVENT_CONNECT, args -> {
                Log.d("HomeFragment", "✅ Socket connected");
            });

            mSocket.on("messageFromServer", args -> {
                String response = args[0].toString();
                Log.d("HomeFragment", "📨 From Server: " + response);
            });

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void checkPermissionAndStartLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }


    private void startLocationUpdates() {
        Log.d("HomeFragment", "start location update");
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setMinUpdateIntervalMillis(1000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;

                for (android.location.Location location : locationResult.getLocations()) {
//
                    try {
                        JSONObject data = new JSONObject();
                        data.put("truck_id", TruckId);
                        data.put("lat", location.getLatitude());
                        data.put("lng", location.getLongitude());
                        data.put("currentGeofences", new JSONArray(tracker.getActiveGeofences()));

                        if (mSocket != null && mSocket.connected()) {
                            mSocket.emit("updateLocation", data);
                            Log.d("HomeFragment", "📤 Sent: " + data.toString());
                        }

                        updateGeofenceList(); // ✅ Update UI live

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private void updateGeofenceList() {
        Set<String> activeGeofences = tracker.getActiveGeofences();

        // Convert Set to List properly
        List<String> geofenceList = new ArrayList<>(activeGeofences);

        geofenceAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                geofenceList
        );

        listGeofences.setAdapter(geofenceAdapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }

}