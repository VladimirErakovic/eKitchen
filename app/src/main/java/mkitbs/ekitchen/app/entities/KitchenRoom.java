package mkitbs.ekitchen.app.entities;

/**
 * Created by verakovic on 16.12.2016.
 */

public class KitchenRoom {

    private int kitchenId;
    private int roomId;

    public KitchenRoom() {}

    public KitchenRoom(int kitchen_id, int room_id) {
        this.kitchenId = kitchen_id;
        this.roomId = room_id;
    }

    public int getKitchenId() {
        return kitchenId;
    }

    public void setKitchenId(int kitchenId) {
        this.kitchenId = kitchenId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

}
