package com.example.cleantrack.truck.model;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class GeofenceTracker {

    private static final String PREF_NAME = "GeofencePrefs";
    private static final String KEY_ACTIVE = "ActiveGeofences";

    private static GeofenceTracker instance;
    private final SharedPreferences prefs;
    private final Set<String> activeGeofences;

    private GeofenceTracker(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        activeGeofences = Collections.synchronizedSet(new HashSet<>(prefs.getStringSet(KEY_ACTIVE, new HashSet<>(Arrays.asList("DEFAULT_1", "DEFAULT_2")))));
    }

    public static synchronized GeofenceTracker getInstance(Context context) {
        if (instance == null) {
            instance = new GeofenceTracker(context);
        }
        return instance;
    }

    public void addGeofence(String id) {
        activeGeofences.add(id);
        save();
    }

    public void removeGeofence(String id) {
        activeGeofences.remove(id);
        save();
    }

    public Set<String> getActiveGeofences() {
        return new HashSet<>(activeGeofences);
    }

    private void save() {
        prefs.edit().putStringSet(KEY_ACTIVE, new HashSet<>(activeGeofences)).apply();
    }
}
