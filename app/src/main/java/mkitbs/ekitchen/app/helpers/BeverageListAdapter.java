package mkitbs.ekitchen.app.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import mkitbs.ekitchen.app.R;
import mkitbs.ekitchen.app.entities.OrderItem;

/**
 * Created by verakovic on 08.12.2016.
 */

public class BeverageListAdapter extends BaseAdapter {

    private Context context;
    private List<OrderItem> orderItemsList;
    private DatabaseHandler databaseHandler;
    private int kitchenId;

    public BeverageListAdapter() {};

    public BeverageListAdapter(Context context, List<OrderItem> order_items, int kitchen_id) {
        this.context = context;
        this.orderItemsList = order_items;
        databaseHandler = new DatabaseHandler(context);
        this.kitchenId = kitchen_id;
    }

    @Override
    public int getCount() {
        return orderItemsList.size();
    }

    @Override
    public Object getItem(int position) {
        return orderItemsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;

        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.kitchen_beverage_list_item, parent, false);
        } else {
            view = (View) convertView;
        }

        OrderItem orderItem = orderItemsList.get(position);

        TextView txtBevName = (TextView) view.findViewById(R.id.listItemKitchenBeverageName);
        if (!orderItem.getBeverageName().isEmpty())
            txtBevName.setText(orderItem.getBeverageName());
        TextView txtBevQantity = (TextView) view.findViewById(R.id.listItemKitchenBeverageQuantity);
        if (orderItem.getItemQuantity() > 0)
            txtBevQantity.setText(String.valueOf(orderItem.getItemQuantity()));

        ImageView imageView = (ImageView) view.findViewById(R.id.imageBeverageItem);
        String bevImageUrl = databaseHandler.getBeverageImageUrl(kitchenId, orderItem.getBeverageId());
        if (bevImageUrl != null && !bevImageUrl.isEmpty()) {
            Picasso.with(context).load(bevImageUrl).into(imageView);
        }

        return view;
    }
}
