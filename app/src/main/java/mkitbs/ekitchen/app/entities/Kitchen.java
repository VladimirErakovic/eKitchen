package mkitbs.ekitchen.app.entities;

/**
 * Created by verakovic on 16.12.2016.
 */

public class Kitchen {

    private int kitchenId;
    private String kitchenName;
    private int kitchenLocationId;

    public Kitchen() {}

    public Kitchen(int kitchen_id, String kitchen_name, int kitchen_location_id) {
        this.kitchenId = kitchen_id;
        this.kitchenName = kitchen_name;
        this.kitchenLocationId = kitchen_location_id;
    }

    public int getKitchenId() {
        return kitchenId;
    }

    public void setKitchenId(int kitchenId) {
        this.kitchenId = kitchenId;
    }

    public String getKitchenName() {
        return kitchenName;
    }

    public void setKitchenName(String kitchenName) {
        this.kitchenName = kitchenName;
    }

    public int getKitchenLocationId() {
        return kitchenLocationId;
    }

    public void setKitchenLocationId(int kitchenLocationId) {
        this.kitchenLocationId = kitchenLocationId;
    }
}
