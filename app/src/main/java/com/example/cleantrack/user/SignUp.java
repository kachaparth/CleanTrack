package com.example.cleantrack.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cleantrack.R;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignUp extends AppCompatActivity {

    EditText EditEmail, EditUsername, EditPassword;
    Button btnSignUp;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        EditEmail = findViewById(R.id.edit_email);
        EditUsername = findViewById(R.id.edit_username);
        EditPassword = findViewById(R.id.edit_password);

        Button btnSignUp = findViewById(R.id.btn_signup);


        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeSignUp();
            }
        });

    }

    private void makeSignUp() {
        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();

        try {
            json.put("email", EditEmail.getText().toString());
            json.put("username", EditUsername.getText().toString());
            json.put("password", EditPassword.getText().toString());
        } catch (Exception e) {
            Log.e("error", "Invalid JSON in SignUp");
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
                Log.e("error", "Invalid JSON in SignUp Error From Server");
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
                            Intent intent = new Intent(SignUp.this, UserDash.class);
                            startActivity(intent);
                            finish();
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
