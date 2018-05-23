package mkitbs.ekitchen.app.kitchen;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import mkitbs.ekitchen.app.R;
import mkitbs.ekitchen.app.entities.OrderItem;
import mkitbs.ekitchen.app.helpers.BeverageListAdapter;
import mkitbs.ekitchen.app.helpers.DatabaseHandler;
import mkitbs.ekitchen.app.helpers.StatusButton;

/**
 * A fragment representing a single KitchenOrder detail screen.
 * This fragment is either contained in a {@link KitchenOrderListActivity}
 * in two-pane mode (on tablets) or a {@link KitchenOrderDetailActivity}
 * on handsets.
 */
public class KitchenOrderDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    private String orderHeaderId = "";
    private String orderStatus = "";
    private String orderDate = "";
    private String orderLocation = "";
    private String orderUser = "";
    private String waiterName = "";
    private String orderNotice = "";
    private int kitchenId = 0;
    private DatabaseHandler databaseHandler;
    private StatusButton statusButton;

    // Define the listener of the interface type
    // listener is the activity itself
    private OnStatusButtonListener listener;

    // Define the events that the fragment will use to communicate
    public interface OnStatusButtonListener {
        public void onStatusChanged(String status, String fragmentId);
    }
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public KitchenOrderDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseHandler = new DatabaseHandler(getActivity());

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            orderHeaderId = getArguments().getString(ARG_ITEM_ID);
            orderStatus = getArguments().getString("order_status");
            orderDate = getArguments().getString("order_date");
            orderLocation = getArguments().getString("order_location");
            orderUser = getArguments().getString("order_user");
            waiterName = getArguments().getString("waiter_name");
            orderNotice = getArguments().getString("order_notice");
            kitchenId = getArguments().getInt("kitchen_id");

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(orderHeaderId);
            }


        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.kitchenorder_detail, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.itemsList);
        List<OrderItem> orderItemList;
        orderItemList = databaseHandler.getAllOrderItemsForHeaderId(Integer.valueOf(orderHeaderId));

        BeverageListAdapter listAdapter = new BeverageListAdapter(getActivity(), orderItemList, kitchenId);

        if (!orderHeaderId.isEmpty()) {
            ((TextView) rootView.findViewById(R.id.textDetailDateTime))
                    .setText(orderDate);
            ((TextView) rootView.findViewById(R.id.textDetailLocation))
                    .setText(orderLocation);
            ((TextView) rootView.findViewById(R.id.textDetailUserCustomer))
                    .setText(getActivity().getResources().getString(R.string.kitchenorder_username) + " " + orderUser);
            ((TextView) rootView.findViewById(R.id.textDetailUserWaiter))
                    .setText(getActivity().getResources().getString(R.string.kitchenorder_waiter) + " " + waiterName);
            ((TextView) rootView.findViewById(R.id.textDetailNotice))
                    .setText(getActivity().getResources().getString(R.string.send_dialog_comment) + " " + orderNotice);
            listView.setAdapter(listAdapter);
        }

        TextView statusText = (TextView) rootView.findViewById(R.id.statusToggleText);
        statusButton = (StatusButton) rootView.findViewById(R.id.statusToggleButton);
        switch (orderStatus) {
            case "N":
                statusButton.setState(StatusButton.StatusEnum.NEW);
                statusText.setText(getResources().getString(R.string.status_new));
                statusText.setTextColor(getResources().getColor(R.color.md_red_500));
                break;
            case "P":
                statusButton.setState(StatusButton.StatusEnum.TAKEN);
                statusText.setText(getResources().getString(R.string.status_taken));
                statusText.setTextColor(getResources().getColor(R.color.md_blue_500));
                break;
            case "I":
                statusButton.setState(StatusButton.StatusEnum.DELIVERED);
                statusText.setText(getResources().getString(R.string.status_delivered));
                statusText.setTextColor(getResources().getColor(R.color.md_green_500));
                break;
            case "R":
                statusButton.setState(StatusButton.StatusEnum.CLEAR);
                statusText.setText(getResources().getString(R.string.status_for_cleanup));
                statusText.setTextColor(getResources().getColor(R.color.orangeDark));
                break;
            case "Z":
                statusButton.setState(StatusButton.StatusEnum.CLOSED);
                statusText.setText(getResources().getString(R.string.status_closed));
                statusText.setTextColor(getResources().getColor(R.color.almost_black));
                break;
        }

        statusButton.setStatusListener(new StatusButton.StatusListener() {
            @Override
            public void onNew() {
                listener.onStatusChanged(statusButton.getState().toString(), orderHeaderId);
            }

            @Override
            public void onTaken() {
                listener.onStatusChanged(statusButton.getState().toString(), orderHeaderId);
            }

            @Override
            public void onDelivered() {
                listener.onStatusChanged(statusButton.getState().toString(), orderHeaderId);
            }
            @Override
            public void onClear() {
                listener.onStatusChanged(statusButton.getState().toString(), orderHeaderId);
            }
            @Override
            public void onClosed() {
                listener.onStatusChanged(statusButton.getState().toString(), orderHeaderId);
            }
        });

        return rootView;
    }

    // Store the listener (activity) that will have events fired once the fragment is attached
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity;
        if (context instanceof Activity){
            activity = (Activity) context;
            if (activity instanceof OnStatusButtonListener) {
                listener = (OnStatusButtonListener) activity;
            } else {
                throw new ClassCastException(activity.toString()
                        + " must implement MyListFragment.OnItemSelectedListener");
            }
        }

    }


}
