package org.droidplanner.services.android.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.droidplanner.services.android.R;

/**
 * Provide a view pager to toggle between the list of active apps, and the recommended list of apps to download.
 */
public class ViewCategoryFragment extends Fragment {

    private static final String EXTRA_SELECTED_CATEGORY_INDEX = "extra_selected_category_index";

    private ViewPager viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_connections, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager = (ViewPager) view.findViewById(R.id.connections_view_pager);
        viewPager.setAdapter(new ConnectionCategoryAdapter(getChildFragmentManager()));

        int categoryIndex = 1;
        if (savedInstanceState != null) {
            categoryIndex = savedInstanceState.getInt(EXTRA_SELECTED_CATEGORY_INDEX, categoryIndex);
        }

        viewPager.setCurrentItem(categoryIndex);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_SELECTED_CATEGORY_INDEX, viewPager.getCurrentItem());
    }

    private static class ConnectionCategoryAdapter extends FragmentPagerAdapter {

        public ConnectionCategoryAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new AppConnectionsFragment();

                case 1:
                default:
                    return new RecommendedAppsFragment();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position){
            switch(position){
                case 0:
                    return "Active";

                case 1:
                default:
                    return "Recommended";
            }
        }
    }
}
