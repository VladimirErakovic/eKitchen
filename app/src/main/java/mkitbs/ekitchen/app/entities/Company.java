package mkitbs.ekitchen.app.entities;

/**
 * Created by verakovic on 01.12.2016.
 */

public class Company {

    private int companyId;
    private String companyName;
    private String companyDescription;
    private String companyLogo;

    public Company(){
    }

    public Company(int comp_id, String comp_name, String comp_description, String comp_logo){
        this.companyId = comp_id;
        this.companyName = comp_name;
        this.companyDescription = comp_description;
        this.companyLogo = comp_logo;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyDescription() {
        return companyDescription;
    }

    public void setCompanyDescription(String companyDescription) {
        this.companyDescription = companyDescription;
    }

    public String getCompanyLogo() {
        return companyLogo;
    }

    public void setCompanyLogo(String companyLogo) {
        this.companyLogo = companyLogo;
    }
}
