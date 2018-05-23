package mkitbs.ekitchen.app.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import mkitbs.ekitchen.app.R;
import mkitbs.ekitchen.app.entities.KitchenOrderHeader;

/**
 * Created by verakovic on 08.12.2016.
 */

public class KitchenOrderAdapter extends RecyclerView.Adapter<KitchenOrderAdapter.MyViewHolder> {

    private MyAdapterListener onClickListener;

    private String userNameString;
    private String statusNew, statusTaken, statusDelivered, statusToClean, statusClosed;
    private int colorNew, colorTaken, colorDelivered, colorToClean, colorClose;

    public interface MyAdapterListener {

        void rowViewOnClick(View v, int position);

    }

    private List<KitchenOrderHeader> kitchenOrderHeaderList;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView dateTime, location, userCustomer, statusText;
        ImageView statusImage;

        MyViewHolder(View view) {
            super(view);
            dateTime = (TextView) view.findViewById(R.id.textDateTime);
            location = (TextView) view.findViewById(R.id.textLocation);
            userCustomer = (TextView) view.findViewById(R.id.textUserCustomer);
            statusImage = (ImageView) view.findViewById(R.id.imageStatusIcon);
            statusText = (TextView) view.findViewById(R.id.textStatus);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.rowViewOnClick(v, getAdapterPosition());
                }
            });

        }

    }

    public KitchenOrderAdapter(List<KitchenOrderHeader> order_header_list, MyAdapterListener listener){
        this.kitchenOrderHeaderList = order_header_list;
        onClickListener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.kitchen_order_list_item, parent, false);

        userNameString = parent.getContext().getResources().getString(R.string.kitchenorder_username);
        statusNew = parent.getContext().getResources().getString(R.string.status_new);
        statusTaken = parent.getContext().getResources().getString(R.string.status_taken);
        statusDelivered = parent.getContext().getResources().getString(R.string.status_delivered);
        statusToClean = parent.getContext().getResources().getString(R.string.status_for_cleanup);
        statusClosed = parent.getContext().getResources().getString(R.string.status_closed);
        colorNew = parent.getContext().getResources().getColor(R.color.md_red_500);
        colorTaken = parent.getContext().getResources().getColor(R.color.md_blue_500);
        colorDelivered = parent.getContext().getResources().getColor(R.color.md_green_500);
        colorToClean = parent.getContext().getResources().getColor(R.color.orangeDark);
        colorClose = parent.getContext().getResources().getColor(R.color.almost_black);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        KitchenOrderHeader kitchenOrderHeader = kitchenOrderHeaderList.get(position);

        if (kitchenOrderHeader.isSelected()) {
            holder.itemView.setSelected(true);
        } else {
            holder.itemView.setSelected(false);
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy kk:mm" , Locale.GERMAN);

        holder.dateTime.setText(simpleDateFormat.format(kitchenOrderHeader.getTimeSent()));
        String status = kitchenOrderHeader.getStatus();
        if (status.equals("N")) {
            holder.statusImage.setImageResource(R.drawable.a2);  // new_icon_red
            holder.statusText.setText(statusNew);
            holder.statusText.setTextColor(colorNew);
        } else if (status.equals("P")) {
            holder.statusImage.setImageResource(R.drawable.b2);  // taken_icon_blue
            holder.statusText.setText(statusTaken);
            holder.statusText.setTextColor(colorTaken);
        } else if (status.equals("I")) {
            holder.statusImage.setImageResource(R.drawable.c2);  // delivered_icon_green
            holder.statusText.setText(statusDelivered);
            holder.statusText.setTextColor(colorDelivered);
        } else if (status.equals("R")) {  //
            holder.statusImage.setImageResource(R.drawable.d2);
            holder.statusText.setText(statusToClean);
            holder.statusText.setTextColor(colorToClean);
        } else {
            holder.statusImage.setImageResource(R.drawable.e2);
            holder.statusText.setText(statusClosed);
            holder.statusText.setTextColor(colorClose);
        }


        holder.location.setText(kitchenOrderHeader.getCompanyName() + " - " + kitchenOrderHeader.getRoomName());
        holder.userCustomer.setText(userNameString + " " + kitchenOrderHeader.getUserCustomerName());
    }

    @Override
    public int getItemCount() {
        return kitchenOrderHeaderList.size();
    }

}
