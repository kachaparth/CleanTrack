package com.example.cleantrack.user;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.cleantrack.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import io.socket.client.IO;
import io.socket.client.Socket;
import com.example.cleantrack.truck.model.Truck;

public class UserSocketActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private final HashMap<String, Marker> markers = new HashMap<>();
    private SupportMapFragment mapFragment;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private Socket mSocket;
    private static final String roomId = "Users";
    private static  final String  role= "User";
    TextView t1;

    private  Set <Truck> trucks= new HashSet<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

       super.onCreate(savedInstanceState);
       setContentView(R.layout.user_socket);
        t1 = findViewById(R.id.textView);

        setupSocket();
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync( this); // since your Activity implements OnMapReadyCallback
        }





    }

    private void setupSocket() {

        IO.Options opts = new IO.Options();
        opts.forceNew = true;
        opts.reconnection = true;
        opts.timeout = 10000; // 10 second timeout
        opts.reconnectionAttempts = 5;
        opts.reconnectionDelay = 1000;
        opts.transports = new String[]{"websocket", "polling"};
        opts.query = "role=user";
        try {


            mSocket = IO.socket("http://10.121.105.112:3030",opts); // Replace with your IP
            mSocket.connect();

            mSocket.on(Socket.EVENT_CONNECT, args -> {

                try {

                    String user_id = getSharedPreferences("UserProfiles", MODE_PRIVATE).getString("user_id", "N/A");

                    JSONObject data = new JSONObject();
                    data.put("user_id",user_id);

                    mSocket.emit("getLiveUpdates",data);
                }
                catch (Exception e)
                {
                    Log.e("error", "❌ Error sending data: "+e);
                }

                mSocket.emit("join",roomId,role);
                Log.d("SocketActivity", "✅ Socket connected");
            });




            mSocket.on("messageFromServer", args -> {
                String response = args[0].toString();
                Log.d("SocketActivity", "📨 From Server: " + response);
            });

            mSocket.on("TruckLocation", args -> {
                JSONObject data= (JSONObject) args[0];
                Log.d("TruckLocation", "📨 From Server: " + data.toString());
                if(data !=null)
                {
                    try {
//                        Log.d("TruckObject", data.getString("truck_id"));
//                        Log.d("TruckObject", Double.toString(data.getDouble("lat")));
//                        Log.d("TruckObject", Double.toString(data.getDouble("lng")));
                        Truck truck = new Truck(
                                data.getString("truck_id"),
                                data.getDouble("lat"),
                                data.getDouble("lng"),
                                true
                        );

                        // Remove existing truck with same ID to avoid duplicates
//                        Log.d("TruckObject", truck.toString());
                        trucks.removeIf(existingTruck -> existingTruck.getTruck_id().equals(truck.getTruck_id()));
                        trucks.add(truck);

                        // Update UI on main thread (CRITICAL for Android)
                        runOnUiThread(() -> {
//                            updateTruckMarker(truck); // add/update marker on map

//                            moveCameraToIncludeAll(); // 👈 zoom map to include all markers

                            // update the TextView list
                            StringBuilder sb = new StringBuilder();
                            sb.append("Active Trucks (").append(trucks.size()).append("):\n\n");
                            Log.d("TruckLocation",sb.toString());
                            trucks.forEach(e -> sb.append("🚛 ").append(e.toString()).append("\n\n"));
                            t1.setText(sb.toString());
                        });


                        Log.d("UserSocketActivity", "✅ Updated truck: " + truck.toString());

                    } catch (JSONException e) {
                        Log.e("UserSocketActivity", "❌ Error parsing truck data: " + e.getMessage());
                        // Don't throw RuntimeException - just log the error
                    }

                }

            });



        } catch (URISyntaxException e) {
            Log.e("error", Objects.requireNonNull(e.getMessage()));
        }

    }


    private void updateTruckMarker(Truck truck) {
        if (mMap == null) return;

        double lat = truck.getLat();
        double lng = truck.getLog(); // change if your Truck model uses getLng()

        LatLng pos = new LatLng(lat, lng);
        Marker existing = markers.get(truck.getTruck_id());

        if (!truck.isActive()) {
            if (existing != null) {
                existing.remove();
                markers.remove(truck.getTruck_id());
            }
            return;
        }

        if (existing == null) {
            MarkerOptions mo = new MarkerOptions()
                    .position(pos)
                    .title("Truck " + truck.getTruck_id())
                    .icon(resizePngIcon(this, R.drawable.ic_truck, 80, 80))
                    .anchor(0.5f, 0.5f);
            Marker marker = mMap.addMarker(mo);
            markers.put(truck.getTruck_id(), marker);
        } else {
            existing.setPosition(pos);
        }

    }

    private void moveCameraToIncludeAll() {
        if (markers.isEmpty() || mMap == null) return;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker m : markers.values()) builder.include(m.getPosition());
        LatLngBounds bounds = builder.build();
        int padding = 120; // px
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
    }
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable drawable = ContextCompat.getDrawable(context, vectorResId);
        Objects.requireNonNull(drawable).setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private BitmapDescriptor resizePngIcon(Context context, int resId, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return BitmapDescriptorFactory.fromBitmap(resizedBitmap);
    }



}

