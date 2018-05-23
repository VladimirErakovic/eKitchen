package mkitbs.ekitchen.app.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import mkitbs.ekitchen.app.entities.Beverage;
import mkitbs.ekitchen.app.entities.Category;
import mkitbs.ekitchen.app.entities.Company;
import mkitbs.ekitchen.app.entities.CompanyLocation;
import mkitbs.ekitchen.app.entities.Configuration;
import mkitbs.ekitchen.app.entities.Kitchen;
import mkitbs.ekitchen.app.entities.KitchenRoom;
import mkitbs.ekitchen.app.entities.Location;
import mkitbs.ekitchen.app.entities.OrderHeader;
import mkitbs.ekitchen.app.entities.OrderItem;
import mkitbs.ekitchen.app.entities.Room;
import mkitbs.ekitchen.app.entities.User;
import mkitbs.ekitchen.app.entities.UserRole;
import mkitbs.ekitchen.app.entities.Waiter;

/**
 * Created by verakovic on 02.12.2016.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 13;

    // Database Name
    private static final String DATABASE_NAME = "ebar";

    // Company table name
    private static final String TABLE_COMPANY = "company";

    // Company table columns names
    private static final String KEY_COMPID = "companyId";
    private static final String KEY_COMPNAME = "companyName";
    private static final String KEY_COMPDESC = "companyDescription";
    private static final String KEY_COMPLOGO = "companyLogo";

    // Location table name
    private static final String TABLE_LOCATION = "location";

    // Location table columns names
    private static final String KEY_LOCID = "locationId";
    private static final String KEY_LOCNAME = "locationName";

    // Company - Location table name
    private static final String TABLE_COMPLOC = "companyLocation";

    // Company - Location table columns names
    private static final String KEY_CLCOMPID = "locationId";
    private static final String KEY_CLLOCID = "companyId";

    // Room table name
    private static final String TABLE_ROOM = "room";

    // Room table columns names
    private static final String KEY_ROOMID = "roomId";
    private static final String KEY_ROOMNAME = "roomName";
    private static final String KEY_ROOMDESC = "roomDescription";
    private static final String KEY_ROOMLOCID = "roomLocationId";
    private static final String KEY_ROOMTYPEID = "roomTypeId";

    // Kitchen - Room table name
    private static final String TABLE_KITCHENROOM = "kitchenRoom";

    // Kitchen - Room table columns names
    private static final String KEY_KRID1 = "kitchenId";
    private static final String KEY_KRID2 = "roomId";

    // Category table name
    private static final String TABLE_CATEGORY = "category";

    // Category table columns names
    private static final String KEY_CATID = "categoryId";
    private static final String KEY_CATNAME = "categoryName";

    // Beverage table name
    private static final String TABLE_BEVERAGE = "beverage";

    // Beverage table columns names
    private static final String KEY_BEVKTCID = "bevKitchenId";
    private static final String KEY_BEVID = "beverageId";
    private static final String KEY_BEVNAME = "beverageName";
    private static final String KEY_BEVDESC = "bevDescription";
    private static final String KEY_BEVAVAIL = "isAvailable";
    private static final String KEY_BEVIMAGE = "bevImage";
    private static final String KEY_BEVCATID = "bevCategoryId";
    private static final String KEY_BEVHCOUNT = "helpCounter";

    // User table name
    private static final String TABLE_USER = "user";

    // Room table columns names
    private static final String KEY_USERID = "userId";
    private static final String KEY_USERNAME = "userName";
    private static final String KEY_USERPASS = "userPassword";
    private static final String KEY_USERREALNAME = "userRealName";
    private static final String KEY_USERPIC = "userPicture";
    private static final String KEY_USERCOMPID = "userCompanyId";
    private static final String KEY_USERROOMID = "userRoomId";

    // User-role table name
    private static final String TABLE_USERROLE = "userRole";

    // User-role table columns names
    private static final String KEY_RUSERID = "userId";
    private static final String KEY_RROLEID = "roleId";

    // Order item table name
    private static final String TABLE_ORDERITEM = "orderItem";

    // Order item table columns names
    private static final String KEY_OIHEADERID = "orderHeaderId";
    private static final String KEY_OIITEMID = "orderItemId";
    private static final String KEY_OIQANTITY = "itemQuantity";
    private static final String KEY_OIBEVNAME = "beverageName";
    private static final String KEY_OIBEVID = "beverageId";

    // Order header table name
    private static final String TABLE_ORDERHEADER = "orderHeader";

    // Order header table columns names
    private static final String KEY_OHID = "orderHeaderId";
    private static final String KEY_OHSENT = "timeSent";
    private static final String KEY_OHDELIVERED = "timeDelivered";
    private static final String KEY_OHSTATUS = "status";
    private static final String KEY_OHCOMMENT = "comment";
    private static final String KEY_OHCOMPID = "companyId";
    private static final String KEY_OHROOMID = "roomId";
    private static final String KEY_OHWAITERID = "userWaiterId";
    private static final String KEY_OHCUSTOMERID = "userCustomerId";

    // Kitchen table name
    private static final String TABLE_KITCHEN = "kitchen";

    // Kitchen table columns names
    private static final String KEY_KITCHENID = "kitchenId";
    private static final String KEY_KITCHENNAME = "kitchenName";
    private static final String KEY_KITCHENLOCID = "kitchenLocationId";

    // Waiter table name
    private static final String TABLE_WAITER = "waiter";

    // Room table columns names
    private static final String KEY_WAITERID = "userId";
    private static final String KEY_WAITERNAME = "userRealName";
    private static final String KEY_WAITERKITCHENID = "userRoomId";

    // Configuration table name
    private static final String TABLE_CONFIGURATION = "configuration";

    // Configuration table columns names
    private static final String KEY_CONFID = "confId";
    private static final String KEY_CONFSERVER = "serverIpAddress";
    private static final String KEY_CONFAPPVERSION = "appVersion";
    private static final String KEY_CONFINSTALLTIME = "installationTime";
    private static final String KEY_CONFKITCHENROLE = "kitchenRoleId";
    private static final String KEY_CONFHALLROLE = "hallRoleId";
    private static final String KEY_CONFWAITERROLE = "waiterRoleId";
    private static final String KEY_CONFCUSTOMERROLE = "customerRoleId";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_COMPANY_TABLE = "CREATE TABLE " + TABLE_COMPANY + "("
                + KEY_COMPID + " INTEGER PRIMARY KEY," + KEY_COMPNAME + " TEXT,"
                + KEY_COMPDESC + " TEXT," + KEY_COMPLOGO + " TEXT" + ")";
        sqLiteDatabase.execSQL(CREATE_COMPANY_TABLE);

        String CREATE_LOCATION_TABLE = "CREATE TABLE " + TABLE_LOCATION + "("
                + KEY_LOCID + " INTEGER PRIMARY KEY," + KEY_LOCNAME + " TEXT" + ")";
        sqLiteDatabase.execSQL(CREATE_LOCATION_TABLE);

        String CREATE_COMPANY_LOCATION_TABLE = "CREATE TABLE " + TABLE_COMPLOC + "("
                + KEY_CLCOMPID + " INTEGER NOT NULL,"
                + KEY_CLLOCID + " INTEGER NOT NULL," + " PRIMARY KEY(" + KEY_CLCOMPID + "," + KEY_CLLOCID + ")" + ")";
        sqLiteDatabase.execSQL(CREATE_COMPANY_LOCATION_TABLE);

        String CREATE_ROOM_TABLE = "CREATE TABLE " + TABLE_ROOM + "("
                + KEY_ROOMID + " INTEGER PRIMARY KEY," + KEY_ROOMNAME + " TEXT,"
                + KEY_ROOMDESC + " TEXT," + KEY_ROOMLOCID + " INTEGER," + KEY_ROOMTYPEID + " INTEGER" + ")";
        sqLiteDatabase.execSQL(CREATE_ROOM_TABLE);

        String CREATE_KITCHENROOM_TABLE = "CREATE TABLE " + TABLE_KITCHENROOM + "("
                + KEY_KRID1 + " INTEGER NOT NULL,"
                + KEY_KRID2 + " INTEGER NOT NULL," + " PRIMARY KEY(" + KEY_KRID1 + "," + KEY_KRID2 + ")" + ")";
        sqLiteDatabase.execSQL(CREATE_KITCHENROOM_TABLE);

        String CREATE_CATEGORY_TABLE = "CREATE TABLE " + TABLE_CATEGORY + "("
                + KEY_CATID + " INTEGER PRIMARY KEY," + KEY_CATNAME + " TEXT" + ")";
        sqLiteDatabase.execSQL(CREATE_CATEGORY_TABLE);

        String CREATE_BEVERAGE_TABLE = "CREATE TABLE " + TABLE_BEVERAGE + "("
                + KEY_BEVKTCID + " INTEGER NOT NULL," + KEY_BEVID + " INTEGER NOT NULL," + KEY_BEVNAME + " TEXT,"
                + KEY_BEVDESC + " TEXT," + KEY_BEVAVAIL + " INTEGER," + KEY_BEVIMAGE + " TEXT,"
                + KEY_BEVCATID + " INTEGER," + KEY_BEVHCOUNT + " INTEGER,"
                + " PRIMARY KEY(" + KEY_BEVKTCID + "," + KEY_BEVID + ")"  + ")";
        sqLiteDatabase.execSQL(CREATE_BEVERAGE_TABLE);

        String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_USERID + " INTEGER PRIMARY KEY," + KEY_USERNAME + " TEXT,"
                + KEY_USERPASS + " TEXT," + KEY_USERREALNAME + " TEXT,"
                + KEY_USERPIC + " TEXT," + KEY_USERCOMPID + " INTEGER,"
                + KEY_USERROOMID + " INTEGER" + ")";
        sqLiteDatabase.execSQL(CREATE_USER_TABLE);

        String CREATE_USERROLE_TABLE = "CREATE TABLE " + TABLE_USERROLE + "("
                + KEY_RUSERID + " INTEGER NOT NULL,"
                + KEY_RROLEID + " INTEGER NOT NULL," + " PRIMARY KEY(" + KEY_RUSERID + "," + KEY_RROLEID + ")" + ")";
        sqLiteDatabase.execSQL(CREATE_USERROLE_TABLE);

        String CREATE_ORDERITEM_TABLE = "CREATE TABLE " + TABLE_ORDERITEM + "("
                + KEY_OIHEADERID + " INTEGER,"
                + KEY_OIITEMID + " INTEGER,"
                + KEY_OIQANTITY + " INTEGER," + KEY_OIBEVNAME + " TEXT," + KEY_OIBEVID + " INTEGER,"
                + " PRIMARY KEY(" + KEY_OIHEADERID + "," + KEY_OIITEMID + ")" + ")";
        sqLiteDatabase.execSQL(CREATE_ORDERITEM_TABLE);

        String CREATE_ORDERHEADER_TABLE = "CREATE TABLE " + TABLE_ORDERHEADER + "("
                + KEY_OHID + " INTEGER PRIMARY KEY," + KEY_OHSENT + " TEXT,"
                + KEY_OHDELIVERED + " TEXT," + KEY_OHSTATUS + " TEXT," + KEY_OHCOMMENT + " TEXT,"
                + KEY_OHCOMPID + " INTEGER," + KEY_OHROOMID + " INTEGER," + KEY_OHWAITERID + " INTEGER,"
                + KEY_OHCUSTOMERID + " INTEGER" + ")";
        sqLiteDatabase.execSQL(CREATE_ORDERHEADER_TABLE);

        String CREATE_KITCHEN_TABLE = "CREATE TABLE " + TABLE_KITCHEN + "("
                + KEY_KITCHENID + " INTEGER PRIMARY KEY," + KEY_KITCHENNAME + " TEXT,"
                + KEY_KITCHENLOCID + " INTEGER" + ")";
        sqLiteDatabase.execSQL(CREATE_KITCHEN_TABLE);

        String CREATE_WAITER_TABLE = "CREATE TABLE " + TABLE_WAITER + "("
                + KEY_WAITERID + " INTEGER PRIMARY KEY," + KEY_WAITERNAME + " TEXT,"
                + KEY_WAITERKITCHENID + " INTEGER" + ")";
        sqLiteDatabase.execSQL(CREATE_WAITER_TABLE);

        String CREATE_CONFIGURATION_TABLE = "CREATE TABLE " + TABLE_CONFIGURATION + "("
                + KEY_CONFID + " INTEGER PRIMARY KEY," + KEY_CONFSERVER + " TEXT,"
                + KEY_CONFAPPVERSION + " TEXT," + KEY_CONFINSTALLTIME + " TEXT,"
                + KEY_CONFKITCHENROLE + " INTEGER," + KEY_CONFHALLROLE + " INTEGER,"
                + KEY_CONFWAITERROLE + " INTEGER," + KEY_CONFCUSTOMERROLE + " INTEGER" + ")";
        sqLiteDatabase.execSQL(CREATE_CONFIGURATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // Drop older table if existed
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPANY);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPLOC);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ROOM);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_KITCHENROOM);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_BEVERAGE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_USERROLE);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERITEM);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERHEADER);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_KITCHEN);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_WAITER);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_CONFIGURATION);

        // Create tables again
        onCreate(sqLiteDatabase);
    }

    // Delete all session data
    public void deleteAllSessionData() {
        this.deleteAllCategories();
        this.deleteAllBeverages();
        this.deleteAllOrderItems();
        this.deleteAllOrderHeaders();
        //this.deleteAllKitchen();
    }

    // Delete all master data
    public void deleteAllMasterData() {
        this.deleteAllCompanies();
        this.deleteAllLocations();
        this.deleteAllCompanyLocation();
        this.deleteAllRooms();
        this.deleteAllKitchenRoom();
        this.deleteAllKitchen();
        this.deleteAllWaiters();
    }

    // Check if Company have data
    public boolean isCompanyFull() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_COMPANY, null);
        if (cursor.getCount() > 0){
            cursor.close();
            db.close();
            return true;
        } else {
            cursor.close();
            db.close();
            return false;
        }
    }

    // Adding new company
    public void addCompany(Company company) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_COMPID, company.getCompanyId());
        values.put(KEY_COMPNAME, company.getCompanyName());
        values.put(KEY_COMPDESC, company.getCompanyDescription());
        values.put(KEY_COMPLOGO, company.getCompanyLogo());

        // Inserting Row
        db.insert(TABLE_COMPANY, null, values);
        //  db.close(); // Closing database connection
    }

    // Get logged user company
    public Company getCompanyById(int compId) {
        Company company = new Company();

        String selectQuery = "SELECT * FROM " + TABLE_COMPANY + " WHERE " + KEY_COMPID + " = " + compId;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            company.setCompanyId(cursor.getInt(0));
            company.setCompanyName(cursor.getString(1));
            company.setCompanyDescription(cursor.getString(2));
            company.setCompanyLogo(cursor.getString(3));
        }

        cursor.close();
        db.close();
        return company;
    }

    // Selecting all companies
    public List<Company> getAllCompanies() {
        List<Company> companyList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_COMPANY;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Company company = new Company();
                company.setCompanyId(cursor.getInt(0));
                company.setCompanyName(cursor.getString(1));
                company.setCompanyDescription(cursor.getString(2));
                company.setCompanyLogo(cursor.getString(3));

                companyList.add(company);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return companyList;
    }

    // Deleting all companies
    public void deleteAllCompanies() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_COMPANY, null, null);
        db.close();
    }

    // Adding new location
    public void addLocation(Location location) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LOCID, location.getLocationId());
        values.put(KEY_LOCNAME, location.getLocationName());

        // Inserting Row
        db.insert(TABLE_LOCATION, null, values);
        //db.close(); // Closing database connection
    }

    // Get location
    public Location getLocationById(int locationId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_LOCATION + " WHERE " + KEY_LOCID + " = " + locationId;
        Cursor cursor = db.rawQuery(selectQuery, null);
        Location location = new Location();
        if (cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                location.setLocationId(cursor.getInt(0));
                location.setLocationName(cursor.getString(1));
            }
        }
        cursor.close();
        db.close();
        return location;
    }

    // Selecting all locations
    public List<Location> getAllLocations() {
        List<Location> locationList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_LOCATION;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Location location = new Location();
                location.setLocationId(cursor.getInt(0));
                location.setLocationName(cursor.getString(1));

                locationList.add(location);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return locationList;
    }

    // Deleting all locations
    public void deleteAllLocations() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LOCATION, null, null);
        db.close();
    }

    // Adding new company - location
    public void addCompanyLocation(CompanyLocation companyLocation) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CLCOMPID, companyLocation.getCompanyId());
        values.put(KEY_CLLOCID, companyLocation.getLocationId());

        // Inserting Row
        db.insert(TABLE_COMPLOC, null, values);
        //db.close(); // Closing database connection
    }

    // Selecting all company - locations
    public List<CompanyLocation> getAllCompanyLocation() {
        List<CompanyLocation> companyLocationList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_COMPLOC;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                CompanyLocation companyLocation = new CompanyLocation();
                companyLocation.setCompanyId(cursor.getInt(0));
                companyLocation.setLocationId(cursor.getInt(1));

                companyLocationList.add(companyLocation);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return companyLocationList;
    }

    // Deleting all company - location
    public void deleteAllCompanyLocation() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_COMPLOC, null, null);
        db.close();
    }

    // Adding new room
    public void addRoom(Room room) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ROOMID, room.getRoomId());
        values.put(KEY_ROOMNAME, room.getRoomName());
        values.put(KEY_ROOMDESC, room.getRoomDescription());
        values.put(KEY_ROOMLOCID, room.getRoomLocationId());
        values.put(KEY_ROOMTYPEID, room.getRoomTypeId());

        // Inserting Row
        db.insert(TABLE_ROOM, null, values);
        //  db.close(); // Closing database connection
    }

    // Get logged user room by room id
    public Room getRoomById(int roomId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_ROOM + " WHERE " + KEY_ROOMID + " = " + roomId;
        Cursor cursor = db.rawQuery(selectQuery, null);
        Room room = new Room();
        if (cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                room.setRoomId(cursor.getInt(0));
                room.setRoomName(cursor.getString(1));
                room.setRoomDescription(cursor.getString(2));
                room.setRoomLocationId(cursor.getInt(3));
                room.setRoomTypeId(cursor.getInt(4));
            }
        }
        cursor.close();
        db.close();
        return room;
    }

    // Get logged user room by user id

    // Selecting all rooms
    public List<Room> getAllRooms() {
        List<Room> roomList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_ROOM;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Room room = new Room();
                room.setRoomId(cursor.getInt(0));
                room.setRoomName(cursor.getString(1));
                room.setRoomDescription(cursor.getString(2));
                room.setRoomLocationId(cursor.getInt(3));
                room.setRoomTypeId(cursor.getInt(4));

                roomList.add(room);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return roomList;
    }

    // Get all rooms at location but without kitchens
    public List<Room> getAllRoomsAtLocation(int locationId) {
        List<Room> roomList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_ROOM + " WHERE " + KEY_ROOMLOCID + " = " + locationId
                                                            + " AND " + KEY_ROOMTYPEID + " <> " + "1";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Room room = new Room();
                room.setRoomId(cursor.getInt(0));
                room.setRoomName(cursor.getString(1));
                room.setRoomDescription(cursor.getString(2));
                room.setRoomLocationId(cursor.getInt(3));
                room.setRoomTypeId(cursor.getInt(4));

                roomList.add(room);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return roomList;
    }

    // Deleting all rooms
    public void deleteAllRooms() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ROOM, null, null);
        db.close();
    }

    // Adding new kitchen - room
    public void addKitchenRoom(KitchenRoom kitchenRoom) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_KRID1, kitchenRoom.getKitchenId());
        values.put(KEY_KRID2, kitchenRoom.getRoomId());

        // Inserting Row
        db.insert(TABLE_KITCHENROOM, null, values);
        //db.close(); // Closing database connection
    }

    // Selecting all kitchen - room
    public List<KitchenRoom> getAllKitchenRoom(int roomId) {
        List<KitchenRoom> kitchenRoomList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_KITCHENROOM + " WHERE " + KEY_KRID2 + " = " + roomId;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                KitchenRoom kitchenRoom = new KitchenRoom();
                kitchenRoom.setKitchenId(cursor.getInt(0));
                kitchenRoom.setRoomId(cursor.getInt(1));

                kitchenRoomList.add(kitchenRoom);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return kitchenRoomList;
    }

    // Deleting all kitchen - room
    public void deleteAllKitchenRoom() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_KITCHENROOM, null, null);
        db.close();
    }

    // Adding new category
    public void addCategory(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CATID, category.getCategoryId());
        values.put(KEY_CATNAME, category.getCategoryName());

        // Inserting Row
        db.insert(TABLE_CATEGORY, null, values);
        //db.close(); // Closing database connection
    }

    // Selecting all locations
    public List<Category> getAllCategories() {
        List<Category> categoryList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_CATEGORY;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Category category = new Category();
                category.setCategoryId(cursor.getInt(0));
                category.setCategoryName(cursor.getString(1));

                categoryList.add(category);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return categoryList;
    }

    // Deleting all categories
    public void deleteAllCategories() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CATEGORY, null, null);
        db.close();
    }

    // Check if Categories have data
    public boolean isCategoriesFull() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CATEGORY, null);
        if (cursor.getCount() > 0){
            cursor.close();
            db.close();
            return true;
        } else {
            cursor.close();
            db.close();
            return false;
        }
    }

    // Check if Beverages are from right Kitchen
    public boolean isBeveragesFromKitchen(int kitchenId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_BEVERAGE + " WHERE " + KEY_BEVKTCID + " = " + kitchenId;
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.getCount() > 0){
            cursor.close();
            db.close();
            return true;
        } else {
            cursor.close();
            db.close();
            return false;
        }
    }

    // Adding new beverage
    public void addBeverage(Beverage beverage) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_BEVKTCID, beverage.getBevKitchenId());
        values.put(KEY_BEVID, beverage.getBeverageId());
        values.put(KEY_BEVNAME, beverage.getBeverageName());
        values.put(KEY_BEVDESC, beverage.getBevDescription());
        values.put(KEY_BEVAVAIL, beverage.isAvailable());
        values.put(KEY_BEVIMAGE, beverage.getBevImage());
        values.put(KEY_BEVCATID, beverage.getBevCategoryId());
        values.put(KEY_BEVHCOUNT, beverage.getHelpCounter());

        // Inserting Row
        db.insert(TABLE_BEVERAGE, null, values);
        //  db.close(); // Closing database connection
    }

    // Get beverage name by id
    public String getBeverageName(int kitchenId, int beverageId) {   // mozda bi ipak trebalo stavljati i kitchen id da bude pun kljuc!!!
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_BEVERAGE + " WHERE " + KEY_BEVKTCID + " = " + kitchenId
                                                                + " AND " + KEY_BEVID + " = " + beverageId;
        Cursor cursor = db.rawQuery(selectQuery, null);
        Beverage beverage = new Beverage();
        if (cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                beverage.setBeverageName(cursor.getString(2));
            }
        }
        cursor.close();
        db.close();
        return beverage.getBeverageName();
    }

    // Get beverage image url
    public String getBeverageImageUrl(int kitchenId, int beverageId) {   // mozda bi ipak trebalo stavljati i kitchen id da bude pun kljuc!!!
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_BEVERAGE + " WHERE " + KEY_BEVKTCID + " = " + kitchenId
                                                                + " AND " + KEY_BEVID + " = " + beverageId;
        Cursor cursor = db.rawQuery(selectQuery, null);
        Beverage beverage = new Beverage();
        if (cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                beverage.setBevImage(cursor.getString(5));
            }
        }
        cursor.close();
        db.close();
        return beverage.getBevImage();
    }

    // Get beverage by Id
    public Beverage getBeverageById(int kitchenId, int beverageId) {       // mozda bi ipak trebalo stavljati i kitchen id da bude pun kljuc!!!
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_BEVERAGE + " WHERE " + KEY_BEVKTCID + " = " + kitchenId
                                                                + " AND " + KEY_BEVID + " = " + beverageId;
        Cursor cursor = db.rawQuery(selectQuery, null);
        Beverage beverage = new Beverage();
        if (cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                beverage.setBevKitchenId(cursor.getInt(0));
                beverage.setBeverageId(cursor.getInt(1));
                beverage.setBeverageName(cursor.getString(2));
                beverage.setBevDescription(cursor.getString(3));
                beverage.setAvailable(cursor.getInt(4));
                beverage.setBevImage(cursor.getString(5));
                beverage.setBevCategoryId(cursor.getInt(6));
            }
        }
        cursor.close();
        db.close();
        return beverage;
    }

    // Update beverage status
    public void updateBeverageStatus(Beverage beverage) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_BEVAVAIL, beverage.isAvailable());

        // Updating Row
        db.update(TABLE_BEVERAGE, values, KEY_BEVID + "=" + beverage.getBeverageId(), null);
        db.close(); // Closing database connection
    }

    // Selecting beverages with status unavailable
    public List<Beverage> getAllUnavailableBeverages() {
        List<Beverage> beverageList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_BEVERAGE + " WHERE " + KEY_BEVAVAIL + " = 0";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Beverage beverage = new Beverage();
                beverage.setBevKitchenId(cursor.getInt(0));
                beverage.setBeverageId(cursor.getInt(1));
                beverage.setBeverageName(cursor.getString(2));
                beverage.setBevDescription(cursor.getString(3));
                beverage.setAvailable(cursor.getInt(4));
                beverage.setBevImage(cursor.getString(5));
                beverage.setBevCategoryId(cursor.getInt(6));
                beverage.setHelpCounter(cursor.getInt(7));

                beverageList.add(beverage);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return beverageList;
    }

    // Selecting beverages with status available
    public List<Beverage> getAllAvailableBeverages() {
        List<Beverage> beverageList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_BEVERAGE + " WHERE " + KEY_BEVAVAIL + " = 1";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Beverage beverage = new Beverage();
                beverage.setBevKitchenId(cursor.getInt(0));
                beverage.setBeverageId(cursor.getInt(1));
                beverage.setBeverageName(cursor.getString(2));
                beverage.setBevDescription(cursor.getString(3));
                beverage.setAvailable(cursor.getInt(4));
                beverage.setBevImage(cursor.getString(5));
                beverage.setBevCategoryId(cursor.getInt(6));
                beverage.setHelpCounter(cursor.getInt(7));

                beverageList.add(beverage);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return beverageList;
    }

    // Selecting all beverages
    public List<Beverage> getAllBeverages() {
        List<Beverage> beverageList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_BEVERAGE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Beverage beverage = new Beverage();
                beverage.setBevKitchenId(cursor.getInt(0));
                beverage.setBeverageId(cursor.getInt(1));
                beverage.setBeverageName(cursor.getString(2));
                beverage.setBevDescription(cursor.getString(3));
                beverage.setAvailable(cursor.getInt(4));
                beverage.setBevImage(cursor.getString(5));
                beverage.setBevCategoryId(cursor.getInt(6));
                beverage.setHelpCounter(cursor.getInt(7));

                beverageList.add(beverage);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return beverageList;
    }

    // Deleting all beverages
    public void deleteAllBeverages() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BEVERAGE, null, null);
        db.close();
    }

    // Adding new user
    public void addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USERID, user.getUserId());
        values.put(KEY_USERNAME, user.getUserName());
        values.put(KEY_USERPASS, user.getUserPassword());
        values.put(KEY_USERREALNAME, user.getUserRealName());
        values.put(KEY_USERPIC, user.getUserPicture());
        values.put(KEY_USERCOMPID, user.getUserCompanyId());
        values.put(KEY_USERROOMID, user.getUserRoomId());

        // Inserting Row
        db.insert(TABLE_USER, null, values);
        //  db.close(); // Closing database connection
    }

    // Selecting all users (actually it can only be one user!)
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setUserId(cursor.getInt(0));
                user.setUserName(cursor.getString(1));
                user.setUserPassword(cursor.getString(2));
                user.setUserRealName(cursor.getString(3));
                user.setUserPicture(cursor.getString(4));
                user.setUserCompanyId(cursor.getInt(5));
                user.setUserRoomId(cursor.getInt(6));

                userList.add(user);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return userList;
    }

    // Check if User have data
    public boolean isUserDataPresent() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER, null);
        if (cursor.getCount() > 0){
            cursor.close();
            db.close();
            return true;
        } else {
            cursor.close();
            db.close();
            return false;
        }
    }

    // Get logged user
    public User getLoggedUser() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER, null);
        User user = new User();
        if (cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                user.setUserId(cursor.getInt(0));
                user.setUserName(cursor.getString(1));
                user.setUserPassword(cursor.getString(2));
                user.setUserRealName(cursor.getString(3));
                user.setUserPicture(cursor.getString(4));
                user.setUserCompanyId(cursor.getInt(5));
                user.setUserRoomId(cursor.getInt(6));
            }
        }
        cursor.close();
        db.close();
        return user;
    }

    // Deleting all users
    public void deleteAllUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER, null, null);
        db.close();
    }

    // Adding new user-role
    public void addUserRole(UserRole userRole) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_RUSERID, userRole.getUserId());
        values.put(KEY_RROLEID, userRole.getRoleId());

        // Inserting Row
        db.insert(TABLE_USERROLE, null, values);
        //  db.close(); // Closing database connection
    }

    // Selecting all user-roles (actually it can only be one user-role!)
    public List<UserRole> getAllUserRoles() {
        List<UserRole> userRoleList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_USERROLE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                UserRole userRole = new UserRole();
                userRole.setUserId(cursor.getInt(0));
                userRole.setRoleId(cursor.getInt(1));

                userRoleList.add(userRole);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return userRoleList;
    }

    // Deleting all user-roles
    public void deleteAllUserRoles() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USERROLE, null, null);
        db.close();
    }

    // Adding new order item
    public void addOrderItem(OrderItem orderItem) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_OIHEADERID, orderItem.getOrderHeaderId());
        values.put(KEY_OIITEMID, orderItem.getOrderItemId());
        values.put(KEY_OIQANTITY, orderItem.getItemQuantity());
        values.put(KEY_OIBEVNAME, orderItem.getBeverageName());
        values.put(KEY_OIBEVID, orderItem.getBeverageId());

        // Inserting Row
        db.insert(TABLE_ORDERITEM, null, values);
        //  db.close(); // Closing database connection
    }

    // Selecting all order items for header id
    public List<OrderItem> getAllOrderItemsForHeaderId(int headerId) {
        List<OrderItem> orderItemList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_ORDERITEM + " WHERE " + KEY_OIHEADERID + " = " + headerId;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderHeaderId(cursor.getInt(0));
                orderItem.setOrderItemId(cursor.getInt(1));
                orderItem.setItemQuantity(cursor.getInt(2));
                orderItem.setBeverageName(cursor.getString(3));
                orderItem.setBeverageId(cursor.getInt(4));

                orderItemList.add(orderItem);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return orderItemList;
    }

    // Selecting all order items for all at once
    public List<OrderItem> getAllOrderItems() {
        List<OrderItem> orderItemList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_ORDERITEM;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderHeaderId(cursor.getInt(0));
                orderItem.setOrderItemId(cursor.getInt(1));
                orderItem.setItemQuantity(cursor.getInt(2));
                orderItem.setBeverageName(cursor.getString(3));
                orderItem.setBeverageId(cursor.getInt(4));

                orderItemList.add(orderItem);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return orderItemList;
    }

    // Deleting all order items
    public void deleteAllOrderItems() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ORDERITEM, null, null);
        db.close();
    }

    private String dateTimeToString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "dd.MM.yyyy kk:mm:ss", Locale.GERMAN);
        //Date date = new Date();
        return dateFormat.format(date);
    }

    private Date stringToDateTime(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "dd.MM.yyyy kk:mm:ss", Locale.GERMAN);
        Date date = new Date();
        try {
            date = dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    // Adding new order header
    public void addOrderHeader(OrderHeader orderHeader) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_OHID, orderHeader.getOrderHeaderId());
        values.put(KEY_OHSENT, dateTimeToString(orderHeader.getTimeSent()));
        values.put(KEY_OHDELIVERED, dateTimeToString(orderHeader.getTimeDelivered()));
        values.put(KEY_OHSTATUS, orderHeader.getStatus());
        values.put(KEY_OHCOMMENT, orderHeader.getComment());
        values.put(KEY_OHCOMPID, orderHeader.getCompanyId());
        values.put(KEY_OHROOMID, orderHeader.getRoomId());
        values.put(KEY_OHWAITERID, orderHeader.getUserWaiterId());
        values.put(KEY_OHCUSTOMERID, orderHeader.getUserCustomerId());

        // Inserting Row
        db.insert(TABLE_ORDERHEADER, null, values);
        //  db.close(); // Closing database connection
    }

    // Get order header by id
    public OrderHeader getOrderHeaderById(int orderheaderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_ORDERHEADER + " WHERE " + KEY_OHID + " = " + orderheaderId;
        Cursor cursor = db.rawQuery(selectQuery, null);
        OrderHeader orderHeader = new OrderHeader();
        if (cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                orderHeader.setOrderHeaderId(cursor.getInt(0));
                orderHeader.setTimeSent(stringToDateTime(cursor.getString(1)));
                orderHeader.setTimeDelivered(stringToDateTime(cursor.getString(2)));
                orderHeader.setStatus(cursor.getString(3));
                orderHeader.setComment(cursor.getString(4));
                orderHeader.setCompanyId(cursor.getInt(5));
                orderHeader.setRoomId(cursor.getInt(6));
                orderHeader.setUserWaiterId(cursor.getInt(7));
                orderHeader.setUserCustomerId(cursor.getInt(8));
            }
        }
        cursor.close();
        db.close();
        return orderHeader;
    }

    // Deleting all order headers
    public void deleteAllOrderHeaders() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ORDERHEADER, null, null);
        db.close();
    }


    // Adding new kitchen
    public void addKitchen(Kitchen kitchen) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_KITCHENID, kitchen.getKitchenId());
        values.put(KEY_KITCHENNAME, kitchen.getKitchenName());
        values.put(KEY_KITCHENLOCID, kitchen.getKitchenLocationId());

        // Inserting Row
        db.insert(TABLE_KITCHEN, null, values);
        //db.close(); // Closing database connection
    }

    // Check if Kitchen have data
    public boolean isKitchenFull() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_KITCHEN, null);
        if (cursor.getCount() > 0){
            cursor.close();
            db.close();
            return true;
        } else {
            cursor.close();
            db.close();
            return false;
        }
    }

    // Get kitchen by id
    public Kitchen getKitchenById(int kitchenId) {
        Kitchen kitchen = new Kitchen();

        String selectQuery = "SELECT * FROM " + TABLE_KITCHEN + " WHERE " + KEY_KITCHENID + " = " + kitchenId;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                kitchen.setKitchenId(cursor.getInt(0));
                kitchen.setKitchenName(cursor.getString(1));
                kitchen.setKitchenLocationId(cursor.getInt(2));
            }
        }

        cursor.close();
        db.close();
        return kitchen;
    }

    // Deleting all kitchen
    public void deleteAllKitchen() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_KITCHEN, null, null);
        db.close();
    }

    // Get Kitchen from logged user
    public Kitchen getKitchenByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_KITCHEN + " AS k INNER JOIN " + TABLE_KITCHENROOM + " AS kr ON "
                + "kr." + KEY_KRID1 + " = k." + KEY_KITCHENID + " INNER JOIN " + TABLE_ROOM + " AS r ON "
                + "r." + KEY_ROOMID + " = kr." + KEY_KRID2 + " INNER JOIN " + TABLE_USER + " AS u ON "
                + "u." + KEY_USERROOMID + " = r." + KEY_ROOMID
                + " WHERE u." + KEY_USERID + " = " + userId;
        Cursor cursor = db.rawQuery(selectQuery, null);
        Kitchen kitchen = new Kitchen();
        if (cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                kitchen.setKitchenId(cursor.getInt(0));
                kitchen.setKitchenName(cursor.getString(1));
                kitchen.setKitchenLocationId(cursor.getInt(2));
            }
        }
        cursor.close();
        db.close();
        return kitchen;
    }

    // Adding new waiter
    public void addWaiter(Waiter waiter) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_WAITERID, waiter.getUserId());
        values.put(KEY_WAITERNAME, waiter.getUserRealName());
        values.put(KEY_WAITERKITCHENID, waiter.getUserRoomId());

        // Inserting Row
        db.insert(TABLE_WAITER, null, values);
        //  db.close(); // Closing database connection
    }

    // Check if Waiter have data
    public boolean isWaiterFull() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_WAITER, null);
        if (cursor.getCount() > 0){
            cursor.close();
            db.close();
            return true;
        } else {
            cursor.close();
            db.close();
            return false;
        }
    }

    // Selecting all waiters
    public List<Waiter> getAllWaiters() {
        List<Waiter> waiterList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_WAITER;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Waiter waiter = new Waiter();
                waiter.setUserId(cursor.getInt(0));
                waiter.setUserRealName(cursor.getString(1));
                waiter.setUserRoomId(cursor.getInt(2));

                waiterList.add(waiter);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return waiterList;
    }

    // Deleting all waiter
    public void deleteAllWaiters() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WAITER, null, null);
        db.close();
    }


    // Adding new configuration
    public void addConfiguration(Configuration configuration) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CONFID, configuration.getConfId());
        values.put(KEY_CONFSERVER, configuration.getServerIpAddress());
        values.put(KEY_CONFAPPVERSION, configuration.getAppVersion());
        values.put(KEY_CONFINSTALLTIME, dateTimeToString(configuration.getInstallationTime()));
        values.put(KEY_CONFKITCHENROLE, configuration.getKitchenRoleId());
        values.put(KEY_CONFHALLROLE, configuration.getHallRoleId());
        values.put(KEY_CONFWAITERROLE, configuration.getWaiterRoleId());
        values.put(KEY_CONFCUSTOMERROLE, configuration.getCustomerRoleId());

        // Inserting Row
        db.insert(TABLE_WAITER, null, values);
        //  db.close(); // Closing database connection
    }

    // Get configuration
    public Configuration getConfiguration() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE_CONFIGURATION;
        Cursor cursor = db.rawQuery(selectQuery, null);
        Configuration configuration = new Configuration();
        if (cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                configuration.setConfId(cursor.getInt(0));
                configuration.setServerIpAddress(cursor.getString(1));
                configuration.setAppVersion(cursor.getString(2));
                configuration.setInstallationTime(stringToDateTime(cursor.getString(3))); // stringToDateTime(
                configuration.setKitchenRoleId(cursor.getInt(4));
                configuration.setHallRoleId(cursor.getInt(5));
                configuration.setWaiterRoleId(cursor.getInt(6));
                configuration.setCustomerRoleId(cursor.getInt(7));
            }
        }
        cursor.close();
        db.close();
        return configuration;
    }

    // Deleting configuration table
    public void deleteAllConfigurations() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONFIGURATION, null, null);
        db.close();
    }

}
