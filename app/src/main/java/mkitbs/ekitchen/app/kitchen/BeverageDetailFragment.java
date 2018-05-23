package mkitbs.ekitchen.app.kitchen;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;

import mkitbs.ekitchen.app.R;
import mkitbs.ekitchen.app.entities.Beverage;
import mkitbs.ekitchen.app.entities.Category;
import mkitbs.ekitchen.app.helpers.DatabaseHandler;
import mkitbs.ekitchen.app.helpers.ImageAdapter;

/**
 * A fragment representing a single Beverage detail screen.
 * This fragment is either contained in a {@link BeverageListActivity}
 * in two-pane mode (on tablets) or a {@link BeverageDetailActivity}
 * on handsets.
 */
public class BeverageDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    private static List<Category> categoriesList;
    private static List<Beverage> beveragesList;
    private int categoriesCounter = 0;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private int fragmentNumber = -1;
    private static boolean mode = false;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BeverageDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Activity activity = this.getActivity();
        DatabaseHandler databaseHandler = new DatabaseHandler(activity);

        categoriesList = databaseHandler.getAllCategories();
        categoriesCounter = categoriesList.size();

        if (getArguments() != null && getArguments().containsKey("mode")) {
            mode = getArguments().getBoolean("mode", false);
        }
        if (mode) {
            beveragesList = databaseHandler.getAllBeverages();
        } else {
            beveragesList = databaseHandler.getAllAvailableBeverages();
        }

        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
           appBarLayout.setTitle(activity.getResources().getString(R.string.title_kitchenorder_detail));
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.beverage_detail, container, false);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) rootView.findViewById(R.id.container);
        mViewPager.setOffscreenPageLimit(categoriesCounter - 1);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        if (fragmentNumber > -1) {
            mViewPager.setCurrentItem(fragmentNumber - 1);
        }

        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        return rootView;
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public static final int NUM_OF_COLUMNS = 3;

        public static final int GRID_PADDING = 5;

        // Define the listener of the interface type
        // listener is the activity itself
        private OnGridItemClickListener listener;
        private ImageAdapter imageAdapter;
        private DatabaseHandler databaseHandler;

        // Define the events that the fragment will use to communicate
        public interface OnGridItemClickListener {
            void onGridItemClick(int bevCatId, String bevName, boolean is_checked);
        }


        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final View rootView = inflater.inflate(R.layout.fragment_selection, container, false);

            databaseHandler = new DatabaseHandler(getActivity());

            final GridView gridView = (GridView) rootView.findViewById(R.id.grid_view);

            final List<Beverage> filteredList = new ArrayList<>();
            if (filteredList.isEmpty()) {
                for (int i = 0; i < beveragesList.size(); i++) {
                    if (Integer.valueOf(beveragesList.get(i).getBevCategoryId()).equals(getArguments().getInt(ARG_SECTION_NUMBER))) {
                        filteredList.add(beveragesList.get(i));
                    }
                }
            }

            rootView.post(new Runnable() {                          // treba videti da li je ovo bas najsrecnije resenje
                @Override
                public void run() {
                    int fragmentWidth = rootView.getMeasuredWidth();
                    int columnWidth = InitilizeGridLayout(getActivity(), gridView, fragmentWidth);
                    if (mode) {
                        imageAdapter = new ImageAdapter(getActivity(), filteredList, columnWidth, true);
                        gridView.setAdapter(imageAdapter);
                        gridView.setSelector(R.drawable.grid_view_selector);
                    } else {
                        imageAdapter = new ImageAdapter(getActivity(), filteredList, columnWidth, false);
                        gridView.setAdapter(imageAdapter);
                        gridView.setSelector(R.drawable.grid_view_selector);
                    }
                }
            });


            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    Beverage beverage = filteredList.get(position);
                    boolean isChecked = false;

                    if (mode) {
                        if (beverage.isAvailable() == 1) {
                            beverage.setAvailable(0);
                            isChecked = true;
                        } else {
                            beverage.setAvailable(1);
                            isChecked = false;
                        }
                    }

                    listener.onGridItemClick(beverage.getBeverageId(), beverage.getBeverageName(), isChecked);

                    if (mode) {
                        imageAdapter.notifyDataSetChanged();
                    }
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
                if (activity instanceof OnGridItemClickListener) {
                    listener = (OnGridItemClickListener) activity;
                } else {
                    throw new ClassCastException(activity.toString()
                            + " must implement MyListFragment.OnItemSelectedListener");
                }
            }

        }


        private static int InitilizeGridLayout(Context context, GridView gridView, int fragmentWidth) {
            int columnWidth = 0;
            Resources r = context.getResources();
            float padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    GRID_PADDING, r.getDisplayMetrics());

            columnWidth = (int) ((fragmentWidth - ((NUM_OF_COLUMNS + 1) * padding)) / NUM_OF_COLUMNS);

            gridView.setNumColumns(NUM_OF_COLUMNS);
            gridView.setColumnWidth(columnWidth);
            gridView.setStretchMode(GridView.NO_STRETCH);
            gridView.setPadding((int) padding, (int) padding, (int) padding,
                    (int) padding);
            gridView.setHorizontalSpacing((int) padding);
            gridView.setVerticalSpacing((int) padding);

            return columnWidth;
        }

    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(categoriesList.get(position).getCategoryId());   // position + 1
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return categoriesCounter;   //categoriesCounter
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (!categoriesList.isEmpty()) {
                return categoriesList.get(position).getCategoryName();
            }
            return null;
        }
    }

}
