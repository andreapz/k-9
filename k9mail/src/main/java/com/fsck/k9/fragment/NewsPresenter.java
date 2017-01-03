package com.fsck.k9.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;


import com.fsck.k9.K9;

import com.fsck.k9.activity.INavigationDrawerActivityListener;
import com.fsck.k9.view.ViewSwitcher;


import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by thomascastangia on 02/01/17.
 */
@Singleton
public class NewsPresenter  {

    private final Context mContext;
    private Intent mIntent;
    private LayoutInflater mInflater;
    private final INavigationDrawerActivityListener mListener;
    private ViewSwitcher mViewSwitcher;
    private ActionBar mActionBar;
    private NewsFragment mNewsFragment;

    private DisplayMode mDisplayMode;

    private int mSelectedTab;

    public DisplayMode getDisplayMode() {
        return mDisplayMode;
    }




    public enum DisplayMode {
        NEWS_VIEW,
        SPLIT_VIEW
    }

    @Inject
    public NewsPresenter(INavigationDrawerActivityListener listener, Intent intent) {
        mListener = listener;
        mContext = listener.getActivity();
        mIntent = intent;
    }

    public void setIntent(Intent intent) {
        mIntent = intent;
    }
    @Nullable
    public void onCreateView(LayoutInflater inflater, Bundle savedInstanceState) {
        mInflater = inflater;



//        initializeActionBar();
//        initializeNavigationDrawer();

//        findFragments();
//        initializeDisplayMode(savedInstanceState);
//        initializeLayout();
//        initializeFragments();
        displayViews();
        //setupGestureDetector(this);
    }







    private boolean useSplitView() {
        K9.SplitViewMode splitViewMode = K9.getSplitViewMode();
        int orientation = mContext.getResources().getConfiguration().orientation;

        return (splitViewMode == K9.SplitViewMode.ALWAYS ||
                (splitViewMode == K9.SplitViewMode.WHEN_IN_LANDSCAPE &&
                        orientation == Configuration.ORIENTATION_LANDSCAPE));
    }
    private void displayViews() {
        switch (mDisplayMode) {

            case NEWS_VIEW: {

                break;
            }
            case SPLIT_VIEW: {

                break;
            }
        }
    }

}
