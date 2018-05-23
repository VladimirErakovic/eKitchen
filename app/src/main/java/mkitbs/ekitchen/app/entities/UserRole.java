package mkitbs.ekitchen.app.entities;

/**
 * Created by verakovic on 05.12.2016.
 */

public class UserRole {

    private int userId;
    private int roleId;

    public UserRole() {
    }

    public UserRole(int user_id, int role_id) {
        this.userId = user_id;
        this.roleId = role_id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

}
