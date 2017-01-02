package com.fsck.k9.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import com.fsck.k9.K9;
import com.fsck.k9.Preferences;
import com.fsck.k9.R;

import com.fsck.k9.activity.misc.SwipeGestureDetector;
import com.fsck.k9.view.ViewSwitcher;


import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by thomascastangia on 02/01/17.
 */
@Singleton
public class NewsPresenter implements SwipeGestureDetector.OnSwipeGestureListener, ViewSwitcher.OnSwitchCompleteListener {

    private final Context mContext;
    private Intent mIntent;
    private LayoutInflater mInflater;
    private ViewSwitcher mViewSwitcher;
    private ActionBar mActionBar;

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
    public NewsPresenter(Context context, Intent intent) {
        mContext = context;
        mIntent = intent;
    }

    public void setIntent(Intent intent) {
        mIntent = intent;
    }
    @Nullable
    public void onCreateView(LayoutInflater inflater, Bundle savedInstanceState) {
        mInflater = inflater;

        if (!useSplitView()) {
            mViewSwitcher = (ViewSwitcher) ((Activity)mContext).findViewById(R.id.container);
            mViewSwitcher.setFirstInAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_in_left));
            mViewSwitcher.setFirstOutAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_out_right));
            mViewSwitcher.setSecondInAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_in_right));
            mViewSwitcher.setSecondOutAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_out_left));
            mViewSwitcher.setOnSwitchCompleteListener(this);
        }

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
    @Override
    public void onSwipeRightToLeft(MotionEvent e1, MotionEvent e2) {

    }

    @Override
    public void onSwipeLeftToRight(MotionEvent e1, MotionEvent e2) {

    }

    @Override
    public void onSwitchComplete(int displayedChild) {

    }
}
