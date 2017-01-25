package com.fsck.k9.fragment;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.bumptech.glide.Glide;
import com.fsck.k9.K9;
import com.fsck.k9.R;
import com.fsck.k9.activity.INavigationDrawerActivityListener;
import com.fsck.k9.adapter.BaseNavDrawerMenuAdapter;
import com.fsck.k9.adapter.TiscaliMenuClickListener;
import com.fsck.k9.api.ApiController;
import com.fsck.k9.api.model.Me;
import com.fsck.k9.api.model.TiscaliMenuItem;
import com.fsck.k9.api.model.UserLogin;
import com.fsck.k9.presenter.PresenterLifeCycle;
import com.fsck.k9.view.ViewSwitcher;
import com.fsck.k9.view.holder.HeaderViewHolder;
import com.fsck.k9.view.holder.ItemViewHolder;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
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
public class NewsPresenter
        implements NewsFragment.NewsFragmentListener, ViewSwitcher.OnSwitchCompleteListener,
        PresenterLifeCycle, ApiController.ApiControllerInterface {

    private static final int HOME_POSITION_ADAPTER = 1;
    private static final int HOME_POSITION_PRESENTER = 0;
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
    private NewsFragment mNewsViewFragment;
    private NewsFragment mNewsDetailFragment;
    private DisplayMode mDisplayMode;
    private TabMode mTabMode;
    private String mCurrentPage;
    private String mMeJson;
    private int mTimeoutRefresh = 1000;
    private boolean mIsHomePage;
    private ProgressBar mActionBarProgress;
    private NewsPresenter.NewsAdapter mNewsAdapter = new NewsAdapter();
    private List<TiscaliMenuItem> mMenuItems = new ArrayList<>();

    private Bundle mSavedInstanceState;
    private boolean mStarted = false;

    public enum DisplayMode {
        NEWS_VIEW, NEWS_DETAIL, SPLIT_VIEW
    }

    public enum TabMode {
        NEWS_TAB, VIDEO_TAB, PRMOTIONS_TAB
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
        initializeActionBar();
        findFragments();

        initializeDisplayMode(mSavedInstanceState);

        mListener.setDrawerListAdapter(mNewsAdapter);

        if (mSavedInstanceState != null) {
            mCurrentPage = mSavedInstanceState.getString(NEWS_STATE_CURRENT_URL);
            if (mCurrentPage != null) {
                initializeFragments(mCurrentPage);
            }
        } else {
            if (mMenuItems.size() > 0) {
                initializeFragments(mMenuItems.get(HOME_POSITION_PRESENTER).getUrl());
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(NEWS_STATE_DISPLAY_MODE, mDisplayMode);
        outState.putString(NEWS_STATE_CURRENT_URL, mCurrentPage);
    }

    private void findFragments() {
        FragmentManager fragmentManager = mContext.getFragmentManager();
        mNewsViewFragment =
                (NewsFragment) fragmentManager.findFragmentById(R.id.news_view_container);
        mNewsDetailFragment =
                (NewsFragment) fragmentManager.findFragmentById(R.id.news_detail_container);
    }

    /**
     * Create fragment instances if necessary.
     */
    private void initializeFragments(String home) {
        FragmentManager fragmentManager = mContext.getFragmentManager();

        boolean hasNewsFragment = (mNewsViewFragment != null);
        if (!hasNewsFragment) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            mNewsViewFragment = NewsFragment.newInstance(home);
            ft.add(R.id.news_view_container, mNewsViewFragment);
            ft.commit();
        }

        if (mDisplayMode.equals(DisplayMode.NEWS_VIEW)) {
            setActionBarToggle();
            showNews();
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

    private void initializeDisplayMode(Bundle savedInstanceState) {
        if (useSplitView()) {
            mDisplayMode = DisplayMode.SPLIT_VIEW;
            return;
        }

        if (savedInstanceState != null) {
            DisplayMode savedDisplayMode =
                    (DisplayMode) savedInstanceState.getSerializable(NEWS_STATE_DISPLAY_MODE);
            if (savedDisplayMode != DisplayMode.SPLIT_VIEW && savedDisplayMode != null) {
                mDisplayMode = savedDisplayMode;

                return;
            }
        }

        mDisplayMode = DisplayMode.NEWS_VIEW;
    }

    public void goBackOnHistory() {
        if (mNewsDetailFragment != null && mNewsDetailFragment.canGoBack()) {
            mNewsDetailFragment.goBackOnHistory();
        } else {
            showNews();
        }
    }

    public void showNews() {
        mDisplayMode = DisplayMode.NEWS_VIEW;
        mViewSwitcher.showFirstView();
    }

    private void removeNewsFragment() {
        if (mNewsViewFragment != null) {
            mNewsViewFragment.mWebView.loadUrl("about:blank");
            FragmentTransaction ft = mContext.getFragmentManager().beginTransaction();
            ft.remove(mNewsViewFragment);
            mNewsViewFragment = null;
            ft.commit();
        }
    }

    private void removeDetailFragment() {
        if (mNewsDetailFragment != null) {
            mNewsDetailFragment.mWebView.loadUrl("about:blank");
            FragmentTransaction ft = mContext.getFragmentManager().beginTransaction();
            ft.remove(mNewsDetailFragment);
            mNewsDetailFragment = null;
            ft.commit();
        }
    }

    private void initializeActionBar() {
        mActionBar = ((AppCompatActivity) mContext).getSupportActionBar();

        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setCustomView(R.layout.actioncustombar_news);

        View customView = mActionBar.getCustomView();
        mActionBarTitle = (TextView) customView.findViewById(R.id.actionbar_title_first);
        mActionBarProgress = (ProgressBar) customView.findViewById(R.id.actionbar_progress);


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
                    public void onError(Throwable e) {

            }

                    @Override
                    public void onNext(Object o) {

            }
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
        showNews();
        if (mNewsViewFragment != null) {
            mNewsViewFragment.updateUrl(url);
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
                    public void onError(Throwable e) {

            }

                    @Override
                    public void onNext(Object o) {

            }
                });

    }


    @Override
    public boolean isDetailStatus() {
        return mDisplayMode == DisplayMode.NEWS_DETAIL;
    }

    @Override
    public boolean isHomePage() {
        if (mDisplayMode == DisplayMode.NEWS_DETAIL) {
            return false;
        } else {
            return mIsHomePage;
        }

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

                if (mIsHomePage && mNewsViewFragment != null) {
                    mNewsViewFragment.refreshUrl();
                }

            }
        });
    }

    @Override
    public void detailPageLoad(String url) {
        mCurrentPage = url;
        if (mNewsDetailFragment == null) {
            NewsFragment fragment = NewsFragment.newInstance(url);
            FragmentTransaction ft = mContext.getFragmentManager().beginTransaction();
            ft.replace(R.id.news_detail_container, fragment);
            mNewsDetailFragment = fragment;
            ft.commit();
        }

        mDisplayMode = DisplayMode.NEWS_DETAIL;
        mViewSwitcher.showSecondView();

        if (mNewsDetailFragment != null) {
            mNewsDetailFragment.getTitle();
            mNewsDetailFragment.getSharable();
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
        } else {
            setActionBarUp();
        }
    }

    @Override
    public void goBack() {
        FragmentManager fragmentManager = ((Activity) mContext).getFragmentManager();
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


    public class CategoryNewsAdapter extends BaseAdapter {
        public Activity context;
        public LayoutInflater inflater;
        List<TiscaliMenuItem> mNewsCategory;

        public CategoryNewsAdapter(List<TiscaliMenuItem> Categories) {
            super();
            this.context = mContext;
            this.inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            this.mNewsCategory = Categories;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mNewsCategory.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return mNewsCategory.get(position);
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
            return mNewsCategory;
        }

        public class ViewHolder {
            public CheckBox news_button;
            public TextView news_category;
            public RelativeLayout rl_news;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            // TODO Auto-generated method stub
            ViewHolder holder;


            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.listview_news_dialogue_row, null);

                holder.news_button = (CheckBox) convertView.findViewById(R.id.toggle_news);
                holder.news_category = (TextView) convertView.findViewById(R.id.category_news);
                holder.rl_news = (RelativeLayout) convertView.findViewById(R.id.row_news);
                holder.news_button.setTag(position);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.news_category.setText(mNewsCategory.get(position).getTitle());
            final ViewHolder final_Holder = holder;

            if ((Boolean) mNewsCategory.get(position).getVisibility()) {
                holder.news_button.setChecked(true);
            } else {
                holder.news_button.setChecked(false);
            }

            holder.news_button
                    .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                            TiscaliMenuItem item = mNewsCategory.get(position);
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

    public class NewsAdapter extends BaseNavDrawerMenuAdapter {

        private static final int HOME_POSITION = 1;
        // data set
        private List<TiscaliMenuItem> mItems = new ArrayList<>();
        private Map<TiscaliMenuItem, List<TiscaliMenuItem>> mTree = new HashMap<>();
        private Map<TiscaliMenuItem, Integer> mDepth = new HashMap<>();
        private TiscaliMenuItem mRoot = new TiscaliMenuItem();
        private MenuHeader mMenuHeader = new MenuHeader();

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

        public NewsAdapter() {}

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
                final TiscaliMenuItem item = getItem(position);
                ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

                // Icon
                if (item.getIco() != null) {
                    itemViewHolder.mItemIconIv.setVisibility(View.VISIBLE);
                    Glide.with(mContext).load(item.getIco()).into(itemViewHolder.mItemIconIv);

                } else {
                    itemViewHolder.mItemIconIv.setVisibility(View.GONE);
                }
                if (position == HOME_POSITION) {
                    itemViewHolder.mItemActionTv.setVisibility(View.VISIBLE);
                    itemViewHolder.mItemActionTv.setText("personalizza");
                    itemViewHolder.mItemActionTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            showDialogCustomize(getCustomizableItems());
                        }
                    });
                }
                // add additionally left margin depending on depth
                if (itemViewHolder.mItemContainerRl
                        .getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams p =
                            (ViewGroup.MarginLayoutParams) itemViewHolder.mItemContainerRl
                                    .getLayoutParams();
                    int marginLeft = (int) (getItemDepth(position)
                            * mContext.getResources().getDimension(R.dimen.margin_standard_16dp));
                    p.setMargins(marginLeft, 0, 0, 0);
                    itemViewHolder.mItemContainerRl.requestLayout();
                }

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
                            notifyDataSetChanged();
                            openSection(item.getUrl(),
                                    item.equals(mItems.get(HOME_POSITION_ADAPTER)));
                        } else {
                            mClickListener.onMenuClick(item);
                        }
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
        removeNewsFragment();
        removeDetailFragment();
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

        mMenuItems.addAll(me.getNews().getTiscaliMenuItem());
        mTimeoutRefresh = me.getNews().getRefreshTimeout() * 1000;
        mMeJson = json;

        if (!isInitialized && mDisplayMode != DisplayMode.NEWS_DETAIL) {
            mIsHomePage = true;
            initializeFragments(mMenuItems.get(HOME_POSITION_PRESENTER).getUrl());
        }

        mNewsAdapter.updateData();

        if (mDisplayMode == DisplayMode.NEWS_VIEW) {
            if (mNewsViewFragment != null) {
                mNewsViewFragment.refreshUrl();
            }
        } else {
            if (mNewsViewFragment != null) {
                mNewsDetailFragment.refreshUrl();
            }
        }
    }

    @Override
    public void setStartInstanceState(Bundle savedInstanceState) {
        mSavedInstanceState = savedInstanceState;
    }


    public void showDialogInformations() {
        mListener.closeDrawer();
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setItems(R.array.informations_titles, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        if (mContext instanceof INavigationDrawerActivityListener) {
                            ((INavigationDrawerActivityListener) mContext).showInformations();
                        }
                        break;
                }
            }
        });
        builder.create().show();
    }


    public void showDialogCustomize(List<TiscaliMenuItem> data) {
        mListener.closeDrawer();

        final Dialog customize = new Dialog(mListener.getActivity(),
                android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        customize.setContentView(R.layout.dialog_custom_news);
        customize.setCancelable(false);

        ListView listInterests = (ListView) customize.findViewById(R.id.list_catagory);

        final CategoryNewsAdapter adapter = new CategoryNewsAdapter(data);

        listInterests.setAdapter(adapter);

        Button btnOk = (Button) customize.findViewById(R.id.btn_close);
        btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                customize.dismiss();
            }
        });

        customize.show();
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
}
