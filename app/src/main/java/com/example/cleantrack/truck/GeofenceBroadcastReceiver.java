package com.example.cleantrack.truck;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.cleantrack.R;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);

        if (event.hasError()) {
            Log.e("GEOFENCE", "Error: " + event.getErrorCode());
            return;
        }

        int transition = event.getGeofenceTransition();
        String transitionMsg = "";

        switch (transition) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                transitionMsg = "You entered the geofence!";
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                transitionMsg = "You exited the geofence!";
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                transitionMsg = "Dwelling inside the geofence!";
                break;
        }

        sendNotification(context, transitionMsg); // 👈 Call the notification sender
    }

    private void sendNotification(Context context, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "geofence_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Geofence Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // replace with your icon
                .setContentTitle("Geofence Alert")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(1001, builder.build());
    }
}
