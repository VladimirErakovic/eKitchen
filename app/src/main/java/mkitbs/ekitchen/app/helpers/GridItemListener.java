package mkitbs.ekitchen.app.helpers;

import android.view.View;

/**
 * Created by verakovic on 04.01.2017.
 */

public interface GridItemListener {

    void btnRemoveOnClick(View v, int position);
    void btnAddOnClick(View v, int position);

}
