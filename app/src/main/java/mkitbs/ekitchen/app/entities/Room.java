package mkitbs.ekitchen.app.entities;

/**
 * Created by verakovic on 01.12.2016.
 */

public class Room {

    private int roomId;
    private String roomName;
    private String roomDescription;
    private int roomLocationId;
    private int roomTypeId;

    public Room(){
    }

    public Room(int room_id, String room_name, String room_description, int room_loc_id, int room_type_id){
        this.roomId = room_id;
        this.roomName = room_name;
        this.roomDescription = room_description;
        this.roomLocationId = room_loc_id;
        this.roomTypeId = room_type_id;
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

    public String getRoomDescription() {
        return roomDescription;
    }

    public void setRoomDescription(String roomDescription) {
        this.roomDescription = roomDescription;
    }

    public int getRoomLocationId() {
        return roomLocationId;
    }

    public void setRoomLocationId(int roomLocationId) {
        this.roomLocationId = roomLocationId;
    }

    public int getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(int roomTypeId) {
        this.roomTypeId = roomTypeId;
    }
}
