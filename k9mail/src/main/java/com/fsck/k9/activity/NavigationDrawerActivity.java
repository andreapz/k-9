/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.fsck.k9.activity;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.inject.Inject;

import com.fsck.k9.Account;
import com.fsck.k9.ApplicationComponent;
import com.fsck.k9.K9;
import com.fsck.k9.Preferences;
import com.fsck.k9.R;
import com.fsck.k9.activity.setup.AccountSetupBasics;
import com.fsck.k9.adapter.NavDrawerMenuAdapter;
import com.fsck.k9.api.ApiController;
import com.fsck.k9.api.model.MainConfig;
import com.fsck.k9.api.model.Me;
import com.fsck.k9.fragment.MailPresenter;
import com.fsck.k9.fragment.MediaFragment;
import com.fsck.k9.fragment.MediaPresenter;
import com.fsck.k9.fragment.MessageListFragment;
import com.fsck.k9.fragment.NewsPresenter;
import com.fsck.k9.fragment.OffersPresenter;
import com.fsck.k9.fragment.VideoPresenter;
import com.fsck.k9.model.NavDrawerMenuItem;
import com.fsck.k9.preferences.StorageEditor;
import com.fsck.k9.search.LocalSearch;
import com.fsck.k9.search.SearchSpecification;
import com.fsck.k9.ui.messageview.MessageViewFragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;


/**
 * This example illustrates a common usage of the DrawerLayout widget in the Android support
 * library.
 * <p/>
 * <p>
 * When a navigation (left) drawer is present, the host activity should detect presses of the action
 * bar's Up affordance as a signal to open and close the navigation drawer. The
 * ActionBarDrawerToggle facilitates this behavior. Items within the drawer should fall into one of
 * two categories:
 * </p>
 * <p/>
 * <ul>
 * <li><strong>View switches</strong>. A view switch follows the same basic policies as list or tab
 * navigation in that a view switch does not create navigation history. This pattern should only be
 * used at the root activity of a task, leaving some form of Up navigation active for activities
 * further down the navigation hierarchy.</li>
 * <li><strong>Selective Up</strong>. The drawer allows the user to choose an alternate parent for
 * Up navigation. This allows a user to jump across an app's navigation hierarchy at will. The
 * application should treat this as it treats Up navigation from a different task, replacing the
 * current task stack using TaskStackBuilder or similar. This is the only form of navigation drawer
 * that should be used outside of the root activity of a task.</li>
 * </ul>
 * <p/>
 * <p>
 * Right side drawers should be used for actions, not navigation. This follows the pattern
 * established by the Action Bar that navigation should be to the left and actions to the right. An
 * action should be an operation performed on the current contents of the window, for example
 * enabling or disabling a data overlay on top of the current content.
 * </p>
 */
public class NavigationDrawerActivity extends K9Activity
        implements MessageListFragment.MessageListFragmentGetListener,
        MessageViewFragment.MessageViewFragmentGetListener, MediaFragment.MediaFragmentGetListener,
        INavigationDrawerActivityListener, ApiController.ApiControllerInterface {

    private static final String EXTRA_SEARCH = "search";
    private static final String EXTRA_NO_THREADING = "no_threading";

    public static final String ACTION_IMPORT_SETTINGS = "importSettings";
    public static final String EXTRA_STARTUP = "startup";
    public static final String EXTRA_ACCOUNT = "account";
    private static final int ACTIVITY_REQUEST_PICK_SETTINGS_FILE = 100;
    private static final int DIALOG_NO_FILE_MANAGER = 40;

    public static final String SELECTED_TAB = "SELECTED_TAB";

    public static final int NONE_TAB_SELECTED = -1;
    public static final int MAIL_TAB_SELECTED = 0;
    public static final int NEWS_TAB_SELECTED = 1;
    public static final int VIDEO_TAB_SELECTED = 2;
    public static final int OFFERS_TAB_SELECTED = 3;


    public static final String MAIL_TAB = "mail";
    public static final String NEWS_TAB = "news";
    public static final String VIDEO_TAB = "video";
    public static final String OFFERS_TAB = "offerte";

    public static int DEFAULT_SELECTED_TAB = NEWS_TAB_SELECTED;
    public static final String DEFAULT_TAB_KEY = "default_tab";


    private DrawerLayout mDrawerLayout;
    private RecyclerView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private NavDrawerMenuAdapter mOffersAdapter;
    private NavDrawerMenuAdapter mVideoAdapter;

    private int mSelectedTab;
    private BottomNavigationView mBottomNav;

    List<NavDrawerMenuItem> mOffersTabMenuItems;
    List<NavDrawerMenuItem> mVideoTabMenuItems;

    @Inject
    MailPresenter mMailPresenter;
    @Inject
    ApiController mApiController;
    @Inject
    NewsPresenter mNewsPresenter;
    @Inject
    VideoPresenter mVideoPresenter;
    @Inject
    OffersPresenter mOffersPresenter;


    private BottomNavigationView.OnNavigationItemSelectedListener mBottomNavigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem item) {

                    switch (mSelectedTab) {
                        case MAIL_TAB_SELECTED:
                            if (item.getItemId() != R.id.menu_mail) {
                                mMailPresenter.onDetach();
                            }
                            break;

                        case NEWS_TAB_SELECTED:
                            if (item.getItemId() != R.id.menu_news) {
                                mNewsPresenter.onDetach();
                            }
                            break;

                        case VIDEO_TAB_SELECTED:
                            if (item.getItemId() != R.id.menu_video) {
                                mVideoPresenter.onDetach();
                            }
                            break;

                        case OFFERS_TAB_SELECTED:
                            if (item.getItemId() != R.id.menu_offers) {
                                mOffersPresenter.onDetach();
                            }
                            break;
                    }

                    switch (item.getItemId()) {
                        case R.id.menu_mail:
                            onMailTabClicked();
                            break;
                        case R.id.menu_news:
                            onNewsTabClicked();
                            break;
                        case R.id.menu_video:
                            onVideoTabClicked();
                            break;
                        case R.id.menu_offers:
                            onOffersTabClicked();
                            break;
                    }
                    return true;
                }
            };

    private FrameLayout mViewContainer;

    public static void importSettings(Context context) {
        Intent intent = new Intent(context, NavigationDrawerActivity.class);
        intent.setAction(ACTION_IMPORT_SETTINGS);
        context.startActivity(intent);
    }

    public static void listMessage(Context context, String accounUUid) {
        Intent intent = new Intent(context, NavigationDrawerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(EXTRA_STARTUP, false);
        intent.putExtra(EXTRA_ACCOUNT, accounUUid);
        context.startActivity(intent);
    }

    public static Intent intentDisplaySearch(Context context, SearchSpecification search,
            boolean noThreading, boolean newTask, boolean clearTop) {
        Intent intent = new Intent(context, NavigationDrawerActivity.class);
        intent.putExtra(EXTRA_SEARCH, search);
        intent.putExtra(EXTRA_NO_THREADING, noThreading);

        if (clearTop) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        if (newTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Preferences pref = Preferences.getPreferences(this);
        List<Account> accounts = pref.getAccounts();

        Intent intent = getIntent();
        // see if we should show the welcome message
        if (ACTION_IMPORT_SETTINGS.equals(intent.getAction())) {
            onImport();
        } else if (accounts.size() < 1) {
            AccountSetupBasics.actionNewAccount(this);
            finish();
            return;
        }

        String accountUUid = intent.getStringExtra(EXTRA_ACCOUNT);
        Intent mailIntent;
        // mail search
        if (intent.getStringExtra(SearchManager.QUERY) != null) {
            mailIntent = intent;
            intent.putExtra(EXTRA_STARTUP, false);
        } else if (accountUUid != null) {
            mailIntent = getMailIntent(pref.getAccount(accountUUid));
        } else {
            mailIntent = getMailIntent(accounts.get(0));
        }

        if (mNewsPresenter == null) {
            buildDaggerComponent(mailIntent);
        }

        if (UpgradeDatabases.actionUpgradeDatabases(this, intent)) {
            finish();
            return;
        }

        setContentView(R.layout.activity_navigation_drawer);

        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (RecyclerView) findViewById(R.id.left_drawer);
        ViewGroup.LayoutParams params = mDrawerList.getLayoutParams();
        params.width = getNavigationDrawerWidth();
        mDrawerList.setLayoutParams(params);
        mBottomNav = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        mViewContainer = (FrameLayout) findViewById(R.id.content_frame);
        mBottomNav.setOnNavigationItemSelectedListener(mBottomNavigationItemSelectedListener);

        initNavigationDrawerMenuData();

        // set a custom shadow that overlays the main content when the drawer opens
        // mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // improve performance by indicating the list if fixed size.
        mDrawerList.setHasFixedSize(true);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle( // R.drawable.ic_menu_black_24dp, /* nav drawer
                // image to replace 'Up' caret */
                this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.drawer_open, /* "open drawer" description for accessibility */
                R.string.drawer_close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        DEFAULT_SELECTED_TAB = pref.getStorage().getInt(DEFAULT_TAB_KEY, DEFAULT_SELECTED_TAB);

        mSelectedTab = NONE_TAB_SELECTED;

        if (savedInstanceState == null) {
            int tempSelectedTab = DEFAULT_SELECTED_TAB;
            // open mail when coming from registration/change of password/account selection
            if (!intent.getBooleanExtra(EXTRA_STARTUP, true)) {
                tempSelectedTab = MAIL_TAB_SELECTED;
            }
            mBottomNav.findViewById(mBottomNav.getMenu().getItem(tempSelectedTab).getItemId())
                    .performClick();
        } else {
            mMailPresenter.setStartInstanceState(savedInstanceState);
            mNewsPresenter.setStartInstanceState(savedInstanceState);
            mVideoPresenter.setStartInstanceState(savedInstanceState);
            mOffersPresenter.setStartInstanceState(savedInstanceState);
            setSelectedTab(savedInstanceState.getInt(SELECTED_TAB));
        }

        mBottomNav.bringToFront();

        // open search results
        if (intent.getStringExtra(SearchManager.QUERY) != null) {
            mBottomNav.setVisibility(View.GONE);
        }

    }

    private int getScreenWidth() {

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    private int getActionBarSize() {
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data,
                    getResources().getDisplayMetrics());
        }

        return 0;
    }

    // get the drawer panel width
    // From Google docs:
    // Side nav width: Equal to the screen width minus the height of the action bar. In the example
    // shown above,
    // the nav drawer is 56dp from the right edge of the screen.
    // Maximum width: The maximum width of the nav drawer is 280dp on mobile and 320dp on tablet.
    // This is calculated by multiplying the standard increment by five (the standard increment is
    // 56dp on mobile and 64dp on tablet).
    private int getNavigationDrawerWidth() {
        return Math.min(getScreenWidth() - getActionBarSize(), 5 * getActionBarSize());
    }


    /**
     * Set the navigation drawer status
     *
     * @param isEnabled: if false will be disabled the ability to open the drawer
     */
    public void setDrawerEnable(boolean isEnabled) {
        if (isEnabled) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mDrawerToggle.onDrawerStateChanged(DrawerLayout.STATE_IDLE);
            mDrawerToggle.setDrawerIndicatorEnabled(true);
            mDrawerToggle.setDrawerIndicatorEnabled(true);
            mDrawerToggle.syncState();
        } else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            mDrawerToggle.onDrawerStateChanged(DrawerLayout.STATE_IDLE);
            mDrawerToggle.setDrawerIndicatorEnabled(false);
            mDrawerToggle.syncState();
        }
    }

    private void buildDaggerComponent(Intent intent) {
        ApplicationComponent component = ((K9) getApplicationContext()).getComponent();
        DaggerNavigationDrawerActivityComponent.builder().applicationComponent(component)
                .activityModule(new ActivityModule(this, intent)).build().inject(this);
    }

    private Intent getMailIntent(Account account) {
        LocalSearch search = new LocalSearch(account.getAutoExpandFolderName());
        search.addAllowedFolder(account.getAutoExpandFolderName());
        search.addAccountUuid(account.getUuid());
        return intentDisplaySearch(this, search, false, true, true);
    }

    private boolean useSplitView() {
        K9.SplitViewMode splitViewMode = K9.getSplitViewMode();
        int orientation = getResources().getConfiguration().orientation;

        return (splitViewMode == K9.SplitViewMode.ALWAYS
                || (splitViewMode == K9.SplitViewMode.WHEN_IN_LANDSCAPE
                        && orientation == Configuration.ORIENTATION_LANDSCAPE));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_TAB, mSelectedTab);

        if (mMailPresenter != null) {
            mMailPresenter.onSaveInstanceState(outState);
        }
        if (mNewsPresenter != null) {
            mNewsPresenter.onSaveInstanceState(outState);
        }
        if (mVideoPresenter != null) {
            mVideoPresenter.onSaveInstanceState(outState);
        }
        if (mOffersPresenter != null) {
            mOffersPresenter.onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putInt(SELECTED_TAB, mSelectedTab);

        if (mMailPresenter != null) {
            mMailPresenter.onSaveInstanceState(outState);
        }
        if (mNewsPresenter != null) {
            mNewsPresenter.onSaveInstanceState(outState);
        }
        if (mVideoPresenter != null) {
            mVideoPresenter.onSaveInstanceState(outState);
        }
        if (mOffersPresenter != null) {
            mOffersPresenter.onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!(this instanceof Search)) {
            // necessary b/c no guarantee Search.onStop will be called before
            // MessageList.onResume
            // when returning from search results
            Search.setActive(false);
        }

        if (mMailPresenter != null) {
            mMailPresenter.onResume();
        }
        if (mNewsPresenter != null) {
            mNewsPresenter.onResume();
        }
        if (mVideoPresenter != null) {
            mVideoPresenter.onResume();
        }
        if (mOffersPresenter != null) {
            mOffersPresenter.onResume();
        }
        mApiController.addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mMailPresenter != null) {
            mMailPresenter.onPause();
        }
        if (mNewsPresenter != null) {
            mNewsPresenter.onPause();
        }
        if (mVideoPresenter != null) {
            mVideoPresenter.onPause();
        }
        if (mOffersPresenter != null) {
            mOffersPresenter.onPause();
        }
        mApiController.removeListener(this);
    }

    private void onImport() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");

        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> infos = packageManager.queryIntentActivities(i, 0);

        if (infos.size() > 0) {
            startActivityForResult(Intent.createChooser(i, null),
                    ACTIVITY_REQUEST_PICK_SETTINGS_FILE);
        } else {
            showDialog(DIALOG_NO_FILE_MANAGER);
        }
    }

    // private void setSelectedTab(int position) {
    // for(int i = 0; i < mBottomNav.getMenu().size(); i++) {
    // if(i == position) {
    // mBottomNav.getMenu().getItem(i).setChecked(true);
    // } else {
    // mBottomNav.getMenu().getItem(i).setChecked(false);
    // }
    // }
    // }

    private void setSelectedTab(int position) {

        switch (position) {
            case MAIL_TAB_SELECTED:
                mBottomNav.findViewById(R.id.menu_mail).performClick();
                onMailTabClicked();
                break;
            case NEWS_TAB_SELECTED:
                mBottomNav.findViewById(R.id.menu_news).performClick();
                break;
            case VIDEO_TAB_SELECTED:
                mBottomNav.findViewById(R.id.menu_video).performClick();
                break;
            case OFFERS_TAB_SELECTED:
                mBottomNav.findViewById(R.id.menu_offers).performClick();
                break;
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

    // populate the drawer navigation
    private void initNavigationDrawerMenuData() {

        // news, video and offers
        String meObjectJsonString = getJsonString(getResources().openRawResource(R.raw.me_object));

        // news tab drawer menu
        // mNewsTabMenuItems = NavDrawerMenuItem.getMenuList(meObjectJsonString, "news");
        // mNewsAdapter = new NavDrawerMenuAdapter(mNewsTabMenuItems, this,mClickListener);


        // video tab drawer menu
        mVideoTabMenuItems = NavDrawerMenuItem.getMenuList(meObjectJsonString, "video");
        mVideoAdapter = new NavDrawerMenuAdapter(mVideoTabMenuItems, this);

        // offers tab drawer menu
        mOffersTabMenuItems = NavDrawerMenuItem.getMenuList(meObjectJsonString, "offers");
        mOffersAdapter = new NavDrawerMenuAdapter(mOffersTabMenuItems, this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.navigation_drawer, menu);
        if (mMailPresenter != null) {
            mMailPresenter.onCreateOptionsMenu(menu, getMenuInflater());
        }
        if (mNewsPresenter != null) {
            mNewsPresenter.onCreateOptionsMenu(menu, getMenuInflater());
        }
        if (mVideoPresenter != null) {
            mVideoPresenter.onCreateOptionsMenu(menu, getMenuInflater());
        }
        if (mOffersPresenter != null) {
            mOffersPresenter.onCreateOptionsMenu(menu, getMenuInflater());
        }
        return true;
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        // boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);

        if (mMailPresenter != null) {
            return mMailPresenter.onPrepareOptionsMenu(menu);
        }
        if (mNewsPresenter != null) {
            return mNewsPresenter.onPrepareOptionsMenu(menu);
        }
        if (mVideoPresenter != null) {
            return mVideoPresenter.onPrepareOptionsMenu(menu);
        }
        if (mOffersPresenter != null) {
            return mOffersPresenter.onPrepareOptionsMenu(menu);
        }

        // menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (mSelectedTab) {
            case MAIL_TAB_SELECTED:
                if (mMailPresenter != null) {
                    return mMailPresenter.onOptionsItemSelected(item);
                }
                break;
            case NEWS_TAB_SELECTED:
                if (mNewsPresenter != null) {
                    return mNewsPresenter.onOptionsItemSelected(item);
                }
                break;
            case VIDEO_TAB_SELECTED:
                if (mVideoPresenter != null) {
                    return mVideoPresenter.onOptionsItemSelected(item);
                }
                break;
            case OFFERS_TAB_SELECTED:
                if (mOffersPresenter != null) {
                    return mOffersPresenter.onOptionsItemSelected(item);
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during onPostCreate() and
     * onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void onMailTabClicked() {
        if (mSelectedTab != MAIL_TAB_SELECTED) {
            mSelectedTab = MAIL_TAB_SELECTED;
            mMailPresenter.onCreateView();
            mMailPresenter.onResume();
        }
    }

    private void onNewsTabClicked() {
        if (mSelectedTab != NEWS_TAB_SELECTED) {
            mSelectedTab = NEWS_TAB_SELECTED;
            mNewsPresenter.onCreateView();
            mNewsPresenter.onResume();
        }
    }

    private void onVideoTabClicked() {
        if (mSelectedTab != VIDEO_TAB_SELECTED) {
            mSelectedTab = VIDEO_TAB_SELECTED;
            mVideoPresenter.onCreateView();
            mVideoPresenter.onResume();
        }
        // setAdapterBasedOnSelectedTab(mSelectedTab);
    }

    private void onOffersTabClicked() {
        if (mSelectedTab != OFFERS_TAB_SELECTED) {
            mSelectedTab = OFFERS_TAB_SELECTED;
            mOffersPresenter.onCreateView();
            mOffersPresenter.onResume();
        }
    }

    private void forceBuildDaggerComponent() {
        List<Account> accounts = Preferences.getPreferences(this).getAccounts();
        Intent intent = getMailIntent(accounts.get(0));
        buildDaggerComponent(intent);
    }


    @Override
    public MessageListFragment.MessageListFragmentListener getMessageListFragmentListner() {
        if (mMailPresenter == null) {
            forceBuildDaggerComponent();
        }
        return mMailPresenter;
    }

    @Override
    public MessageViewFragment.MessageViewFragmentListener getMessageViewFragmentListner() {
        if (mMailPresenter == null) {
            forceBuildDaggerComponent();
        }
        return mMailPresenter;
    }

    @Override
    public void onBackPressed() {
        if (mNewsPresenter != null
                && mNewsPresenter.getDisplayMode() == MediaPresenter.DisplayMode.MEDIA_DETAIL) {
            mNewsPresenter.goBackOnHistory();
        } else if (mVideoPresenter != null
                && mVideoPresenter.getDisplayMode() == MediaPresenter.DisplayMode.MEDIA_DETAIL) {
            mVideoPresenter.goBackOnHistory();
        } else if (mOffersPresenter != null
                && mOffersPresenter.getDisplayMode() == MediaPresenter.DisplayMode.MEDIA_DETAIL) {
            mOffersPresenter.goBackOnHistory();
        } else if (mMailPresenter != null
                && (mMailPresenter.getDisplayMode() == MailPresenter.DisplayMode.MESSAGE_VIEW
                        && mMailPresenter.getMessageListWasDisplayed())) {
            mMailPresenter.showMessageList();
        } else if (mMailPresenter != null
                && getIntent().getStringExtra(SearchManager.QUERY) != null) {
            mMailPresenter.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public FrameLayout getContainer() {
        return mViewContainer;
    }

    @Override
    public void setDrawerListAdapter(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter) {
        if (mDrawerList != null && adapter != null) {
            mDrawerList.setAdapter(adapter);
        }
    }

    @Override
    public void closeDrawer() {
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    public void updateMe(Me me, String json) {

        String defaultTab = me.getDefaultTab();
        int defaultTabIndex = NEWS_TAB_SELECTED;

        if (defaultTab.equals(MAIL_TAB)) {
            defaultTabIndex = MAIL_TAB_SELECTED;
        } else if (defaultTab.equals(VIDEO_TAB)) {
            defaultTabIndex = NEWS_TAB_SELECTED;
        } else if (defaultTab.equals(OFFERS_TAB)) {
            defaultTabIndex = OFFERS_TAB_SELECTED;
        }

        StorageEditor editor = Preferences.getPreferences(this).getStorage().edit();
        editor.putInt(DEFAULT_TAB_KEY, defaultTabIndex);
        editor.commit();
    }

    @Override
    public ApiController getApiController() {
        return mApiController;
    }

    @Override
    public void showInformations() {
        final Dialog customize =
                new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        customize.setContentView(R.layout.dialog_informations);
        customize.setCancelable(true);

        WebView view = (WebView) customize.findViewById(R.id.webview);

        view.getSettings().setJavaScriptEnabled(true);
        MainConfig mainConfig = null;
        if (getApiController() != null) {
            mainConfig = getApiController().getMainConfig();
        }
        if (mainConfig != null && mainConfig.getEndpoints() != null
                && mainConfig.getEndpoints().getInfoAbout() != null) {
            view.loadUrl(mainConfig.getEndpoints().getInfoAbout().getUrl());
        }
        view.setWebViewClient(new WebViewClient());
        Button btnOk = (Button) customize.findViewById(R.id.btn_close);
        btnOk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                customize.dismiss();
            }
        });

        customize.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MediaPresenter.MEDIA_PRESENTER_BROWSING) {
            if (resultCode == Activity.RESULT_OK) {
                if (mNewsPresenter != null && mNewsPresenter
                        .getDisplayMode() == MediaPresenter.DisplayMode.MEDIA_DETAIL) {
                    mNewsPresenter.goBackOnHistory();
                } else if (mVideoPresenter != null && mVideoPresenter
                        .getDisplayMode() == MediaPresenter.DisplayMode.MEDIA_DETAIL) {
                    mVideoPresenter.goBackOnHistory();
                } else if (mOffersPresenter != null && mOffersPresenter
                        .getDisplayMode() == MediaPresenter.DisplayMode.MEDIA_DETAIL) {
                    mOffersPresenter.goBackOnHistory();
                }
            }

        }
    }// onActivityResult

    @Override
    public MediaFragment.MediaFragmentListener getMediaFragmentListener(MediaPresenter.Type type) {
        if (mNewsPresenter == null) {
            forceBuildDaggerComponent();
        }

        if (MediaPresenter.Type.VIDEO == type) {
            return mVideoPresenter;
        }

        if (MediaPresenter.Type.OFFERS == type) {
            return mOffersPresenter;
        }

        return mNewsPresenter;
    }
}
