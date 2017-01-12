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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fsck.k9.Account;
import com.fsck.k9.K9;

import com.fsck.k9.Preferences;
import com.fsck.k9.R;
import com.fsck.k9.activity.FolderInfoHolder;
import com.fsck.k9.activity.INavigationDrawerActivityListener;
import com.fsck.k9.adapter.BaseNavDrawerMenuAdapter;
import com.fsck.k9.adapter.MailNavDrawerClickListener;
import com.fsck.k9.adapter.NavDrawerClickListener;
import com.fsck.k9.adapter.NavDrawerMenuAdapter;
import com.fsck.k9.model.NavDrawerMenuItem;
import com.fsck.k9.search.LocalSearch;
import com.fsck.k9.view.ViewSwitcher;
import com.fsck.k9.view.holder.HeaderViewHolder;
import com.fsck.k9.view.holder.ItemViewHolder;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private TextView mActionBarTitle;
    private NewsFragment mNewsViewFragment;
    private NewsFragment mNewsDetailFragment;
    private DisplayMode mDisplayMode;
    private String mDefaultHomePage;
    private ProgressBar mActionBarProgress;
    private NewsAdapter mNewsAdapter;
    List<NavDrawerMenuItem> mNewsTabMenuItems;


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
    public void onCreateView(LayoutInflater inflater, Bundle savedInstanceState) {
        mInflater = inflater;
        FrameLayout container = mListener.getContainer();
        mInflater.inflate(R.layout.fragment_news, container, true);
        mViewSwitcher = (ViewSwitcher) container.findViewById(R.id.container);
        mViewSwitcher.setFirstInAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_in_left));
        mViewSwitcher.setFirstOutAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_out_right));
        mViewSwitcher.setSecondInAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_in_right));
        mViewSwitcher.setSecondOutAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_out_left));
        mViewSwitcher.setOnSwitchCompleteListener(this);
        initializeActionBar();
        findFragments();
        initializeDisplayMode(savedInstanceState);

        // news, video and offers
        String meObjectJsonString = getJsonString(mContext.getResources().openRawResource(R.raw.me_object));
        mNewsTabMenuItems = NavDrawerMenuItem.getMenuList(meObjectJsonString, "news");
        mDefaultHomePage = mNewsTabMenuItems.get(0).getUrl();
        mNewsAdapter = new NewsAdapter(mNewsTabMenuItems,mContext);
        mListener.setDrawerListAdapter(mNewsAdapter);

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
        mActionBarTitle = (TextView) customView.findViewById(R.id.actionbar_title_first);
        mActionBarProgress = (ProgressBar) customView.findViewById(R.id.actionbar_progress);



        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    public void setActionBarTitle(String title) {
        mActionBarTitle.setText(title);
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

    private String getJsonString(InputStream inputStream) {
        try {
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            String jsonString = new String(buffer, "UTF-8");
            return jsonString;

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public class NewsAdapter extends BaseNavDrawerMenuAdapter {

        // data set
        private List<NavDrawerMenuItem> mItems;
        Context mContext;
        private List<NavDrawerMenuItem> mVisibleItems = new ArrayList<>();
        private HashMap<String, Boolean> mItemsOpenStatus = new HashMap<>();
        private HashMap<String, Integer> mItemsDepth = new HashMap<>();

        NavDrawerClickListener mClickListener = new NavDrawerClickListener() {
            @Override
            public void onSettingsClick() {
                super.onSettingsClick();
                Preferences prefs = Preferences.getPreferences(mContext.getApplicationContext());
                List<Account> accounts = prefs.getAccounts();
                mListener.showDialogSettings(accounts.get(0));
            }
            @Override
            public void onMenuClick(NavDrawerMenuItem item) {
                super.onMenuClick(item);
                mListener.closeDrawer();
                openSection(item.getUrl());

            }

        };

        public NewsAdapter(List<NavDrawerMenuItem> data, Context context) {

            mItems = data;
            mVisibleItems.addAll(data);
            mContext = context;
            // init item status hash map. Elements all collapsed
            setItemsStatus(mVisibleItems, false);
            // init depth level
            setItemsDepth(mVisibleItems, -1);
        }

        private void setItemsStatus(List<NavDrawerMenuItem> items, boolean status) {
            for(NavDrawerMenuItem item : items) {
                // items initially collapsed
                mItemsOpenStatus.put(item.getSectionId(), status);
            }
        }

        private void setItemsDepth(List<NavDrawerMenuItem> items, int upperDepth) {
            for(int i=0; i<items.size(); i++) {
                NavDrawerMenuItem item = items.get(i);
                if(!mItemsDepth.containsKey(item.getSectionId())) {
                    mItemsDepth.put(item.getSectionId(), upperDepth+1);
                }
            }
        }

        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if(holder instanceof HeaderViewHolder) {
                // TODO
                final HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                headerViewHolder.mAccountTv.setVisibility(View.VISIBLE);
                headerViewHolder.mAccountTv.setText("Name Surname");

                headerViewHolder.mSettingsIv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mClickListener.onSettingsClick();
                    }
                });
            } else if (holder instanceof ItemViewHolder) {
                final NavDrawerMenuItem item = getItem(position);
                ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

                //Icon
                if(item.getIconUrl() != null) {
                    itemViewHolder.mItemIconIv.setVisibility(View.VISIBLE);
                    Glide.with(mContext)
                            .load(item.getIconUrl())
                            .into(itemViewHolder.mItemIconIv);

                } else {
                    itemViewHolder.mItemIconIv.setVisibility(View.GONE);
                }

                // add additionally left margin depending on depth
                if(itemViewHolder.mItemContainerRl.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) itemViewHolder.mItemContainerRl.getLayoutParams();
                    int marginLeft = (int) (getItemDepth(position)*mContext.getResources().getDimension(R.dimen.margin_standard_16dp));
                    p.setMargins(marginLeft, 0, 0, 0);
                    itemViewHolder.mItemContainerRl.requestLayout();
                }

                // toggle icon for expandable items
                if(hasChildren(position)) {
                    itemViewHolder.mItemToggleIv.setVisibility(View.VISIBLE);
                    // expandable item opened
                    if(isItemExpanded(position)) {
                        itemViewHolder.mItemToggleIv.setImageResource(getCollapseIconResource());
                    } else {
                        itemViewHolder.mItemToggleIv.setImageResource(getExpandIconResource());
                    }
                } else {
                    itemViewHolder.mItemToggleIv.setVisibility(View.GONE);
                }

                if(item.getTitle() != null) {
                    itemViewHolder.mItemTitleTv.setText(item.getTitle());
                }

                //click listener
                itemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(hasChildren(position)) {
                            //update status for expandable item (if opened set to close, and vice versa)
                            boolean oldStatus = isItemExpanded(position);
                            boolean newStatus = !oldStatus;
                            mItemsOpenStatus.put(item.getSectionId(), newStatus);

                            // update data set for rendering
                            int childPosition = getFirstChildPosition(position);
                            if(newStatus) { // expandable item just opened
                                mVisibleItems.addAll(childPosition, item.getSections());
                                setItemsStatus(item.getSections(), false);
                                setItemsDepth(item.getSections(), getItemDepth(position));
                            } else { // expandable item just closed
                                // remove all elements with depth > item depth (consider nested children)
                                setItemsStatus(getItemsToHide(position), false);
                                mVisibleItems.removeAll(getItemsToHide(position));
                            }
                            notifyDataSetChanged();
                        } else {
                            mClickListener.onMenuClick(item);
                        }
                    }
                });
            }
        }

        @Override
        public int getChildrenCount(int position) {
            return getItem(position).getSections().size();
        }

        @Override
        public int getItemDepth(int position) {
            return mItemsDepth.get(getItem(position).getSectionId());
        }

        @Override
        public boolean isItemExpanded(int position) {
            return mItemsOpenStatus.get(getItem(position).getSectionId());
        }

        private int getFirstChildPosition(int position) {
            // without header
//        return position + 1;
            //with header
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            // without header
//        return ITEM;
            // with header
            return position == 0 ? HEADER : ITEM;
        }

        private NavDrawerMenuItem getItem(int position) {
            // without header
//        return mVisibleItems.get(position);
            // with header
            return mVisibleItems.get(position - 1);
        }

        @Override
        public int getItemCount() {
            // without header
//        return mVisibleItems.size();
            // with header
            return mVisibleItems.size() + 1;
        }

        private List<NavDrawerMenuItem> getItemsToHide(int position) {
            List<NavDrawerMenuItem> itemsToHide = new ArrayList<>();
            // without header
//        int firstItemPosition = 0;
            //with header
            int firstItemPosition = 1;
            for(int i=firstItemPosition; i<getItemCount(); i++) {
                if(getItemDepth(i) > getItemDepth(position)) {
                    itemsToHide.add(getItem(i));
                }
            }
            return itemsToHide;
        }
    }


}
