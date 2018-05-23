package mkitbs.ekitchen.app.entities;

import java.util.Date;

/**
 * Created by verakovic on 20.01.2017.
 */

public class UserCall {

    private int userCallId;
    private Date timeSent;
    private String callStatus;
    private String callMessage;
    private int callRoomId;
    private String callRoomName;

    public UserCall() {}

    public UserCall(int user_call_id, Date time_sent, String call_status, String call_msg, int call_room_id, String call_room_name) {
        this.userCallId = user_call_id;
        this.timeSent = time_sent;
        this.callStatus = call_status;
        this.callMessage = call_msg;
        this.callRoomId = call_room_id;
        this.callRoomName = call_room_name;
    }

    public int getUserCallId() {
        return userCallId;
    }

    public void setUserCallId(int userCallId) {
        this.userCallId = userCallId;
    }

    public Date getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(Date timeSent) {
        this.timeSent = timeSent;
    }

    public String getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(String callStatus) {
        this.callStatus = callStatus;
    }

    public String getCallMessage() {
        return callMessage;
    }

    public void setCallMessage(String callMessage) {
        this.callMessage = callMessage;
    }

    public int getCallRoomId() {
        return callRoomId;
    }

    public void setCallRoomId(int callRoomId) {
        this.callRoomId = callRoomId;
    }

    public String getCallRoomName() {
        return callRoomName;
    }

    public void setCallRoomName(String callRoomName) {
        this.callRoomName = callRoomName;
    }
}
