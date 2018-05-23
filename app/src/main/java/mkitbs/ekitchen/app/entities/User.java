package mkitbs.ekitchen.app.entities;

/**
 * Created by verakovic on 05.12.2016.
 */

public class User {

    private int userId;
    private String userName;
    private String userPassword;
    private String userRealName;
    private String userPicture;
    private int userCompanyId;   //CompanyCEO fali
    private int userRoomId;

    public User(){
    }

    public User(int user_id, String user_name, String user_pass, String user_real_name, String user_pic,
                int user_comp_id, int user_room_id) {
        this.userId = user_id;
        this.userName = user_name;
        this.userPassword = user_pass;
        this.userRealName = user_real_name;
        this.userPicture = user_pic;
        this.userCompanyId = user_comp_id;
        this.userRoomId = user_room_id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserRealName() {
        return userRealName;
    }

    public void setUserRealName(String userRealName) {
        this.userRealName = userRealName;
    }

    public String getUserPicture() {
        return userPicture;
    }

    public void setUserPicture(String userPicture) {
        this.userPicture = userPicture;
    }

    public int getUserCompanyId() {
        return userCompanyId;
    }

    public void setUserCompanyId(int userCompanyId) {
        this.userCompanyId = userCompanyId;
    }

    public int getUserRoomId() {
        return userRoomId;
    }

    public void setUserRoomId(int userRoomId) {
        this.userRoomId = userRoomId;
    }

}
