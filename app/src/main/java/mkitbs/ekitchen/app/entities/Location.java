package mkitbs.ekitchen.app.entities;

/**
 * Created by verakovic on 01.12.2016.
 */

public class Location {

    private int locationId;
    private String locationName;

    public Location(){
    }

    public Location(int location_id, String location_name){
        this.locationId = location_id;
        this.locationName = location_name;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

}
