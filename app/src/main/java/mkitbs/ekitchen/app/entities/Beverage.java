package mkitbs.ekitchen.app.entities;

/**
 * Created by verakovic on 24.11.2016.
 */

public class Beverage {

    private int bevKitchenId;
    private int beverageId;
    private String beverageName;
    private String bevDescription;
    private int isAvailable;
    private String bevImage;
    private int bevCategoryId;
    private int helpCounter;


    public Beverage() {}

    public Beverage(int bev_kitchen_id, int beverage_id, String beverage_name, String bev_desc, int is_available,
                    String bev_image, int bev_category_id, int help_counter) {
        this.bevKitchenId = bev_kitchen_id;
        this.beverageId = beverage_id;
        this.beverageName = beverage_name;
        this.bevDescription = bev_desc;
        this.isAvailable = is_available;
        this.bevImage = bev_image;
        this.bevCategoryId = bev_category_id;
        this.helpCounter = help_counter;
    }

    public int getBevKitchenId() {
        return bevKitchenId;
    }

    public void setBevKitchenId(int bevKitchenId) {
        this.bevKitchenId = bevKitchenId;
    }

    public int getBeverageId() {
        return beverageId;
    }

    public void setBeverageId(int beverageId) {
        this.beverageId = beverageId;
    }

    public String getBeverageName() {
        return beverageName;
    }

    public void setBeverageName(String beverageName) {
        this.beverageName = beverageName;
    }

    public String getBevDescription() {
        return bevDescription;
    }

    public void setBevDescription(String bevDescription) {
        this.bevDescription = bevDescription;
    }

    public int isAvailable() {
        return isAvailable;
    }

    public void setAvailable(int available) {
        isAvailable = available;
    }

    public String getBevImage() {
        return bevImage;
    }

    public void setBevImage(String bevImage) {
        this.bevImage = bevImage;
    }

    public int getBevCategoryId() {
        return bevCategoryId;
    }

    public void setBevCategoryId(int bevCategoryId) {
        this.bevCategoryId = bevCategoryId;
    }

    public int getHelpCounter() {
        return helpCounter;
    }

    public void setHelpCounter(int helpCounter) {
        this.helpCounter = helpCounter;
    }
}
