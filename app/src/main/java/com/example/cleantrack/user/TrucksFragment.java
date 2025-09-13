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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.cleantrack.R;
import com.example.cleantrack.truck.model.Truck;
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

// 1. Make your Fragment implement OnMapReadyCallback
public class TrucksFragment extends Fragment implements OnMapReadyCallback {

    // 2. Copy ALL member variables from your Activity here
    private GoogleMap mMap;
    private final HashMap<String, Marker> markers = new HashMap<>();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private Socket mSocket;
    private static final String roomId = "Users";
    private static final String role = "User";
//    private TextView t1;
    private final Set<Truck> trucks = new HashSet<>();

    /**
     * Use onCreateView just to inflate the layout.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 3. Inflate the NEW fragment_trucks.xml layout
        return inflater.inflate(R.layout.fragment_trucks, container, false);
    }

    /**
     * 4. Use onViewCreated to find views and set up your logic.
     * This runs right after onCreateView and is the correct place for logic
     * that was previously in your Activity's onCreate().
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 5. Find views using the 'view' object passed into this method
//        t1 = view.findViewById(R.id.textView);

        // 6. IMPORTANT: Use getChildFragmentManager() to find a fragment embedded in your fragment's layout
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment != null) {
            // 7. 'this' (the Fragment) is now the callback receiver
            mapFragment.getMapAsync(this);
        }

        // 8. Setup the socket just like before
        setupSocket();
    }

    private void setupSocket() {
        IO.Options opts = new IO.Options();
        // ... (all your options are correct and stay the same) ...
        opts.forceNew = true;
        opts.reconnection = true;
        opts.timeout = 10000;
        opts.reconnectionAttempts = 5;
        opts.reconnectionDelay = 1000;
        opts.transports = new String[]{"websocket", "polling"};
        opts.query = "role=user";
        try {
            mSocket = IO.socket("http://127.0.0.1:3030", opts);
            mSocket.connect();

            mSocket.on(Socket.EVENT_CONNECT, args -> {
                try {
                    // 9. Use requireActivity() or requireContext() to get a Context for things like SharedPreferences
                    String user_id = requireActivity().getSharedPreferences("UserProfiles", Context.MODE_PRIVATE)
                            .getString("user_id", "N/A");

                    JSONObject data = new JSONObject();
                    data.put("user_id", user_id);

                    mSocket.emit("getLiveUpdates", data);
                } catch (Exception e) {
                    Log.e("error", "❌ Error sending data: " + e);
                }
                mSocket.emit("join", roomId, role);
                Log.d("SocketFragment", "✅ Socket connected");
            });

            mSocket.on("messageFromServer", args -> {
                String response = args[0].toString();
                Log.d("SocketFragment", "📨 From Server: " + response);
            });

            mSocket.on("TruckLocation", args -> {
                JSONObject data = (JSONObject) args[0];
                if (data != null) {
                    try {
                        Truck truck = new Truck(
                                data.getString("truck_id"),
                                data.getDouble("lat"),
                                data.getDouble("lng"),
                                true
                        );

                        trucks.removeIf(existingTruck -> existingTruck.getTruck_id().equals(truck.getTruck_id()));
                        trucks.add(truck);

                        // 10. CRITICAL: Use getActivity().runOnUiThread() to update UI from a background thread
                        // Add a null check for safety, as the Activity could be destroyed if the user navigates away.
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                updateTruckMarker(truck);
                                moveCameraToIncludeAll();

                                StringBuilder sb = new StringBuilder();
                                sb.append("Active Trucks (").append(trucks.size()).append("):\n\n");
                                trucks.forEach(e -> sb.append("🚛 ").append(e.toString()).append("\n\n"));

                                // Make sure t1 is not null (view might be destroying)
//                                if (t1 != null) {
//                                    t1.setText(sb.toString());
//                                }
                            });
                        }

                    } catch (JSONException e) {
                        Log.e("UserSocketFragment", "❌ Error parsing truck data: " + e.getMessage());
                    }
                }
            });

        } catch (URISyntaxException e) {
            Log.e("error", Objects.requireNonNull(e.getMessage()));
        }
    }

    /**
     * 11. This entire method is copied from your Activity.
     * The only change is how the custom icon is called.
     */
    private void updateTruckMarker(Truck truck) {
        if (mMap == null) return;

        double lat = truck.getLat();
        double lng = truck.getLng();

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
                    // 12. Use requireContext() to get the Context needed for this method
                    .icon(resizePngIcon(requireContext(), R.drawable.ic_truck, 80, 80))
                    .anchor(0.5f, 0.5f);
            Marker marker = mMap.addMarker(mo);
            if(marker != null) {
                markers.put(truck.getTruck_id(), marker);
            }
        } else {
            existing.setPosition(pos);
        }
    }

    /**
     * 13. Copied directly, no changes needed as it only uses mMap and markers.
     */
    private void moveCameraToIncludeAll() {
        if (markers.isEmpty() || mMap == null) return;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker m : markers.values()) {
            builder.include(m.getPosition());
        }
        LatLngBounds bounds = builder.build();
        int padding = 120; // px
        try {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        } catch (IllegalStateException e) {
            Log.e("MapCamera", "Error animating camera, map may not be ready or laid out.", e);
        }
    }

    /**
     * 14. Copied directly, no changes needed. This is a perfect utility method.
     */
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable drawable = ContextCompat.getDrawable(context, vectorResId);
        Objects.requireNonNull(drawable).setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /**
     * 15. Copied directly, no changes needed.
     */
    private BitmapDescriptor resizePngIcon(Context context, int resId, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(context.getResources(), resId);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return BitmapDescriptorFactory.fromBitmap(resizedBitmap);
    }


    /**
     * 16. onMapReady method is copied, but permission checks are updated for a Fragment context.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        // 17. Use ContextCompat.checkSelfPermission with requireContext()
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // 18. A Fragment has its OWN requestPermissions method. Use it directly.
            // The result will be delivered to this Fragment's onRequestPermissionsResult (which you should override)
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * 19. IMPORTANT LIFECYCLE MANAGEMENT!
     * When the Fragment's view is destroyed (e.g., user navigates away or tab is switched and removed),
     * you MUST disconnect the socket to prevent leaks and errors.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("SocketFragment", "View is being destroyed, disconnecting socket.");
        if (mSocket != null) {
            // Remove all listeners to be safe
            mSocket.off(Socket.EVENT_CONNECT);
            mSocket.off("messageFromServer");
            mSocket.off("TruckLocation");
            mSocket.disconnect();
        }
        // Clear references
        mMap = null;
//        if(t1 != null) {
//            t1 = null;
//        }
        markers.clear();
        trucks.clear();
    }
}