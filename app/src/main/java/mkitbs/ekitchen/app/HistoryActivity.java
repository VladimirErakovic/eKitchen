package mkitbs.ekitchen.app;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import mkitbs.ekitchen.app.entities.OrderHeader;
import mkitbs.ekitchen.app.entities.OrderItem;
import mkitbs.ekitchen.app.helpers.DatabaseHandler;
import mkitbs.ekitchen.app.helpers.ExpandableListAdapter;

public class HistoryActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private ExpandableListView ordersExpList;
    private List<OrderHeader> orderHeaderList;
    private List<OrderItem> orderItemList;
    private DatabaseHandler databaseHandler;
    private int kitchenId;
    private String serverAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        databaseHandler = new DatabaseHandler(getApplicationContext());

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        serverAddress = sharedPreferences.getString("server_ip_address", "");

        kitchenId = this.getIntent().getIntExtra("kitchen_id", 0);

        ordersExpList = (ExpandableListView) findViewById(R.id.listExpOrders);

        ordersExpList.setClickable(true);

        orderHeaderList = new ArrayList<>();
        orderItemList = new ArrayList<>();

        new GetOrdersHistory().execute();

        // Listview Group click listener
        ordersExpList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                // ovde moze da se trigeruje neki brojac recimo
                return false;
            }
        });

        // Listview on child click listener
        ordersExpList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {

               // Contact cont = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);

                return false;
            }
        });

        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    /**
     * Async task to get all item categories
     * */
    private class GetOrdersHistory extends AsyncTask<Void, Void, Void> {

        DatabaseHandler databaseHandler;

        @Override
        protected void onPreExecute() {

            databaseHandler = new DatabaseHandler(getApplicationContext());
            super.onPreExecute();
            progressDialog = new ProgressDialog(HistoryActivity.this);
            progressDialog.setMessage(getResources().getString(R.string.loading_msg));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            String userCustomerId = String.valueOf(databaseHandler.getLoggedUser().getUserId());

            // "http://mkitsql.mk-group.org/ECafe/api/OrderHeader/History/" + userCustomerId + "/" + kitchenId;
            String URL_ORDERS_HALL_TODAY = serverAddress + getResources().getString(R.string.history) + userCustomerId + "/" + kitchenId;

            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(URL_ORDERS_HALL_TODAY);
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
                Log.e("JSON Header Data", "Didn't receive any data from server!");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            populateList();

            if (progressDialog.isShowing())
                progressDialog.dismiss();
        }

    }


    private void populateList() {

        List<OrderHeader> listDataHeader = new ArrayList<>(orderHeaderList);
        HashMap<String, List<OrderItem>> listDataChild = new HashMap<>();

        Collections.reverse(listDataHeader);

        if (orderHeaderList != null && orderHeaderList.size() > 0) {

            for (int i = 0; i < listDataHeader.size(); i++) {
                List<OrderItem> children = new ArrayList<>();
                for (int x = 0; x < orderItemList.size(); x++) {
                    OrderItem orderItem = orderItemList.get(x);
                    if (listDataHeader.get(i).getOrderHeaderId() == orderItem.getOrderHeaderId()) {
                        orderItem.setBeverageName(databaseHandler.getBeverageName(kitchenId, orderItem.getBeverageId()));
                        children.add(orderItem);
                    }
                }
                listDataChild.put(String.valueOf(listDataHeader.get(i).getOrderHeaderId()), children);
            }

            ExpandableListAdapter listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild, "USER");
            ordersExpList.setAdapter(listAdapter);

        } else {
            Toast.makeText(HistoryActivity.this,
                    getResources().getString(R.string.list_is_empty), Toast.LENGTH_SHORT).show();
        }
    }
}
