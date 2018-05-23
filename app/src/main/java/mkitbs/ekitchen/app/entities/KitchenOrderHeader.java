package mkitbs.ekitchen.app.entities;

import java.util.Date;

/**
 * Created by verakovic on 08.12.2016.
 */

public class KitchenOrderHeader {

    private int orderHeaderId;
    private Date timeSent;
    private Date timeDelivered;
    private String status;
    private String comment;
    private int companyId;
    private String companyName;
    private int roomId;
    private String roomName;
    private int userWaiterId;
    private String userWaiterName;
    private int userCustomerId;
    private String userCustomerName;
    private boolean isSelected;

    public KitchenOrderHeader() {}

    public KitchenOrderHeader(int header_id, Date time_sent, Date time_delivered, String status, String comment, int comp_id,
                              String comp_name, int room_id, String room_name, int user_waiter_id, String user_waiter_name, int user_customer_id,
                              String user_customer_name, boolean is_selected) {
        this.orderHeaderId = header_id;
        this.timeSent = time_sent;
        this.timeDelivered = time_delivered;
        this.status = status;
        this.comment = comment;
        this.companyId = comp_id;
        this.companyName = comp_name;
        this.roomId = room_id;
        this.roomName = room_name;
        this.userWaiterId = user_waiter_id;
        this.userWaiterName = user_waiter_name;
        this.userCustomerId = user_customer_id;
        this.userCustomerName = user_customer_name;
        this.isSelected = is_selected;
    }

    public int getOrderHeaderId() {
        return orderHeaderId;
    }

    public void setOrderHeaderId(int orderHeaderId) {
        this.orderHeaderId = orderHeaderId;
    }

    public Date getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(Date timeSent) {
        this.timeSent = timeSent;
    }

    public Date getTimeDelivered() {
        return timeDelivered;
    }

    public void setTimeDelivered(Date timeDelivered) {
        this.timeDelivered = timeDelivered;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getUserWaiterId() {
        return userWaiterId;
    }

    public void setUserWaiterId(int userWaiterId) {
        this.userWaiterId = userWaiterId;
    }

    public String getUserWaiterName() {
        return userWaiterName;
    }

    public void setUserWaiterName(String userWaiterName) {
        this.userWaiterName = userWaiterName;
    }

    public int getUserCustomerId() {
        return userCustomerId;
    }

    public void setUserCustomerId(int userCustomerId) {
        this.userCustomerId = userCustomerId;
    }

    public String getUserCustomerName() {
        return userCustomerName;
    }

    public void setUserCustomerName(String userCustomerName) {
        this.userCustomerName = userCustomerName;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
