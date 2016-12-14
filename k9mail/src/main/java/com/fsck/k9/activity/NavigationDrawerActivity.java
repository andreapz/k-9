/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fsck.k9.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.BottomNavigationView;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.fsck.k9.Account;
import com.fsck.k9.ApplicationComponent;
import com.fsck.k9.K9;
import com.fsck.k9.Preferences;
import com.fsck.k9.R;
import com.fsck.k9.activity.setup.AccountSettings;
import com.fsck.k9.activity.setup.Prefs;
import com.fsck.k9.activity.setup.WelcomeMessage;
import com.fsck.k9.adapter.BaseNavDrawerMenuAdapter;
import com.fsck.k9.adapter.MailNavDrawerMenuAdapter;
import com.fsck.k9.adapter.NavDrawerMenuAdapter;
import com.fsck.k9.adapter.MailNavDrawerClickListener;
import com.fsck.k9.controller.MessagingController;
import com.fsck.k9.controller.MessagingListener;
import com.fsck.k9.fragment.MailPresenter;
import com.fsck.k9.fragment.MessageListFragment;
import com.fsck.k9.mailstore.LocalFolder;
import com.fsck.k9.model.NavDrawerMenuItem;
import com.fsck.k9.search.LocalSearch;
import com.fsck.k9.ui.messageview.MessageViewFragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;


/**
 * This example illustrates a common usage of the DrawerLayout widget
 * in the Android support library.
 * <p/>
 * <p>When a navigation (left) drawer is present, the host activity should detect presses of
 * the action bar's Up affordance as a signal to open and close the navigation drawer. The
 * ActionBarDrawerToggle facilitates this behavior.
 * Items within the drawer should fall into one of two categories:</p>
 * <p/>
 * <ul>
 * <li><strong>View switches</strong>. A view switch follows the same basic policies as
 * list or tab navigation in that a view switch does not create navigation history.
 * This pattern should only be used at the root activity of a task, leaving some form
 * of Up navigation active for activities further down the navigation hierarchy.</li>
 * <li><strong>Selective Up</strong>. The drawer allows the user to choose an alternate
 * parent for Up navigation. This allows a user to jump across an app's navigation
 * hierarchy at will. The application should treat this as it treats Up navigation from
 * a different task, replacing the current task stack using TaskStackBuilder or similar.
 * This is the only form of navigation drawer that should be used outside of the root
 * activity of a task.</li>
 * </ul>
 * <p/>
 * <p>Right side drawers should be used for actions, not navigation. This follows the pattern
 * established by the Action Bar that navigation should be to the left and actions to the right.
 * An action should be an operation performed on the current contents of the window,
 * for example enabling or disabling a data overlay on top of the current content.</p>
 */
public class NavigationDrawerActivity extends K9Activity
        implements MessageListFragment.MessageListFragmentGetListener,
        MessageViewFragment.MessageViewFragmentGetListener,
        IDrawerActivityListener
{

    public static final String ACTION_IMPORT_SETTINGS = "importSettings";
    public static final String EXTRA_STARTUP = "startup";
    private static final int ACTIVITY_REQUEST_PICK_SETTINGS_FILE = 100;
    private static final int DIALOG_NO_FILE_MANAGER = 40;

    public static final String SELECTED_TAB = "SELECTED_TAB";
    public static final int MAIL_TAB_SELECTED = 0;
    public static final int NEWS_TAB_SELECTED = 1;
    public static final int VIDEO_TAB_SELECTED = 2;
    public static final int OFFERS_TAB_SELECTED = 3;

    public static final int DEFAULT_SELECTED_TAB = MAIL_TAB_SELECTED;

    private DrawerLayout mDrawerLayout;
    private RecyclerView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private NavDrawerMenuAdapter mOffersAdapter;
    private NavDrawerMenuAdapter mNewsAdapter;
    private NavDrawerMenuAdapter mVideoAdapter;
    private MailNavDrawerMenuAdapter mMailAdapter;

    private int mSelectedTab;
    private BottomNavigationView mBottomNav;

    List<FolderInfoHolder> mMailTabMenuItems = new ArrayList<>();
    List<NavDrawerMenuItem> mOffersTabMenuItems;
    List<NavDrawerMenuItem> mNewsTabMenuItems;
    List<NavDrawerMenuItem> mVideoTabMenuItems;

    Account mAccount;

    @Inject MailPresenter mMailPresenter;

    private BottomNavigationView.OnNavigationItemSelectedListener mBottomNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {

            switch (item.getItemId()) {
                case R.id.menu_mail:
                    mSelectedTab = NavigationDrawerActivity.MAIL_TAB_SELECTED;
                    onMailTabClicked();
                    break;
                case R.id.menu_news:
                    mSelectedTab = NavigationDrawerActivity.NEWS_TAB_SELECTED;
                    onNewsTabClicked();
                    break;
                case R.id.menu_video:
                    mSelectedTab = NavigationDrawerActivity.VIDEO_TAB_SELECTED;
                    onVideoTabClicked();
                    break;
                case R.id.menu_offers:
                    mSelectedTab = NavigationDrawerActivity.OFFERS_TAB_SELECTED;
                    onOffersTabClicked();
                    break;
            }
            setSelectedTab(mSelectedTab);
            return true;
        }
    };

    private MessagingListener mListener = new MessagingListener() {

        @Override
        public void listFolders(Account account, List<LocalFolder> folders) {

            mMailTabMenuItems.clear();
            //mail tab drawer menu
            if (account.equals(mAccount)) {

                for (LocalFolder folder : folders) {
                    if (TiscaliUtility.isFolderInTopGroup(getApplication().getApplicationContext(), folder.getName())) {
                        mMailTabMenuItems.add(new FolderInfoHolder(NavigationDrawerActivity.this, folder, mAccount, -1));
                    }
                }
                TiscaliUtility.sortFoldersInTopGroup(NavigationDrawerActivity.this, mMailTabMenuItems);

                mMailAdapter = new MailNavDrawerMenuAdapter(account, mMailTabMenuItems, NavigationDrawerActivity.this, new MailNavDrawerClickListener() {
                    @Override
                    public void onSettingsClick() {
                        super.onSettingsClick();
                        showDialogSettings();
                    }

                    @Override
                    public void onFolderClick(Account account, FolderInfoHolder folder) {
                        super.onFolderClick(account, folder);

                        if(mMailPresenter != null) {
                            LocalSearch search = new LocalSearch(folder.name);
                            search.addAllowedFolder(folder.name);
                            search.addAccountUuid(account.getUuid());
                            mMailPresenter.showFolder(search);

                            mDrawerLayout.closeDrawer(mDrawerList);
                        }
                    }
                });

                setAdapterBasedOnSelectedTab(mSelectedTab);
            }

            super.listFolders(account, folders);
        }

        @Override
        public void listFoldersFailed(Account account, String message) {
            mMailTabMenuItems.clear();
            mMailAdapter = new MailNavDrawerMenuAdapter(account, mMailTabMenuItems, NavigationDrawerActivity.this, new MailNavDrawerClickListener() {
                @Override
                public void onSettingsClick() {
                    super.onSettingsClick();
                    showDialogSettings();
                }
            });

            super.listFoldersFailed(account, message);
        }
    };

    private FrameLayout mViewContainer;

    public static void importSettings(Context context) {
        Intent intent = new Intent(context, NavigationDrawerActivity.class);
        intent.setAction(ACTION_IMPORT_SETTINGS);
        context.startActivity(intent);
    }

    public static void listMessage(Context context) {
        Intent intent = new Intent(context, NavigationDrawerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(EXTRA_STARTUP, false);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<Account> accounts = Preferences.getPreferences(this).getAccounts();

        Intent intent = getIntent();
        // see if we should show the welcome message
        if (ACTION_IMPORT_SETTINGS.equals(intent.getAction())) {
            onImport();
        } else if (accounts.size() < 1) {
            WelcomeMessage.showWelcomeMessage(this);
            finish();
            return;
        }

        Intent mailIntent = getMailIntent(accounts.get(0));

        if(mMailPresenter == null) {
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
        mBottomNav = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        mBottomNav.setOnNavigationItemSelectedListener(mBottomNavigationItemSelectedListener);

        initNavigationDrawerMenuData();

        // set a custom shadow that overlays the main content when the drawer opens
//        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // improve performance by indicating the list if fixed size.
        mDrawerList.setHasFixedSize(true);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle( //R.drawable.ic_menu_black_24dp,  /* nav drawer image to replace 'Up' caret */
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                setAdapterBasedOnSelectedTab(mSelectedTab);
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        // init values
        if (savedInstanceState == null) {
            mSelectedTab = DEFAULT_SELECTED_TAB;
            View menuItem = mBottomNav.findViewById(mBottomNav.getMenu().getItem(mSelectedTab).getItemId());
            menuItem.performClick();
        } else {
            mSelectedTab = savedInstanceState.getInt(SELECTED_TAB);
            // selected item not automatically saved after rotation
            setSelectedTab(mSelectedTab);
        }

        setAdapterBasedOnSelectedTab(mSelectedTab);

        mViewContainer = (FrameLayout) findViewById(R.id.content_frame);
                
//        mMailPresenter = new MailPresenter(this, getMailIntent(accounts.get(0)));

//        mMailPresenter.setIntent(intent);

        if (useSplitView()) {
            getLayoutInflater().inflate(R.layout.split_message_list, mViewContainer, true);
        } else {
            getLayoutInflater().inflate(R.layout.message_list, mViewContainer, true);
        }

        mMailPresenter.onCreateView(getLayoutInflater(), savedInstanceState);

        mBottomNav.bringToFront();

    }

    public void setDrawerEnable(boolean isEnabled) {
        if ( isEnabled ) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mDrawerToggle.onDrawerStateChanged(DrawerLayout.LOCK_MODE_UNLOCKED);
            mDrawerToggle.setDrawerIndicatorEnabled(true);
            mDrawerToggle.setDrawerIndicatorEnabled(true);
            mDrawerToggle.syncState();

        }
        else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            mDrawerToggle.onDrawerStateChanged(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            mDrawerToggle.setDrawerIndicatorEnabled(false);
            mDrawerToggle.syncState();
        }
    }

    private void buildDaggerComponent(Intent intent) {
        ApplicationComponent component = ((K9) getApplicationContext()).getComponent();
        DaggerNavigationDrawerActivityComponent.builder()
                .applicationComponent(component)
                .activityModule(new ActivityModule(this, intent))
                .build()
                .inject(this);
    }

    private Intent getMailIntent(Account account) {
        LocalSearch search = new LocalSearch(account.getAutoExpandFolderName());
        search.addAllowedFolder(account.getAutoExpandFolderName());
        search.addAccountUuid(account.getUuid());
        return MessageList.intentDisplaySearch(this, search, false, true, true);
    }

    private boolean useSplitView() {
        K9.SplitViewMode splitViewMode = K9.getSplitViewMode();
        int orientation = getResources().getConfiguration().orientation;

        return (splitViewMode == K9.SplitViewMode.ALWAYS ||
                (splitViewMode == K9.SplitViewMode.WHEN_IN_LANDSCAPE &&
                        orientation == Configuration.ORIENTATION_LANDSCAPE));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_TAB, mSelectedTab);
        if(mMailPresenter != null) {
            mMailPresenter.onSaveInstanceState(outState);
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
        if(mMailPresenter != null) {
            mMailPresenter.onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mMailPresenter != null) {
            mMailPresenter.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mMailPresenter != null) {
            mMailPresenter.onPause();
        }
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

    private void setSelectedTab(int position) {
        for(int i=0; i<mBottomNav.getMenu().size(); i++) {
            if(i == position) {
                mBottomNav.getMenu().getItem(i).setChecked(true);
            } else {
                mBottomNav.getMenu().getItem(i).setChecked(false);
            }
        }
    }

    private void setAdapterBasedOnSelectedTab(int selectedTab) {
        BaseNavDrawerMenuAdapter selectedAdapter = null;
        switch (selectedTab) {
            case MAIL_TAB_SELECTED:
                selectedAdapter = mMailAdapter;
                break;
            case NEWS_TAB_SELECTED:
                selectedAdapter = mNewsAdapter;
                break;
            case VIDEO_TAB_SELECTED:
                selectedAdapter = mVideoAdapter;
                break;
            case OFFERS_TAB_SELECTED:
                selectedAdapter = mOffersAdapter;
                break;
        }

        if(mDrawerList != null && selectedAdapter != null) {
            mDrawerList.setAdapter(selectedAdapter);
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

        // mail tab
        List<Account> accounts = Preferences.getPreferences(this).getAccounts();
        if(accounts != null && !accounts.isEmpty()) {
            mAccount = accounts.get(0);
            MessagingController.getInstance(getApplication()).listFolders(mAccount, false, mListener);
        }

        // news, video and offers
        String meObjectJsonString = getJsonString(getResources().openRawResource(R.raw.me_object));

        //news tab drawer menu
        mNewsTabMenuItems = NavDrawerMenuItem.getMenuList(meObjectJsonString, "news");
        mNewsAdapter = new NavDrawerMenuAdapter(mNewsTabMenuItems, this);

        //video tab drawer menu
        mVideoTabMenuItems = NavDrawerMenuItem.getMenuList(meObjectJsonString, "video");
        mVideoAdapter = new NavDrawerMenuAdapter(mVideoTabMenuItems, this);

        //offers tab drawer menu
        mOffersTabMenuItems = NavDrawerMenuItem.getMenuList(meObjectJsonString, "offers");
        mOffersAdapter = new NavDrawerMenuAdapter(mOffersTabMenuItems, this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.navigation_drawer, menu);
        return true;
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
//        menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
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
        mSelectedTab = MAIL_TAB_SELECTED;
        setAdapterBasedOnSelectedTab(mSelectedTab);
        // TODO show content
    }

    private void onNewsTabClicked() {
        mSelectedTab = NEWS_TAB_SELECTED;
        setAdapterBasedOnSelectedTab(mSelectedTab);
        // TODO show content
    }

    private void onVideoTabClicked() {
        mSelectedTab = VIDEO_TAB_SELECTED;
        setAdapterBasedOnSelectedTab(mSelectedTab);
        // TODO show content
    }

    private void onOffersTabClicked() {
        mSelectedTab = OFFERS_TAB_SELECTED;
        // TODO show content
    }

    private void forceBuildDaggerComponent() {
        List<Account> accounts = Preferences.getPreferences(this).getAccounts();
        Intent intent = getMailIntent(accounts.get(0));
        buildDaggerComponent(intent);
    }

    @Override
    public MessageListFragment.MessageListFragmentListener getMessageListFragmentListner() {
        if(mMailPresenter == null) {
            forceBuildDaggerComponent();
        }
        return mMailPresenter;
    }

    @Override
    public MessageViewFragment.MessageViewFragmentListener getMessageViewFragmentListner() {
        if(mMailPresenter == null) {
            forceBuildDaggerComponent();
        }
        return mMailPresenter;
    }

    private void showDialogSettings() {
        mDrawerLayout.closeDrawer(mDrawerList);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(R.array.settings_titles, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        AccountSettings.actionSettings(NavigationDrawerActivity.this, mAccount);
                        break;
                    case 1:
                        Prefs.actionPrefs(NavigationDrawerActivity.this);
                }
            }
        });
        builder.create().show();
    }
}
