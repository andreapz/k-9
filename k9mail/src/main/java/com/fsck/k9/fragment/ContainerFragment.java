package com.fsck.k9.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.fsck.k9.R;
import com.fsck.k9.activity.NavigationDrawerActivity;
import com.fsck.k9.listener.BottomNavigationViewListener;
import com.fsck.k9.listener.ContentFragmentListener;

/**
 * Created by Annalisa Sini on 10/11/2016.
 */

public class ContainerFragment extends Fragment implements ContentFragmentListener {

    protected BottomNavigationView mBottomNav;
    protected int mSelectedItem;
    protected BottomNavigationViewListener mBottomNavigationViewListener;

    protected BottomNavigationView.OnNavigationItemSelectedListener mBottomNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {

            switch (item.getItemId()) {
                case R.id.menu_mail:
                    mSelectedItem = NavigationDrawerActivity.MAIL_TAB_SELECTED;
                    mBottomNavigationViewListener.onMailTabClicked();
                    break;
                case R.id.menu_news:
                    mSelectedItem = NavigationDrawerActivity.NEWS_TAB_SELECTED;
                    mBottomNavigationViewListener.onNewsTabClicked();
                    break;
                case R.id.menu_video:
                    mSelectedItem = NavigationDrawerActivity.VIDEO_TAB_SELECTED;
                    mBottomNavigationViewListener.onVideoTabClicked();
                    break;
                case R.id.menu_offers:
                    mSelectedItem = NavigationDrawerActivity.OFFERS_TAB_SELECTED;
                    mBottomNavigationViewListener.onOffersTabClicked();
                    break;
            }
            setSelected(mSelectedItem);
            return true;
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        if(savedInstanceState == null) {
            if(getArguments() != null) {
                mSelectedItem = getArguments().getInt(NavigationDrawerActivity.SELECTED_TAB, NavigationDrawerActivity.DEFAULT_SELECTED_TAB);
            }
        }
    }

    // FIXME onAttach deprecated
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if(activity instanceof BottomNavigationViewListener) {
            mBottomNavigationViewListener = (BottomNavigationViewListener) activity;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_bottom_navigation_view, container, false);

        mBottomNav = (BottomNavigationView) view.findViewById(R.id.bottom_navigation);
        mBottomNav.setOnNavigationItemSelectedListener(mBottomNavigationItemSelectedListener);
        if(savedInstanceState == null) {
            View menuItem = mBottomNav.findViewById(mBottomNav.getMenu().getItem(mSelectedItem).getItemId());
            menuItem.performClick();
        } else {
            // selected item not automatically saved after rotation
            setSelected(mSelectedItem);
        }

        return view;
    }

    private void showFragment(String fragmentName, Bundle args){

        if(getActivity() != null) {
            FragmentManager fragmentManager = getActivity().getFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();

            Fragment fragment = Fragment.instantiate(getActivity(), fragmentName, args);
            ft.replace(R.id.inner_fragment, fragment);

            ft.commit();
        }
    }

    private void setSelected(int position) {
        for(int i=0; i<mBottomNav.getMenu().size(); i++) {
            if(i == position) {
                mBottomNav.getMenu().getItem(i).setChecked(true);
            } else {
                mBottomNav.getMenu().getItem(i).setChecked(false);
            }
        }
    }

    @Override
    public void showContentFragment(String fragmentName, Bundle bundle) {
        showFragment(fragmentName, bundle);
    }
}
