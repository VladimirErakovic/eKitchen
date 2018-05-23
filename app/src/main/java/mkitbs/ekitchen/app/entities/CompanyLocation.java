package mkitbs.ekitchen.app.entities;

/**
 * Created by verakovic on 16.12.2016.
 */

public class CompanyLocation {

    private int companyId;
    private int locationId;

    public CompanyLocation() {}

    public CompanyLocation(int company_id, int location_id) {
        this.companyId = company_id;
        this.locationId = location_id;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }
}
