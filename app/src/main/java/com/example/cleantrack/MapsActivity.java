package com.example.cleantrack; // 📦 This is your app’s package name (same as in Manifest)

import androidx.fragment.app.FragmentActivity; // 🧱 Needed because Map is in a Fragment
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory; // 🎥 For moving/zooming the map
import com.google.android.gms.maps.GoogleMap; // 🌍 The main map object
import com.google.android.gms.maps.OnMapReadyCallback; // 📞 Callback triggered when the map is ready
import com.google.android.gms.maps.SupportMapFragment; // 🧩 The map UI component
import com.google.android.gms.maps.model.LatLng; // 📍 Represents a coordinate (latitude, longitude)
import com.google.android.gms.maps.model.MarkerOptions; // 📌 To place a marker on the map

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap; // 🎯 Reference to the GoogleMap once it's ready

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
    }

    // 🔧 This method is called once the map is ready to use
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap; // 🔗 Save map reference for future use (zoom, markers, etc.)

        // 🌐 Create a LatLng object for a specific location (Ahmedabad here)
        LatLng DDU = new LatLng(22.6796337, 72.8801478);

        // 📌 Add a marker at the location with a title
        mMap.addMarker(new MarkerOptions().position(DDU).title("Marker in Ahmedabad"));

        // 🔍 Move the camera to the location with a zoom level (0 = far, 21 = street)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DDU, 17));
    }
}
