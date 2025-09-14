package com.example.cleantrack.common;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cleantrack.R;
import com.example.cleantrack.truck.DriverDash;
import com.example.cleantrack.user.SignUp;
import com.example.cleantrack.user.UserDash;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Login extends AppCompatActivity {

    EditText EditEmail, EditPassword;
    Button  btnLogin;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("UserProfiles", MODE_PRIVATE);

        String email = prefs.getString("email", null);
        String userId = prefs.getString("user_id", null);
        String username = prefs.getString("username", null);
        String role = prefs.getString("role",null);
        if (email!= null && userId!=null && username!=null && role.equals("user")) {
            // All values exist -> redirect to dashboard
            Intent intent = new Intent(Login.this, UserDash.class);
            startActivity(intent);
            finish(); // close current activity so user can’t go back to login
        } else if (email!=null && userId!=null && username!=null && role.equals("driver") ) {


            GeofencesSetupDriver geofencesSetup = new GeofencesSetupDriver(Login.this);
            geofencesSetup.registerDriverGeofences();
            Intent intent = new Intent(Login.this, DriverDash.class);
            startActivity(intent);


            finish();
        }

        setContentView(R.layout.login);

        EditEmail = findViewById(R.id.edit_email_login);
        EditPassword = findViewById(R.id.edit_password_login);

        Button btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Login","Login button clicked");
                  makeLogin();
            }

        });

    }

    private void makeLogin() {
        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();

        try {
            json.put("email", EditEmail.getText().toString());
            json.put("password", EditPassword.getText().toString());
        } catch (Exception e) {
            Log.e("error", "Invalid JSON in Login");
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url("http://10.121.105.112:3030/api/login")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("error", "Invalid JSON in Login Error From Server", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // Handle success
                if (response.isSuccessful()) {
                    String responseData = response.body().string(); // Get the raw JSON string
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);

                        String role = jsonResponse.optString("role");
                        String username = jsonResponse.optString("username");
                        String email = jsonResponse.optString("email");
                        String user_id = jsonResponse.optString("user_id");

                        Log.d("SignupResponse", "Role: " + role + ", Username: " + username +
                                ", email:" + email  + ", user_id: "+user_id);


                        // Example: Save to SharedPreferences
                        getSharedPreferences("UserProfiles", MODE_PRIVATE)
                                .edit()
                                .putString("role", role)
                                .putString("username", username)
                                .putString("email", email)
                                .putString("user_id",user_id)
                                .apply();

                        // Example: Navigate based on role
                        runOnUiThread(() -> {
                            if ("user".equalsIgnoreCase(role)) {
                                // open driver dashboard
                                Intent intent = new Intent(Login.this, UserDash.class);
                                startActivity(intent);
                                finish();
                            } else if ("driver".equalsIgnoreCase(role)) {
                                // open admin dashboard
                                Intent intent = new Intent(Login.this, DriverDash.class);
                                startActivity(intent);
                                finish();
                            } else if ("admin".equalsIgnoreCase(role)) {
                                finish();
                            }
                        });



                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("SignupResponse", "Error: " + response.code());
                }

            }
        });
    }
}
