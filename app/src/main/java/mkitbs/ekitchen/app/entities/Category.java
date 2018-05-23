package mkitbs.ekitchen.app.entities;

/**
 * Created by verakovic on 24.11.2016.
 */

public class Category {

    private int categoryId;
    private String categoryName;

    public Category() {
    }

    public Category(int category_id, String category_name) {
        this.categoryId = category_id;
        this.categoryName = category_name;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
