package mkitbs.ekitchen.app.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import mkitbs.ekitchen.app.R;
import mkitbs.ekitchen.app.entities.Company;
import mkitbs.ekitchen.app.entities.Location;
import mkitbs.ekitchen.app.entities.OrderHeader;
import mkitbs.ekitchen.app.entities.OrderItem;
import mkitbs.ekitchen.app.entities.Room;

/**
 * Created by verakovic on 05.12.2016.
 */

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private DatabaseHandler databaseHandler;
    private List<OrderHeader> listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<OrderItem>> listDataChild;
    private String activityTag;


    public ExpandableListAdapter(Context _context, List<OrderHeader> _listDataHeader,
                                 HashMap<String, List<OrderItem>> _listChildData, String activity_tag) {
        this.context = _context;
        this.listDataHeader = _listDataHeader;
        this.listDataChild = _listChildData;
        databaseHandler = new DatabaseHandler(context);
        this.activityTag = activity_tag;
    }


    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this.listDataChild.get(String.valueOf(this.listDataHeader.get(groupPosition).getOrderHeaderId()))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final OrderItem childText = (OrderItem) getChild(groupPosition, childPosition);

        View view;

        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.expandable_list_item, parent, false);
        } else {
            view = (View) convertView;
        }

        TextView txtListChildBevName = (TextView) view.findViewById(R.id.listItemBeverageName);
        TextView txtListChildBevQuan = (TextView) view.findViewById(R.id.listItemBeverageQuantity);

        txtListChildBevName.setText(childText.getBeverageName());
        txtListChildBevQuan.setText(String.valueOf(childText.getItemQuantity()));

        return view;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.listDataChild.get(String.valueOf(this.listDataHeader.get(groupPosition).getOrderHeaderId()))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return String.valueOf(this.listDataHeader.get(groupPosition).getOrderHeaderId());
    }

    @Override
    public int getGroupCount() {
        return this.listDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        //String headerTitle = (String) getGroup(groupPosition);
        OrderHeader orderHeader = listDataHeader.get(groupPosition);

        ImageView statusImage;
        TextView statusText;

        int layoutId;
        if (activityTag.equals("HALL") || activityTag.equals("CONSUM")) {
            layoutId = R.layout.expandable_list_group_hall;
        } else {
            layoutId = R.layout.expandable_list_group_user;
        }

        View view;

        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(layoutId, parent, false);
        } else {
            view = (View) convertView;
        }

        if (activityTag.equals("HALL") || activityTag.equals("CONSUM")) {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy kk:mm", Locale.GERMAN);
            TextView txtDateTime = (TextView) view.findViewById(R.id.textDateTimeOverviewHall);
            txtDateTime.setText(simpleDateFormat.format(orderHeader.getTimeSent()));

            Company company = databaseHandler.getCompanyById(orderHeader.getCompanyId());
            if (activityTag.equals("HALL")) {
                TextView txtCompany = (TextView) view.findViewById(R.id.textCompanyOverviewHall);
                txtCompany.setText(company.getCompanyName());
            } else {
                Room room = databaseHandler.getRoomById(orderHeader.getRoomId());
                TextView txtCompany = (TextView) view.findViewById(R.id.textCompanyOverviewHall);
                txtCompany.setText(company.getCompanyName() + " - " + room.getRoomName());
            }

            statusImage = (ImageView) view.findViewById(R.id.imageStatusIconOverviewHall);
            statusText = (TextView) view.findViewById(R.id.textStatusOverviewHall);

        } else {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy kk:mm", Locale.GERMAN);
            TextView txtDateTime = (TextView) view.findViewById(R.id.textDateTimeHistoryUser);
            txtDateTime.setText(simpleDateFormat.format(orderHeader.getTimeSent()));

            Room room = databaseHandler.getRoomById(orderHeader.getRoomId());
            TextView txtOrderId = (TextView) view.findViewById(R.id.textRoomHistoryUser);
            txtOrderId.setText(room.getRoomName());

            Location location = databaseHandler.getLocationById(room.getRoomLocationId());
            TextView txtLocation = (TextView) view.findViewById(R.id.textLocationHistoryUser);
            txtLocation.setText(location.getLocationName());

            statusImage = (ImageView) view.findViewById(R.id.imageStatusIconHistoryUser);
            statusText = (TextView) view.findViewById(R.id.textStatusHistoryUser);

        }

        String status = orderHeader.getStatus();
        switch (status) {
            case "N":   // novi
                statusImage.setImageResource(R.drawable.a2);  // new_icon_red
                statusText.setText(context.getResources().getString(R.string.status_new));
                statusText.setTextColor(context.getResources().getColor(R.color.md_red_500));
                break;
            case "P":    // preuzet
                statusImage.setImageResource(R.drawable.b2);  // taken_icon_blue
                statusText.setText(context.getResources().getString(R.string.status_taken));
                statusText.setTextColor(context.getResources().getColor(R.color.md_blue_500));
                break;
            case "I":    // isporucen
                statusImage.setImageResource(R.drawable.c2);  // delivered_icon_green
                statusText.setText(context.getResources().getString(R.string.status_delivered));
                statusText.setTextColor(context.getResources().getColor(R.color.md_green_500));
                break;
            case "R":
                statusImage.setImageResource(R.drawable.d2);
                statusText.setText(context.getResources().getString(R.string.status_for_cleanup));
                statusText.setTextColor(context.getResources().getColor(R.color.orangeDark));
                break;
            case "Z":
                statusImage.setImageResource(R.drawable.e2);
                statusText.setText(context.getResources().getString(R.string.status_closed));
                statusText.setTextColor(context.getResources().getColor(R.color.almost_black));
                break;
        }

        return view;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


}
