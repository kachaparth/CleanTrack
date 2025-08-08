package schema.model;

public class Truck {
    private static final Truck instance = new Truck();
    String truck_id;
    double lat;
    double log;
    boolean active;

    public Truck(String truck_id, double lat, double log, boolean active) {
        this.truck_id = truck_id;
        this.lat = lat;
        this.log = log;
        this.active = active;
    }

    public Truck() {
    }

    public static Truck getInstance() {
        return instance;
    }
    public String getTruck_id() {
        return truck_id;
    }

    public void setTruck_id(String truck_id) {
        this.truck_id = truck_id;
    }

    public double getLog() {
        return log;
    }

    public void setLog(double log) {
        this.log = log;
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
}
