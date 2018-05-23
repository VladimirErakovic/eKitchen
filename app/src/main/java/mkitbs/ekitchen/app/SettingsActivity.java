package mkitbs.ekitchen.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import mkitbs.ekitchen.app.entities.Location;
import mkitbs.ekitchen.app.entities.Room;
import mkitbs.ekitchen.app.helpers.DatabaseHandler;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private Spinner locationSpinner;
    private Spinner roomSpinner;
    private RadioButton fabRadioBtn;
    private RadioButton notifRadioBtn;
    private List<Location> locationList;
    private List<Room> roomList;
    private int roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        boolean isFabAll = sharedPreferences.getBoolean("fab_is_all", true);
        String notifStatus = sharedPreferences.getString("notification_status", "");
        int locationId = sharedPreferences.getInt("default_location", 0);
        roomId = sharedPreferences.getInt("default_room", 0);

        locationSpinner = (Spinner) findViewById(R.id.spinnerLocationSettings);
        roomSpinner = (Spinner) findViewById(R.id.spinnerRoomSettings);

        if (isFabAll) {
            fabRadioBtn = (RadioButton) findViewById(R.id.radioMainAll);
            fabRadioBtn.setChecked(true);
        } else {
            fabRadioBtn = (RadioButton) findViewById(R.id.radioMainSingle);
            fabRadioBtn.setChecked(true);
        }

        if (!notifStatus.isEmpty()) {
            if (notifStatus.equals("Y")) {
                notifRadioBtn = (RadioButton) findViewById(R.id.radioYes);
                notifRadioBtn.setChecked(true);
            } else {
                notifRadioBtn = (RadioButton) findViewById(R.id.radioNo);
                notifRadioBtn.setChecked(true);
            }
        }

        final DatabaseHandler databaseHandler = new DatabaseHandler(getApplicationContext());
        locationList = databaseHandler.getAllLocations();
        roomList = databaseHandler.getAllRooms();

        // Location spinner set
        ArrayList<String> locationNamesList = new ArrayList<>();
        if (locationList != null && locationList.size() > 0) {
            for (Location location : locationList) {
                locationNamesList.add(location.getLocationName());
            }
        }
        locationNamesList.add(getResources().getString(R.string.spinner_default_location));
        final int locationNamesListSize = locationNamesList.size() - 1;
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<String>(SettingsActivity.this,
                android.R.layout.simple_spinner_item, locationNamesList){
            @Override
            public int getCount() {
                return (locationNamesListSize);
            }
        };
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationAdapter.notifyDataSetChanged();
        locationSpinner.setAdapter(locationAdapter);
        locationSpinner.setSelection(locationNamesListSize);

        int locationPosition = 0;
        if (locationId > 0) {
            for (int i = 0; i < locationList.size(); i++) {
                if (locationId == locationList.get(i).getLocationId()) {
                    locationPosition = i;
                    break;
                }
            }
            locationSpinner.setSelection(locationPosition);
        }

        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                String selectedLocationName = adapterView.getItemAtPosition(position).toString();
                int selectedLocationId = 0;
                List<Room> roomList;
                int userDefaultRoomListPosition = -1;

                if (!locationList.isEmpty()) {
                    for (int i = 0; i < locationList.size(); i++) {
                        if (locationList.get(i).getLocationName().equals(selectedLocationName)) {
                            selectedLocationId = locationList.get(i).getLocationId();
                            break;
                        }
                    }
                    roomList = databaseHandler.getAllRoomsAtLocation(selectedLocationId);
                } else {
                    roomList = databaseHandler.getAllRooms();
                }
                ArrayList<String> roomNames = new ArrayList<>();
                if (roomList != null && roomList.size() > 0) {
                    for (int i = 0; i < roomList.size(); i++) {
                        Room room = roomList.get(i);
                        roomNames.add(room.getRoomName());
                        if (roomId == room.getRoomId()) {
                            userDefaultRoomListPosition = i;
                            break;
                        }
                    }
                }
                roomNames.add(getResources().getString(R.string.spinner_default_room));
                final int roomNamesListSize = roomNames.size() - 1;
                ArrayAdapter<String> roomAdapter = new ArrayAdapter<String>(SettingsActivity.this,
                        android.R.layout.simple_spinner_item, roomNames){
                    @Override
                    public int getCount() {
                        return (roomNamesListSize);
                    }
                };
                roomAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                roomAdapter.notifyDataSetChanged();
                roomSpinner.setAdapter(roomAdapter);
                if (userDefaultRoomListPosition > -1) {
                    roomSpinner.setSelection(userDefaultRoomListPosition);
                } else {
                    roomSpinner.setSelection(roomNamesListSize);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        Button resetBtn = (Button) findViewById(R.id.resetLocationRoomButton);
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationSpinner.setSelection(locationNamesListSize);
            }
        });

        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_save:
                saveSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveSettings() {

        String selectedLocation = locationSpinner.getSelectedItem().toString();
        int locationId = 0;

        for (Location location: locationList) {
            if (selectedLocation.equals(location.getLocationName())) {
                locationId = location.getLocationId();
                break;
            }
        }

        String selectedRoom = roomSpinner.getSelectedItem().toString();
        int roomId = 0;

        for (Room room : roomList) {
            if (selectedRoom.equals(room.getRoomName())) {
                roomId = room.getRoomId();
                break;
            }
        }

        fabRadioBtn = (RadioButton) findViewById(R.id.radioMainAll);
        boolean fabIsAll = true;
        if (fabRadioBtn.isChecked()) {
            fabIsAll = true;
        } else {
            fabIsAll = false;
        }

        notifRadioBtn = (RadioButton) findViewById(R.id.radioYes);
        String notificationStatus;
        if (notifRadioBtn.isChecked()) {
            notificationStatus = "Y";
        } else {
            notificationStatus = "N";
        }


        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("default_location", locationId);
        editor.putInt("default_room", roomId);
        editor.putString("notification_status", notificationStatus);
        editor.putBoolean("fab_is_all", fabIsAll);
        editor.apply();

        Toast.makeText(getApplicationContext(),
                getResources().getString(R.string.settings_saved), Toast.LENGTH_SHORT).show();
    }

}
