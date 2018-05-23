package mkitbs.ekitchen.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

import mkitbs.ekitchen.app.entities.Company;
import mkitbs.ekitchen.app.entities.KitchenRoom;
import mkitbs.ekitchen.app.entities.Location;
import mkitbs.ekitchen.app.entities.OrderItem;
import mkitbs.ekitchen.app.entities.Room;
import mkitbs.ekitchen.app.entities.User;
import mkitbs.ekitchen.app.helpers.DatabaseHandler;
import mkitbs.ekitchen.app.recyclerview.OrderAdapter;
import mkitbs.ekitchen.app.recyclerview.SeparatorDecoration;


public class MainActivity extends AppCompatActivity {

    private List<OrderItem> orderItemList = new ArrayList<>();
    private RecyclerView recyclerView;
    private OrderAdapter orderAdapter;
    private DatabaseHandler databaseHandler;
    private static final int RESULT_OK = 5;
    private int fragmentNumber = -1;
    private int orderHeaderId = 0;
    private int roomIdSpinner = 0;
    private int userCompanyId = 0;
    private int kitchenId = 0;
    private String comment = "";
    private boolean isLocationSet = false;
    private boolean isFABClick = false;
    private boolean isFABLongPress = false;
    private boolean fromGetAll = false;
    private int userLocationId;
    private int userRoomId;
    private Timer repeatTask;
    private Context context;
    private String serverAddress;
    private String userKind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setIcon(R.drawable.ecafe_bar_icon);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        serverAddress = sharedPreferences.getString("server_ip_address", "");
        final boolean isFabClickAll = sharedPreferences.getBoolean("fab_is_all", true);
        userLocationId = sharedPreferences.getInt("default_location", 0);
        userRoomId = sharedPreferences.getInt("default_room", 0);

        userKind = sharedPreferences.getString("user_kind", "");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (orderItemList.isEmpty() && !isLocationSet) {

                    isFABClick = true;
                    locationDialog(isFabClickAll);

                } else {

                    if (isFabClickAll) {
                        fromGetAll = true;
                        Intent intentSecond = new Intent(MainActivity.this, SelectionActivity.class);
                        if (fragmentNumber > -1) {
                            intentSecond.putExtra("fragment_number", fragmentNumber);
                        }
                        intentSecond.putExtra("kitchen_id", kitchenId);
                        intentSecond.putExtra("all_at_once", true);
                        startActivity(intentSecond);
                    } else {
                        Intent intent = new Intent(MainActivity.this, SelectionActivity.class);
                        if (fragmentNumber > -1) {
                            intent.putExtra("fragment_number", fragmentNumber);
                        }
                        intent.putExtra("kitchen_id", kitchenId);
                        startActivityForResult(intent, RESULT_OK);
                    }
                }
            }
        });
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                if (orderItemList.isEmpty() && !isLocationSet) {

                    isFABLongPress = true;
                    locationDialog(isFabClickAll);

                } else {
                    if (isFabClickAll) {
                        Intent intent = new Intent(MainActivity.this, SelectionActivity.class);
                        if (fragmentNumber > -1) {
                            intent.putExtra("fragment_number", fragmentNumber);
                        }
                        intent.putExtra("kitchen_id", kitchenId);
                        startActivityForResult(intent, RESULT_OK);
                    } else {
                        fromGetAll = true;
                        Intent intentSecond = new Intent(MainActivity.this, SelectionActivity.class);
                        if (fragmentNumber > -1) {
                            intentSecond.putExtra("fragment_number", fragmentNumber);
                        }
                        intentSecond.putExtra("kitchen_id", kitchenId);
                        intentSecond.putExtra("all_at_once", true);
                        startActivity(intentSecond);
                    }
                }
                return false;
            }
        });

        databaseHandler = new DatabaseHandler(getApplicationContext());

        if (userRoomId == 0) {
            User user = databaseHandler.getLoggedUser();
            userRoomId = user.getUserRoomId();
            if (userLocationId == 0) {
                Room room = databaseHandler.getRoomById(userRoomId);
                userLocationId = room.getRoomLocationId();
            }
        }

        LinearLayout locationTopBox = (LinearLayout) findViewById(R.id.topBox);
        locationTopBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationDialog(false);
            }
        });

      //  User user = databaseHandler.getLoggedUser();
      //  Kitchen kitchen = databaseHandler.getKitchenByUser(user.getUserId());

        Button sendButton = (Button) findViewById(R.id.sendButton);
        Button cancelButton = (Button) findViewById(R.id.cancelButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!orderItemList.isEmpty()) {

                    AlertDialog.Builder builderSend = new AlertDialog.Builder(MainActivity.this);
                    LayoutInflater layoutInflaterSend = LayoutInflater.from(MainActivity.this);

                    View promptsViewSend = layoutInflaterSend.inflate(R.layout.comment_dialog, (ViewGroup)null);

                    final Spinner spinnerCompany = (Spinner) promptsViewSend
                            .findViewById(R.id.spinnerCompanyWaiter);
                    final TextView textViewCompany = (TextView) promptsViewSend
                            .findViewById(R.id.textViewCompany);
                    final EditText commentEditText = (EditText) promptsViewSend
                            .findViewById(R.id.commentEditText);
                    final Button btnCancel = (Button) promptsViewSend
                            .findViewById(R.id.cancelButtonCommentDialog);
                    final Button btnSend = (Button) promptsViewSend
                            .findViewById(R.id.sendButtonCommentDialog);

                    builderSend.setView(promptsViewSend)
                            .setTitle(R.string.send_dialog_title)
                            .setCancelable(false);

                    final AlertDialog alertDialog = builderSend.create();

                    btnSend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();

                            if (userKind.equals("WAITER")) {
                                String companyName = spinnerCompany.getSelectedItem().toString();
                                List<Company> companyList = databaseHandler.getAllCompanies();
                                //int companyIdSpinner = 0;
                                for (Company company : companyList) {
                                    if (company.getCompanyName().equals(companyName)) {
                                        userCompanyId = company.getCompanyId();
                                    }
                                }
                            }

                            comment = commentEditText.getText().toString();
                            comment = comment.replace("č", "c").replace("Č", "C").replace("ć", "c")
                                    .replace("Ć", "C").replace("š", "s").replace("Š", "S")
                                    .replace("ž", "z").replace("Ž", "Z").replace("đ", "dj").replace("Đ", "Dj");

                            new PostNewOrderHeader().execute();
                        }
                    });
                    btnCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.dismiss();
                        }
                    });


                    if (userKind.equals("WAITER")) {
                        // hide edittext
                        textViewCompany.setVisibility(View.GONE);

                        List<Company> companyList = databaseHandler.getAllCompanies();
                        ArrayList<String> companyNames = new ArrayList<>();
                        if (companyList != null && companyList.size() > 0) {
                            for (Company company : companyList) {
                                companyNames.add(company.getCompanyName());
                            }
                        }
                        ArrayAdapter<String> companyAdapter = new ArrayAdapter<>(MainActivity.this,
                                R.layout.spinner_item_arrow, companyNames);
                        companyAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                        spinnerCompany.setAdapter(companyAdapter);

                    } else {
                        // hide spinner
                        spinnerCompany.setVisibility(View.GONE);
                        // check for user company
                        User user = databaseHandler.getLoggedUser();
                        userCompanyId = user.getUserCompanyId();

                        if (userCompanyId > 0) {
                            Company company = databaseHandler.getCompanyById(userCompanyId);
                            textViewCompany.setText(company.getCompanyName());
                        }
                    }

                    alertDialog.show();

                } else {
                    Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.no_beverage_entered), Toast.LENGTH_LONG).show();
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builderDelete = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater layoutInflaterDelete = LayoutInflater.from(MainActivity.this);

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

                if (!orderItemList.isEmpty()) {
                    msgText.setText(getResources().getString(R.string.delete_all_dialog_msg));
                } else {
                    msgText.setText(getResources().getString(R.string.exit_app_dialog_msg));
                }

                btnYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                        if (!orderItemList.isEmpty()) {
                            orderItemList.clear();
                            orderAdapter.notifyDataSetChanged();
                        } else {
                            finish();
                        }
                    }
                });
                btnNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });

                alertDialog.show();
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

                AlertDialog.Builder builderSwipeDelete = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater layoutInflaterDelete = LayoutInflater.from(MainActivity.this);

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
                        // User cancelled the dialog
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

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        orderAdapter = new OrderAdapter(orderItemList, new OrderAdapter.MyAdapterListener() {
            @Override
            public void btnRemoveOnClick(View v, int position) {
                final OrderItem item = orderItemList.get(position);
                final int itemPosition = position;
                if (item.getItemQuantity() > 1) {
                    item.setItemQuantity(item.getItemQuantity() - 1);
                    orderAdapter.notifyItemChanged(position);
                } else {

                    AlertDialog.Builder builderButtonDelete = new AlertDialog.Builder(MainActivity.this);
                    LayoutInflater layoutInflaterDelete = LayoutInflater.from(MainActivity.this);

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
                            // User cancelled the dialog
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

        // for swipe
        itemTouchHelper.attachToRecyclerView(recyclerView);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new SeparatorDecoration(getResources().getColor(R.color.md_grey_100), 1));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(orderAdapter);


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            int beverageId = data.getIntExtra("beverage_id", 1);
            boolean isDuplicate = false;
           // int kitchenId = 1;          // ZASTO JE OVO 1????
            if (!orderItemList.isEmpty()) {
                for (int i = 0; i < orderItemList.size(); i++) {
                    if (beverageId == orderItemList.get(i).getBeverageId()) {
                        orderItemList.get(i).setItemQuantity(orderItemList.get(i).getItemQuantity() + 1);
                        isDuplicate = true;
                        break;
                    }
                }
                if (!isDuplicate) {
                    String beverageName = data.getStringExtra("beverage_name");
                    int orderId = orderItemList.size() + 1;
                    OrderItem item = new OrderItem(0, orderId, 1, 0.0, 0.0, kitchenId, beverageId, beverageName);
                    orderItemList.add(item);
                }
            } else {
                String beverageName = data.getStringExtra("beverage_name");
                int orderItemId = orderItemList.size() + 1;
                OrderItem item = new OrderItem(0, orderItemId, 1, 0.0, 0.0, kitchenId, beverageId, beverageName);
                orderItemList.add(item);
            }
            fragmentNumber = data.getIntExtra("bev_cat_id", 0);
            orderAdapter.notifyDataSetChanged();
        }
    }

    public void locationDialog(boolean isFabClickAll) {

        final boolean stupidFab = isFabClickAll;

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater li = LayoutInflater.from(MainActivity.this);

        View promptsView = li.inflate(R.layout.location_dialog, (ViewGroup)null);

        final Spinner locationSpinner= (Spinner) promptsView
                .findViewById(R.id.spinnerLocationOnly);
        final Spinner roomSpinner= (Spinner) promptsView
                .findViewById(R.id.spinnerRoomOnly);
        final Button okButton = (Button) promptsView
                .findViewById(R.id.okButtonLocationDialog);

        builder.setView(promptsView)
                .setTitle(R.string.location_dialog_title)
                .setCancelable(true);
                                                    // ovde je bilo zakomentarisanog koda ...
        // create alert dialog
        final AlertDialog alertDialog = builder.create();

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String roomName = roomSpinner.getSelectedItem().toString();
                List<Room> roomList = databaseHandler.getAllRooms();
                for (Room room : roomList) {
                    if (room.getRoomName().equals(roomName)) {
                        roomIdSpinner = room.getRoomId();
                        break;
                    }
                }
                int currentKitchenId = 0;
                List<KitchenRoom> kitchenRoomList = databaseHandler.getAllKitchenRoom(roomIdSpinner);
                if (!kitchenRoomList.isEmpty())
                    currentKitchenId = kitchenRoomList.get(0).getKitchenId();

                if (currentKitchenId > 0 && kitchenId > 0 && (currentKitchenId != kitchenId)) {
                    if (!orderItemList.isEmpty()) {
                        orderItemList.clear();
                        orderAdapter.notifyDataSetChanged();
                        Toast.makeText(MainActivity.this,
                                getResources().getString(R.string.list_cleaned), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this,
                                getResources().getString(R.string.location_changed), Toast.LENGTH_LONG).show();
                    }
                }
                kitchenId = currentKitchenId;

                TextView locationTitle = (TextView) findViewById(R.id.textLocationTitle);
                locationTitle.setText(locationSpinner.getSelectedItem().toString() + " - " + roomName);

                isLocationSet = true;

                if (isFABClick) {
                    if (stupidFab) {
                        fromGetAll = true;
                        Intent intentSecond = new Intent(MainActivity.this, SelectionActivity.class);
                        if (fragmentNumber > -1) {
                            intentSecond.putExtra("fragment_number", fragmentNumber);
                        }
                        intentSecond.putExtra("kitchen_id", kitchenId);
                        intentSecond.putExtra("all_at_once", true);
                        startActivity(intentSecond);
                    } else {
                        Intent intent = new Intent(MainActivity.this, SelectionActivity.class);
                        if (fragmentNumber > -1) {
                            intent.putExtra("fragment_number", fragmentNumber);
                        }
                        intent.putExtra("kitchen_id", kitchenId);
                        startActivityForResult(intent, RESULT_OK);
                    }
                }

                if (isFABLongPress) {
                    if (stupidFab) {
                        Intent intent = new Intent(MainActivity.this, SelectionActivity.class);
                        if (fragmentNumber > -1) {
                            intent.putExtra("fragment_number", fragmentNumber);
                        }
                        intent.putExtra("kitchen_id", kitchenId);
                        startActivityForResult(intent, RESULT_OK);
                    } else {
                        fromGetAll = true;
                        Intent intentSecond = new Intent(MainActivity.this, SelectionActivity.class);
                        if (fragmentNumber > -1) {
                            intentSecond.putExtra("fragment_number", fragmentNumber);
                        }
                        intentSecond.putExtra("kitchen_id", kitchenId);
                        intentSecond.putExtra("all_at_once", true);
                        startActivity(intentSecond);
                    }
                }
                alertDialog.dismiss();
            }
        });


        int userDefaultLocationListPosition = -1;

        final List<Location> locationList = databaseHandler.getAllLocations();
        final ArrayList<String> locationNames = new ArrayList<>();
        if (locationList.size() > 0) {
            for (int i = 0; i < locationList.size(); i++) {
                locationNames.add(locationList.get(i).getLocationName());
                if (userLocationId == locationList.get(i).getLocationId()) {
                    userDefaultLocationListPosition = i;
                }
            }
        }
        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(MainActivity.this,
                R.layout.spinner_item_arrow, locationNames);
        locationAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        locationSpinner.setAdapter(locationAdapter);
        if (userDefaultLocationListPosition > -1)
            locationSpinner.setSelection(userDefaultLocationListPosition);


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
                        if (userRoomId == room.getRoomId()) {
                            userDefaultRoomListPosition = i;
                        }
                    }
                }
                ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(MainActivity.this,
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

        alertDialog.show();

    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        if (fromGetAll) {
            boolean isDuplicate = false;
            List<OrderItem> orderItemListBundle = databaseHandler.getAllOrderItems();  // item bundle
            int originalListSize = orderItemList.size();
            if (!orderItemListBundle.isEmpty()) {
                if (!orderItemList.isEmpty()) {
                    for (OrderItem orderItemNew : orderItemListBundle) {
                        for (OrderItem orderItemOriginal : orderItemList) {
                            if (orderItemNew.getBeverageId() == orderItemOriginal.getBeverageId()) {
                                orderItemOriginal.setItemQuantity(orderItemOriginal.getItemQuantity() + orderItemNew.getItemQuantity());
                                isDuplicate = true;
                                break;
                            } else {
                                isDuplicate = false;
                            }
                        }
                        if (!isDuplicate) {
                            orderItemNew.setOrderItemId(originalListSize + 1);
                            originalListSize = originalListSize + 1;
                            orderItemList.add(orderItemNew);
                            orderAdapter.notifyItemInserted(originalListSize);
                        } else {
                            orderAdapter.notifyDataSetChanged();
                        }
                    }
                } else {
                    for (OrderItem orderItemNew: orderItemListBundle) {
                        //Log.d("IF_PETLJA", "add item");
                        orderItemNew.setOrderItemId(originalListSize + 1);
                        originalListSize = originalListSize + 1;
                        orderItemList.add(orderItemNew);
                        orderAdapter.notifyItemInserted(originalListSize);
                    }
                }
                databaseHandler.deleteAllOrderItems();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private class PostNewOrderHeader extends AsyncTask<Void,Void,Void> {

        //"http://mkitsql.mk-group.org/ECafe/api/OrderHeader/InsertOrderHeader";
        String URL_ORDER_HEADER = serverAddress + getResources().getString(R.string.insert_order_header);
        JSONObject jsonObject;
        String response = "";
        int responseCode = 0;

        @Override
        protected Void doInBackground(Void... params) {

            try {
                Calendar currentTime = Calendar.getInstance();
                Date timeDate = currentTime.getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                User user = databaseHandler.getLoggedUser();

                jsonObject = new JSONObject();
                jsonObject.put("TimeSent", sdf.format(timeDate));
                jsonObject.put("TimeDelivered", sdf.format(timeDate));
                jsonObject.put("Status", "N"); // novi
                jsonObject.put("Comment", comment);
                jsonObject.put("CompanyId", userCompanyId);
                jsonObject.put("RoomId", roomIdSpinner);
                jsonObject.put("UserWaiterId", kitchenId); // kitchen id initially
                jsonObject.put("UserCustomerId", user.getUserId());


                URL url = new URL(URL_ORDER_HEADER);
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
                    Toast.makeText(MainActivity.this, "Greška. Ključ porudžbine je nula.", Toast.LENGTH_LONG).show();
                }
            }

        }

    }


    private class PostNewOrderItems extends AsyncTask<List<OrderItem>,Void,Void> {

        // "http://mkitsql.mk-group.org/ECafe/api/OrderItem/InsertOrderItem";
        String URL_ORDER_ITEM = serverAddress + getResources().getString(R.string.insert_order_item);
        JSONObject jsonObject;
        JSONArray jsonArray;
        String response = "";
        int counter;

        private long repeatInterval = 5000; // 5 sec

        @Override
        protected Void doInBackground(List<OrderItem>... params) {

            List<OrderItem> orderItemList = params[0];
            jsonArray = new JSONArray();

            for (int i = 0; i < orderItemList.size(); i++) {

                counter = i + 1;

                try {
                    jsonObject = new JSONObject();

                    jsonObject.put("OrderHeaderId", orderHeaderId);
                    jsonObject.put("OrderItemId", counter);
                    jsonObject.put("Quantity", orderItemList.get(i).getItemQuantity());
                    jsonObject.put("KitchenId", kitchenId);
                    jsonObject.put("BeverageId", orderItemList.get(i).getBeverageId());

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                jsonArray.put(jsonObject);
            }

            webServiceCall(jsonArray.toString());


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (response.equals("ERROR")) {
                Toast.makeText(MainActivity.this,
                        getResources().getString(R.string.just_error_msg), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this,
                        getResources().getString(R.string.order_sent_successfully), Toast.LENGTH_LONG).show();
                orderItemList.clear();
                orderAdapter.notifyDataSetChanged();

                repeatTask = new Timer();
                // this task for specified time it will run Repeat
                repeatTask.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        // Here do something
                        // This task will run every 5 sec repeat
                        new GetOrderStatus().execute();
                    }
                }, repeatInterval, repeatInterval);
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


    private class GetOrderStatus extends AsyncTask<Void, Void, Void> {

        String orderStatus = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {

            // "http://mkitsql.mk-group.org/ECafe/api/OrderHeader/CustomerStatusNotification/" + orderHeaderId;
            String URL_USER = serverAddress + getResources().getString(R.string.customer_status_notification) + orderHeaderId;
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

            orderStatus = result.toString();


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!orderStatus.isEmpty()) {
                if (!orderStatus.equals("N")) {
                    // order taken
                    repeatTask.cancel();
                    Toast.makeText(MainActivity.this,
                            getResources().getString(R.string.order_status_changed), Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(context, MainActivity.class);
                    PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                    builder.setAutoCancel(true)
                            .setWhen(System.currentTimeMillis())
                            .setSmallIcon(R.drawable.ecafe_icon3)
                            .setContentTitle(getResources().getString(R.string.app_name))
                            .setContentText(getResources().getString(R.string.order_status_changed))
                            .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                            .setContentIntent(contentIntent);


                    NotificationManager notificationManager = (NotificationManager)
                            context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(1, builder.build());

                }
            }

        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_location:
                isFABClick = false;
                isFABLongPress = false;
                locationDialog(false); // ovo valjda nece praviti probleme jer nece ni stici tamo
                return true;
            case R.id.action_settings:
                Intent intentSettings = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intentSettings);
                return true;
            case R.id.action_about:
                Intent intentAbout = new Intent(MainActivity.this, AboutActivity.class);
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

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater li = LayoutInflater.from(MainActivity.this);

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
                sharedPreferences.edit().remove("server_ip_address").apply();

                databaseHandler.deleteAllUsers();
                databaseHandler.deleteAllUserRoles();
                databaseHandler.deleteAllConfigurations();

                finish();
            }
        });

        alertDialog.show();
    }

}
