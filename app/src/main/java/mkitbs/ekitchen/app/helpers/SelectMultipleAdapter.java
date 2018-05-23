package mkitbs.ekitchen.app.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import mkitbs.ekitchen.app.R;
import mkitbs.ekitchen.app.entities.Beverage;
/**
 * Created by verakovic on 04.01.2017.
 */

public class SelectMultipleAdapter extends BaseAdapter  {

    private GridItemListener onClickListener;

    private Context mContext;
    private int imageWidth;
    private List<Beverage> filteredList;

    // Constructor
    public SelectMultipleAdapter(Context c, List<Beverage> filtered_list, int image_width, GridItemListener listener) {
        this.mContext = c;
        this.filteredList = filtered_list;
        this.imageWidth	= image_width;
        onClickListener = listener;
    }

    public int getCount() {
        return filteredList.size();
    }

    public Object getItem(int position) {
        return filteredList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {

        View v;

        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            v = layoutInflater.inflate(R.layout.grid_item_experiment, parent, false);
            v.setLayoutParams(new GridView.LayoutParams(imageWidth, imageWidth));
        } else {
            v = (View) convertView;
        }


        TextView counter = (TextView)v.findViewById(R.id.itemCounter);
        counter.setText(String.valueOf(filteredList.get(position).getHelpCounter()));
        if (filteredList.get(position).getHelpCounter() > 0) {
            counter.setVisibility(View.VISIBLE);
        } else {
            counter.setVisibility(View.INVISIBLE);
        }
        final int pos = position;
        View leftView = v.findViewById(R.id.leftClickSurface);
        View rightView = v.findViewById(R.id.rightClickSurface);

        leftView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.btnRemoveOnClick(v, pos);
            }
        });
        rightView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.btnAddOnClick(v, pos);
            }
        });

        TextView title = (TextView)v.findViewById(R.id.beverageName);
        title.setText(filteredList.get(position).getBeverageName());

        ImageView imageView = (ImageView) v.findViewById(R.id.beverageImage);
        if (filteredList.get(position).getBevImage() != null && !filteredList.get(position).getBevImage().isEmpty()) {
            Picasso.with(mContext).load(filteredList.get(position).getBevImage()).into(imageView);
        }

        return v;
    }

}

