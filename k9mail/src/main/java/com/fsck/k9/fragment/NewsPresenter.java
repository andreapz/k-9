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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import com.fsck.k9.K9;

import com.fsck.k9.R;
import com.fsck.k9.activity.INavigationDrawerActivityListener;
import com.fsck.k9.view.ViewSwitcher;


import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by thomascastangia on 02/01/17.
 */

@Singleton
public class NewsPresenter  implements NewsFragment.NewsFragmentListener, ViewSwitcher.OnSwitchCompleteListener {

    private final Context mContext;
    private static final String ARG_HOME = "HOME";
    private Intent mIntent;
    private ViewSwitcher mViewSwitcher;
    private LayoutInflater mInflater;
    private static final String STATE_DISPLAY_MODE = "displayMode";
    private final INavigationDrawerActivityListener mListener;
    private ActionBar mActionBar;
    private NewsFragment mNewsViewFragment;
    private NewsFragment mNewsDetailFragment;
    private DisplayMode mDisplayMode;
    private String mDefaultHomePage;
    private ProgressBar mActionBarProgress;


    public enum DisplayMode {
        NEWS_VIEW,
        NEWS_DETAIL,
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
        mViewSwitcher = (ViewSwitcher) container.findViewById(R.id.container);
        mViewSwitcher.setFirstInAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_in_left));
        mViewSwitcher.setFirstOutAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_out_right));
        mViewSwitcher.setSecondInAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_in_right));
        mViewSwitcher.setSecondOutAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_out_left));
        mViewSwitcher.setOnSwitchCompleteListener(this);
        mDefaultHomePage = home;
        initializeActionBar();
        findFragments();
        initializeDisplayMode(savedInstanceState);
        initializeFragments(mDefaultHomePage);

    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(STATE_DISPLAY_MODE, mDisplayMode);
    }

    private void findFragments() {
        FragmentManager fragmentManager = ((Activity)mContext).getFragmentManager();
        mNewsViewFragment = (NewsFragment) fragmentManager.findFragmentById(R.id.news_view_container);
        mNewsDetailFragment = (NewsFragment) fragmentManager.findFragmentById(R.id.news_detail_container);
    }


    /**
     * Create fragment instances if necessary.
     *
     */
    private void initializeFragments(String home) {
        FragmentManager fragmentManager = ((Activity)mContext).getFragmentManager();

        boolean hasNewsFragment = (mNewsViewFragment != null);

        if (!hasNewsFragment) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            mNewsViewFragment = NewsFragment.newInstance(home);
            ft.add(R.id.news_view_container, mNewsViewFragment);
            ft.commit();

        }
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


    public void showNews() {
        mDisplayMode = DisplayMode.NEWS_VIEW;
        mViewSwitcher.showFirstView();

    }

    private void removeNewsFragment() {
        FragmentTransaction ft = ((Activity)mContext).getFragmentManager().beginTransaction();
        ft.remove(mNewsViewFragment);
        mNewsViewFragment = null;
        ft.commit();
    }

    private void removeDetailFragment() {
        FragmentTransaction ft = ((Activity)mContext).getFragmentManager().beginTransaction();
        ft.remove(mNewsDetailFragment);
        mNewsDetailFragment = null;
        ft.commit();
    }

    private void initializeActionBar() {
        mActionBar = ((AppCompatActivity)mContext).getSupportActionBar();

        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setCustomView(R.layout.actionbar_custom);

        View customView = mActionBar.getCustomView();
        mActionBarProgress = (ProgressBar) customView.findViewById(R.id.actionbar_progress);



        mActionBar.setDisplayHomeAsUpEnabled(true);
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
        showNews();


    }

    @Override
    public void enableActionBarProgress(boolean enable) {
        if(enable){
            mActionBarProgress.setVisibility(ProgressBar.VISIBLE);
        }else{
            mActionBarProgress.setVisibility(ProgressBar.GONE);
        }

    }

    @Override
    public boolean isDetailStatus() {
        return mDisplayMode == DisplayMode.NEWS_DETAIL;
    }



    @Override
    public void detailPageLoad(String url) {

        NewsFragment fragment = NewsFragment.newInstance(url);
        FragmentTransaction ft = ((Activity)mContext).getFragmentManager().beginTransaction();
        ft.replace(R.id.news_detail_container, fragment);
        mNewsDetailFragment = fragment;
        ft.commit();
        mDisplayMode = DisplayMode.NEWS_DETAIL;
        mViewSwitcher.showSecondView();


    }

    public DisplayMode getDisplayMode() {
        return mDisplayMode;
    }

    @Override
    public void onSwitchComplete(int displayedChild) {
        if (displayedChild == 0) {
            removeDetailFragment();
            setActionBarToggle();
        }else{
            setActionBarUp();
        }

    }

    @Override
    public void goBack() {
        FragmentManager fragmentManager = ((Activity)mContext).getFragmentManager();
        if (mDisplayMode == DisplayMode.NEWS_DETAIL) {
            showNews();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home: {
                goBack();
                return true;
            }
            default: {
                return true;
            }
         }


    }
    @Override
    public void setActionBarUp() {
        if(mContext instanceof INavigationDrawerActivityListener) {
            ((INavigationDrawerActivityListener) mContext).setDrawerEnable(false);
        }
    }

    @Override
    public void setActionBarToggle() {
        if(mContext instanceof INavigationDrawerActivityListener) {
            ((INavigationDrawerActivityListener) mContext).setDrawerEnable(true);
        }
    }


}
