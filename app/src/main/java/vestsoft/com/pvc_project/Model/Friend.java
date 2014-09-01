package vestsoft.com.pvc_project.Model;

/**
 * Created by Filip on 01-09-2014.
 */
public class Friend {
    String name = null;
    String phone = null;
    boolean selected = false;

    public Friend(String name, String phone, boolean selected) {
        super();
        this.name = name;
        this.phone = phone;
        this.selected = selected;
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

    public boolean isSelected() {
        return selected;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
