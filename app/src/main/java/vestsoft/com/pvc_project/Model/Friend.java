package vestsoft.com.pvc_project.Model;

/**
 * Created by Filip on 01-09-2014.
 */
public class Friend {
    String name = null;
    String phone = null;
    String dateTime = null;
    boolean selected = false;
    double latitude = 0;
    double longitude = 0;

    public Friend() {

    }
    public Friend(String name, String phone, String dateTime, boolean selected, double latitude, double longitude) {
        super();
        this.name = name;
        this.phone = phone;
        this.dateTime = dateTime;
        this.selected = selected;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDateTime() {
        return dateTime;
    }
    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
    public double getLatitide() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;;
    }

    public boolean isSelected() {
        return selected;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
