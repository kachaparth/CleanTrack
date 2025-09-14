package com.example.cleantrack.user;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

//import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.cleantrack.R;
import com.google.android.gms.internal.maps.zzaj;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DustbinFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Button setGeofences;
    private final String TAG="DustbinFragment";
    private final int fillColor = Color.argb(100, 255, 0, 0);
    private final int strokeColor = Color.RED;
    private final int selectedFillColor = Color.argb(100, 0, 255, 0);
    private final int selectedStrokeColor = Color.GREEN;
    private Set<String> selectedCircleTags = new HashSet<>();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dustbin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setGeofences = view.findViewById(R.id.setGeofences);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        if(mapFragment != null){
            mapFragment.getMapAsync(this);
        }
        setGeofences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, selectedCircleTags.toString());
            }
        });

    }

    public void displayAllGeofences(){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://127.0.0.1:3030/api/get-all-geofences")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Invalid JSON in Login Error From Server", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()){
                    String responseData = response.body().string();
                    try{
                        JSONArray jsonResponse = new JSONArray(responseData);
                        Log.d(TAG, jsonResponse.toString());

                        requireActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    drawCircles(jsonResponse);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private void drawCircles(JSONArray jsonResponse) throws JSONException {
        for(int i = 0; i< jsonResponse.length(); i++){
            JSONObject geofenceDetails = jsonResponse.getJSONObject(i);
            String geofence_id = geofenceDetails.getString("geofence_id");
            double lat = geofenceDetails.getDouble("lat");
            double lng = geofenceDetails.getDouble("lng");
            double rad = geofenceDetails.getDouble("radius");

            CircleOptions circleOptions = new CircleOptions();
            LatLng centre = new LatLng(lat, lng);
            circleOptions.center(centre)
                    .radius(rad)
                    .fillColor(fillColor)
                    .strokeColor(strokeColor)
                    .strokeWidth(2)
                    .clickable(true);
            if(mMap!=null){
                Circle circle = mMap.addCircle(circleOptions);
                circle.setTag(geofence_id);
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(centre));
                marker.setTag(circle);
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        LatLng nadiadLocation = new LatLng(22.677115, 72.8819087);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(nadiadLocation)
                .zoom(10.0f)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mMap.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
            @Override
            public void onCircleClick(@NonNull Circle circle) {
                // The SDK gives you the EXACT circle that was clicked!
                // No loops, no distance math. 👍

                String circleTag = (String) circle.getTag();
                if (circleTag == null) return; // Should always have a tag

                if (selectedCircleTags.contains(circleTag)) {
                    // It was selected, so DE-SELECT
                    selectedCircleTags.remove(circleTag);
                    circle.setStrokeColor(strokeColor);
                    circle.setFillColor(fillColor);

                } else {
                    // It was not selected, so SELECT
                    selectedCircleTags.add(circleTag);
                    // Set style to "selected"
                    circle.setStrokeColor(selectedStrokeColor);
                    circle.setFillColor(selectedFillColor);
                }

                // Your list of selected IDs is always up to date
                Log.d("CleanTrackApp", "Selected circles: " + selectedCircleTags.toString());
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                String circleTag = "";
                Circle circle = null;
                Object markerTag = marker.getTag();
                if (markerTag == null) return false; // Should always have a tag
                if(markerTag instanceof Circle){
                    circle = (Circle) markerTag;
                    circleTag = (String) circle.getTag();
                }
                if(circle == null || circleTag == null) return false;
                if (selectedCircleTags.contains(circleTag)) {
                    // It was selected, so DE-SELECT
                    selectedCircleTags.remove(circleTag);
                    circle.setStrokeColor(strokeColor);
                    circle.setFillColor(fillColor);

                } else {
                    // It was not selected, so SELECT
                    selectedCircleTags.add(circleTag);
                    // Set style to "selected"
                    circle.setStrokeColor(selectedStrokeColor);
                    circle.setFillColor(selectedFillColor);
                }

                // Your list of selected IDs is always up to date
                Log.d("CleanTrackApp", "Selected circles: " + selectedCircleTags.toString());
                return true;
            }
        });
        displayAllGeofences();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "Destroying...");
        mMap = null;
    }
}