package mkitbs.ekitchen.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.ldoublem.loadingviewlib.view.LVCircularJump;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import mkitbs.ekitchen.app.entities.Beverage;
import mkitbs.ekitchen.app.entities.Category;
import mkitbs.ekitchen.app.entities.Company;
import mkitbs.ekitchen.app.entities.CompanyLocation;
import mkitbs.ekitchen.app.entities.Kitchen;
import mkitbs.ekitchen.app.entities.KitchenRoom;
import mkitbs.ekitchen.app.entities.Location;
import mkitbs.ekitchen.app.entities.Room;
import mkitbs.ekitchen.app.entities.User;
import mkitbs.ekitchen.app.entities.Waiter;
import mkitbs.ekitchen.app.hall.OverviewActivity;
import mkitbs.ekitchen.app.helpers.DatabaseHandler;
import mkitbs.ekitchen.app.kitchen.KitchenOrderListActivity;

public class SplashActivity extends AppCompatActivity {

    public static final String PROPERTY_USERNAME = "username";

    private String URL_COMPANY;

    private LVCircularJump mLVCircularJump;

    private DatabaseHandler databaseHandler;
    private String serverAddress;
    private String userKind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (getSupportActionBar() != null)
            getSupportActionBar().setIcon(R.drawable.ecafe_bar_icon);

        mLVCircularJump = (LVCircularJump) findViewById(R.id.lv_circularJump);
        mLVCircularJump.setViewColor(Color.rgb(138, 27, 4));
        mLVCircularJump.startAnim();

        databaseHandler = new DatabaseHandler(getApplicationContext());
        databaseHandler.deleteAllSessionData();
        databaseHandler.deleteAllMasterData();  // razmisli da li je ovo ovde neophodno

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (!databaseHandler.isUserDataPresent()) {
            String username = sharedPreferences.getString(PROPERTY_USERNAME, "");
            if (!username.isEmpty()) {
                sharedPreferences.edit().remove(PROPERTY_USERNAME).apply();
                sharedPreferences.edit().remove("password").apply();
                sharedPreferences.edit().remove("user_kind").apply();
            }
        }

        serverAddress = sharedPreferences.getString("server_ip_address", "");

        // "http://mkitsql.mk-group.org/ECafe/api/Company/GetAllCompany";
        URL_COMPANY = serverAddress + getResources().getString(R.string.get_all_company);

        if (!isConnectingToInternet(getApplicationContext())) {

            mLVCircularJump.stopAnim();

            AlertDialog.Builder builderConnErr = new AlertDialog.Builder(SplashActivity.this);
            LayoutInflater li = LayoutInflater.from(SplashActivity.this);

            View promptsView = li.inflate(R.layout.connection_error_dialog, (ViewGroup)null);

            final TextView msgErrorText = (TextView) promptsView
                    .findViewById(R.id.errorText);
            final Button okButton = (Button) promptsView
                    .findViewById(R.id.okConnectionErrorDialog);

            builderConnErr.setView(promptsView)
                    .setTitle(R.string.server_unavailable_title)
                    .setCancelable(false);

            final AlertDialog alertDialog = builderConnErr.create();

            msgErrorText.setText(getResources().getString(R.string.no_internet_connection));

            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.dismiss();
                    finish();
                }
            });

            alertDialog.show();

        } else {

            if (serverAddress.isEmpty()) {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                new CheckServerAvailability().execute();
            }
        }

    }


    /**
     * Checking for active internet provider
     * **/
    public boolean isConnectingToInternet(Context context){

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        return (netInfo != null && netInfo.isConnected());
    }


    private class CheckServerAvailability extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //this method will be running on UI thread
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try{
                URL url = new URL(URL_COMPANY);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(2 * 1000);          // 2 s.
                urlConnection.connect();

                return (urlConnection.getResponseCode() == 200);

            } catch (Exception e) {
                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            boolean serverResponse = result;

            if (serverResponse) {

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String username = sharedPreferences.getString(PROPERTY_USERNAME, "");
                userKind = sharedPreferences.getString("user_kind", "");

                if (username.isEmpty()) {   // OVO ISPADA KAO DODATNA PROVERA JER NEMA SMISLA DA IMA SERVER A NEMA USERNAME..

                    mLVCircularJump.stopAnim();

                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {

                    if (!databaseHandler.isCompanyFull()) {

                        new GetMasterData().execute();

                    } else {

                        if (userKind.equals("CUSTOMER")) {

                            mLVCircularJump.stopAnim();

                            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            new GetKitchensBeveragesAndCategories().execute();
                        }
                    }
                }

            } else {

                mLVCircularJump.stopAnim();

                AlertDialog.Builder builderConnErr = new AlertDialog.Builder(SplashActivity.this);
                LayoutInflater li = LayoutInflater.from(SplashActivity.this);

                View promptsView = li.inflate(R.layout.connection_error_dialog, (ViewGroup)null);

                final TextView msgErrorText = (TextView) promptsView
                        .findViewById(R.id.errorText);
                final Button okButton = (Button) promptsView
                        .findViewById(R.id.okConnectionErrorDialog);

                builderConnErr.setView(promptsView)
                        .setTitle(R.string.server_unavailable_title)
                        .setCancelable(false);

                final AlertDialog alertDialog = builderConnErr.create();

                msgErrorText.setText(getResources().getString(R.string.no_company_network));

                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                        finish();
                    }
                });

                alertDialog.show();
            }

        }
    }


    /**
     * Async task to get all item categories
     * */
    private class GetMasterData extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            // "http://mkitsql.mk-group.org/ECafe/api/Location/GetAllLocation";
            String URL_LOCATION = serverAddress + getResources().getString(R.string.get_all_location);
            // "http://mkitsql.mk-group.org/ECafe/api/CompanyLocation/GetAllCompanyLocation";
            String URL_COMPANY_LOCATION = serverAddress + getResources().getString(R.string.get_all_company_location);
            // "http://mkitsql.mk-group.org/ECafe/api/Room/GetAllRoom";
            String URL_ROOM = serverAddress + getResources().getString(R.string.get_all_room);
            // "http://mkitsql.mk-group.org/ECafe/api/KitchenRoom/GetAllKitchenRoom";
            String URL_KITCHEN_ROOM = serverAddress + getResources().getString(R.string.get_all_kitchen_room);
            // "http://mkitsql.mk-group.org/ECafe/api/Kitchen/GetAllKitchen";
            String URL_KITCHEN = serverAddress + getResources().getString(R.string.get_all_kitchen);  // OVO JOS VIDI DALI MOZES RAZRADITI!!!
            String jsonCompany = webServiceCall(URL_COMPANY);
            String jsonLocation = webServiceCall(URL_LOCATION);
            String jsonCompanyLocation = webServiceCall(URL_COMPANY_LOCATION);
            String jsonRoom = webServiceCall(URL_ROOM);
            String jsonKitchenRoom = webServiceCall(URL_KITCHEN_ROOM);
            String jsonKitchen = "";
            if (!databaseHandler.isKitchenFull())
                jsonKitchen = webServiceCall(URL_KITCHEN);

            // read Company JSON
            if (!jsonCompany.isEmpty()) {
                try {
                    JSONObject jsonObjComp = new JSONObject(jsonCompany);
                    if (!jsonObjComp.isNull("Company")) {
                        JSONArray companies = jsonObjComp.getJSONArray("Company");

                        for (int i = 0; i < companies.length(); i++) {
                            JSONObject compObj = (JSONObject) companies.get(i);
                            Company company = new Company(compObj.getInt("CompanyId"),
                                    compObj.getString("Name"), compObj.getString("Description"),
                                    compObj.getString("Logo"));
                            databaseHandler.addCompany(company);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("JSON Company Data", "Didn't receive any data from server!");   // ovo sve treba zameniti sa pravim porukama
            }

            // read Location JSON
            if (!jsonLocation.isEmpty()) {
                try {
                    JSONObject jsonObjLoc = new JSONObject(jsonLocation);
                    if (!jsonObjLoc.isNull("Location")) {
                        JSONArray locations = jsonObjLoc.getJSONArray("Location");

                        for (int i = 0; i < locations.length(); i++) {
                            JSONObject locObj = (JSONObject) locations.get(i);
                            Location location = new Location(locObj.getInt("LocationId"),
                                    locObj.getString("Name"));
                            databaseHandler.addLocation(location);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("JSON Location Data", "Didn't receive any data from server!");
            }

            // read CompanyLocation JSON
            if (!jsonCompanyLocation.isEmpty()) {
                try {
                    JSONObject jsonObjCompLoc = new JSONObject(jsonCompanyLocation);
                    if (!jsonObjCompLoc.isNull("CompanyLocation")) {
                        JSONArray companyLocations = jsonObjCompLoc.getJSONArray("CompanyLocation");

                        for (int i = 0; i < companyLocations.length(); i++) {
                            JSONObject compLocObj = (JSONObject) companyLocations.get(i);
                            CompanyLocation companyLocation = new CompanyLocation(compLocObj.getInt("CompanyId"),
                                    compLocObj.getInt("LocationId"));
                            databaseHandler.addCompanyLocation(companyLocation);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("JSON CompLoc Data", "Didn't receive any data from server!");
            }

            // // read Room JSON
            if (!jsonRoom.isEmpty()) {
                try {
                    JSONObject jsonObjRoom = new JSONObject(jsonRoom);
                    if (!jsonObjRoom.isNull("Room")) {
                        JSONArray rooms = jsonObjRoom.getJSONArray("Room");

                        for (int i = 0; i < rooms.length(); i++) {
                            JSONObject roomObj = (JSONObject) rooms.get(i);
                            Room room = new Room(roomObj.getInt("RoomId"),
                                    roomObj.getString("Name"), roomObj.getString("Description"),
                                    roomObj.getInt("LocationId"), roomObj.getInt("RoomTypeId"));
                            databaseHandler.addRoom(room);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("JSON Room Data", "Didn't receive any data from server!");
            }

            // read KitchenRoom JSON
            if (!jsonKitchenRoom.isEmpty()) {
                try {
                    JSONObject jsonObjKitchenRoom = new JSONObject(jsonKitchenRoom);
                    if (!jsonObjKitchenRoom.isNull("KitchenRoom")) {
                        JSONArray kitchenRooms = jsonObjKitchenRoom.getJSONArray("KitchenRoom");

                        for (int i = 0; i < kitchenRooms.length(); i++) {
                            JSONObject kitchenRoomObj = (JSONObject) kitchenRooms.get(i);
                            KitchenRoom kitchenRoom = new KitchenRoom(kitchenRoomObj.getInt("KitchenId"),
                                    kitchenRoomObj.getInt("RoomId"));
                            databaseHandler.addKitchenRoom(kitchenRoom);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("JSON KitchRoom Data", "Didn't receive any data from server!");
            }

            // read Kitchen JSON
            if (!jsonKitchen.isEmpty()) {
                try {
                    JSONObject jsonObjKitchen = new JSONObject(jsonKitchen);
                    if (!jsonObjKitchen.isNull("Kitchen")) {
                        JSONArray kitchens = jsonObjKitchen.getJSONArray("Kitchen");

                        for (int i = 0; i < kitchens.length(); i++) {
                            JSONObject kitchenObj = (JSONObject) kitchens.get(i);
                            Kitchen kitchen = new Kitchen(kitchenObj.getInt("KitchenId"), kitchenObj.getString("Name"),
                                    kitchenObj.getInt("LocationId"));
                            databaseHandler.addKitchen(kitchen);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("JSON KITCHEN Data", "Didn't receive any data from server!");
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            databaseHandler.close();

            if (userKind.equals("CUSTOMER")) {

                mLVCircularJump.stopAnim();

                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                new GetKitchensBeveragesAndCategories().execute();
            }
        }

        @NonNull
        private String webServiceCall(String urlAddress) {

            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urlAddress);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                urlConnection.disconnect();
            }

            return result.toString();
        }

    }


    private class GetKitchensBeveragesAndCategories extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //databaseHandler.deleteAllKitchen();   // ako je kuhinja u pitanju ne moj brisati iz baze i isto za konobare
            //databaseHandler.deleteAllWaiters();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            // "http://mkitsql.mk-group.org/ECafe/api/Kitchen/GetAllKitchen";
            String URL_KITCHEN = serverAddress + getResources().getString(R.string.get_all_kitchen);
            // "http://mkitsql.mk-group.org/ECafe/api/BeverageCategory/GetKitchenBeverage/";
            String URL_CATEGORY_BEVERAGE = serverAddress + getResources().getString(R.string.get_kitchen_beverage);
            // "http://mkitsql.mk-group.org/ECafe/api/User/GetKitchenWaiter/";
            String URL_KITCHEN_WAITER = serverAddress + getResources().getString(R.string.get_kitchen_waiter);
            String jsonKitchen = "";
            if (!databaseHandler.isKitchenFull())
                jsonKitchen = webServiceCall(URL_KITCHEN);

            // read Kitchen JSON
            if (!jsonKitchen.isEmpty()) {
                try {
                    JSONObject jsonObjKitchen = new JSONObject(jsonKitchen);
                    if (!jsonObjKitchen.isNull("Kitchen")) {
                        JSONArray kitchens = jsonObjKitchen.getJSONArray("Kitchen");

                        for (int i = 0; i < kitchens.length(); i++) {
                            JSONObject kitchenObj = (JSONObject) kitchens.get(i);
                            Kitchen kitchen = new Kitchen(kitchenObj.getInt("KitchenId"), kitchenObj.getString("Name"),
                                    kitchenObj.getInt("LocationId"));
                            databaseHandler.addKitchen(kitchen);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("JSON KITCHEN Data", "Didn't receive any data from server!");
            }

            User user = databaseHandler.getLoggedUser();
            Kitchen kitchen = databaseHandler.getKitchenByUser(user.getUserId());
            String jsonCategoryBeverage = webServiceCall(URL_CATEGORY_BEVERAGE  + String.valueOf(kitchen.getKitchenId()));

            // read Categories and Beverages JSON
            if (!jsonCategoryBeverage.isEmpty()) {
                try {
                    JSONObject jsonObjCat = new JSONObject(jsonCategoryBeverage);
                    if (!jsonObjCat.isNull("BeverageCategory")) {
                        JSONArray categories = jsonObjCat.getJSONArray("BeverageCategory");

                        for (int i = 0; i < categories.length(); i++) {
                            JSONObject catObj = (JSONObject) categories.get(i);
                            Category cat = new Category(catObj.getInt("BeverageCategoryId"),
                                    catObj.getString("Name"));
                            databaseHandler.addCategory(cat);

                            if (!catObj.isNull("Beverages")) {
                                JSONArray beverages = catObj.getJSONArray("Beverages");
                                for (int x = 0; x < beverages.length(); x++) {
                                    JSONObject bevObj = (JSONObject) beverages.get(x);
                                    int available;
                                    if (bevObj.getBoolean("Available")) {
                                        available = 1;
                                    } else {
                                        available = 0;
                                    }
                                    Beverage bev = new Beverage(bevObj.getInt("KitchenId"), bevObj.getInt("BeverageId"),
                                            bevObj.getString("Name"), bevObj.getString("Description"), available,
                                            bevObj.getString("Picture"), bevObj.getInt("BeverageCategoryId"), 0);
                                    databaseHandler.addBeverage(bev);
                                }
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("JSON BEVERAGES Data", "Didn't receive any data from server!");
            }

            String jsonWaiter = "";
            if (!databaseHandler.isWaiterFull())
                jsonWaiter = webServiceCall(URL_KITCHEN_WAITER  + String.valueOf(kitchen.getKitchenId()));

            // read Waiter JSON
            if (!jsonWaiter.isEmpty()) {
                Log.d("IMA WAITER", jsonWaiter);
                try {
                    JSONObject jsonObjWaiter = new JSONObject(jsonWaiter);
                    if (!jsonObjWaiter.isNull("User")) {
                        JSONArray waiters = jsonObjWaiter.getJSONArray("User");

                        for (int i = 0; i < waiters.length(); i++) {
                            JSONObject waiterObj = (JSONObject) waiters.get(i);
                            Waiter waiter = new Waiter(waiterObj.getInt("UserId"), "", "",  waiterObj.getString("Name"),
                                    "", 0,  waiterObj.getInt("RoomId"));
                            databaseHandler.addWaiter(waiter);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("JSON WAITER Data", "Didn't receive any data from server!");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            mLVCircularJump.stopAnim();

            databaseHandler.close();

            switch (userKind) {
             case "WAITER":     // Konobar
                 Intent intent2 = new Intent(SplashActivity.this, MainActivity.class);
                 startActivity(intent2);
                 break;
             case "MEETING_ROOM":     // Sala
                 Intent intentHall = new Intent(SplashActivity.this, OverviewActivity.class);
                 startActivity(intentHall);
                 break;
             case "KITCHEN":     // Kuhinja
                 Intent intentKitchen = new Intent(SplashActivity.this, KitchenOrderListActivity.class);
                 startActivity(intentKitchen);;
                 break;
             }

            finish();
        }

        @NonNull
        private String webServiceCall(String urlAddress) {

            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urlAddress);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                urlConnection.disconnect();
            }

            return result.toString();
        }
    }

}