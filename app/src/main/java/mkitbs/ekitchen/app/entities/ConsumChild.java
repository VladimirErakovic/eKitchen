package mkitbs.ekitchen.app.entities;

import java.util.Date;

/**
 * Created by verakovic on 4/3/2017.
 */

public class ConsumChild {

    private int orderHeaderId;
    private Date dateTimeSent;
    private int quantity;
    private int roomId;
    private int companyId;

    public ConsumChild() {}

    public ConsumChild(int order_header_id, Date date_time_sent, int quan, int room_id, int company_id) {
        this.orderHeaderId = order_header_id;
        this.dateTimeSent = date_time_sent;
        this.quantity = quan;
        this.roomId = room_id;
        this.companyId = company_id;
    }

    public int getOrderHeaderId() {
        return orderHeaderId;
    }

    public void setOrderHeaderId(int orderHeaderId) {
        this.orderHeaderId = orderHeaderId;
    }

    public Date getDateTimeSent() {
        return dateTimeSent;
    }

    public void setDateTimeSent(Date dateTimeSent) {
        this.dateTimeSent = dateTimeSent;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }
}
