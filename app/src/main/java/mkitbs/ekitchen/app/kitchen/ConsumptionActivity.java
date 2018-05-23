package mkitbs.ekitchen.app.kitchen;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFHeader;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import mkitbs.ekitchen.app.R;
import mkitbs.ekitchen.app.entities.Company;
import mkitbs.ekitchen.app.entities.ConsumChild;
import mkitbs.ekitchen.app.entities.Kitchen;
import mkitbs.ekitchen.app.entities.OrderHeader;
import mkitbs.ekitchen.app.entities.OrderItem;
import mkitbs.ekitchen.app.entities.Room;
import mkitbs.ekitchen.app.helpers.DatabaseHandler;
import mkitbs.ekitchen.app.helpers.ExpandConsumListAdapter;
import mkitbs.ekitchen.app.helpers.ExpandableListAdapter;

public class ConsumptionActivity extends AppCompatActivity {

    private Spinner spinnerCompanyFilter;
    private DatabaseHandler databaseHandler;
    private List<OrderHeader> orderHeaderList;
    private List<OrderItem> orderItemList;
    private String serverAddress;
    private EditText dateFrom, dateTo;
    private Calendar myCalendar;
    private ProgressDialog progressDialog;
    private String companyId, dateFromString, dateToString;
    private int userKitchenId;
    private int sortIndex = 0;
    private CharSequence charSequenceToday;
    private ExpandableListView consumptionExpBevList;
    private ExpandableListView consumptionExpOrderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consumption);

        databaseHandler = new DatabaseHandler(getApplicationContext());

        int userId = databaseHandler.getLoggedUser().getUserId();
        Kitchen userKitchen = databaseHandler.getKitchenByUser(userId);
        userKitchenId = userKitchen.getKitchenId();

        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        spinnerCompanyFilter = (Spinner) findViewById(R.id.spinnerCompanyFilter);
        dateFrom = (EditText) findViewById(R.id.editDateFrom);
        dateTo = (EditText) findViewById(R.id.editDateTo);

        List<Company> companyList = databaseHandler.getAllCompanies();
        ArrayList<String> companyNames = new ArrayList<>();
        companyNames.add(getResources().getString(R.string.consum_spinner_all_companies));
        if (companyList != null && companyList.size() > 0) {
            for (Company company : companyList) {
                companyNames.add(company.getCompanyName());
            }
        }
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(ConsumptionActivity.this,
                R.layout.spinner_item_arrow, companyNames);
        filterAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerCompanyFilter.setAdapter(filterAdapter);

        Spinner spinnerSortFilter = (Spinner) findViewById(R.id.spinnerSortFilter);

        String sorts[] = {getResources().getString(R.string.hall_consum_sort_order),
                            getResources().getString(R.string.hall_consum_sort_bev)};

        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(ConsumptionActivity.this,
                R.layout.spinner_item_arrow, sorts);
        sortAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerSortFilter.setAdapter(sortAdapter);

        spinnerSortFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortIndex = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                sortIndex = 1;
            }
        });

        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        //calendar.add(Calendar.DATE, 1);
        date = calendar.getTime();
        charSequenceToday = DateFormat.format("dd.MM.yyyy", date);
        dateTo.setText(charSequenceToday);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        serverAddress = sharedPreferences.getString("server_ip_address", "");

        myCalendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener dateFromPicker = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateFrom();
            }
        };
        final DatePickerDialog.OnDateSetListener dateToPicker = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateTo();
            }
        };

        dateFrom.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerDialog(ConsumptionActivity.this, dateFromPicker, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        dateTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(ConsumptionActivity.this, dateToPicker, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        orderHeaderList = new ArrayList<>();
        orderItemList = new ArrayList<>();

        consumptionExpBevList = (ExpandableListView) findViewById(R.id.listExpBevsHall);
        consumptionExpBevList.setClickable(true);

        consumptionExpOrderList = (ExpandableListView) findViewById(R.id.listExpOrdersHall);
        consumptionExpOrderList.setClickable(true);

        Button exportXLSandMailBtn = (Button) findViewById(R.id.exportXLSandMailButton);

        exportXLSandMailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dateFrom.getText().toString().isEmpty() || dateTo.getText().toString().isEmpty()) {
                    Toast.makeText(ConsumptionActivity.this,
                            getResources().getString(R.string.consum_edit_date_err_msg), Toast.LENGTH_LONG).show();
                } else {
                    boolean dateRangeCheck = checkDateRange(dateFrom.getText().toString(), dateTo.getText().toString());
                    if (dateRangeCheck) {
                        new GetConsumptionItems().execute();
                    } else {
                        Toast.makeText(ConsumptionActivity.this,
                                getResources().getString(R.string.consum_date_range_err_msg), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void updateDateFrom() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
        dateFrom.setText(sdf.format(myCalendar.getTime()));
    }
    private void updateDateTo() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
        dateTo.setText(sdf.format(myCalendar.getTime()));
    }

    private boolean checkDateRange(String dateFrom, String dateTo) {

        long elapsedDays = 0;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
        try {
            Date startDate = simpleDateFormat.parse(dateFrom);
            Date endDate = simpleDateFormat.parse(dateTo);

            long difference = endDate.getTime() - startDate.getTime();

            long secondsInMilli = 1000;
            long minutesInMilli = secondsInMilli * 60;
            long hoursInMilli = minutesInMilli * 60;
            long daysInMilli = hoursInMilli * 24;

            elapsedDays = difference / daysInMilli;

        } catch (Exception exception) {
            Log.d("DATE", "Conversion failed");
        }

        if (elapsedDays < 0) {
            Toast.makeText(ConsumptionActivity.this,
                    getResources().getString(R.string.consum_date_negative_err_msg), Toast.LENGTH_LONG).show();
        }

        return (elapsedDays <= 90 && elapsedDays >= 0);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_consumption, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_show:
                exportToXLSandEmail();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void exportToXLSandEmail() {

        if (orderItemList.size() > 0) {
            //New Workbook
            Workbook workBook = new HSSFWorkbook();

            Cell cell = null;

            //Cell style for header row
            CellStyle cellStyle = workBook.createCellStyle();
            cellStyle.setFillForegroundColor(HSSFColor.AQUA.index);
            cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

            //New Sheet
            Sheet sheet1 = null;
            sheet1 = workBook.createSheet(getResources().getString(R.string.consum_sheet_name));

            // Generate column headings
            Row rowHeader = sheet1.createRow(0);

            cell = rowHeader.createCell(0);
            cell.setCellValue(getResources().getString(R.string.consum_sheet_column1));
            cell.setCellStyle(cellStyle);

            cell = rowHeader.createCell(1);
            cell.setCellValue(getResources().getString(R.string.consum_sheet_column2));
            cell.setCellStyle(cellStyle);

            cell = rowHeader.createCell(2);
            cell.setCellValue(getResources().getString(R.string.consum_sheet_column3));
            cell.setCellStyle(cellStyle);

            cell = rowHeader.createCell(3);
            cell.setCellValue(getResources().getString(R.string.consum_sheet_column4));
            cell.setCellStyle(cellStyle);

            cell = rowHeader.createCell(4);
            cell.setCellValue(getResources().getString(R.string.consum_sheet_column5));
            cell.setCellStyle(cellStyle);

            cell = rowHeader.createCell(5);
            cell.setCellValue(getResources().getString(R.string.consum_sheet_column6));
            cell.setCellStyle(cellStyle);

            cell = rowHeader.createCell(6);
            cell.setCellValue(getResources().getString(R.string.consum_sheet_column7));
            cell.setCellStyle(cellStyle);

            sheet1.setColumnWidth(0, 3000);
            sheet1.setColumnWidth(1, 1500);
            sheet1.setColumnWidth(2, 7000);
            sheet1.setColumnWidth(3, 2000);
            sheet1.setColumnWidth(4, 6500);
            sheet1.setColumnWidth(5, 4000);
            sheet1.setColumnWidth(6, 5000);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy kk:mm:ss", Locale.GERMAN);
            String orderDatetime = "";
            for (int i = 0; i < orderItemList.size(); i++) {
                OrderItem orderItem = orderItemList.get(i);
                orderItem.setBeverageName(databaseHandler.getBeverageName(userKitchenId, orderItem.getBeverageId()));
                String roomName = "";
                String companyName = "";
                for (OrderHeader orderHeader: orderHeaderList) {
                    if (orderItem.getOrderHeaderId() == orderHeader.getOrderHeaderId()) {
                        Room room = databaseHandler.getRoomById(orderHeader.getRoomId());
                        roomName = room.getRoomName();
                        Company company = databaseHandler.getCompanyById(orderHeader.getCompanyId());
                        companyName = company.getCompanyName();
                        orderDatetime = simpleDateFormat.format(orderHeader.getTimeSent());
                        break;
                    }
                }

                Row row = sheet1.createRow(i + 1);
                row.createCell(0).setCellValue(orderItemList.get(i).getOrderHeaderId());
                row.createCell(1).setCellValue(orderItemList.get(i).getOrderItemId());
                row.createCell(2).setCellValue(orderItem.getBeverageName());
                row.createCell(3).setCellValue(orderItemList.get(i).getItemQuantity());
                row.createCell(4).setCellValue(roomName);
                row.createCell(5).setCellValue(orderDatetime);
                row.createCell(6).setCellValue(companyName);
            }

            String fileName = spinnerCompanyFilter.getSelectedItem().toString() + " - " + charSequenceToday + ".xls";

            File file;
            FileOutputStream outputStream;
            try {
                file = new File(getExternalCacheDir(), fileName);

                outputStream = new FileOutputStream(file);
                workBook.write(outputStream);
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            File fileLocation = new File(getExternalCacheDir(), fileName);
            Uri path = Uri.fromFile(fileLocation);

            Intent gmailIntent = new Intent(Intent.ACTION_SEND);
            gmailIntent.setType("message/rfc822");
            // mail recepient
            gmailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "mkitbs@gmail.com" });
            // mail attachment
            gmailIntent.putExtra(Intent.EXTRA_STREAM, path);
            // mail subject
            gmailIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name)
                    + ": " + dateFrom.getText().toString() + " - " + dateTo.getText().toString());
            // mail body
            gmailIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.consum_email_msg));
            startActivity(gmailIntent);
        } else {
            Toast.makeText(ConsumptionActivity.this,
                    getResources().getString(R.string.consum_empty_list_msg), Toast.LENGTH_LONG).show();
        }
    }


    private class GetConsumptionItems extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(ConsumptionActivity.this);
            progressDialog.setMessage(getResources().getString(R.string.loading_msg));
            progressDialog.setCancelable(false);
            progressDialog.show();

            orderHeaderList.clear();
            orderItemList.clear();

            String companyName = spinnerCompanyFilter.getSelectedItem().toString();
            if (spinnerCompanyFilter.getSelectedItemPosition() != 0) {
                List<Company> companyList = databaseHandler.getAllCompanies();
                for (Company company : companyList) {
                    if (company.getCompanyName().equals(companyName)) {
                        companyId = String.valueOf(company.getCompanyId());
                    }
                }
            } else {
                companyId = "0";
            }

            dateFromString = dateFrom.getText().toString().substring(6,10)
                                + "-" + dateFrom.getText().toString().substring(3,5)
                                + "-" + dateFrom.getText().toString().substring(0,2);
            dateToString = dateTo.getText().toString().substring(6,10)
                                + "-" + dateTo.getText().toString().substring(3,5)
                                + "-" + dateTo.getText().toString().substring(0,2);
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            String URL_ORDERS_HISTORY = serverAddress +
                    getResources().getString(R.string.consumption) + companyId + "/" + dateFromString + "/" + dateToString;

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

            populateConsumptionList();

            if (orderItemList.isEmpty()) {
                Toast.makeText(ConsumptionActivity.this,
                        getResources().getString(R.string.consum_no_orders_err_msg), Toast.LENGTH_LONG).show();
            }

            if (progressDialog.isShowing())
                progressDialog.dismiss();

        }
    }

    private void populateConsumptionList() {

       // ExpandableListView consumptionExpBevList = (ExpandableListView) findViewById(R.id.listExpBevsHall);
       // consumptionExpBevList.setClickable(true);

       // ExpandableListView consumptionExpOrderList = (ExpandableListView) findViewById(R.id.listExpOrdersHall);
       // consumptionExpOrderList.setClickable(true);

        // Two different lists
        if (sortIndex == 1) {

            if(consumptionExpBevList.getVisibility() == View.VISIBLE) {
                consumptionExpBevList.setVisibility(View.GONE);
            }
            if(consumptionExpOrderList.getVisibility() == View.GONE) {
                consumptionExpOrderList.setVisibility(View.VISIBLE);
            }

            List<OrderHeader> listDataHeader = new ArrayList<>(orderHeaderList);
            HashMap<String, List<OrderItem>> listDataChild = new HashMap<>();

            Collections.reverse(listDataHeader);

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

                ExpandableListAdapter listOrdersAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild, "CONSUM");
                consumptionExpOrderList.setAdapter(listOrdersAdapter);
                listOrdersAdapter.notifyDataSetChanged();
            }

        } else if (sortIndex == 2) {

            if(consumptionExpOrderList.getVisibility() == View.VISIBLE) {
                consumptionExpOrderList.setVisibility(View.GONE);
            }
            if(consumptionExpBevList.getVisibility() == View.GONE) {
                consumptionExpBevList.setVisibility(View.VISIBLE);
            }

            Map<Integer, Integer> map = new HashMap<>();
            for(OrderItem orderItem : orderItemList){
                if(map.containsKey(orderItem.getBeverageId())){
                    int quantity = map.get(orderItem.getBeverageId()) + orderItem.getItemQuantity();
                    map.put(orderItem.getBeverageId(), quantity);
                } else {
                    map.put(orderItem.getBeverageId(), orderItem.getItemQuantity());
                }
            }

            List<OrderItem> listSumBevHeader = new ArrayList<>();
            HashMap<Integer, List<ConsumChild>> listConsumChild = new HashMap<>();

            for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setBeverageId(entry.getKey());
                orderItem.setItemQuantity(entry.getValue());
                orderItem.setBeverageName(databaseHandler.getBeverageName(userKitchenId, entry.getKey()));
                listSumBevHeader.add(orderItem);
            }

            for (int i = 0; i < listSumBevHeader.size(); i++) {
                List<ConsumChild> children = new ArrayList<>();
                for (int x = 0; x < orderItemList.size(); x++) {
                    OrderItem orderItemChild = orderItemList.get(x);
                    if (listSumBevHeader.get(i).getBeverageId() == orderItemChild.getBeverageId()) {
                        for (int h = 0; h < orderHeaderList.size(); h++) {
                            OrderHeader orderHeader = orderHeaderList.get(h);
                            if (orderItemChild.getOrderHeaderId() == orderHeader.getOrderHeaderId()) {
                                ConsumChild consumChild = new ConsumChild(orderItemChild.getOrderHeaderId(),
                                        orderHeader.getTimeSent(), orderItemChild.getItemQuantity(), orderHeader.getRoomId(), orderHeader.getCompanyId());
                                children.add(consumChild);
                            }
                        }
                    }
                }
                listConsumChild.put(listSumBevHeader.get(i).getBeverageId(), children);
            }

            ExpandConsumListAdapter listBevSumAdapter = new ExpandConsumListAdapter(this, listSumBevHeader, listConsumChild, userKitchenId);
            consumptionExpBevList.setAdapter(listBevSumAdapter);
            consumptionExpBevList.deferNotifyDataSetChanged();
        }
    }


}
