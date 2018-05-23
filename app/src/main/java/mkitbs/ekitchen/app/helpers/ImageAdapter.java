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
 * Created by verakovic on 24.11.2016.
 */

public class ImageAdapter extends BaseAdapter {

    private Context mContext;
    private int imageWidth;
    private List<Beverage> filteredList;
    private boolean imageAvailableVisibility = false;

    // Constructor
    public ImageAdapter(Context c, List<Beverage> filtered_list, int image_width,
                        boolean image_available_visibility) {
        this.mContext = c;
        this.filteredList = filtered_list;
        this.imageWidth	= image_width;
        this.imageAvailableVisibility = image_available_visibility;
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

        View view;

        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            view = layoutInflater.inflate(R.layout.grid_item, parent, false);
            view.setLayoutParams(new GridView.LayoutParams(imageWidth, imageWidth));
        } else {
            view = (View) convertView;
        }

        TextView title = (TextView) view.findViewById(R.id.beverageName);
        title.setText(filteredList.get(position).getBeverageName());

        ImageView imageView = (ImageView) view.findViewById(R.id.beverageImage);
        if (filteredList.get(position).getBevImage() != null && !filteredList.get(position).getBevImage().isEmpty()) {
            Picasso.with(mContext).load(filteredList.get(position).getBevImage()).into(imageView);
        }

        if (imageAvailableVisibility) {
            ImageView availableImage = (ImageView) view.findViewById(R.id.availableImage);
            availableImage.setVisibility(View.VISIBLE);
            if (filteredList.get(position).isAvailable() == 1) {
                availableImage.setImageResource(R.drawable.ok);
            } else {
                availableImage.setImageResource(R.drawable.cancel);
            }
        }

        return view;
    }

}
