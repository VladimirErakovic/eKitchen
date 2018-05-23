package mkitbs.ekitchen.app.recyclerview;

/**
 * Created by verakovic on 22.11.2016.
 */

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import mkitbs.ekitchen.app.R;
import mkitbs.ekitchen.app.entities.OrderItem;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.MyViewHolder> {

    private MyAdapterListener onClickListener;

    public interface MyAdapterListener {

        void btnRemoveOnClick(View v, int position);
        void btnAddOnClick(View v, int position);
    }

    private List<OrderItem> orderItemList;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, counter;
        Button removeBtn;
        Button addBtn;

        MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.beverageTitle);
            counter = (TextView) view.findViewById(R.id.beverageCounter);
            removeBtn = (Button) view.findViewById(R.id.removeButton);
            addBtn = (Button) view.findViewById(R.id.addButton);

            removeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.btnRemoveOnClick(v, getAdapterPosition());
                }
            });
            addBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.btnAddOnClick(v, getAdapterPosition());
                }
            });
        }

    }

    public OrderAdapter(List<OrderItem> order_item_list, MyAdapterListener listener){
        this.orderItemList = order_item_list;
        onClickListener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.order_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        OrderItem item = orderItemList.get(position);
        holder.title.setText(item.getBeverageName());
        holder.counter.setText(Integer.toString(item.getItemQuantity()));
    }

    @Override
    public int getItemCount() {
        return orderItemList.size();
    }

}
