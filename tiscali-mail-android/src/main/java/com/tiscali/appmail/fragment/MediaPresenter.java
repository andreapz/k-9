package com.tiscali.appmail.fragment;


import static com.tiscali.appmail.fragment.MediaPresenter.DisplayMode.MEDIA_VIEW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.bumptech.glide.Glide;
import com.tiscali.appmail.K9;
import com.tiscali.appmail.R;
import com.tiscali.appmail.activity.INavigationDrawerActivityListener;
import com.tiscali.appmail.activity.NavigationDrawerActivity;
import com.tiscali.appmail.adapter.BaseNavDrawerMenuAdapter;
import com.tiscali.appmail.adapter.TiscaliMenuClickListener;
import com.tiscali.appmail.analytics.LogManager;
import com.tiscali.appmail.api.ApiController;
import com.tiscali.appmail.api.model.MainConfig;
import com.tiscali.appmail.api.model.Me;
import com.tiscali.appmail.api.model.TiscaliMenuItem;
import com.tiscali.appmail.api.model.UserLogin;
import com.tiscali.appmail.presenter.PresenterLifeCycle;
import com.tiscali.appmail.service.TiscaliAppFirebaseMessagingService;
import com.tiscali.appmail.view.ViewSwitcher;
import com.tiscali.appmail.view.holder.HeaderViewHolder;
import com.tiscali.appmail.view.holder.ItemViewHolder;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by thomascastangia on 02/01/17.
 */

@Singleton
public abstract class MediaPresenter
        implements MediaFragment.MediaFragmentListener, ViewSwitcher.OnSwitchCompleteListener,
        PresenterLifeCycle, ApiController.ApiControllerInterface {

    private static final int HOME_POSITION_ADAPTER = 1;
    private static final int HOME_POSITION_PRESENTER = 0;
    public static final int MEDIA_PRESENTER_BROWSING = 5;
    public static final int MEDIA_PRESENTER_INFORMATION_SETTINGS = 6;
    public static final String DEFAULT_ACTIONBAR_TITLE = "Tiscali";
    private final Activity mContext;
    private static final String ARG_HOME = "HOME";
    private Intent mIntent;
    private ViewSwitcher mViewSwitcher;
    private LayoutInflater mInflater;
    private static final String NEWS_STATE_DISPLAY_MODE = "News_displayMode";
    private static final String NEWS_STATE_CURRENT_URL = "News_currentUrl";
    private final INavigationDrawerActivityListener mListener;
    private ActionBar mActionBar;
    private TextView mActionBarTitle;
    private MediaFragment mMediaViewFragment;
    private MediaFragment mMediaDetailFragment;
    private DisplayMode mDisplayMode;
    private String mCurrentPage;
    private String mMeJson;
    private int mTimeoutRefresh = 1000;
    private boolean mIsHomePage;
    private ProgressBar mActionBarProgress;
    private List<String> mWalledGarden = new ArrayList<>();
    private MediaAdapter mNewsAdapter = new MediaAdapter();
    private List<TiscaliMenuItem> mMenuItems = new ArrayList<>();

    @Inject
    public LogManager mLogManager;

    private boolean mStarted = false;
    private boolean isAttached = false;
    private boolean mIsExternalBrowsing = false;

    public enum DisplayMode {
        MEDIA_VIEW, MEDIA_DETAIL, SPLIT_VIEW
    }

    public enum Type {
        NEWS, VIDEO, OFFERS
    }

    public MediaPresenter(INavigationDrawerActivityListener listener, Intent intent) {
        mListener = listener;
        mContext = listener.getActivity();
        mIntent = intent;
    }

    public void setIntent(Intent intent) {
        mIntent = intent;
    }

    public abstract Type getType();

    @Nullable
    public void onCreateView() {

        mStarted = true;
        mInflater = mContext.getLayoutInflater();
        FrameLayout container = mListener.getContainer();
        mInflater.inflate(R.layout.fragment_news, container, true);
        mViewSwitcher = (ViewSwitcher) container.findViewById(R.id.container_news);
        mViewSwitcher
                .setFirstInAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_in_left));
        mViewSwitcher.setFirstOutAnimation(
                AnimationUtils.loadAnimation(mContext, R.anim.slide_out_right));
        mViewSwitcher.setSecondInAnimation(
                AnimationUtils.loadAnimation(mContext, R.anim.slide_in_right));
        mViewSwitcher.setSecondOutAnimation(
                AnimationUtils.loadAnimation(mContext, R.anim.slide_out_left));
        mViewSwitcher.setOnSwitchCompleteListener(this);

        mIsHomePage = true;

        ((NavigationDrawerActivity) mContext).getComponent().injectMediaPresenter(this);

        initializeActionBar();
        // findFragments();
        initializeDisplayMode();

        mListener.setDrawerListAdapter(mNewsAdapter);

        if (mCurrentPage != null) {
            initializeFragments(mCurrentPage);
        } else {
            if (mMenuItems.size() > 0) {
                mCurrentPage = mMenuItems.get(HOME_POSITION_PRESENTER).getUrl();
                initializeFragments(mCurrentPage);
            }
        }
        if (mIntent.getExtras() != null) {
            String extrasUrl = mIntent.getExtras()
                    .getString(TiscaliAppFirebaseMessagingService.NOTIFICATION_URL);
            startFromNotification(extrasUrl);
        }

        mLogManager.track(mContext.getResources()
                .getString(R.string.com_tiscali_appmail_fragment_MediaFragment_Home_News));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // outState.putSerializable(NEWS_STATE_DISPLAY_MODE, mDisplayMode);
        // outState.putString(NEWS_STATE_CURRENT_URL, mCurrentPage);
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (intent.getExtras() != null) {
            String extrasUrl = intent.getExtras()
                    .getString(TiscaliAppFirebaseMessagingService.NOTIFICATION_URL);
            startFromNotification(extrasUrl);
        }

    }

    private void startFromNotification(String extrasUrl) {

        if (extrasUrl != null) {
            for (int i = 0; i < mMenuItems.size(); i++) {
                TiscaliMenuItem item = mMenuItems.get(i);
                if (item.getUrl().equals(extrasUrl)) {
                    if (item.getUrl().equals(mMenuItems.get(0).getUrl())) {
                        openSection(extrasUrl, true);
                        return;
                    } else {
                        openSection(extrasUrl, false);
                        return;
                    }
                }
                if (item.getSections() != null && item.getSections().size() > 0) {
                    for (int j = 0; j < item.getSections().size(); j++) {
                        TiscaliMenuItem section = item.getSections().get(j);
                        if (section.getUrl().equals(extrasUrl)) {
                            openSection(extrasUrl, false);
                            return;
                        }
                    }
                }
            }
            detailPageLoad(extrasUrl);
        }
    }

    private void findFragments() {
        FragmentManager fragmentManager = mContext.getFragmentManager();
        mMediaViewFragment =
                (MediaFragment) fragmentManager.findFragmentById(R.id.news_view_container);
        mMediaDetailFragment =
                (MediaFragment) fragmentManager.findFragmentById(R.id.news_detail_container);
    }

    /**
     * Create fragment instances if necessary.
     */
    private void initializeFragments(String home) {
        FragmentManager fragmentManager = mContext.getFragmentManager();

        boolean hasNewsFragment = (mMediaViewFragment != null);
        if (hasNewsFragment) {
            mMediaViewFragment.setType(getType());
            mMediaViewFragment.setUrl(home);
            mMediaViewFragment.mWebView.setVisibility(View.VISIBLE);
        } else {
            mMediaViewFragment = MediaFragment.newInstance(home, getType(), true);
        }

        FragmentTransaction ft = fragmentManager.beginTransaction();

        if (isAttached) {
            ft.show(mMediaViewFragment);
        } else {
            ft.add(R.id.news_view_container, mMediaViewFragment);
            isAttached = true;
        }
        ft.commit();

        if (mDisplayMode.equals(MEDIA_VIEW)) {
            setActionBarToggle();
            showMedia();
        } else {
            setActionBarUp();
            detailPageLoad(home);
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        mContext.getMenuInflater().inflate(R.menu.news_menu_option, menu);
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    private void initializeDisplayMode() {
        if (useSplitView()) {
            mDisplayMode = DisplayMode.SPLIT_VIEW;
            return;
        }

        if (mCurrentPage == null) {
            mDisplayMode = MEDIA_VIEW;
        }
    }

    public void goBackOnHistory() {
        if (mMediaDetailFragment != null && mMediaDetailFragment.canGoBack()) {
            mMediaDetailFragment.goBackOnHistory();
        } else {
            showMedia();
        }
    }

    public void onActivityResult() {
        setExternalBrowsing(false);
    }

    public void showMedia() {
        mDisplayMode = MEDIA_VIEW;
        mViewSwitcher.showFirstView();
        if (mMediaViewFragment != null) {
            mMediaViewFragment.getTitle();
        }
    }

    private void removeFragment(Fragment fragment) {
        FragmentManager manager = mContext.getFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.remove(fragment);
        ft.commit();
        manager.popBackStackImmediate();
    }

    private void hideFragment(Fragment fragment) {
        FragmentManager manager = mContext.getFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.hide(fragment);
        ft.commit();
    }

    private void removeMediaFragment() {
        if (mMediaViewFragment != null) {
            mMediaViewFragment.mWebView.setVisibility(View.INVISIBLE);
            hideFragment(mMediaViewFragment);
        }
    }

    private void removeDetailFragment() {
        if (mMediaDetailFragment != null) {
            removeFragment(mMediaDetailFragment);
            mMediaDetailFragment = null;
        }
    }

    private void initializeActionBar() {
        mActionBar = ((AppCompatActivity) mContext).getSupportActionBar();

        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setCustomView(R.layout.actioncustombar_news);

        View customView = mActionBar.getCustomView();
        mActionBarTitle = (TextView) customView.findViewById(R.id.actionbar_title_first);
        mActionBarProgress = (ProgressBar) customView.findViewById(R.id.actionbar_progress);

        mActionBarTitle.setText(DEFAULT_ACTIONBAR_TITLE);

        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    public void setActionBarTitle(final String title) {
        Observable.empty().observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                        if (mActionBarTitle != null) {
                            mActionBarTitle.setText(title);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(Object o) {}
                });
    }

    private boolean useSplitView() {
        K9.SplitViewMode splitViewMode = K9.getSplitViewMode();
        int orientation = mContext.getResources().getConfiguration().orientation;

        return (splitViewMode == K9.SplitViewMode.ALWAYS
                || (splitViewMode == K9.SplitViewMode.WHEN_IN_LANDSCAPE
                        && orientation == Configuration.ORIENTATION_LANDSCAPE));
    }

    public void openSection(String url, boolean isHome) {
        showMedia();
        if (mMediaViewFragment != null) {
            mMediaViewFragment.updateUrl(url);
            mLogManager.track(getCurrentPageId());
        }
        mIsHomePage = isHome;
    }

    @Override
    public void enableActionBarProgress(final boolean enable) {
        Observable.empty().observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                        if (mActionBarProgress != null) {
                            if (enable) {
                                mActionBarProgress.setVisibility(ProgressBar.VISIBLE);
                            } else {
                                mActionBarProgress.setVisibility(ProgressBar.GONE);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(Object o) {}
                });

    }


    @Override
    public boolean isDetailStatus() {
        return mDisplayMode == DisplayMode.MEDIA_DETAIL;
    }

    @Override
    public boolean isHomePage() {
        if (mDisplayMode == DisplayMode.MEDIA_DETAIL) {
            return false;
        } else {
            return mIsHomePage;
        }
    }

    @Override
    public boolean isWalledGarden(String domain) {
        for (int i = 0; i < mWalledGarden.size(); i++) {
            String walled = mWalledGarden.get(i);
            if (domain.contains(walled)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setExternalBrowsing(boolean value) {
        mIsExternalBrowsing = value;
    }

    @Override
    public boolean isExternalBrowsing() {

        return mIsExternalBrowsing;
    }

    @Override
    public String getMeJSON() {
        return mMeJson;
    }

    @Override
    public int getRefreshTimeout() {
        return mTimeoutRefresh;
    }

    @Override
    public void setPageTitle(String title) {
        setActionBarTitle(title);
    }

    @Override
    public void setFavoriteSection(final String sectionId, final boolean value) {

        mListener.getApiController().sectionFave(sectionId, value, new Action1<UserLogin>() {
            @Override
            public void call(UserLogin userLogin) {
                String msgDialog = null;
                String sectionName = null;
                for (int i = 0; i < mMenuItems.size(); i++) {
                    if (mMenuItems.get(i).getSectionId().compareTo(sectionId) == 0) {
                        sectionName = mMenuItems.get(i).getTitle();
                    }
                }
                if (value) {
                    msgDialog = mContext.getResources().getString(R.string.dialog_fave_positive_msg,
                            sectionName);
                } else {
                    msgDialog = mContext.getResources().getString(R.string.dialog_fave_negative_msg,
                            sectionName);
                }
                new AlertDialog.Builder(mContext).setMessage(msgDialog)
                        .setPositiveButton(android.R.string.yes,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // nop
                                    }
                                })
                        .setIcon(android.R.drawable.ic_dialog_alert).show();
            }
        }, new Action1<UserLogin>() {
            @Override
            public void call(UserLogin userLogin) {

                if (mIsHomePage && mMediaViewFragment != null) {
                    mMediaViewFragment.setPageStatus();
                }

            }
        });
    }

    @Override
    public void detailPageLoad(String url) {
        mCurrentPage = url;
        if (mMediaDetailFragment == null) {
            MediaFragment fragment = MediaFragment.newInstance(url, getType(), false);
            FragmentTransaction ft = mContext.getFragmentManager().beginTransaction();
            ft.replace(R.id.news_detail_container, fragment);
            mMediaDetailFragment = fragment;
            ft.commit();
        }

        mDisplayMode = DisplayMode.MEDIA_DETAIL;
        mViewSwitcher.showSecondView();

        if (mMediaDetailFragment != null) {
            mMediaDetailFragment.getTitle();
            mMediaDetailFragment.getSharable();
        }
    }

    public DisplayMode getDisplayMode() {
        return mDisplayMode;
    }

    @Override
    public void onSwitchComplete(int displayedChild) {
        if (displayedChild == 0) {
            removeDetailFragment();
            setActionBarToggle();
            mLogManager.track(getCurrentPageId());
        } else {
            setActionBarUp();
            mLogManager.track(mMediaDetailFragment.getUrl());
        }
    }

    private String getCurrentPageId() {
        for (int i = 0; i < mMenuItems.size(); i++) {
            TiscaliMenuItem item = mMenuItems.get(i);
            if (item.getUrl().equals(mMediaViewFragment.getUrl())) {
                if (i == 0) {
                    switch (getType()) {
                        case NEWS:
                            return mContext.getResources().getString(
                                    R.string.com_tiscali_appmail_fragment_MediaFragment_Home_News);
                        case VIDEO:
                            return mContext.getResources().getString(
                                    R.string.com_tiscali_appmail_fragment_MediaFragment_Home_Video);
                        case OFFERS:
                            return mContext.getResources().getString(
                                    R.string.com_tiscali_appmail_fragment_MediaFragment_Home_Promo);
                    }
                } else {
                    return mContext.getResources().getString(
                            R.string.com_tiscali_appmail_fragment_MediaFragment_Home_SectionId,
                            item.getSectionId());
                }

            }
        }
        return "";
    }

    @Override
    public void goBack() {
        if (mDisplayMode == DisplayMode.MEDIA_DETAIL) {
            showMedia();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home: {
                goBack();
                return true;
            }
            case R.id.menu_item_share: {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, mContext.getResources()
                        .getString(R.string.webview_contextmenu_link_share_action));
                i.putExtra(Intent.EXTRA_TEXT, mContext.getResources()
                        .getString(R.string.webview_contextmenu_link_share_text) + mCurrentPage);
                mContext.startActivity(Intent.createChooser(i, mContext.getResources()
                        .getString(R.string.webview_contextmenu_link_share_action)));
                return true;
            }

            default: {
                return true;
            }
        }
    }

    @Override
    public void setActionBarUp() {
        if (mContext instanceof INavigationDrawerActivityListener) {
            ((INavigationDrawerActivityListener) mContext).setDrawerEnable(false);
        }
    }

    @Override
    public void setActionBarToggle() {
        if (mContext instanceof INavigationDrawerActivityListener) {
            ((INavigationDrawerActivityListener) mContext).setDrawerEnable(true);
        }
    }

    @Override
    public void setCurrentUrl(String url) {
        mCurrentPage = url;
    }


    public class CategoryMediaAdapter extends BaseAdapter {
        public Activity context;
        public LayoutInflater inflater;
        List<TiscaliMenuItem> mMediaCategory;

        public CategoryMediaAdapter(List<TiscaliMenuItem> Categories) {
            super();
            this.context = mContext;
            this.inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            this.mMediaCategory = Categories;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mMediaCategory.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return mMediaCategory.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        public List<TiscaliMenuItem> getSelectedItmes() {
            return mMediaCategory;
        }

        public class ViewHolder {
            public CheckBox media_button;
            public TextView media_category;
            public RelativeLayout rl_media;
            public ImageView media_icon;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            // TODO Auto-generated method stub
            ViewHolder holder;


            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.listview_news_dialogue_row, null);

                holder.media_button = (CheckBox) convertView.findViewById(R.id.toggle_media);
                holder.media_category = (TextView) convertView.findViewById(R.id.category_media);
                holder.rl_media = (RelativeLayout) convertView.findViewById(R.id.row_media);
                holder.media_icon = (ImageView) convertView.findViewById(R.id.item_icon);
                holder.media_button.setTag(position);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (mMediaCategory.get(position).getIco() != null) {
                Glide.with(mContext).load(mMediaCategory.get(position).getIco())
                        .into(holder.media_icon);
            } else {
                holder.media_icon.setVisibility(View.INVISIBLE);
            }

            holder.media_category.setText(mMediaCategory.get(position).getTitle());
            final ViewHolder final_Holder = holder;

            if ((Boolean) mMediaCategory.get(position).getVisibility()) {
                holder.media_button.setChecked(true);
            } else {
                holder.media_button.setChecked(false);
            }

            holder.media_button
                    .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                            TiscaliMenuItem item = mMediaCategory.get(position);
                            item.setVisibility(isChecked);
                            if (mListener != null) {
                                mListener.getApiController().sectionVisibility(item.getSectionId(),
                                        (Boolean) item.getVisibility());
                            }
                        }
                    });


            return convertView;
        }
    }

    public class MediaAdapter extends BaseNavDrawerMenuAdapter {

        private static final int HOME_POSITION = 1;
        // data set
        private List<TiscaliMenuItem> mItems = new ArrayList<>();
        private Map<TiscaliMenuItem, List<TiscaliMenuItem>> mTree = new HashMap<>();
        private Map<TiscaliMenuItem, Integer> mDepth = new HashMap<>();
        private TiscaliMenuItem mRoot = new TiscaliMenuItem();
        private MenuHeader mMenuHeader = new MenuHeader();
        private int mSelectedPos = 1; // default first element below header

        TiscaliMenuClickListener mClickListener = new TiscaliMenuClickListener() {
            @Override
            public void onSettingsClick() {
                super.onSettingsClick();
                showDialogInformations();
            }

            @Override
            public void onMenuClick(TiscaliMenuItem item) {
                super.onMenuClick(item);
                mListener.closeDrawer();
                openSection(item.getUrl(), item.equals(mItems.get(HOME_POSITION_ADAPTER)));
            }
        };

        public MediaAdapter() {}

        public void setSelectedPos(String sectionId) {

            if (sectionId == null) {
                return;
            }

            for (int i = 0; i < mItems.size(); i++) {
                TiscaliMenuItem menuItem = mItems.get(i);
                if (sectionId.equals(menuItem.getSectionId())) {
                    mSelectedPos = i;
                }
            }
        }

        public void updateData() {
            mItems.clear();
            mTree.clear();
            mDepth.clear();

            mItems.add(mMenuHeader);
            mItems.addAll(mMenuItems);

            mTree.put(mRoot, mItems);

            for (TiscaliMenuItem item : mItems) {
                mDepth.put(item, 0);
            }
            notifyDataSetChanged();
        }


        private int removeSubTree(TiscaliMenuItem root, TiscaliMenuItem node) {

            List<TiscaliMenuItem> children = mTree.get(node);
            int count = (children == null) ? 0 : children.size();

            if (children != null) {
                for (TiscaliMenuItem child : children) {
                    count += removeSubTree(root, child);
                }
                mTree.remove(node);
            }

            if (!root.equals(node)) {
                mDepth.remove(node);
                mItems.remove(node);
                Log.i("NEWS-ADAPTER", "REMOVE:" + node.getSectionId());
            }

            return count;
        }

        private int addSubTree(TiscaliMenuItem root, List<TiscaliMenuItem> items, int position) {
            mTree.put(root, items);

            int depth = mDepth.get(root);

            for (TiscaliMenuItem item : items) {
                mDepth.put(item, depth + 1);
            }

            mItems.addAll(position + 1, items);

            return items.size();
        }

        @Override
        public int getChildrenCount(int position) {
            return getItem(position).getSections().size();
        }

        @Override
        public int getItemDepth(int position) {
            return mDepth.get(getItem(position));
        }

        @Override
        public boolean isItemExpanded(int position) {
            TiscaliMenuItem item = mItems.get(position);
            return mTree.containsKey(item);
        }

        private int getFirstChildPosition(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? HEADER : ITEM;
        }

        private TiscaliMenuItem getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof HeaderViewHolder) {
                final HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                String sectionName = null;
                switch (getType()) {
                    case NEWS:
                        sectionName = mContext.getString(R.string.tab_news);
                        break;
                    case VIDEO:
                        sectionName = mContext.getString(R.string.tab_video);
                        break;
                    case OFFERS:
                        sectionName = mContext.getString(R.string.tab_offers);
                        break;
                }
                if (sectionName != null) {
                    headerViewHolder.mSectionName.setText(sectionName, TextView.BufferType.NORMAL);
                }
                headerViewHolder.mAccountContainer.setVisibility(View.GONE);

                headerViewHolder.mSettingsIv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mClickListener.onSettingsClick();
                    }
                });
            } else if (holder instanceof ItemViewHolder) {
                final TiscaliMenuItem item = getItem(position);
                ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
                itemViewHolder.itemView.setSelected(mSelectedPos == position);

                // row background
                if (itemViewHolder.itemView.isSelected()) {
                    itemViewHolder.mItemContainerRl.setBackgroundColor(
                            ContextCompat.getColor(mContext, R.color.colorItemSelected));
                } else {
                    itemViewHolder.mItemContainerRl.setBackgroundColor(
                            ContextCompat.getColor(mContext, android.R.color.transparent));
                }

                RelativeLayout.LayoutParams params =
                        (RelativeLayout.LayoutParams) itemViewHolder.mItemTitleTv.getLayoutParams();
                int materialMarginLeft =
                        mContext.getResources().getDimensionPixelSize(R.dimen.margin_standard_56dp);
                // Icon
                if (item.getIco() != null) {
                    itemViewHolder.mItemIconIv.setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        if (itemViewHolder.itemView.isSelected()) {
                            itemViewHolder.mItemIconIv.setImageAlpha(255);
                        } else {
                            itemViewHolder.mItemIconIv.setImageAlpha(138);
                        }
                    }
                    Glide.with(mContext).load(item.getIco()).into(itemViewHolder.mItemIconIv);
                } else {
                    itemViewHolder.mItemIconIv.setVisibility(View.GONE);
                    materialMarginLeft = 0;
                }
                params.setMargins(materialMarginLeft, params.topMargin, params.rightMargin,
                        params.bottomMargin);
                itemViewHolder.mItemTitleTv.setLayoutParams(params);

                if (position == HOME_POSITION) {
                    if (Type.NEWS == getType()) {
                        itemViewHolder.mItemActionTv.setVisibility(View.VISIBLE);
                        itemViewHolder.mItemActionTv.setText(
                                mContext.getResources().getString(R.string.menu_item_customize));
                        itemViewHolder.mItemActionTv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                showDialogCustomize(getCustomizableItems());
                            }
                        });
                    } else {
                        itemViewHolder.mItemActionTv.setVisibility(View.GONE);
                    }
                } else {
                    itemViewHolder.mItemActionTv.setVisibility(View.GONE);
                }

                // add additionally left padding depending on depth
                // if (itemViewHolder.mItemContainerRl
                // .getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                // ViewGroup.MarginLayoutParams p =
                // (ViewGroup.MarginLayoutParams) itemViewHolder.mItemContainerRl
                // .getLayoutParams();
                // int marginLeft = (int) (getItemDepth(position)
                // * mContext.getResources().getDimension(R.dimen.margin_standard_16dp));
                // p.setMargins(marginLeft, 0, 0, 0);
                // itemViewHolder.mItemContainerRl.requestLayout();
                // }
                int additionalPaddingLeft = (int) (getItemDepth(position) * mContext.getResources()
                        .getDimensionPixelSize(R.dimen.margin_standard_16dp));
                int basePaddingLeft =
                        mContext.getResources().getDimensionPixelSize(R.dimen.margin_standard_16dp);
                int paddingLeft = basePaddingLeft + additionalPaddingLeft;
                itemViewHolder.mItemContainerRl.setPadding(paddingLeft,
                        itemViewHolder.mItemContainerRl.getPaddingTop(),
                        itemViewHolder.mItemContainerRl.getPaddingRight(),
                        itemViewHolder.mItemContainerRl.getPaddingBottom());
                itemViewHolder.mItemContainerRl.requestLayout();

                // toggle icon for expandable items
                if (hasChildren(position)) {
                    itemViewHolder.mItemToggleIv.setVisibility(View.VISIBLE);
                    // expandable item opened
                    if (isItemExpanded(position)) {
                        itemViewHolder.mItemToggleIv.setImageResource(getCollapseIconResource());
                    } else {
                        itemViewHolder.mItemToggleIv.setImageResource(getExpandIconResource());
                    }
                } else {
                    itemViewHolder.mItemToggleIv.setVisibility(View.GONE);
                }

                if (item.getTitle() != null) {
                    itemViewHolder.mItemTitleTv.setText(item.getTitle());
                }

                // click listener
                itemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (hasChildren(position)) {
                            TiscaliMenuItem item = mItems.get(position);
                            if (mTree.containsKey(item)) {
                                // isExpanded
                                removeSubTree(item, item);
                            } else {
                                // isCollapsed
                                addSubTree(item, item.getSections(), position);
                            }
                            openSection(item.getUrl(),
                                    item.equals(mItems.get(HOME_POSITION_ADAPTER)));
                        } else {
                            mClickListener.onMenuClick(item);
                        }
                        setSelectedPos(item.getSectionId());
                        notifyDataSetChanged();
                    }
                });
            }
        }

        class MenuHeader extends TiscaliMenuItem {
            @Override
            public String getSectionId() {
                return "HEADER";
            }

            @Override
            public String getTitle() {
                return "HEADER";
            }
        }
    }


    @Override
    public void onResume() {
        if (!mStarted) {
            return;
        }
        mListener.getApiController().addListener(this);
    }

    @Override
    public void onPause() {
        if (!mStarted) {
            return;
        }
        mListener.getApiController().removeListener(this);
    }


    @Override
    public void onDetach() {
        if (!mStarted) {
            return;
        }
        mListener.getApiController().removeListener(this);
        removeMediaFragment();
        removeDetailFragment();
    }

    @Override
    public void updateMainConfig(MainConfig mainConfig) {

    }

    @Override
    public void updateMe(Me me, String json) {
        boolean isInitialized = false;
        Log.d("UpdateMe ", "[ME]:" + json);

        if (mMenuItems.size() > 0) {
            // Me already present
            isInitialized = true;
            mMenuItems.clear();
        }

        if (Type.VIDEO == getType()) {
            mMenuItems.addAll(me.getVideo().getTiscaliMenuItem());
        } else if (Type.OFFERS == getType()) {
            mMenuItems.addAll(me.getOffers().getTiscaliMenuItem());
        } else { // NEWS
            mMenuItems.addAll(me.getNews().getTiscaliMenuItem());
        }

        mTimeoutRefresh = me.getNews().getRefreshTimeout() * 1000;
        mMeJson = json;

        if (!isInitialized && mDisplayMode == MEDIA_VIEW) {
            mIsHomePage = true;
            mCurrentPage = mMenuItems.get(HOME_POSITION_PRESENTER).getUrl();
            initializeFragments(mCurrentPage);
        }
        if (mListener != null) {
            mWalledGarden =
                    mListener.getApiController().getMainConfig().getConfig().getWalledGarden();
        }

        mNewsAdapter.updateData();

        if (mDisplayMode == MEDIA_VIEW) {
            if (mMediaViewFragment != null) {
                mMediaViewFragment.setPageStatus();
            }
        } else {
            if (mMediaDetailFragment != null) {
                mMediaDetailFragment.setPageStatus();
            }
        }
    }

    @Override
    public void setStartInstanceState(Bundle savedInstanceState) {}


    public void showDialogInformations() {
        mListener.closeDrawer();
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setItems(R.array.informations_titles, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        if (mContext instanceof INavigationDrawerActivityListener) {
                            ((INavigationDrawerActivityListener) mContext).showInformations();
                            mLogManager
                                    .track(mContext.getString(R.string.com_tiscali_appmail_Info));
                        }
                        break;
                }
            }
        });
        builder.create().show();
    }


    public void showDialogCustomize(List<TiscaliMenuItem> data) {
        mListener.closeDrawer();

        final AppCompatDialog customizeDialog =
                new AppCompatDialog(mListener.getActivity(), R.style.Theme_K9_Light_NoActionBar);
        customizeDialog.setCancelable(true);
        customizeDialog.setContentView(R.layout.dialog_custom_news);
        Toolbar toolbar =
                (Toolbar) customizeDialog.getWindow().getDecorView().findViewById(R.id.toolbar);
        toolbar.setTitle(mContext.getString(R.string.action_bar_title_customize));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customizeDialog.dismiss();
            }
        });

        mLogManager.track(mContext.getResources()
                .getString(R.string.com_tiscali_appmail_News_Customization_Visibility));

        ListView listInterests = (ListView) customizeDialog.findViewById(R.id.list_catagory);
        final CategoryMediaAdapter adapter = new CategoryMediaAdapter(data);
        listInterests.setAdapter(adapter);

        customizeDialog.show();
    }

    public List<TiscaliMenuItem> getCustomizableItems() {
        List<TiscaliMenuItem> visibilityItems = new ArrayList<>();
        for (TiscaliMenuItem item : mMenuItems) {
            if (item.getVisibility() != null) {
                visibilityItems.add(item);
            }
        }
        return visibilityItems;

    }

    @Override
    public void showBottomNav() {
        if (mContext instanceof INavigationDrawerActivityListener) {
            ((INavigationDrawerActivityListener) mContext).showBottomNav();
        }
    }

    @Override
    public void hideBottomNav() {
        if (mContext instanceof INavigationDrawerActivityListener) {
            ((INavigationDrawerActivityListener) mContext).hideBottomNav();
        }
    }
}
