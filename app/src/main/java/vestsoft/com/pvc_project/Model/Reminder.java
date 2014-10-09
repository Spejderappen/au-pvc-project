package vestsoft.com.pvc_project.Model;

/**
 * Created by Filip on 07-10-2014.
 */
public class Reminder {
    int id = -1;
    String text;
    double latitude = 0;
    double longitude = 0;
    double radius = 0;

    boolean isWithinRange = false;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public boolean isWithinRange() {
        return isWithinRange;
    }

    public void setWithinRange(boolean isWithinRange) {
        this.isWithinRange = isWithinRange;
    }
}
