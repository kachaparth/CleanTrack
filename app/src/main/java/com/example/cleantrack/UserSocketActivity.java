package com.example.cleantrack;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.socket.client.IO;
import io.socket.client.Socket;
import schema.model.Truck;

public class UserSocketActivity extends AppCompatActivity {
    private Socket mSocket;
    private static final String roomId = "Users";
    private static  final String  role= "User";
    TextView t1;

    private  Set <Truck> trucks= new HashSet<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

       super.onCreate(savedInstanceState);
       setContentView(R.layout.user_socket);
        setupSocket();

        t1 = findViewById(R.id.alltrucks);


    }

    private void setupSocket() {

        IO.Options opts = new IO.Options();
        opts.forceNew = true;
        opts.reconnection = true;
        opts.timeout = 10000; // 10 second timeout
        opts.reconnectionAttempts = 5;
        opts.reconnectionDelay = 1000;
        opts.transports = new String[]{"websocket", "polling"};

        try {


            mSocket = IO.socket("http://10.0.2.2:3030",opts); // Replace with your IP
            mSocket.connect();

            mSocket.on(Socket.EVENT_CONNECT, args -> {

                mSocket.emit("join",roomId,role);
                Log.d("SocketActivity", "✅ Socket connected");
            });

            mSocket.on("messageFromServer", args -> {
                String response = args[0].toString();
                Log.d("SocketActivity", "📨 From Server: " + response);
            });

            mSocket.on("TruckLocation", args -> {
                JSONObject data= (JSONObject) args[0];

                if(data !=null)
                {
                    try {
                        Truck truck = new Truck(
                                data.getString("truck_id"),
                                data.getDouble("lat"),
                                data.getDouble("log"),
                                data.getBoolean("active")
                        );

                        // Remove existing truck with same ID to avoid duplicates
                        trucks.removeIf(existingTruck -> existingTruck.getTruck_id().equals(truck.getTruck_id()));
                        trucks.add(truck);

                        // Update UI on main thread (CRITICAL for Android)
                        runOnUiThread(() -> {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Active Trucks (").append(trucks.size()).append("):\n\n");

                            trucks.forEach(e -> {
                                sb.append("🚛 ").append(e.toString()).append("\n\n");
                            });

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


}

