package com.example.cleantrack.common;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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
        setContentView(R.layout.login);

        EditEmail = findViewById(R.id.edit_email_login);
        EditPassword = findViewById(R.id.edit_password_login);

        Button btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
                .url("https://cleantrack.herokuapp.com/api/auth/signup")
                .put(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("error", "Invalid JSON in Login Error From Server");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Handle success
                if (response.isSuccessful()) {
                    String responseData = response.body().string(); // Get the raw JSON string
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);

                        String role = jsonResponse.optString("role");
                        String username = jsonResponse.optString("username");
                        String email = jsonResponse.optString("email");

                        Log.d("SignupResponse", "Role: " + role + ", Username: " + username);

                        // Example: Save to SharedPreferences
                        getSharedPreferences("UserProfiles", MODE_PRIVATE)
                                .edit()
                                .putString("role", role)
                                .putString("username", username)
                                .putString("email", email)
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
