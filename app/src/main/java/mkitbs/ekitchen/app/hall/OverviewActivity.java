package mkitbs.ekitchen.app.hall;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

import mkitbs.ekitchen.app.AboutActivity;
import mkitbs.ekitchen.app.R;
import mkitbs.ekitchen.app.SettingsActivity;
import mkitbs.ekitchen.app.entities.Kitchen;
import mkitbs.ekitchen.app.entities.OrderHeader;
import mkitbs.ekitchen.app.entities.OrderItem;
import mkitbs.ekitchen.app.helpers.DatabaseHandler;
import mkitbs.ekitchen.app.helpers.ExpandableListAdapter;
import mkitbs.ekitchen.app.kitchen.BeverageListActivity;
import mkitbs.ekitchen.app.kitchen.ConsumptionActivity;


public class OverviewActivity extends AppCompatActivity {

    private Timer repeatTask;

    private ExpandableListView ordersExpListHall;
    private ExpandableListAdapter listAdapter;
    private List<OrderHeader> orderHeaderList;
    private List<OrderItem> orderItemList;
    private ProgressDialog progressDialog;
    private DatabaseHandler databaseHandler;
    private String userCustomerId;
    private String callMessage = "";
    private int userRoomId;
    private int userKitchenId;
    private boolean timerIsRunning = false;
    private String serverAddress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        databaseHandler = new DatabaseHandler(getApplicationContext());

        int userId = databaseHandler.getLoggedUser().getUserId();
        userCustomerId = String.valueOf(userId);
        Kitchen userKitchen = databaseHandler.getKitchenByUser(userId);
        userKitchenId = userKitchen.getKitchenId();
        String userHallName = databaseHandler.getLoggedUser().getUserRealName();
        userRoomId = databaseHandler.getLoggedUser().getUserRoomId();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        serverAddress = sharedPreferences.getString("server_ip_address", "");

        ordersExpListHall = (ExpandableListView) findViewById(R.id.listExpOrdersHall);
        ordersExpListHall.setClickable(true);

        orderHeaderList = new ArrayList<>();
        orderItemList = new ArrayList<>();

       // new GetHallsOrdersFromToday().execute();

        // Listview Group click listener
        ordersExpListHall.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                // ovde moze da se trigeruje neki brojac recimo
                return false;
            }
        });

        // Listview on child click listener
        ordersExpListHall.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                // Contact cont = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
                return false;
            }
        });

        if(getSupportActionBar() != null)
            getSupportActionBar().setTitle(userHallName);

        Button callKitchenButton = (Button) findViewById(R.id.callKitchenButton);
        Button clearHallButton =  (Button) findViewById(R.id.cleanHallButton);
        Button makeOrderButton = (Button) findViewById(R.id.makeOrderButton);

        callKitchenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // obavestenje kuhinji
                AlertDialog.Builder builderCall = new AlertDialog.Builder(OverviewActivity.this);
                LayoutInflater layoutInflater = LayoutInflater.from(OverviewActivity.this);

                View promptsView = layoutInflater.inflate(R.layout.call_kitchen_dialog, (ViewGroup)null);

                final EditText commentEditText = (EditText) promptsView
                        .findViewById(R.id.commentEditTextCallKitchen);
                final Button btnCancel = (Button) promptsView
                        .findViewById(R.id.cancelButtonCallDialog);
                final Button btnSend = (Button) promptsView
                        .findViewById(R.id.sendButtonCallDialog);
                final CheckBox chkCall = (CheckBox) promptsView.findViewById(R.id.chkBoxCall);
                final CheckBox chkCome = (CheckBox) promptsView.findViewById(R.id.chkBoxCome);

                builderCall.setView(promptsView)
                        .setTitle(R.string.call_kitchen_dialog_title)
                        .setCancelable(true);

                final AlertDialog alertDialog = builderCall.create();

                chkCall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (chkCall.isChecked()) {
                            commentEditText.setText(chkCall.getText().toString());
                        }
                    }
                });
                chkCome.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (chkCome.isChecked()) {
                            commentEditText.setText(chkCome.getText().toString());
                        }
                    }
                });

                btnSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                        callMessage = commentEditText.getText().toString();
                        callMessage = callMessage.replace("č", "c").replace("Č", "C").replace("ć", "c")
                                .replace("Ć", "C").replace("š", "s").replace("Š", "S")
                                .replace("ž", "z").replace("Ž", "Z").replace("đ", "dj").replace("Đ", "Dj");

                        new SendCallToKitchen().execute();
                    }
                });
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });

                alertDialog.show();

            }
        });
        clearHallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builderClearHall = new AlertDialog.Builder(OverviewActivity.this);
                LayoutInflater li = LayoutInflater.from(OverviewActivity.this);

                View promptsView = li.inflate(R.layout.confirm_dialog, (ViewGroup) null);
                final Button noBtn = (Button) promptsView.findViewById(R.id.noButtonDialog);
                final Button yesBtn = (Button) promptsView.findViewById(R.id.yesButtonDialog);
                final TextView confirmText = (TextView) promptsView.findViewById(R.id.confirmDialogText);
                confirmText.setText(getResources().getString(R.string.clear_dialog_text));

                builderClearHall.setView(promptsView)
                        .setTitle(R.string.clear_dialog_title)
                        .setCancelable(false);
                // create alert dialog
                final AlertDialog alertDialogClearHall = builderClearHall.create();

                noBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialogClearHall.dismiss();
                    }
                });
                yesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialogClearHall.dismiss();

                        new UpdateOrderStatusToClean().execute();
                    }
                });

                alertDialogClearHall.show();

            }
        });
        makeOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentMakeOrder = new Intent(OverviewActivity.this, BeverageListActivity.class);
                intentMakeOrder.putExtra("mode", false);
                startActivityForResult(intentMakeOrder, 1);
            }
        });

        repeatTask = new Timer();

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 550) {
            int newOrderHeaderId = data.getIntExtra("new_order_id", 0);
            if (newOrderHeaderId > 0) {

                if (!orderHeaderList.isEmpty())
                    orderHeaderList.clear();

                if (!orderItemList.isEmpty())
                    orderItemList.clear();

                new GetHallsOrdersFromToday().execute();

                long firstInterval = 10000; // 10 sec
                long repeatInterval = 5000; // 5 sec

                if (!timerIsRunning) {
                    // this task for specified time it will run Repeat
                    repeatTask.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            // Here do something
                            // This task will run every 20 sec repeat
                            Log.d("Repeat_task", "Started");
                            new UpdateHallsOrdersFromToday().execute();
                            timerIsRunning = true;
                        }
                    }, firstInterval, repeatInterval);
                }
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onRestart() {
        super.onRestart();
        //new UpdateHallsOrdersFromToday().execute();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Repeat_task", "Canceled");
        if(repeatTask != null){
            repeatTask.cancel();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_overview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Intent intentSettings = new Intent(OverviewActivity.this, SettingsActivity.class);
                startActivity(intentSettings);
                return true;
            case R.id.action_about:
                Intent intentAbout = new Intent(OverviewActivity.this, AboutActivity.class);
                startActivity(intentAbout);
                return true;
            case R.id.action_logout:
                deleteCredentials();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Async task to get all item categories
     * */
    private class GetHallsOrdersFromToday extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(OverviewActivity.this);
            progressDialog.setMessage(getResources().getString(R.string.loading_msg));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            String URL_ORDERS_HISTORY = serverAddress + getResources().getString(R.string.hall_overview) + userCustomerId; // "http://mkitsql.mk-group.org/ECoffee/api/OrderHeader/HallOverview/" + userCustomerId;

            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(URL_ORDERS_HISTORY);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            }catch( Exception e) {
                e.printStackTrace();
            }
            finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }

            String jsonHeader = result.toString();

            if (!jsonHeader.isEmpty()) {
                try {
                    JSONObject jsonObjHeader = new JSONObject(jsonHeader);
                    if (!jsonObjHeader.isNull("OrderHeader")) {
                        JSONArray headers = jsonObjHeader.getJSONArray("OrderHeader");

                        for (int i = 0; i < headers.length(); i++) {
                            JSONObject headObj = (JSONObject) headers.get(i);

                            Date timeSent = null;
                            Date timeDelivered = null;

                            timeSent = new DateTime(headObj.getString("TimeSent")).toDate();
                            timeDelivered = new DateTime(headObj.getString("TimeDelivered")).toDate();

                            OrderHeader orderHeader = new OrderHeader(headObj.getInt("OrderHeaderId"),
                                    timeSent, timeDelivered, headObj.getString("Status"), headObj.getString("Comment"),
                                    headObj.getInt("CompanyId"), headObj.getInt("RoomId"), headObj.getInt("UserWaiterId"),
                                    headObj.getInt("UserCustomerId"));
                            orderHeaderList.add(orderHeader);

                            if (!headObj.isNull("OrderItems")) {
                                JSONArray items = headObj.getJSONArray("OrderItems");
                                for (int x = 0; x < items.length(); x++) {
                                    JSONObject itemObj = (JSONObject) items.get(x);
                                    OrderItem item = new OrderItem(itemObj.getInt("OrderHeaderId"), itemObj.getInt("OrderItemId"),
                                            itemObj.getInt("Quantity"), itemObj.getDouble("Price"), itemObj.getDouble("Amount"),
                                            itemObj.getInt("KitchenId"), itemObj.getInt("BeverageId"), "");
                                    orderItemList.add(item);
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("JSON Data", "Didn't receive any data from server!");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            populateHallsOrderList();

            if (progressDialog.isShowing())
                progressDialog.dismiss();

        }
    }


    private void populateHallsOrderList() {

        List<OrderHeader> listDataHeader = new ArrayList<>(orderHeaderList);
        HashMap<String, List<OrderItem>> listDataChild = new HashMap<>();

        Collections.reverse(listDataHeader);

        DatabaseHandler databaseHandler = new DatabaseHandler(getApplicationContext());

        if (orderHeaderList != null && orderHeaderList.size() > 0) {

            for (int i = 0; i < listDataHeader.size(); i++) {
                List<OrderItem> children = new ArrayList<>();
                for (int x = 0; x < orderItemList.size(); x++) {
                    OrderItem orderItem = orderItemList.get(x);
                    if (listDataHeader.get(i).getOrderHeaderId() == orderItem.getOrderHeaderId()) {
                        orderItem.setBeverageName(databaseHandler.getBeverageName(userKitchenId, orderItem.getBeverageId()));
                        children.add(orderItem);
                    }
                }
                listDataChild.put(String.valueOf(listDataHeader.get(i).getOrderHeaderId()), children);
            }

            listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild, "HALL");
            ordersExpListHall.setAdapter(listAdapter);

        }
    }


    private class UpdateHallsOrdersFromToday extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            String URL_ORDERS_HISTORY = serverAddress + getResources().getString(R.string.hall_overview) + userCustomerId; // "http://mkitsql.mk-group.org/ECoffee/api/OrderHeader/HallOverview/" + userCustomerId;

            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(URL_ORDERS_HISTORY);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            }catch( Exception e) {
                e.printStackTrace();
            }
            finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }

            String jsonHeader = result.toString();

            if (!jsonHeader.isEmpty()) {
                orderHeaderList.clear();
                orderItemList.clear();
                try {
                    JSONObject jsonObjHeader = new JSONObject(jsonHeader);
                    if (!jsonObjHeader.isNull("OrderHeader")) {
                        JSONArray headers = jsonObjHeader.getJSONArray("OrderHeader");

                        for (int i = 0; i < headers.length(); i++) {
                            JSONObject headObj = (JSONObject) headers.get(i);

                            Date timeSent = null;
                            Date timeDelivered = null;

                            timeSent = new DateTime(headObj.getString("TimeSent")).toDate();
                            timeDelivered = new DateTime(headObj.getString("TimeDelivered")).toDate();
                            // TODO ovo treba proveriti
                            if (!headObj.getString("Status").equals("R")) {  // kad Ivan promeni servis ovo mogu skloniti

                                OrderHeader orderHeader = new OrderHeader(headObj.getInt("OrderHeaderId"),
                                        timeSent, timeDelivered, headObj.getString("Status"), headObj.getString("Comment"),
                                        headObj.getInt("CompanyId"), headObj.getInt("RoomId"), headObj.getInt("UserWaiterId"),
                                        headObj.getInt("UserCustomerId"));
                                orderHeaderList.add(orderHeader);

                                if (!headObj.isNull("OrderItems")) {
                                    JSONArray items = headObj.getJSONArray("OrderItems");
                                    for (int x = 0; x < items.length(); x++) {
                                        JSONObject itemObj = (JSONObject) items.get(x);
                                        OrderItem item = new OrderItem(itemObj.getInt("OrderHeaderId"), itemObj.getInt("OrderItemId"),
                                                itemObj.getInt("Quantity"), itemObj.getDouble("Price"), itemObj.getDouble("Amount"),
                                                itemObj.getInt("KitchenId"), itemObj.getInt("BeverageId"), "");
                                        orderItemList.add(item);
                                    }
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("JSON Data", "Didn't receive any data from server!");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            populateHallsOrderList();
        }
    }



    private void deleteCredentials() {

        AlertDialog.Builder builder = new AlertDialog.Builder(OverviewActivity.this);
        LayoutInflater li = LayoutInflater.from(OverviewActivity.this);

        View promptsView = li.inflate(R.layout.delete_exit_dialog, (ViewGroup)null);
        final Button noBtn = (Button) promptsView.findViewById(R.id.noButtonDeleteDialog);
        final Button yesBtn = (Button) promptsView.findViewById(R.id.yesButtonDeleteDialog);
        final TextView deleteExitMsg = (TextView) promptsView.findViewById(R.id.textDeleteExitMsg);
        deleteExitMsg.setText(getResources().getString(R.string.delete_user_dialog_msg));

        builder.setView(promptsView)
                .setTitle(R.string.logout_dialog_title)
                .setCancelable(false);

        final AlertDialog alertDialog = builder.create();

        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alertDialog.dismiss();

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                sharedPreferences.edit().remove("username").apply();
                sharedPreferences.edit().remove("password").apply();

                databaseHandler.deleteAllUsers();
                databaseHandler.deleteAllUserRoles();

                finish();
            }
        });

        alertDialog.show();
    }


    // Update order status
    private class UpdateOrderStatusToClean extends AsyncTask<Void, Void, Void> {

        private String URL_ORDER_STATUS_HALL = serverAddress + getResources().getString(R.string.update_order_header); // "http://mkitsql.mk-group.org/ECoffee/api/OrderHeader/UpdateOrderHeader";
        private JSONObject jsonObject;
        private JSONArray jsonArray;
        private String response = "";
        private int responseCode = 0;
        private boolean isChanged = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(OverviewActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            jsonArray = new JSONArray();

            try {

                for (int i = 0; i < orderHeaderList.size(); i++) {

                    if (orderHeaderList.get(i).getStatus().equals("I")) {

                        try {

                            jsonObject = new JSONObject();
                            jsonObject.put("OrderHeaderId", orderHeaderList.get(i).getOrderHeaderId());
                            jsonObject.put("Status", "R");

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        isChanged = true;
                        orderHeaderList.get(i).setStatus("R");
                        jsonArray.put(jsonObject);
                    }
                }


                URL url = new URL(URL_ORDER_STATUS_HALL); //Enter URL here
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setReadTimeout(15000);
                httpURLConnection.setConnectTimeout(15000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.connect();

                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(jsonArray.toString());
                wr.flush();
                wr.close();

                responseCode = httpURLConnection.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (progressDialog.isShowing())
                progressDialog.dismiss();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                if (response.equals("OK")) {
                    if (isChanged)
                        listAdapter.notifyDataSetChanged();

                } else {
                    Toast.makeText(OverviewActivity.this, response, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(OverviewActivity.this,
                        getResources().getString(R.string.error_msg), Toast.LENGTH_LONG).show();
            }
        }

    }


    // Send call to kitchen
    private class SendCallToKitchen extends AsyncTask<Void,Void,Void> {

        private String URL_ORDER_HEADER = serverAddress + getResources().getString(R.string.insert_user_call); // "http://mkitsql.mk-group.org/ECoffee/api/UserCall/InsertUserCall";
        private JSONObject jsonObject;
        private String response = "";
        private int responseCode = 0;
        private String statusNew = "N";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(OverviewActivity.this);
            progressDialog.setMessage(getResources().getString(R.string.sending_data));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                Calendar currentTime = Calendar.getInstance();
                Date timeDate = currentTime.getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                jsonObject = new JSONObject();
                jsonObject.put("TimeSent", sdf.format(timeDate));
                jsonObject.put("Status", statusNew); // novi
                jsonObject.put("Message", callMessage);
                jsonObject.put("RoomId", userRoomId);


                URL url = new URL(URL_ORDER_HEADER); //Enter URL here
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setReadTimeout(15000);
                httpURLConnection.setConnectTimeout(15000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.connect();

                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(jsonObject.toString());
                wr.flush();
                wr.close();

                responseCode = httpURLConnection.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }
                }
                else {
                    response="";
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            if (progressDialog.isShowing())
                progressDialog.dismiss();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                if (response.equals("ERROR")) {
                    Toast.makeText(OverviewActivity.this,
                            getResources().getString(R.string.error_msg), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(OverviewActivity.this,
                            getResources().getString(R.string.hall_call_msg_success), Toast.LENGTH_LONG).show();
                }
            }
        }
    }


}
