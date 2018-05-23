package mkitbs.ekitchen.app.entities;

import java.util.Date;

/**
 * Created by verakovic on 02.12.2016.
 */

public class OrderHeader {

    private int orderHeaderId;
    private Date timeSent;
    private Date timeDelivered;
    private String status;
    private String comment;
    private int companyId;
    private int roomId;
    private int userWaiterId;
    private int userCustomerId;

    public OrderHeader() {}

    public OrderHeader(int header_id, Date time_sent, Date time_delivered, String status, String comment, int comp_id,
                       int room_id, int user_waiter_id, int user_customer_id) {
        this.orderHeaderId = header_id;
        this.timeSent = time_sent;
        this.timeDelivered = time_delivered;
        this.status = status;
        this.comment = comment;
        this.companyId = comp_id;
        this.roomId = room_id;
        this.userWaiterId = user_waiter_id;
        this.userCustomerId = user_customer_id;
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

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getUserWaiterId() {
        return userWaiterId;
    }

    public void setUserWaiterId(int userWaiterId) {
        this.userWaiterId = userWaiterId;
    }

    public int getUserCustomerId() {
        return userCustomerId;
    }

    public void setUserCustomerId(int userCustomerId) {
        this.userCustomerId = userCustomerId;
    }
}
