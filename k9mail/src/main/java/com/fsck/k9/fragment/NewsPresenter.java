package com.fsck.k9.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;


import com.fsck.k9.K9;

import com.fsck.k9.R;
import com.fsck.k9.activity.INavigationDrawerActivityListener;


import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by thomascastangia on 02/01/17.
 */
@Singleton
public class NewsPresenter  implements NewsFragment.NewsFragmentListener {

    private final Context mContext;
    private Intent mIntent;
    private LayoutInflater mInflater;
    private static final String STATE_DISPLAY_MODE = "displayMode";

    private final INavigationDrawerActivityListener mListener;
//    private ViewSwitcher mViewSwitcher;
    private ActionBar mActionBar;
    private NewsFragment mNewsViewFragment;
    private ViewGroup mNewsViewContainer;
    private View mNewsViewPlaceHolder;

    private DisplayMode mDisplayMode;
    private String mDefaultHomePage;

    private int mSelectedTab;
    private ProgressBar mActionBarProgress;

    public DisplayMode getDisplayMode() {
        return mDisplayMode;
    }

    @Override
    public void enableActionBarProgress(boolean enable) {
        if(enable){
            mActionBarProgress.setVisibility(ProgressBar.VISIBLE);
        }else{
            mActionBarProgress.setVisibility(ProgressBar.GONE);
        }

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
    public void onCreateView(LayoutInflater inflater, Bundle savedInstanceState, String home) {
        mInflater = inflater;
        FrameLayout container = mListener.getContainer();
        mInflater.inflate(R.layout.fragment_news, container, true);
        mDefaultHomePage = home;


        initializeActionBar();

        findFragments();
        initializeDisplayMode(savedInstanceState);
        initializeLayout();
        initializeFragments(mDefaultHomePage);


    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(STATE_DISPLAY_MODE, mDisplayMode);

    }

    private void findFragments() {
        FragmentManager fragmentManager = ((Activity)mContext).getFragmentManager();

        mNewsViewFragment = (NewsFragment) fragmentManager.findFragmentById(R.id.news_view_container);
    }

    private void initializeLayout() {
        mNewsViewContainer = (ViewGroup) ((Activity)mContext).findViewById(R.id.news_view_container);

        mNewsViewPlaceHolder = mInflater.inflate(R.layout.empty_message_view, mNewsViewContainer, false);
    }

    private void initializeDisplayMode(Bundle savedInstanceState) {
        if (useSplitView()) {
            mDisplayMode = DisplayMode.SPLIT_VIEW;
            return;
        }

        if (savedInstanceState != null) {
            DisplayMode savedDisplayMode =
                    (DisplayMode) savedInstanceState.getSerializable(STATE_DISPLAY_MODE);
            if (savedDisplayMode != DisplayMode.SPLIT_VIEW) {
                mDisplayMode = savedDisplayMode;
                return;
            }
        }


        mDisplayMode = DisplayMode.NEWS_VIEW;

    }

    private void initializeActionBar() {
        mActionBar = ((AppCompatActivity)mContext).getSupportActionBar();

        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setCustomView(R.layout.actionbar_custom);

        View customView = mActionBar.getCustomView();
        mActionBarProgress = (ProgressBar) customView.findViewById(R.id.actionbar_progress);



        mActionBar.setDisplayHomeAsUpEnabled(true);
    }


    /**
     * Create fragment instances if necessary.
     *
     * @see #findFragments()
     */
    private void initializeFragments(String home) {
        FragmentManager fragmentManager = ((Activity)mContext).getFragmentManager();
//        fragmentManager.addOnBackStackChangedListener(this);

        boolean hasNewsFragment = (mNewsViewFragment != null);

        if (!hasNewsFragment) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            mNewsViewFragment = NewsFragment.newInstance(home);
            ft.add(R.id.news_view_container, mNewsViewFragment);
            ft.commit();

        }


    }
    private boolean useSplitView() {
        K9.SplitViewMode splitViewMode = K9.getSplitViewMode();
        int orientation = mContext.getResources().getConfiguration().orientation;

        return (splitViewMode == K9.SplitViewMode.ALWAYS ||
                (splitViewMode == K9.SplitViewMode.WHEN_IN_LANDSCAPE &&
                        orientation == Configuration.ORIENTATION_LANDSCAPE));
    }


    public void openSection(String url) {
        mDisplayMode = DisplayMode.NEWS_VIEW;
        if(mNewsViewFragment != null){
            mNewsViewFragment.updateUrl(url);
        }

    }

}
