package mkitbs.ekitchen.app.kitchen;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.Toast;

import mkitbs.ekitchen.app.R;
import mkitbs.ekitchen.app.entities.KitchenOrderHeader;

/**
 * An activity representing a single KitchenOrder detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link KitchenOrderListActivity}.
 */
public class KitchenOrderDetailActivity extends AppCompatActivity implements KitchenOrderDetailFragment.OnStatusButtonListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kitchenorder_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

       // FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
       // fab.setOnClickListener(new View.OnClickListener() {
       //     @Override
       //     public void onClick(View view) {
       //         Snackbar.make(view, "Replace with your own detail action", Snackbar.LENGTH_LONG)
      //                  .setAction("Action", null).show();
       //     }
      //  });

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(KitchenOrderDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(KitchenOrderDetailFragment.ARG_ITEM_ID));
            KitchenOrderDetailFragment fragment = new KitchenOrderDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.kitchenorder_detail_container, fragment)
                    .commit();
        }
    }


    // Now we can define the action to take in the activity when the fragment event fires
    @Override
    public void onStatusChanged(String status, String fragmentId) {
      /**  int position = 0;
        boolean isCanceled = false;
        for (int i = 0; i <  orderHeaderList.size(); i++) {
            KitchenOrderHeader kitchenOrderHeader = orderHeaderList.get(i);
            if (String.valueOf(kitchenOrderHeader.getOrderHeaderId()).equals(fragmentId)) {
                if (status.equals("NEW")) {
                    orderHeaderList.get(i).setStatus("N");
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.status_new), Toast.LENGTH_LONG).show();
                } else if (status.equals("TAKEN")) {
                    orderHeaderList.get(i).setStatus("P");
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.status_taken), Toast.LENGTH_LONG).show();
                } else if (status.equals("DELIVERED")) {
                    orderHeaderList.get(i).setStatus("I");
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.status_delivered), Toast.LENGTH_LONG).show();
                    isCanceled = true;

                } else {
                    orderHeaderList.get(i).setStatus("O");
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.status_canceled), Toast.LENGTH_LONG).show();
                    isCanceled = true;

                }
                position = i;
                break;
            }
        }
        if (isCanceled) {
            //kitchenOrderAdapter.notifyItemRemoved(position);
            final int deletePosition = position;
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(KitchenOrderDetailActivity.this);
            builder.setMessage(R.string.delete_dialog_msg)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            orderHeaderList.remove(deletePosition);
                            kitchenOrderAdapter.notifyItemRemoved(deletePosition);
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                            kitchenOrderAdapter.notifyItemChanged(deletePosition);
                        }
                    });
            // create alert dialog
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        } else {
            kitchenOrderAdapter.notifyItemChanged(position);
        }  **/
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo(new Intent(this, KitchenOrderListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
