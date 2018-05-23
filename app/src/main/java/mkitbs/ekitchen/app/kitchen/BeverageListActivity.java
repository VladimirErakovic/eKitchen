package mkitbs.ekitchen.app.kitchen;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import mkitbs.ekitchen.app.R;
import mkitbs.ekitchen.app.AboutActivity;
import mkitbs.ekitchen.app.entities.Beverage;
import mkitbs.ekitchen.app.entities.Company;
import mkitbs.ekitchen.app.entities.Kitchen;
import mkitbs.ekitchen.app.entities.Location;
import mkitbs.ekitchen.app.entities.OrderHeader;
import mkitbs.ekitchen.app.entities.OrderItem;
import mkitbs.ekitchen.app.entities.Room;
import mkitbs.ekitchen.app.entities.User;
import mkitbs.ekitchen.app.helpers.DatabaseHandler;
import mkitbs.ekitchen.app.recyclerview.BeverageAdapter;
import mkitbs.ekitchen.app.recyclerview.OrderAdapter;
import mkitbs.ekitchen.app.recyclerview.SeparatorDecoration;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * An activity representing a list of Beverages. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link BeverageDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class BeverageListActivity extends AppCompatActivity implements BeverageDetailFragment.PlaceholderFragment.OnGridItemClickListener{

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private OrderAdapter orderAdapter;
    private BeverageAdapter beverageAdapter;
    private RecyclerView recyclerView;
    private List<OrderItem> orderItemList;
    private List<Beverage> beverageList;
    private DatabaseHandler databaseHandler;
    private ProgressDialog progressDialog;

    private User user;
    private String userKind;
    private Kitchen userKitchen;
    private int userKitchenId;

    private int orderHeaderId = 0;
    private int roomIdSpinner = 0;
    private int companyIdSpinner = 0;
    private String comment = "";
    private boolean mode = false;
    private String serverAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beverage_list);

        // for change availability mode
        if (getIntent().hasExtra("mode"))
            mode = getIntent().getBooleanExtra("mode", false);

        TextView listTitle = (TextView) findViewById(R.id.beverageListTitle);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mode) {
            toolbar.setTitle(getResources().getString(R.string.title_kitchen_status_change));
            listTitle.setText(getResources().getString(R.string.beverage_status_list_title));
        } else {
            toolbar.setTitle(getResources().getString(R.string.title_kitchen_ordering));
            listTitle.setVisibility(View.GONE);
        }
        setSupportActionBar(toolbar);
        // Show the Up button in the action bar.
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        databaseHandler = new DatabaseHandler(getApplicationContext());
        user = databaseHandler.getLoggedUser();
        userKitchen = databaseHandler.getKitchenByUser(user.getUserId());
        userKitchenId = userKitchen.getKitchenId();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        userKind = sharedPreferences.getString("user_kind", "MEETING_ROOM");
        serverAddress = sharedPreferences.getString("server_ip_address", "");

        orderItemList = new ArrayList<>();

        Button cancelBtn = (Button) findViewById(R.id.cancelButtonKitchen);
        Button sendBtn = (Button) findViewById(R.id.sendButtonKitchen);

        if(mode) {
            beverageList = databaseHandler.getAllUnavailableBeverages();  // samo nedostupna pica
            sendBtn.setText(getResources().getString(R.string.btn_update));
        }

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!orderItemList.isEmpty()) {

                    AlertDialog.Builder builderDelete = new AlertDialog.Builder(BeverageListActivity.this);
                    LayoutInflater layoutInflaterDelete = LayoutInflater.from(BeverageListActivity.this);

                    View promptsViewDelete = layoutInflaterDelete.inflate(R.layout.delete_exit_dialog, (ViewGroup)null);

                    final TextView msgText = (TextView) promptsViewDelete
                            .findViewById(R.id.textDeleteExitMsg);
                    final Button btnNo = (Button) promptsViewDelete
                            .findViewById(R.id.noButtonDeleteDialog);
                    final Button btnYes = (Button) promptsViewDelete
                            .findViewById(R.id.yesButtonDeleteDialog);

                    builderDelete.setView(promptsViewDelete)
                            .setCancelable(false);

                    final AlertDialog alertDialog = builderDelete.create();

                    msgText.setText(getResources().getString(R.string.delete_all_dialog_msg));

                    btnYes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                            orderItemList.clear();
                            orderAdapter.notifyDataSetChanged();
                        }
                    });
                    btnNo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                        }
                    });

                    alertDialog.show();

                } else {

                    finish();
                }
            }
        });
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mode) {
                    //if (!beverageList.isEmpty()) {
                        updateAvailability();
                    //} else {
                    //    Toast.makeText(BeverageListActivity.this,
                    //            getResources().getString(R.string.no_beverage_entered), Toast.LENGTH_LONG).show();
                    //}
                } else {
                    if (!orderItemList.isEmpty()) {
                        sendOrder();
                    } else {
                        Toast.makeText(BeverageListActivity.this,
                                getResources().getString(R.string.no_beverage_entered), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //Remove swiped item from list and notify the RecyclerView
                final int adapterPosition = viewHolder.getAdapterPosition();
                final OrderItem orderItem = orderItemList.get(adapterPosition);

                AlertDialog.Builder builderSwipeDelete = new AlertDialog.Builder(BeverageListActivity.this);
                LayoutInflater layoutInflaterDelete = LayoutInflater.from(BeverageListActivity.this);

                View promptsViewDelete = layoutInflaterDelete.inflate(R.layout.delete_exit_dialog, (ViewGroup)null);

                final TextView msgText = (TextView) promptsViewDelete
                        .findViewById(R.id.textDeleteExitMsg);
                final Button btnNo = (Button) promptsViewDelete
                        .findViewById(R.id.noButtonDeleteDialog);
                final Button btnYes = (Button) promptsViewDelete
                        .findViewById(R.id.yesButtonDeleteDialog);

                builderSwipeDelete.setView(promptsViewDelete)
                        .setCancelable(false);

                final AlertDialog alertDialogSwipe = builderSwipeDelete.create();

                msgText.setText(getResources().getString(R.string.delete_dialog_msg));

                btnYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialogSwipe.dismiss();
                    }
                });
                btnNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialogSwipe.dismiss();
                        orderItemList.add(adapterPosition, orderItem);
                        orderAdapter.notifyItemInserted(adapterPosition);
                        recyclerView.scrollToPosition(adapterPosition);
                    }
                });

                alertDialogSwipe.show();


                orderItemList.remove(viewHolder.getAdapterPosition());
                orderAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());

            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);

        recyclerView = (RecyclerView) findViewById(R.id.beverage_list);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new SeparatorDecoration(getResources().getColor(R.color.md_grey_100), 1));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        if (mode) {

            beverageAdapter = new BeverageAdapter(beverageList);
            recyclerView.setAdapter(beverageAdapter);

        } else {

            //za swipe
            itemTouchHelper.attachToRecyclerView(recyclerView);

            orderAdapter = new OrderAdapter(orderItemList, new OrderAdapter.MyAdapterListener() {
                @Override
                public void btnRemoveOnClick(View v, int position) {
                    final OrderItem item = orderItemList.get(position);
                    final int itemPosition = position;
                    if (item.getItemQuantity() > 1) {
                        item.setItemQuantity(item.getItemQuantity() - 1);
                        orderAdapter.notifyItemChanged(position);
                    } else {

                        AlertDialog.Builder builderButtonDelete = new AlertDialog.Builder(BeverageListActivity.this);
                        LayoutInflater layoutInflaterDelete = LayoutInflater.from(BeverageListActivity.this);

                        View promptsViewDelete = layoutInflaterDelete.inflate(R.layout.delete_exit_dialog, (ViewGroup)null);

                        final TextView msgText = (TextView) promptsViewDelete
                                .findViewById(R.id.textDeleteExitMsg);
                        final Button btnNo = (Button) promptsViewDelete
                                .findViewById(R.id.noButtonDeleteDialog);
                        final Button btnYes = (Button) promptsViewDelete
                                .findViewById(R.id.yesButtonDeleteDialog);

                        builderButtonDelete.setView(promptsViewDelete)
                                .setCancelable(false);

                        final AlertDialog alertDialogButton = builderButtonDelete.create();

                        msgText.setText(getResources().getString(R.string.delete_dialog_msg));

                        btnYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                alertDialogButton.dismiss();
                                orderItemList.remove(itemPosition);
                                orderAdapter.notifyItemRemoved(itemPosition);
                            }
                        });
                        btnNo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                alertDialogButton.dismiss();
                            }
                        });

                        alertDialogButton.show();
                    }
                }

                @Override
                public void btnAddOnClick(View v, int position) {
                    OrderItem item = orderItemList.get(position);
                    item.setItemQuantity(item.getItemQuantity() + 1);
                    orderAdapter.notifyItemChanged(position);
                }
            });
            recyclerView.setAdapter(orderAdapter);
        }


        if (findViewById(R.id.beverage_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        if (mTwoPane) {
            BeverageDetailFragment fragment = new BeverageDetailFragment();

            Bundle arguments = new Bundle();
            arguments.putBoolean("mode", mode);
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.beverage_detail_container, fragment)
                    .commit();
        } else {

            // For two screens - not implemented for now
            // Context context = v.getContext();
            // Intent intent = new Intent(context, BeverageDetailActivity.class);
            // intent.putExtra(BeverageDetailFragment.ARG_ITEM_ID, holder.mItem.id);

            // context.startActivity(intent);
        }
    }


    @Override
    public void onGridItemClick(int beverageId, String beverageName, boolean isChecked) {

        if (mode) {
            Beverage beverage = databaseHandler.getBeverageById(userKitchenId, beverageId);
            beverage.setBeverageName(beverageName);
            if (!beverageList.isEmpty()) {
                if (isChecked) {
                    beverageList.add(beverage);
                    beverageAdapter.notifyItemInserted(beverageList.size());
                } else {
                    for (int i = 0; i < beverageList.size(); i++) {
                        if (beverageId == beverageList.get(i).getBeverageId()) {
                            beverageList.remove(i);
                            beverageAdapter.notifyItemRemoved(i);
                            break;
                        }
                    }
                }
            } else {  // ovo treba proveriti
                beverageList.add(beverage);
                beverageAdapter.notifyItemInserted(beverageList.size());
            }
        } else {
            boolean isDuplicate = false;
            if (!orderItemList.isEmpty()) {
                for (int i = 0; i < orderItemList.size(); i++) {
                    if (beverageId == orderItemList.get(i).getBeverageId()) {
                        orderItemList.get(i).setItemQuantity(orderItemList.get(i).getItemQuantity() + 1);
                        isDuplicate = true;
                        break;
                    }
                }
                if (!isDuplicate) {
                    int orderId = orderItemList.size() + 1;
                    OrderItem item = new OrderItem(0, orderId, 1, 0.0, 0.0, userKitchenId, beverageId, beverageName);
                    orderItemList.add(item);
                }
            } else {
                int orderItemId = orderItemList.size() + 1;
                OrderItem item = new OrderItem(0, orderItemId, 1, 0.0, 0.0, userKitchenId, beverageId, beverageName);
                orderItemList.add(item);
            }
            orderAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.kitchen_ordering_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_about:
                Intent intentAbout = new Intent(BeverageListActivity.this, AboutActivity.class);
                startActivity(intentAbout);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void sendOrder() {          // sve ovo bi trebalo malo drugacije

        AlertDialog.Builder builder = new AlertDialog.Builder(BeverageListActivity.this);
        LayoutInflater li = LayoutInflater.from(BeverageListActivity.this);

        View promptsView = li.inflate(R.layout.set_location_dialog, (ViewGroup)null);

        final Spinner locationSpinner= (Spinner) promptsView
                .findViewById(R.id.spinnerLocation);
        final Spinner roomSpinner= (Spinner) promptsView
                .findViewById(R.id.spinnerRoom);
        final Spinner companySpinner= (Spinner) promptsView
                .findViewById(R.id.spinnerCompany);
        final EditText commentEditText = (EditText) promptsView
                .findViewById(R.id.commentEditText);
        final Button cancelBtn = (Button) promptsView.
                findViewById(R.id.cancelButtonSendDialog);
        final Button sendBtn = (Button) promptsView.
                findViewById(R.id.sendButtonSendDialog);

        builder.setView(promptsView)
                .setTitle(R.string.send_dialog_title)
                .setCancelable(false);

        final AlertDialog alertDialog = builder.create();

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alertDialog.dismiss();

                if (!orderItemList.isEmpty()) {
                    String roomName = roomSpinner.getSelectedItem().toString();
                    List<Room> roomList = databaseHandler.getAllRooms();

                    for (Room room : roomList) {
                        if (room.getRoomName().equals(roomName)) {
                            roomIdSpinner = room.getRoomId();
                        }
                    }
                    String companyName = companySpinner.getSelectedItem().toString();
                    List<Company> companyList =  databaseHandler.getAllCompanies();

                    for (Company company : companyList) {
                        if (company.getCompanyName().equals(companyName)) {
                            companyIdSpinner = company.getCompanyId();
                        }
                    }
                    comment = commentEditText.getText().toString();

                    new PostNewOrderHeader().execute();
                } else {
                    Toast.makeText(BeverageListActivity.this,
                            getResources().getString(R.string.no_beverage_entered), Toast.LENGTH_LONG).show();
                }
            }
        });

        // check for default values
        final int userDefaultRoom = user.getUserRoomId();
        Room userRoom = databaseHandler.getRoomById(userDefaultRoom);

        int spinnerKind = 0;
        List<Location> locationListHelp = new ArrayList<>();
        Location defaultLocation;
        if (userKind.equals("MEETING_ROOM") || userKind.equals("KITCHEN")) {
            defaultLocation = databaseHandler.getLocationById(userRoom.getRoomLocationId());
            locationListHelp.add(defaultLocation);
            spinnerKind = R.layout.spinner_item_no_arrow;
        } else {
            locationListHelp = databaseHandler.getAllLocations();
            spinnerKind = R.layout.spinner_item_arrow;
        }

        final List<Location> locationList = new ArrayList<>(locationListHelp);
        final ArrayList<String> locationNames = new ArrayList<>();
        if (locationList.size() > 0) {
            for (Location location : locationList) {
                locationNames.add(location.getLocationName());
            }
        }
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(BeverageListActivity.this,
                spinnerKind, locationNames);
        locationAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        locationSpinner.setAdapter(locationAdapter);
        if (userKind.equals("MEETING_ROOM") || userKind.equals("KITCHEN")) {
            locationSpinner.setEnabled(false);
        }

        if (userKind.equals("MEETING_ROOM") || userKind.equals("KITCHEN")) {

            List<Room> roomList = new ArrayList<>();
            roomList.add(userRoom);
            ArrayList<String> roomNames = new ArrayList<>();
            if (roomList.size() > 0) {
                for (int i = 0; i < roomList.size(); i++) {
                    Room room = roomList.get(i);
                    roomNames.add(room.getRoomName());
                }
            }
            ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(BeverageListActivity.this,
                    R.layout.spinner_item_no_arrow, roomNames);
            roomAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            roomSpinner.setAdapter(roomAdapter);
            roomSpinner.setEnabled(false);

        } else {

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
                            if (room.getRoomId() == userDefaultRoom) {
                                userDefaultRoomListPosition = i;
                            }
                        }
                    }
                    ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(BeverageListActivity.this,
                            R.layout.spinner_item_arrow, roomNames);
                    roomAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    roomAdapter.notifyDataSetChanged();
                    roomSpinner.setAdapter(roomAdapter);
                    if (userDefaultRoomListPosition > -1)
                        roomSpinner.setSelection(userDefaultRoomListPosition);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });

        }

        List<Company> companyList = new ArrayList<>();
        if (userKind.equals("MEETING_ROOM") || userKind.equals("KITCHEN"))  // a sta u ostalim slucajevima?
            companyList =  databaseHandler.getAllCompanies();

        ArrayList<String> companyNames = new ArrayList<>();
        if (companyList != null && companyList.size() > 0) {
            for (Company company : companyList) {
                companyNames.add(company.getCompanyName());
            }
        }
        ArrayAdapter<String> companyAdapter = new ArrayAdapter<>(BeverageListActivity.this,   // treba napraviti custom Adapter
                R.layout.spinner_item_arrow, companyNames);
        companyAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        companySpinner.setAdapter(companyAdapter);

        alertDialog.show();

    }


    // Send order to server
    private class PostNewOrderHeader extends AsyncTask<Void,Void,Void> {

        // "http://mkitsql.mk-group.org/ECafe/api/OrderHeader/InsertOrderHeader";
        private String URL_ORDER_HEADER = serverAddress + getResources().getString(R.string.insert_order_header);
        private JSONObject jsonObject;
        private String response = "";
        private int responseCode = 0;
        private OrderHeader orderHeader = new OrderHeader();   // ovaj nacin bih trebao da zamenim sa Parcelable
        private String statusNew = "N";

         @Override
         protected void onPreExecute() {
             super.onPreExecute();
             progressDialog = new ProgressDialog(BeverageListActivity.this);
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
                orderHeader.setTimeSent(timeDate);
                jsonObject.put("TimeDelivered", sdf.format(timeDate));
                orderHeader.setTimeDelivered(timeDate);
                jsonObject.put("Status", statusNew); // novi
                orderHeader.setStatus(statusNew);
                jsonObject.put("Comment", comment);
                orderHeader.setComment(comment);
                jsonObject.put("CompanyId", companyIdSpinner);
                orderHeader.setCompanyId(companyIdSpinner);
                jsonObject.put("RoomId", roomIdSpinner); //ovde zapravo treba odabrana soba
                orderHeader.setRoomId(roomIdSpinner);
                jsonObject.put("UserWaiterId", user.getUserId());  // treba izabrati usera!!!!!!
                orderHeader.setUserWaiterId(user.getUserId());
                jsonObject.put("UserCustomerId", user.getUserId());
                orderHeader.setUserCustomerId(user.getUserId());


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
                    orderHeaderId = Integer.valueOf(response);

                    orderHeader.setOrderHeaderId(orderHeaderId);
                    databaseHandler.addOrderHeader(orderHeader);
                    databaseHandler.close();
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

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                if (orderHeaderId > 0) {
                    new PostNewOrderItems().execute(orderItemList);
                } else {
                    Toast.makeText(BeverageListActivity.this, "Greška. Ključ porudžbine je nula.", Toast.LENGTH_LONG).show();
                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                }
            }

        }

    }


    private class PostNewOrderItems extends AsyncTask<List<OrderItem>,Void,Void> {

        // "http://mkitsql.mk-group.org/ECafe/api/OrderItem/InsertOrderItem";
        private String URL_ORDER_ITEM = serverAddress + getResources().getString(R.string.insert_order_item);
        private JSONObject jsonObject;
        private JSONArray jsonArray;
        private String response = "";
        private int counter;
        private OrderItem orderItem = new OrderItem();

        @SafeVarargs
        @Override
        final protected Void doInBackground(List<OrderItem>... params) {

            List<OrderItem> orderItemList = params[0];
            jsonArray = new JSONArray();

            for (int i = 0; i < orderItemList.size(); i++) {

                counter = i + 1;

                try {
                    jsonObject = new JSONObject();

                    jsonObject.put("OrderHeaderId", orderHeaderId);
                    orderItem.setOrderHeaderId(orderHeaderId);
                    jsonObject.put("OrderItemId", counter);
                    orderItem.setOrderItemId(counter);
                    jsonObject.put("Quantity", orderItemList.get(i).getItemQuantity());
                    orderItem.setItemQuantity(orderItemList.get(i).getItemQuantity());
                    orderItem.setItemPrice(0.0);
                    orderItem.setItemAmount(0.0);
                    jsonObject.put("KitchenId", userKitchen.getKitchenId()); // e ovo treba resiti
                    orderItem.setKitchenId(userKitchen.getKitchenId());
                    jsonObject.put("BeverageId", orderItemList.get(i).getBeverageId());
                    orderItem.setBeverageId(orderItemList.get(i).getBeverageId());
                    orderItem.setBeverageName(databaseHandler.getBeverageName(userKitchenId, orderItemList.get(i).getBeverageId()));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                jsonArray.put(jsonObject);
                databaseHandler.addOrderItem(orderItem);
            }

            databaseHandler.close();
            webServiceCall(jsonArray.toString());


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            if (progressDialog.isShowing())
                progressDialog.dismiss();

            if (response.equals("ERROR")) {
                Toast.makeText(BeverageListActivity.this,
                        getResources().getString(R.string.error_msg), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(BeverageListActivity.this,
                        getResources().getString(R.string.order_sent_successfully), Toast.LENGTH_LONG).show();
                orderItemList.clear();
                orderAdapter.notifyDataSetChanged();

                Intent intent = new Intent();
                intent.putExtra("new_order_id", orderHeaderId);
                setResult(550, intent);
                finish();
            }
        }

        private String webServiceCall(String jsonArrayString) {

            try {
                URL url = new URL(URL_ORDER_ITEM);
                HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setReadTimeout(15000);
                httpURLConnection.setConnectTimeout(15000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.connect();

                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(jsonArrayString);
                wr.flush();
                wr.close();

                int responseCode = httpURLConnection.getResponseCode();

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
            }

            return response;
        }

    }

    private void updateAvailability() {

        AlertDialog.Builder builder = new AlertDialog.Builder(BeverageListActivity.this);
        LayoutInflater li = LayoutInflater.from(BeverageListActivity.this);

        View promptsView = li.inflate(R.layout.confirm_dialog, (ViewGroup)null);
        final Button noBtn = (Button) promptsView.findViewById(R.id.noButtonDialog);
        final Button yesBtn = (Button) promptsView.findViewById(R.id.yesButtonDialog);
        final TextView confirmText = (TextView) promptsView.findViewById(R.id.confirmDialogText);
        confirmText.setText(getResources().getString(R.string.update_dialog_text));

        builder.setView(promptsView)
                .setTitle(R.string.update_dialog_title)
                .setCancelable(false);
        // create alert dialog
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

                new UpdateBeverageAvailability().execute();
            }
        });

        alertDialog.show();
    }

    private class UpdateBeverageAvailability extends AsyncTask<Void,Void,Void> {

        // "http://mkitsql.mk-group.org/ECafe/api/Beverage/UpdateBeverageAvailability";
        private String URL_BEVERAGE_STATUS = serverAddress + getResources().getString(R.string.update_beverage_availability);
        private JSONObject jsonObject;
        private JSONArray jsonArray;
        private String response = "";
        private int responseCode = 0;
        private List<Beverage> beverageListFromDatabase;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(BeverageListActivity.this);
            progressDialog.setMessage(getResources().getString(R.string.sending_data));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                jsonArray = new JSONArray();

                if (!beverageList.isEmpty()) {

                    beverageListFromDatabase = databaseHandler.getAllBeverages();

                    for (Beverage beverage : beverageListFromDatabase) {

                        beverage.setAvailable(1);

                        try {
                            jsonObject = new JSONObject();

                            jsonObject.put("KitchenId", beverage.getBevKitchenId());
                            jsonObject.put("BeverageId", beverage.getBeverageId());
                            jsonObject.put("Available", beverage.isAvailable());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        jsonArray.put(jsonObject);

                    }

                } else {

                    beverageListFromDatabase = databaseHandler.getAllBeverages();

                    for (Beverage beverage : beverageListFromDatabase) {

                        for (Beverage beverageUnavailable : beverageList) {
                            if (beverageUnavailable.getBeverageId() == beverage.getBeverageId()) {
                                beverage.setAvailable(0);
                                break;
                            } else {
                                beverage.setAvailable(1);
                            }
                        }

                        try {
                            jsonObject = new JSONObject();

                            jsonObject.put("KitchenId", beverage.getBevKitchenId());
                            jsonObject.put("BeverageId", beverage.getBeverageId());
                            jsonObject.put("Available", beverage.isAvailable());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        jsonArray.put(jsonObject);
                    }
                }

                URL url = new URL(URL_BEVERAGE_STATUS); //Enter URL here
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
                else {
                    response="";
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

            if (progressDialog.isShowing())
                progressDialog.dismiss();

            if (response.equals("ERROR")) {
                Toast.makeText(BeverageListActivity.this,
                        getResources().getString(R.string.error_msg), Toast.LENGTH_LONG).show();
            } else {

                // only now update beverages on phone database
                for (Beverage beverage: beverageListFromDatabase) {
                    databaseHandler.updateBeverageStatus(beverage);
                }

                Toast.makeText(BeverageListActivity.this,
                        getResources().getString(R.string.send_statuses_success_msg), Toast.LENGTH_LONG).show();

            }

        }


    }

}
