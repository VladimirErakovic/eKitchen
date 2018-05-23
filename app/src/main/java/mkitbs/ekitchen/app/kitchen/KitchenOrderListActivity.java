package mkitbs.ekitchen.app.kitchen;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import mkitbs.ekitchen.app.R;
import mkitbs.ekitchen.app.AboutActivity;
import mkitbs.ekitchen.app.entities.Company;
import mkitbs.ekitchen.app.entities.Kitchen;
import mkitbs.ekitchen.app.entities.KitchenOrderHeader;
import mkitbs.ekitchen.app.entities.OrderHeader;
import mkitbs.ekitchen.app.entities.OrderItem;
import mkitbs.ekitchen.app.entities.Room;
import mkitbs.ekitchen.app.entities.User;
import mkitbs.ekitchen.app.entities.UserCall;
import mkitbs.ekitchen.app.entities.Waiter;
import mkitbs.ekitchen.app.helpers.DatabaseHandler;
import mkitbs.ekitchen.app.helpers.StatusButton;
import mkitbs.ekitchen.app.recyclerview.KitchenOrderAdapter;
import mkitbs.ekitchen.app.recyclerview.SeparatorDecoration;

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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

/**
 * An activity representing a list of KitchenOrders. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link KitchenOrderDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class KitchenOrderListActivity extends AppCompatActivity implements KitchenOrderDetailFragment.OnStatusButtonListener{

    private Timer repeatTask;

    private RecyclerView recyclerView;
    private KitchenOrderAdapter kitchenOrderAdapter;
    private DatabaseHandler databaseHandler;
    private ProgressDialog progressDialog;
    private List<KitchenOrderHeader> orderHeaderList;
    private List<KitchenOrderHeader> secondOrderHeaderList;
    private int kitchenId = 0;
    private StatusButton statusButton;
    private TextView statusText;
    private FloatingActionButton fabMsg;
    private List<UserCall> userCallList;
    private List<UserCall> userCallListSecond;
    private String waiterName = "";
    private String serverAddress;

    private List<String> dateAndRoom;
    private ArrayAdapter<String> adapterUserMsg;
    private AlertDialog alertDialogUserCalls;
    private ListView userCallListView;

    KitchenOrderDetailFragment detailFragment;
    KitchenOrderHeader newKitchenOrderHeader;
    FragmentManager fragmentManager;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kitchenorder_list);

        databaseHandler = new DatabaseHandler(getApplicationContext());

        User loggedUser = databaseHandler.getLoggedUser();
        Kitchen kitchen = databaseHandler.getKitchenByUser(loggedUser.getUserId());
        kitchenId = kitchen.getKitchenId();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        serverAddress = sharedPreferences.getString("server_ip_address", "");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (!kitchen.getKitchenName().isEmpty()) {
            getSupportActionBar().setTitle(kitchen.getKitchenName());
        } else {
            toolbar.setTitle(getTitle());
        }

        orderHeaderList = new ArrayList<>();
        secondOrderHeaderList = new ArrayList<>();

        userCallList = new ArrayList<>();
        userCallListSecond = new ArrayList<>();

        recyclerView = (RecyclerView) findViewById(R.id.kitchenorder_list);

        detailFragment = (KitchenOrderDetailFragment)
                getSupportFragmentManager().findFragmentById(R.id.kitchenorder_detail_container);


        kitchenOrderAdapter = new KitchenOrderAdapter(orderHeaderList, new KitchenOrderAdapter.MyAdapterListener() {

            @Override
            public void rowViewOnClick(View v, int position) {
                KitchenOrderHeader orderHeader = orderHeaderList.get(position);

                // For item selection marking
                if (!orderHeader.isSelected()) {
                    orderHeaderList.get(position).setSelected(true);
                }

                for (int i = 0; i < orderHeaderList.size(); i++) {
                    if (!orderHeaderList.get(i).equals(orderHeader)) {
                        orderHeaderList.get(i).setSelected(false);
                    }
                }
                kitchenOrderAdapter.notifyDataSetChanged();

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy kk:mm" , Locale.GERMAN);
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putString(KitchenOrderDetailFragment.ARG_ITEM_ID, String.valueOf(orderHeader.getOrderHeaderId()));
                    arguments.putString("order_status" , orderHeader.getStatus());
                    arguments.putString("order_date", simpleDateFormat.format(orderHeader.getTimeSent()));
                    arguments.putString("order_location", orderHeader.getCompanyName() + " - " + orderHeader.getRoomName());
                    arguments.putString("order_user", orderHeader.getUserCustomerName());
                    arguments.putString("waiter_name", orderHeader.getUserWaiterName());    //proveri ovo!!!
                    arguments.putString("order_notice", orderHeader.getComment());
                    arguments.putInt("kitchen_id", kitchenId);
                    KitchenOrderDetailFragment fragment = new KitchenOrderDetailFragment();
                    fragment.setArguments(arguments);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.kitchenorder_detail_container, fragment, String.valueOf(orderHeader.getOrderHeaderId()))
                            .commit();

                    fragmentManager = getSupportFragmentManager();

                } else {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, KitchenOrderDetailActivity.class);
                    intent.putExtra(KitchenOrderDetailFragment.ARG_ITEM_ID, String.valueOf(orderHeader.getOrderHeaderId()));
                    intent.putExtra("order_status" , orderHeader.getStatus());
                    intent.putExtra("order_date", simpleDateFormat.format(orderHeader.getTimeSent()));
                    intent.putExtra("order_location", orderHeader.getCompanyName() + " - " + orderHeader.getRoomName());
                    intent.putExtra("order_user", orderHeader.getUserCustomerName());
                    intent.putExtra("waiter_name", orderHeader.getUserWaiterName());    //proveri ovo!!!
                    intent.putExtra("order_notice", orderHeader.getComment());

                    context.startActivity(intent);
                }
            }

        });

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new SeparatorDecoration(getResources().getColor(R.color.md_grey_100), 1));
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        fabMsg = (FloatingActionButton) findViewById(R.id.fabKitchenMsg);


        fabMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builderUserCalls = new AlertDialog.Builder(KitchenOrderListActivity.this);
                LayoutInflater li = LayoutInflater.from(KitchenOrderListActivity.this);

                View promptsView = li.inflate(R.layout.user_calls_dialog, (ViewGroup) null);

                final Button cancelBtn = (Button) promptsView.findViewById(R.id.cancelButtonUserCallsDialog);
                final Button updateAllBtn = (Button) promptsView.findViewById(R.id.updateAllButtonUserCallsDialog);
                final Button updateCheckedBtn = (Button) promptsView.findViewById(R.id.updateCheckedButtonUserCallsDialog);
                userCallListView = (ListView) promptsView.findViewById(R.id.userCallsList);

                userCallListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

                dateAndRoom = new ArrayList<>();

                if (userCallList.size() > 0) {

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss" , Locale.GERMAN);
                    for (UserCall userCall: userCallList) {
                        String dateRoom = simpleDateFormat.format(userCall.getTimeSent()) + " - "
                                + userCall.getCallRoomName() + " - " + userCall.getCallMessage() + " - " + userCall.getCallStatus();
                        dateAndRoom.add(dateRoom);
                    }
                    adapterUserMsg = new ArrayAdapter<>(KitchenOrderListActivity.this,
                            android.R.layout.simple_list_item_checked, dateAndRoom);
                    userCallListView.setAdapter(adapterUserMsg);
                }

                builderUserCalls.setView(promptsView)
                        .setTitle(R.string.user_calls_dialog_title)
                        .setCancelable(false);

                alertDialogUserCalls = builderUserCalls.create();

                // added because of width
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(alertDialogUserCalls.getWindow().getAttributes());
                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialogUserCalls.dismiss();
                    }
                });
                updateAllBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (userCallList.size() > 0) {
                            long[] checkedIds = new long[userCallList.size()];
                            for (int i = 0; i < userCallList.size(); i++) {
                                checkedIds[i] = userCallList.get(i).getUserCallId();
                            }
                            // call service
                            new UpdateUserCalls().execute(checkedIds);
                        }

                        int itemCount = userCallListView.getCount();
                        for(int i=itemCount-1; i >= 0; i--){
                            adapterUserMsg.remove(dateAndRoom.get(i));
                        }
                        adapterUserMsg.notifyDataSetChanged();
                    }
                });
                updateCheckedBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        SparseBooleanArray checkedItemPositions = userCallListView.getCheckedItemPositions();
                        int itemCount = userCallListView.getCount();

                        long[] checkedIds = new long[checkedItemPositions.size()];

                        int counter = 0;
                        for(int i=itemCount-1; i >= 0; i--){            // int i = 0; i < itemCount; i++
                            if(checkedItemPositions.get(i)){
                                adapterUserMsg.remove(dateAndRoom.get(i));
                                checkedIds[counter] = userCallList.get(i).getUserCallId();
                                userCallList.remove(i);
                                counter = counter + 1;
                            }
                        }
                        checkedItemPositions.clear();
                        adapterUserMsg.notifyDataSetChanged();
                        if (checkedIds.length > 0) {

                            new UpdateUserCalls().execute(checkedIds);
                        }
                    }
                });

                alertDialogUserCalls.show();

                alertDialogUserCalls.getWindow().setAttributes(layoutParams);

            }
        });


        new GetNewOrders().execute();

        long firstTimeDelay = 2000; // 2 sec
        long repeatInterval = 5000; // 5 sec

        repeatTask = new Timer();
        // this task for specified time it will run Repeat
        repeatTask.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Here do something
                // This task will run every 5 sec repeat
                new UpdateOrdersList().execute();

            }
        }, firstTimeDelay, repeatInterval);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 550) {
            int newOrderHeaderId = data.getIntExtra("new_order_id", 1);
            OrderHeader newOrderHeader = databaseHandler.getOrderHeaderById(newOrderHeaderId);
            Company companyHelper = databaseHandler.getCompanyById(newOrderHeader.getCompanyId());
            Room roomHelper = databaseHandler.getRoomById(newOrderHeader.getRoomId());

            newKitchenOrderHeader = new KitchenOrderHeader();
            newKitchenOrderHeader.setOrderHeaderId(newOrderHeader.getOrderHeaderId());
            newKitchenOrderHeader.setTimeSent(newOrderHeader.getTimeSent());
            newKitchenOrderHeader.setStatus(newOrderHeader.getStatus());
            newKitchenOrderHeader.setCompanyId(newOrderHeader.getCompanyId());
            newKitchenOrderHeader.setCompanyName(companyHelper.getCompanyName());
            newKitchenOrderHeader.setRoomId(newOrderHeader.getRoomId());
            newKitchenOrderHeader.setRoomName(roomHelper.getRoomName());
            newKitchenOrderHeader.setUserCustomerId(newOrderHeader.getUserCustomerId());

            String userCustomerId = String.valueOf(newOrderHeader.getUserCustomerId());
            new GetUserCustomerName().execute(userCustomerId);

        }
    }


    // Now we can define the action to take in the activity when the fragment event fires
    @Override
    public void onStatusChanged(String status, String fragmentTag) {

        statusButton = (StatusButton) findViewById(R.id.statusToggleButton);
        statusText = (TextView) findViewById(R.id.statusToggleText);

        final String finalFragmentTag = fragmentTag;
        for (int i = 0; i <  orderHeaderList.size(); i++) {
            KitchenOrderHeader kitchenOrderHeader = orderHeaderList.get(i);
            if (String.valueOf(kitchenOrderHeader.getOrderHeaderId()).equals(fragmentTag)) {
                if (status.equals("TAKEN")) {
                    statusButton.setState(StatusButton.StatusEnum.NEW);

                    final int changedPosition = i;

                    AlertDialog.Builder builderTake = new AlertDialog.Builder(KitchenOrderListActivity.this);
                    LayoutInflater li = LayoutInflater.from(KitchenOrderListActivity.this);

                    View promptsView = li.inflate(R.layout.take_order_dialog, (ViewGroup)null);

                    final RadioGroup rgWaiters = (RadioGroup) promptsView.findViewById(R.id.radioGroupWaiters);
                    final Button cancelBtn = (Button) promptsView.findViewById(R.id.cancelButtonDialog);
                    final Button sendBtn = (Button) promptsView.findViewById(R.id.sendButtonDialog);

                    builderTake.setView(promptsView)
                            .setTitle(R.string.take_order_dialog_title)
                            .setIcon(R.drawable.upload_icon)
                            .setCancelable(false);

                    final AlertDialog alertDialogTake = builderTake.create();

                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialogTake.dismiss();
                        }
                    });
                    sendBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if (rgWaiters.getCheckedRadioButtonId() == -1) {
                                Toast.makeText(KitchenOrderListActivity.this,
                                        getResources().getString(R.string.take_order_waiter_error), Toast.LENGTH_SHORT).show();
                            } else {
                                alertDialogTake.dismiss();

                                statusButton.setState(StatusButton.StatusEnum.TAKEN);
                                statusText.setText(getResources().getString(R.string.status_taken));
                                statusText.setTextColor(getResources().getColor(R.color.md_blue_500));
                                int waiterId = 0;
                                int checkedBtnId = rgWaiters.getCheckedRadioButtonId();
                                waiterId = checkedBtnId - 10000;
                                RadioButton radioButton = (RadioButton) rgWaiters.findViewById(checkedBtnId);
                                waiterName = (String) radioButton.getText();

                                TextView waiterTextView = (TextView) findViewById(R.id.textDetailUserWaiter);  // treba ga i sacuvati negde
                                waiterTextView.setText(getResources().getString(R.string.kitchenorder_waiter) + " " + waiterName);

                                KitchenOrderHeader kitchenOrderHeader = orderHeaderList.get(changedPosition);
                                orderHeaderList.get(changedPosition).setUserWaiterId(waiterId);
                                orderHeaderList.get(changedPosition).setUserWaiterName(waiterName);
                                kitchenOrderHeader.setUserWaiterId(waiterId);
                                new UpdateOrderStatus(kitchenOrderHeader, changedPosition, "P", "").execute();
                            }
                        }
                    });

                    List<Waiter> waiterList = databaseHandler.getAllWaiters();

                    for (int x = 1; x <= waiterList.size() ; x++) {
                        RadioButton radioButton = new RadioButton(this);
                        radioButton.setId(waiterList.get(x - 1).getUserId() + 10000);
                        radioButton.setText(waiterList.get(x - 1).getUserRealName());
                        radioButton.setTextSize(35);
                        radioButton.setPadding(0, 10, 0, 10);
                        rgWaiters.addView(radioButton);
                    }

                    alertDialogTake.show();


                } else if (status.equals("DELIVERED")) {

                    statusButton.setState(StatusButton.StatusEnum.TAKEN);

                    final int deletePosition = i;

                    AlertDialog.Builder builderDeliver = new AlertDialog.Builder(KitchenOrderListActivity.this);
                    LayoutInflater li = LayoutInflater.from(KitchenOrderListActivity.this);

                    View promptsView = li.inflate(R.layout.confirm_dialog, (ViewGroup) null);
                    final Button noBtn = (Button) promptsView.findViewById(R.id.noButtonDialog);
                    final Button yesBtn = (Button) promptsView.findViewById(R.id.yesButtonDialog);
                    final TextView confirmText = (TextView) promptsView.findViewById(R.id.confirmDialogText);
                    confirmText.setText(getResources().getString(R.string.delivered_dialog_text));

                    builderDeliver.setView(promptsView)
                            .setTitle(R.string.delivered_dialog_title)
                            .setCancelable(false);

                    final AlertDialog alertDialogDeliver = builderDeliver.create();

                    noBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialogDeliver.dismiss();
                        }
                    });
                    yesBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialogDeliver.dismiss();

                            KitchenOrderHeader kitchenOrderHeader = orderHeaderList.get(deletePosition);
                            new UpdateOrderStatus(kitchenOrderHeader, deletePosition, "I", finalFragmentTag).execute();
                        }
                    });

                    alertDialogDeliver.show();

                } else if (status.equals("CLOSED")) {

                    statusButton.setState(StatusButton.StatusEnum.CLEAR);

                    final int deletePosition = i;

                    AlertDialog.Builder builderClear = new AlertDialog.Builder(KitchenOrderListActivity.this);
                    LayoutInflater li = LayoutInflater.from(KitchenOrderListActivity.this);

                    View promptsView = li.inflate(R.layout.confirm_dialog, (ViewGroup) null);
                    final Button noBtn = (Button) promptsView.findViewById(R.id.noButtonDialog);
                    final Button yesBtn = (Button) promptsView.findViewById(R.id.yesButtonDialog);
                    final TextView confirmText = (TextView) promptsView.findViewById(R.id.confirmDialogText);
                    confirmText.setText(getResources().getString(R.string.clear_dialog_text));

                    builderClear.setView(promptsView)
                            .setTitle(R.string.clear_dialog_title)
                            .setCancelable(false);

                    final AlertDialog alertDialogClear = builderClear.create();

                    noBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialogClear.dismiss();
                        }
                    });
                    yesBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialogClear.dismiss();

                            KitchenOrderHeader kitchenOrderHeader = orderHeaderList.get(deletePosition);
                            new UpdateOrderStatus(kitchenOrderHeader, deletePosition, "Z", finalFragmentTag).execute(); //Z - zatvorena
                        }
                    });

                    alertDialogClear.show();

                } else {

                    orderHeaderList.get(i).setStatus("O");
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.status_canceled), Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.kitchen_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_place_order:
                Intent intent = new Intent(KitchenOrderListActivity.this, BeverageListActivity.class);
                intent.putExtra("mode", false);
                startActivityForResult(intent, 550);
                return true;
            case R.id.action_change_mode:
                Intent intentModeChange = new Intent(KitchenOrderListActivity.this, BeverageListActivity.class);
                intentModeChange.putExtra("mode", true);
                startActivity(intentModeChange);
                return true;
            case R.id.action_consumption:
                Intent intentConsumption = new Intent(KitchenOrderListActivity.this, ConsumptionActivity.class);
                startActivity(intentConsumption);
                return true;
            case R.id.action_about:
                Intent intentAbout = new Intent(KitchenOrderListActivity.this, AboutActivity.class);
                startActivity(intentAbout);
                return true;
            case R.id.action_logout:
                deleteCredentials();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteCredentials() {

        AlertDialog.Builder builder = new AlertDialog.Builder(KitchenOrderListActivity.this);
        LayoutInflater li = LayoutInflater.from(KitchenOrderListActivity.this);

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


     @Override
     protected void onDestroy(){
         super.onDestroy();
         if(repeatTask != null){
             repeatTask.cancel();
         }
     }


    private class GetNewOrders extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(KitchenOrderListActivity.this);
            progressDialog.setMessage(getResources().getString(R.string.loading_msg));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            // "http://mkitsql.mk-group.org/ECafe/api/OrderHeader/GetAllOrderHeader/" + kitchenId;
            String URL_KITCHEN_ORDERS = serverAddress + getResources().getString(R.string.get_all_order_header) + kitchenId;
            // "http://mkitsql.mk-group.org/ECafe/api/User/ViewUser/";
            String URL_USER = serverAddress + getResources().getString(R.string.view_user);
            String jsonKitchenOrders = webServiceCall(URL_KITCHEN_ORDERS);
            databaseHandler.deleteAllOrderItems();

            if (!jsonKitchenOrders.isEmpty()) {
                try {
                    JSONObject jsonObjHeader = new JSONObject(jsonKitchenOrders);
                    if (!jsonObjHeader.isNull("OrderHeader")) {
                        JSONArray headers = jsonObjHeader.getJSONArray("OrderHeader");

                        for (int i = 0; i < headers.length(); i++) {
                            JSONObject headObj = (JSONObject) headers.get(i);
                            Date timeSent = null;
                            Date timeDelivered = null;

                            timeSent = new DateTime(headObj.getString("TimeSent")).toDate();
                            timeDelivered = new DateTime(headObj.getString("TimeDelivered")).toDate();

                            Company company = databaseHandler.getCompanyById(headObj.getInt("CompanyId"));
                            Room room = databaseHandler.getRoomById(headObj.getInt("RoomId"));
                            KitchenOrderHeader orderHeader = new KitchenOrderHeader(headObj.getInt("OrderHeaderId"),
                                    timeSent, timeDelivered, headObj.getString("Status"), headObj.getString("Comment"),
                                    headObj.getInt("CompanyId"), company.getCompanyName(), headObj.getInt("RoomId"),
                                    room.getRoomName(), headObj.getInt("UserWaiterId"), "",
                                    headObj.getInt("UserCustomerId"), "", false);
                            orderHeaderList.add(orderHeader);

                            if (!headObj.isNull("OrderItems")) {
                                JSONArray items = headObj.getJSONArray("OrderItems");
                                for (int x = 0; x < items.length(); x++) {
                                    JSONObject itemObj = (JSONObject) items.get(x);
                                    OrderItem item = new OrderItem(itemObj.getInt("OrderHeaderId"), itemObj.getInt("OrderItemId"),
                                            itemObj.getInt("Quantity"), itemObj.getDouble("Price"), itemObj.getDouble("Amount"),
                                            itemObj.getInt("KitchenId"), itemObj.getInt("BeverageId"), "");
                                    item.setBeverageName(databaseHandler.getBeverageName(kitchenId, item.getBeverageId()));
                                    databaseHandler.addOrderItem(item);
                                }
                            }
                        }
                        databaseHandler.close();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("JSON Data", "Didn't receive any data from server!");
            }

            for (KitchenOrderHeader kitchenOrderHeader : orderHeaderList) {
                String jsonUser = webServiceCall(URL_USER + kitchenOrderHeader.getUserCustomerId());
                if (!jsonUser.isEmpty()) {
                    try {
                        JSONObject jsonObjUser = new JSONObject(jsonUser);
                        if (!jsonObjUser.isNull("User")) {
                            JSONArray users = jsonObjUser.getJSONArray("User");

                            for (int i = 0; i < users.length(); i++) {
                                JSONObject userObj = (JSONObject) users.get(i);
                                kitchenOrderHeader.setUserCustomerName(userObj.getString("Name"));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("JSON Data", "Didn't receive any data from server!");
                }
                if (kitchenOrderHeader.getUserWaiterId() != kitchenId) {
                    String jsonWaiter = webServiceCall(URL_USER + kitchenOrderHeader.getUserWaiterId());
                    if (!jsonWaiter.isEmpty()) {
                        try {
                            JSONObject jsonObjWaiter = new JSONObject(jsonWaiter);
                            if (!jsonObjWaiter.isNull("User")) {
                                JSONArray users = jsonObjWaiter.getJSONArray("User");

                                for (int i = 0; i < users.length(); i++) {
                                    JSONObject userObj = (JSONObject) users.get(i);
                                    kitchenOrderHeader.setUserWaiterName(userObj.getString("Name"));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e("JSON Data", "Didn't receive any data from server!");
                    }
                }
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            recyclerView.setAdapter(kitchenOrderAdapter);


            if (findViewById(R.id.kitchenorder_detail_container) != null) {
                // The detail container view will be present only in the
                // large-screen layouts (res/values-w900dp).
                // If this view is present, then the
                // activity should be in two-pane mode.
                mTwoPane = true;
            }

            if (progressDialog.isShowing())
                progressDialog.dismiss();
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
                if (urlConnection != null)
                    urlConnection.disconnect();
            }

            return result.toString();
        }


    }


    private class UpdateOrdersList extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            // "http://mkitsql.mk-group.org/ECafe/api/OrderHeader/GetAllOrderHeader/" + kitchenId;
            String URL_ORDERS_UPDATE = serverAddress + getResources().getString(R.string.get_all_order_header) + kitchenId;
            // "http://mkitsql.mk-group.org/ECafe/api/UserCall/GetNewUserCalls/" + kitchenId;
            String URL_GET_USER_CALLS = serverAddress + getResources().getString(R.string.get_new_user_calls) + kitchenId;
            // "http://mkitsql.mk-group.org/ECafe/api/User/ViewUser/";
            String URL_USER = serverAddress + getResources().getString(R.string.view_user);
            String jsonHeader = webServiceCall(URL_ORDERS_UPDATE);
            String jsonUserCalls = webServiceCall(URL_GET_USER_CALLS);

            if (!jsonHeader.isEmpty()) {
                try {
                    JSONObject jsonObjHeader = new JSONObject(jsonHeader);
                    if (!jsonObjHeader.isNull("OrderHeader")) {
                        JSONArray headers = jsonObjHeader.getJSONArray("OrderHeader");

                        secondOrderHeaderList.clear();
                        for (int i = 0; i < headers.length(); i++) {
                            JSONObject headObj = (JSONObject) headers.get(i);
                            Date timeOpened = null;
                            Date timeDelivered = null;

                            timeOpened = new DateTime(headObj.getString("TimeSent")).toDate();
                            timeDelivered = new DateTime(headObj.getString("TimeDelivered")).toDate();

                            Company company = databaseHandler.getCompanyById(headObj.getInt("CompanyId"));
                            Room room = databaseHandler.getRoomById(headObj.getInt("RoomId"));
                            KitchenOrderHeader orderHeader = new KitchenOrderHeader(headObj.getInt("OrderHeaderId"),
                                    timeOpened, timeDelivered, headObj.getString("Status"),  headObj.getString("Comment"),
                                    headObj.getInt("CompanyId"), company.getCompanyName(),  headObj.getInt("RoomId"),
                                    room.getRoomName(), headObj.getInt("UserWaiterId"), "",
                                    headObj.getInt("UserCustomerId"), "", false);
                            boolean isOld = false;
                            for(KitchenOrderHeader orderHeader2 : orderHeaderList) {
                                if (orderHeader2.getOrderHeaderId() == orderHeader.getOrderHeaderId()) {
                                    isOld = true;
                                    break;
                                }
                            }
                            if (!isOld) {
                                secondOrderHeaderList.add(orderHeader);

                                if (!headObj.isNull("OrderItems")) {
                                    JSONArray items = headObj.getJSONArray("OrderItems");
                                    for (int x = 0; x < items.length(); x++) {
                                        JSONObject itemObj = (JSONObject) items.get(x);
                                        OrderItem item = new OrderItem(itemObj.getInt("OrderHeaderId"), itemObj.getInt("OrderItemId"),
                                                itemObj.getInt("Quantity"), itemObj.getDouble("Price"), itemObj.getDouble("Amount"),
                                                itemObj.getInt("KitchenId"), itemObj.getInt("BeverageId"), "");
                                        item.setBeverageName(databaseHandler.getBeverageName(kitchenId, item.getBeverageId()));
                                        databaseHandler.addOrderItem(item);
                                    }
                                }
                            }
                        }
                        databaseHandler.close();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("JSON Update Orders Data", "Didn't receive any data from server!");
            }

            if (!secondOrderHeaderList.isEmpty()) {
                for (KitchenOrderHeader kitchenOrderHeader : secondOrderHeaderList) {
                    String jsonUser = webServiceCall(URL_USER + kitchenOrderHeader.getUserCustomerId());
                    if (!jsonUser.isEmpty()) {
                        try {
                            JSONObject jsonObjUser = new JSONObject(jsonUser);
                            if (!jsonObjUser.isNull("User")) {
                                JSONArray users = jsonObjUser.getJSONArray("User");

                                for (int i = 0; i < users.length(); i++) {
                                    JSONObject userObj = (JSONObject) users.get(i);
                                    kitchenOrderHeader.setUserCustomerName(userObj.getString("UserName"));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e("JSON Data", "Didn't receive any data from server!");
                    }
                }
            }

            if (!jsonUserCalls.isEmpty()) {
                userCallListSecond.clear();
                try {
                    JSONObject jsonObjUserCall = new JSONObject(jsonUserCalls);
                    if (!jsonObjUserCall.isNull("UserCall")) {
                        JSONArray userCalls = jsonObjUserCall.getJSONArray("UserCall");

                        for (int i = 0; i < userCalls.length(); i++) {
                            JSONObject userCallObj = (JSONObject) userCalls.get(i);

                            Date timeSent = null;
                            timeSent = new DateTime(userCallObj.getString("TimeSent")).toDate();
                            Room room = databaseHandler.getRoomById(userCallObj.getInt("RoomId"));

                            UserCall userCall = new UserCall(userCallObj.getInt("UserCallId"), timeSent,
                                    userCallObj.getString("Status"), userCallObj.getString("Message"), userCallObj.getInt("RoomId"),
                                    room.getRoomName());
                            userCallListSecond.add(userCall);
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("JSON User Calls Data", "Didn't receive any data from server!");
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            Ringtone notificationSound;

            if (secondOrderHeaderList.size() > 0) {
                for (KitchenOrderHeader orderHeader : secondOrderHeaderList) {
                    orderHeaderList.add(orderHeader);
                    kitchenOrderAdapter.notifyItemInserted(orderHeaderList.size() - 1);
                    try {
                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        notificationSound = RingtoneManager.getRingtone(getApplicationContext(), notification);
                        if (!notificationSound.isPlaying())
                            notificationSound.play();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (userCallListSecond.size() > 0) {
                boolean newUserCalls = false;
                if (userCallList.isEmpty()) {
                    userCallList.addAll(userCallListSecond);
                    Collections.reverse(userCallList);
                    newUserCalls = true;
                } else {
                    boolean isNew;
                    for (UserCall userCallSecond: userCallListSecond) {
                        isNew = false;
                        for (UserCall userCall: userCallList) {
                            if (userCall.getUserCallId() == userCallSecond.getUserCallId()) {
                                isNew = false;
                                break;
                            } else {
                                isNew = true;
                            }
                        }
                        if (isNew) {

                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss" , Locale.GERMAN);
                            userCallList.add(0, userCallSecond);
                            // add
                            adapterUserMsg.insert(simpleDateFormat.format(userCallSecond.getTimeSent()) + " - "
                                    + userCallSecond.getCallRoomName() + " - " + userCallSecond.getCallMessage(), 0);
                            newUserCalls = true;
                        }
                    }
                }
                if (newUserCalls) {
                    fabMsg.setImageResource(R.drawable.kitchen_fab);
                    try {
                        Uri alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        notificationSound = RingtoneManager.getRingtone(getApplicationContext(), alarm);
                        if (!notificationSound.isPlaying())
                            notificationSound.play();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (alertDialogUserCalls != null && alertDialogUserCalls.isShowing()) {
                        if (adapterUserMsg != null) {
                            adapterUserMsg.notifyDataSetChanged();
                        } else {
                            dateAndRoom = new ArrayList<>();

                            if (userCallList.size() > 0) {

                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss" , Locale.GERMAN);
                                for (UserCall userCall: userCallList) {
                                    String dateRoom = simpleDateFormat.format(userCall.getTimeSent()) + " - "
                                            + userCall.getCallRoomName() + " - " + userCall.getCallMessage() + " - " + userCall.getCallStatus();
                                    dateAndRoom.add(dateRoom);
                                }
                                adapterUserMsg = new ArrayAdapter<>(KitchenOrderListActivity.this,
                                        android.R.layout.simple_list_item_checked, dateAndRoom);
                                userCallListView.setAdapter(adapterUserMsg);
                            }
                        }
                        //Toast.makeText(getApplicationContext(), "notify", Toast.LENGTH_LONG).show();
                    }
                }
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
                if (urlConnection != null)
                    urlConnection.disconnect();
            }
            return result.toString();
        }
    }


    private class GetUserCustomerName extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {

            String userCustomerId = params[0];
            // "http://mkitsql.mk-group.org/ECafe/api/User/ViewUser/" + userCustomerId;
            String URL_USER = serverAddress + getResources().getString(R.string.view_user) + userCustomerId;
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(URL_USER);
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

            String jsonUser = result.toString();

            if (!jsonUser.isEmpty()) {
                try {
                    JSONObject jsonObjUser = new JSONObject(jsonUser);
                    if (!jsonObjUser.isNull("User")) {
                        JSONArray users = jsonObjUser.getJSONArray("User");

                        for (int i = 0; i < users.length(); i++) {
                            JSONObject userObj = (JSONObject) users.get(i);
                            newKitchenOrderHeader.setUserCustomerName(userObj.getString("UserName"));
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

            orderHeaderList.add(newKitchenOrderHeader);
            kitchenOrderAdapter.notifyItemInserted(orderHeaderList.size()-1);
            try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                r.play();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    // Update order status
    private class UpdateOrderStatus extends AsyncTask<Void, Void, Void> {

        // "http://mkitsql.mk-group.org/ECafe/api/OrderHeader/UpdateOrderHeader";
        private String URL_ORDER_STATUS = serverAddress + getResources().getString(R.string.update_order_header);
        private JSONObject jsonObject;
        private JSONArray jsonArray;
        String response = "";
        private int responseCode = 0;
        private KitchenOrderHeader kitchenOrderHeader;
        private int position;
        private String statusCharacter;
        private String fragmentTag = "";

        UpdateOrderStatus(KitchenOrderHeader kitchen_order_header, int pos, String status_char, String frag_tag) {
            this.kitchenOrderHeader = kitchen_order_header;
            this.position = pos;
            this.statusCharacter = status_char;
            this.fragmentTag = frag_tag;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(KitchenOrderListActivity.this);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            jsonArray = new JSONArray();

            try {

                jsonObject = new JSONObject();
                jsonObject.put("OrderHeaderId", kitchenOrderHeader.getOrderHeaderId());
                jsonObject.put("Status", statusCharacter);
                if (statusCharacter.equals("P"))
                    jsonObject.put("UserWaiterId", kitchenOrderHeader.getUserWaiterId());

                jsonArray.put(jsonObject);


                URL url = new URL(URL_ORDER_STATUS); //Enter URL here
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
            } catch (JSONException e) {
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

                    if (statusCharacter.equals("P")) {  // Preuzeta

                        orderHeaderList.get(position).setStatus("P");
                        kitchenOrderAdapter.notifyItemChanged(position);

                    } else if (statusCharacter.equals("I") || statusCharacter.equals("Z")) {  // Isporucena ili Zatvorena

                        orderHeaderList.remove(position);
                        kitchenOrderAdapter.notifyItemRemoved(position);

                        if (!fragmentTag.isEmpty()) {
                            Fragment fragment = fragmentManager.findFragmentByTag(fragmentTag);
                            fragmentManager.beginTransaction().remove(fragment).commit();
                            fragmentManager.executePendingTransactions();
                        }
                    }

                } else {
                    Toast.makeText(KitchenOrderListActivity.this, response, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(KitchenOrderListActivity.this,
                        getResources().getString(R.string.error_msg), Toast.LENGTH_LONG).show();
            }
        }

    }


    // Update order status
    private class UpdateUserCalls extends AsyncTask<long[], Void, Void> {

        // "http://mkitsql.mk-group.org/ECafe/api/OrderHeader/UpdateOrderHeader";
        private String URL_ORDER_STATUS = serverAddress + getResources().getString(R.string.update_user_calls);
        private JSONObject jsonObject;
        private JSONArray jsonArray;
        String response = "";
        private int responseCode = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(long[] ... params) {

            long[] userCallIds = params[0];
            jsonArray = new JSONArray();

            for (int i = 0; i < userCallIds.length; i++) {
           // for (long userCallId: userCallIds) {

                try {
                    jsonObject = new JSONObject();

                    jsonObject.put("UserCallId", (int) userCallIds[i]);
                    jsonObject.put("Status", "Z");

                    Log.d("user_call_id", String.valueOf((int) userCallIds[i]));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                jsonArray.put(jsonObject);
            }

            try {

                URL url = new URL(URL_ORDER_STATUS); //Enter URL here
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

            // da li ovde nesto treba????
            Toast.makeText(KitchenOrderListActivity.this, response, Toast.LENGTH_LONG).show();
        }

    }


}
