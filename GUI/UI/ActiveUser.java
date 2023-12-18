package UI;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.ImageView;

public class ActiveUser {

    private SimpleStringProperty Name;
    private int logicalClock;
    private ImageView active;

    public ActiveUser() {
    }

    public ActiveUser(String Name, int logicalClock, boolean active) {
        this.Name = new SimpleStringProperty(Name);
        this.logicalClock = logicalClock;
        if (active) {
            this.active = new ImageView(UI.active_image);
        } else {
            this.active = new ImageView(UI.inactive_image);
        }
    }

    /**
     * @return the Active image
     */
    public ImageView getActive() {
        return active;
    }

    /**
     * @return the Name
     */
    public String getName() {
        return Name.get() + " (" + logicalClock + ")";
    }

    /**
     * @param Name the Name to set
     */
    public void setName(String Name) {
        this.Name.set(Name);
    }
}
