package mkitbs.ekitchen.app.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import mkitbs.ekitchen.app.R;
import mkitbs.ekitchen.app.entities.Beverage;

/**
 * Created by verakovic on 29.12.2016.
 */

public class BeverageAdapter extends RecyclerView.Adapter<BeverageAdapter.MyViewHolder>  {

    private Context context;
    private List<Beverage> beverageList;

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView beverageName;
        ImageView beverageImage;

        MyViewHolder(View view) {
            super(view);
            beverageName = (TextView) view.findViewById(R.id.textBeverageUnavailable);
            beverageImage = (ImageView) view.findViewById(R.id.imageBeverageUnavailable);
        }

    }



    public BeverageAdapter(List<Beverage> beverage_list){
        this.beverageList = beverage_list;
    }

    @Override
    public BeverageAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.beverage_unavailable_list_item, parent, false);

        context = parent.getContext();

        return new BeverageAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(BeverageAdapter.MyViewHolder holder, final int position) {

        Beverage beverage = beverageList.get(position);
        holder.beverageName.setText(beverage.getBeverageName());

        ImageView imageView = holder.beverageImage;
        if (beverageList.get(position).getBevImage() != null && !beverageList.get(position).getBevImage().isEmpty()) {
            Picasso.with(context).load(beverageList.get(position).getBevImage()).into(imageView);
        }
    }

    @Override
    public int getItemCount() {
        return beverageList.size();
    }

}
