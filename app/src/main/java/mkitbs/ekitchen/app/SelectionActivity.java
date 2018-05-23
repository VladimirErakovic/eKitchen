package mkitbs.ekitchen.app;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

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
import java.util.List;

import mkitbs.ekitchen.app.entities.Beverage;
import mkitbs.ekitchen.app.entities.Category;
import mkitbs.ekitchen.app.entities.Kitchen;
import mkitbs.ekitchen.app.entities.OrderItem;
import mkitbs.ekitchen.app.helpers.DatabaseHandler;
import mkitbs.ekitchen.app.helpers.GridItemListener;
import mkitbs.ekitchen.app.helpers.ImageAdapter;
import mkitbs.ekitchen.app.helpers.SelectMultipleAdapter;

public class SelectionActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private List<Category> categoriesList;
    private static List<Category> categoryCopyList;
    private static List<Beverage> beveragesList;
    private static List<OrderItem> orderItemList;  // dodao za all at once
    private int categoriesCounter = 0;
    private ProgressDialog progressDialog;
    private DatabaseHandler databaseHandler;

    public static final int NUM_OF_COLUMNS = 2;

    public static final int GRID_PADDING = 5;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private int fragmentNumber = -1;
    private int kitchenId;
    private static boolean allAtOnce = false;
    private static int orderItemNumber = 0;
    private static int kitchenIdStatic;
    private String serverAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);
        
        databaseHandler = new DatabaseHandler(getApplicationContext());
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        serverAddress = sharedPreferences.getString("server_ip_address", "");

        categoriesList = new ArrayList<>();
        beveragesList = new ArrayList<>();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            fragmentNumber = extras.getInt("fragment_number", 0);
            kitchenId = extras.getInt("kitchen_id", 0);
            kitchenIdStatic = kitchenId;
            allAtOnce = extras.getBoolean("all_at_once", false);
        }

        FloatingActionButton fabBack = (FloatingActionButton) findViewById(R.id.fabBack);
        if (!allAtOnce) {
            fabBack.setVisibility(View.GONE);
        } else {
            orderItemList = new ArrayList<>();
        }

        fabBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // back to list
                upFabActionForAll();
            }
        });

        if (kitchenId > 0 && !databaseHandler.isBeveragesFromKitchen(kitchenId)) {
            categoryCopyList = new ArrayList<>();

            if (!isConnectingToInternet(getApplicationContext())) {

                AlertDialog.Builder builderConnErr = new AlertDialog.Builder(SelectionActivity.this);
                LayoutInflater li = LayoutInflater.from(SelectionActivity.this);

                View promptsView = li.inflate(R.layout.connection_error_dialog, (ViewGroup)null);

                final TextView msgErrorText = (TextView) promptsView
                        .findViewById(R.id.errorText);
                final Button okButton = (Button) promptsView
                        .findViewById(R.id.okConnectionErrorDialog);

                builderConnErr.setView(promptsView)
                        .setTitle(R.string.server_unavailable_title)
                        .setCancelable(false);

                // create alert dialog
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

                return;
            }
            databaseHandler.deleteAllBeverages();
            databaseHandler.deleteAllCategories();

            new GetCategoriesAndBeverages().execute();   // ovo treba malo razmisliti i testirati

        } else {
            categoriesList = databaseHandler.getAllCategories();
            categoryCopyList = new ArrayList<>(categoriesList);
            categoriesCounter = categoriesList.size();
            beveragesList = databaseHandler.getAllBeverages();

            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

            mViewPager = (ViewPager) findViewById(R.id.container);
            mViewPager.setOffscreenPageLimit(categoriesCounter - 1);
            mViewPager.setAdapter(mSectionsPagerAdapter);

            if(fragmentNumber > -1){
                mViewPager.setCurrentItem(fragmentNumber - 1);
            }

            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(mViewPager);
        }

        Kitchen kitchen = databaseHandler.getKitchenById(kitchenId);
        String kitchenLocation = databaseHandler.getLocationById(kitchen.getKitchenLocationId()).getLocationName();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(kitchenLocation);
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

    private void upFabActionForAll() {
        if (allAtOnce) {
            if (!orderItemList.isEmpty()) {   // && orderItemList != null
                for (OrderItem orderItem : orderItemList) {
                    databaseHandler.addOrderItem(orderItem);
                }
            }
            finish();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_selection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_history:
                Intent intentHistory = new Intent(SelectionActivity.this, HistoryActivity.class);
                intentHistory.putExtra("kitchen_id", kitchenId);
                startActivity(intentHistory);
                return true;
            case R.id.action_settings:
                Intent intentSettings = new Intent(SelectionActivity.this, SettingsActivity.class);
                startActivity(intentSettings);
                return true;
            case R.id.action_about:
                Intent intentAbout = new Intent(SelectionActivity.this, AboutActivity.class);
                startActivity(intentAbout);
                return true;
            case android.R.id.home:
                upFabActionForAll();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        SelectMultipleAdapter selectMultipleAdapter;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            final View rootView = inflater.inflate(R.layout.fragment_selection, container, false);

            final List<Beverage> filteredList = new ArrayList<>();
            if (filteredList.isEmpty()) {
                for (int i = 0; i < beveragesList.size(); i++) {
                    if (Integer.valueOf(beveragesList.get(i).getBevCategoryId()).equals(getArguments().getInt(ARG_SECTION_NUMBER))) {
                        filteredList.add(beveragesList.get(i));
                    }
                }
            }

            final GridView gridView = (GridView) rootView.findViewById(R.id.grid_view);

            int columnWidth = InitilizeGridLayout(getActivity(), gridView);


            if (allAtOnce) {

                selectMultipleAdapter  = new SelectMultipleAdapter(getActivity(), filteredList, columnWidth, new GridItemListener() {
                    @Override
                    public void btnRemoveOnClick(View v, int position) {

                        Beverage beverage = filteredList.get(position);
                        if (beverage.getHelpCounter() > 0) {
                            beverage.setHelpCounter(beverage.getHelpCounter() - 1);
                        } else {
                            beverage.setHelpCounter(0);
                        }

                        boolean deleteIt = false;
                        int itemPosition = -1;
                        for (OrderItem orderItem: orderItemList) {
                            itemPosition = itemPosition + 1;
                            if (orderItem.getBeverageId() == beverage.getBeverageId()) {
                                if (orderItem.getItemQuantity() > 1) {
                                    orderItem.setItemQuantity(orderItem.getItemQuantity() - 1);
                                    deleteIt = false;
                                } else {
                                    deleteIt = true;
                                }
                                break;
                            }
                        }
                        if (deleteIt && !orderItemList.isEmpty())
                            orderItemList.remove(itemPosition);

                        selectMultipleAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void btnAddOnClick(View v, int position) {

                        Beverage beverage = filteredList.get(position);
                        beverage.setHelpCounter(beverage.getHelpCounter() + 1);

                        boolean addIt = false;
                        if (orderItemList.isEmpty()) {
                            orderItemNumber = orderItemNumber + 1;
                            OrderItem orderItemNew = new OrderItem(0, orderItemNumber, beverage.getHelpCounter(), 0.0, 0.0,
                                    kitchenIdStatic, beverage.getBeverageId(), beverage.getBeverageName());
                            orderItemList.add(orderItemNew);
                        } else {

                            for (OrderItem orderItem : orderItemList) {
                                if (orderItem.getBeverageId() == beverage.getBeverageId()) {
                                    orderItem.setItemQuantity(orderItem.getItemQuantity() + 1);
                                    addIt = false;
                                    break;
                                } else {
                                    addIt = true;
                                }
                            }
                            if (addIt) {                            // trebalo bi izmestiti ovu kreiranje objekata ...
                                orderItemNumber = orderItemNumber + 1;
                                OrderItem orderItemNew = new OrderItem(0, orderItemNumber, beverage.getHelpCounter(), 0.0, 0.0,
                                        kitchenIdStatic, beverage.getBeverageId(), beverage.getBeverageName());
                                orderItemList.add(orderItemNew);
                            }

                        }
                        selectMultipleAdapter.notifyDataSetChanged();
                    }
                });
                gridView.setAdapter(selectMultipleAdapter);

            } else {
                gridView.setAdapter(new ImageAdapter(getActivity(), filteredList, columnWidth, false));
                gridView.setSelector(R.drawable.grid_view_selector);

                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        Beverage beverage = filteredList.get(position);
                        Intent intent = new Intent();
                        intent.putExtra("beverage_id", beverage.getBeverageId());
                        intent.putExtra("bev_cat_id", beverage.getBevCategoryId());
                        intent.putExtra("beverage_name", beverage.getBeverageName());
                        intent.putExtra("all_or_single", 1);
                        getActivity().setResult(5, intent);
                        getActivity().onBackPressed();
                    }
                });
            }

            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(categoriesList.get(position).getCategoryId());       // position + 1
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return categoriesCounter;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (!categoriesList.isEmpty()) {
                return categoriesList.get(position).getCategoryName();
            }
            return null;
        }
    }


    /**
     * Async task to get all item categories
     * */
    private class GetCategoriesAndBeverages extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(SelectionActivity.this);
            progressDialog.setMessage(getResources().getString(R.string.loading_msg));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            // "http://mkitsql.mk-group.org/ECafe/api/BeverageCategory/GetKitchenBeverageAvailable/" + kitchenId;
            String URL_CATEGORY = serverAddress + getResources().getString(R.string.get_kitchen_beverage_available) + kitchenId;
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(URL_CATEGORY);
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
                urlConnection.disconnect();
            }

            String jsonCat = result.toString();

            if (!jsonCat.isEmpty()) {
                try {
                    JSONObject jsonObjCat = new JSONObject(jsonCat);
                    if (!jsonObjCat.isNull("BeverageCategory")) {
                        JSONArray categories = jsonObjCat.getJSONArray("BeverageCategory");

                        for (int i = 0; i < categories.length(); i++) {
                            JSONObject catObj = (JSONObject) categories.get(i);
                            Category cat = new Category(catObj.getInt("BeverageCategoryId"),
                                    catObj.getString("Name"));
                            categoriesList.add(cat);
                            categoryCopyList.add(cat);
                            categoriesCounter = categoriesCounter + 1;
                            databaseHandler.addCategory(cat);

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
                                beveragesList.add(bev);
                                databaseHandler.addBeverage(bev);
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
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Create the adapter that will return a fragment for each of the three
            // primary sections of the activity.
            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

            // Set up the ViewPager with the sections adapter.
            mViewPager = (ViewPager) findViewById(R.id.container);
            mViewPager.setOffscreenPageLimit(categoriesCounter - 1);
            mViewPager.setAdapter(mSectionsPagerAdapter);

            if(fragmentNumber > -1){
                mViewPager.setCurrentItem(fragmentNumber - 1);
            }

            TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(mViewPager);

            if (progressDialog.isShowing())
                progressDialog.dismiss();
        }

    }


    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    public static int getScreenWidth(Context context) {
        int columnWidth;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        final Point point = new Point();
        try {
            display.getSize(point);
        } catch (java.lang.NoSuchMethodError ignore) { // Older device
            point.x = display.getWidth();
            point.y = display.getHeight();
        }
        columnWidth = point.x;
        return columnWidth;
    }

    private static int InitilizeGridLayout(Context context, GridView gridView) {
        int columnWidth = 0;
        Resources r = context.getResources();
        float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                GRID_PADDING, r.getDisplayMetrics());

        columnWidth = (int) ((getScreenWidth(context) - ((NUM_OF_COLUMNS + 1) * padding)) / NUM_OF_COLUMNS);

        gridView.setNumColumns(NUM_OF_COLUMNS);
        gridView.setColumnWidth(columnWidth);
        gridView.setStretchMode(GridView.NO_STRETCH);
        gridView.setPadding((int) padding, (int) padding, (int) padding,
                (int) padding);
        gridView.setHorizontalSpacing((int) padding);
        gridView.setVerticalSpacing((int) padding);
        return columnWidth;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

}
