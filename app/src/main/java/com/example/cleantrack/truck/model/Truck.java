package com.example.cleantrack.truck.model;

import java.util.Objects;

public class Truck {

    String truck_id;
    double lat;
    double lng;

    @Override
    public String toString() {
        return "Truck{" +
                "truck_id='" + truck_id + '\'' +
                ", lat=" + lat +
                ", log=" + lng +
                ", active=" + active +
                '}';
    }

    boolean active;

    public Truck(String truck_id, double lat, double log, boolean active) {
        this.truck_id = truck_id;
        this.lat = lat;
        this.lng = log;
        this.active = active;
    }

    public Truck() {
    }

    public String getTruck_id() {
        return truck_id;
    }


    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // same object
        if (o == null || getClass() != o.getClass()) return false; // not same type
        Truck truck = (Truck) o;
        return Objects.equals(truck_id, truck.truck_id); // only compare truck_id
    }

    @Override
    public int hashCode() {
        return Objects.hash(truck_id); // hash based on truck_id
    }
}
