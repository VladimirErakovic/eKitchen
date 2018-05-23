package mkitbs.ekitchen.app.entities;

/**
 * Created by verakovic on 22.12.2016.
 */

public class Waiter extends User {

    public Waiter() {
        super();
    }

    public Waiter(int user_id, String user_name, String user_pass, String user_real_name, String user_pic,
                  int user_comp_id, int user_room_id) {
        super(user_id, user_name, user_pass, user_real_name, user_pic, user_comp_id, user_room_id);
    }
}
