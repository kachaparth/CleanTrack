package com.example.cleantrack.common;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.cleantrack.R;

public class MainActivity extends AppCompatActivity {

    ImageView gifAnimationView;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.splash);
         gifAnimationView = findViewById(R.id.gifView);
         mediaPlayer = MediaPlayer.create(this, R.raw.beep);
         mediaPlayer.start();
        Glide.with(this)
                .asGif()
                .load(R.drawable.splash_truck_small) // transparent GIF
                .into(gifAnimationView);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mediaPlayer.stop();
                Intent intent = new Intent(MainActivity.this,Login.class);
                startActivity(intent);
                finish();
            }
        },1700);
    }
}