package mkitbs.ekitchen.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import mkitbs.ekitchen.app.entities.User;
import mkitbs.ekitchen.app.helpers.DatabaseHandler;

public class AboutActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DatabaseHandler databaseHandler = new DatabaseHandler(getApplicationContext());

        TextView loggedUserRealName = (TextView) findViewById(R.id.logedUsernameText);

        User user = databaseHandler.getLoggedUser();

        loggedUserRealName.setText(user.getUserRealName());

    }
}
