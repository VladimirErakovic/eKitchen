package mkitbs.ekitchen.app.entities;

/**
 * Created by verakovic on 22.11.2016.
 */

public class OrderItem {

    private int orderHeaderId;
    private int orderItemId;
    private int itemQuantity;
    private Double itemPrice;
    private Double itemAmount;
    private int kitchenId;
    private int beverageId;
    private String beverageName;

    public OrderItem(){
    }

    public OrderItem(int order_header_id, int order_item_id, int quantity, Double price,
                     Double amount, int kitchen_id, int beverage_id, String beverage_name){
        this.orderHeaderId = order_header_id;
        this.orderItemId = order_item_id;
        this.itemQuantity = quantity;
        this.itemPrice = price;
        this.itemAmount = amount;
        this.kitchenId = kitchen_id;
        this.beverageId = beverage_id;
        this.beverageName = beverage_name;
    }

    public int getOrderHeaderId() {
        return orderHeaderId;
    }

    public void setOrderHeaderId(int orderHeaderId) {
        this.orderHeaderId = orderHeaderId;
    }

    public int getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(int orderItemId) {
        this.orderItemId = orderItemId;
    }

    public int getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(int itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public Double getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(Double itemPrice) {
        this.itemPrice = itemPrice;
    }

    public Double getItemAmount() {
        return itemAmount;
    }

    public void setItemAmount(Double itemAmount) {
        this.itemAmount = itemAmount;
    }

    public int getKitchenId() {
        return kitchenId;
    }

    public void setKitchenId(int kitchenId) {
        this.kitchenId = kitchenId;
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

}
