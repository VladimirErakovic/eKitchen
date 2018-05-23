package mkitbs.ekitchen.app.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import mkitbs.ekitchen.app.R;
import mkitbs.ekitchen.app.entities.Company;
import mkitbs.ekitchen.app.entities.ConsumChild;
import mkitbs.ekitchen.app.entities.OrderItem;
import mkitbs.ekitchen.app.entities.Room;

/**
 * Created by verakovic on 4/3/2017.
 */

public class ExpandConsumListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private DatabaseHandler databaseHandler;
    private List<OrderItem> listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<Integer, List<ConsumChild>> listDataChild;
    private int kitchenId;

    public ExpandConsumListAdapter(Context _context, List<OrderItem> _listDataHeader,
                                 HashMap<Integer, List<ConsumChild>> _listChildData, int kitchen_id) {
        this.context = _context;
        this.listDataHeader = _listDataHeader;
        this.listDataChild = _listChildData;
        databaseHandler = new DatabaseHandler(context);
        this.kitchenId = kitchen_id;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition).getBeverageId())
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final ConsumChild childText = (ConsumChild) getChild(groupPosition, childPosition);

        View view;

        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.consum_expand_list_item, parent, false);
        } else {
            view = (View) convertView;
        }

        TextView txtListChildOrderNumber = (TextView) view.findViewById(R.id.listItemOrderNumber);
        TextView txtListChildDateTime = (TextView) view.findViewById(R.id.listItemDateTime);
        TextView txtListChildQuantity = (TextView) view.findViewById(R.id.listItemQuantity);
        TextView txtListChildRoom = (TextView) view.findViewById(R.id.listItemRoom);

        txtListChildOrderNumber.setText(context.getResources().getString(R.string.order_number) + " " + String.valueOf(childText.getOrderHeaderId()));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy kk:mm", Locale.GERMAN);
        txtListChildDateTime.setText(simpleDateFormat.format(childText.getDateTimeSent()));
        txtListChildQuantity.setText(context.getResources().getString(R.string.hall_consum_amount) + " " + String.valueOf(childText.getQuantity()));
        Room room = databaseHandler.getRoomById(childText.getRoomId());
        Company company = databaseHandler.getCompanyById(childText.getCompanyId());
        txtListChildRoom.setText(company.getCompanyName() + " - " + room.getRoomName());

        return view;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition).getBeverageId())
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return String.valueOf(this.listDataHeader.get(groupPosition).getBeverageId());
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

        View view;

        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.kitchen_beverage_list_item, parent, false);
        } else {
            view = (View) convertView;
        }

    /**    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy kk:mm", Locale.GERMAN);
        TextView txtDateTime = (TextView) view.findViewById(R.id.textDateTimeOverviewHall);
        txtDateTime.setText(simpleDateFormat.format(orderHeader.getTimeSent()));

        Company company = databaseHandler.getCompanyById(orderHeader.getCompanyId());
        TextView txtCompany = (TextView) view.findViewById(R.id.textCompanyOverviewHall);
        txtCompany.setText(company.getCompanyName());  **/

        OrderItem orderItem = listDataHeader.get(groupPosition);

        TextView txtBevName = (TextView) view.findViewById(R.id.listItemKitchenBeverageName);
        if (!orderItem.getBeverageName().isEmpty())
            txtBevName.setText(orderItem.getBeverageName());
        TextView txtBevQantity = (TextView) view.findViewById(R.id.listItemKitchenBeverageQuantity);
        if (orderItem.getItemQuantity() > 0)
            txtBevQantity.setText(String.valueOf(orderItem.getItemQuantity()));

        ImageView imageView = (ImageView) view.findViewById(R.id.imageBeverageItem);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(10, 0, 0, 0);
        imageView.setLayoutParams(lp);
        String bevImageUrl = databaseHandler.getBeverageImageUrl(kitchenId, orderItem.getBeverageId());
        if (bevImageUrl != null && !bevImageUrl.isEmpty()) {
            Picasso.with(context).load(bevImageUrl).into(imageView);
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
