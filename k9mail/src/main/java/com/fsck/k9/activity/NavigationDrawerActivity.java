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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.BottomNavigationView;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
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
import com.fsck.k9.activity.setup.WelcomeMessage;
import com.fsck.k9.fragment.MailPresenter;
import com.fsck.k9.fragment.MessageListFragment;
import com.fsck.k9.search.LocalSearch;
import com.fsck.k9.ui.messageview.MessageViewFragment;

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

    public static final int DEFAULT_SELECTED_TAB = NEWS_TAB_SELECTED;

    private int mSelectedTab;
    private BottomNavigationView mBottomNav;

    private DrawerLayout mDrawerLayout;
    private RecyclerView mDrawerList;

    @Inject MailPresenter mMailPresenter;

    private BottomNavigationView.OnNavigationItemSelectedListener mBottomNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem item) {

            switch (item.getItemId()) {
                case R.id.menu_mail:
                    mSelectedTab = MAIL_TAB_SELECTED;
                    break;
                case R.id.menu_news:
                    mSelectedTab = NEWS_TAB_SELECTED;
                    break;
                case R.id.menu_video:
                    mSelectedTab = VIDEO_TAB_SELECTED;
                    break;
                case R.id.menu_offers:
                    mSelectedTab = OFFERS_TAB_SELECTED;
                    break;
            }
            if(mMailPresenter != null) {
                mMailPresenter.setAdapterBasedOnSelectedTab(mSelectedTab);
            }
            setCheckedTab(mSelectedTab);
            return true;
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

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (RecyclerView) findViewById(R.id.left_drawer);
        mBottomNav = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        mBottomNav.setOnNavigationItemSelectedListener(mBottomNavigationItemSelectedListener);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // init values
        if (savedInstanceState == null) {
            mSelectedTab = DEFAULT_SELECTED_TAB;
            View menuItem = mBottomNav.findViewById(mBottomNav.getMenu().getItem(mSelectedTab).getItemId());
            menuItem.performClick();
        } else {
            mSelectedTab = savedInstanceState.getInt(SELECTED_TAB);
            // selected item not automatically saved after rotation
            setCheckedTab(mSelectedTab);
        }

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

    private void setCheckedTab(int position) {
        for(int i=0; i<mBottomNav.getMenu().size(); i++) {
            if(i == position) {
                mBottomNav.getMenu().getItem(i).setChecked(true);
            } else {
                mBottomNav.getMenu().getItem(i).setChecked(false);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.navigation_drawer, menu);
        if(mMailPresenter != null) {
            mMailPresenter.onCreateOptionsMenu(menu, getMenuInflater());
        }

        return true;
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);

        if(mMailPresenter != null) {
            return mMailPresenter.onPrepareOptionsMenu(menu);
        }

//        menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(mMailPresenter != null) {
            return mMailPresenter.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(mMailPresenter != null) {
            mMailPresenter.onPostCreate();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(mMailPresenter != null) {
            mMailPresenter.onConfigurationChanged(newConfig);
        }
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

    @Override
    public void onBackPressed() {
        if(mMailPresenter != null
                && (mMailPresenter.getDisplayMode() == MailPresenter.DisplayMode.MESSAGE_VIEW
                    && mMailPresenter.getMessageListWasDisplayed())) {
                mMailPresenter.showMessageList();
        }
        else {
            super.onBackPressed();
        }
    }


    @Override
    public int getSelectedTab() {
        return mSelectedTab;
    }

    @Override
    public void onInvalidateOptionsMenu() {
        invalidateOptionsMenu();
    }

    @Override
    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    @Override
    public RecyclerView getDrawerList() {
        return mDrawerList;
    }

    @Override
    public ActionBar getDrawerActivityActionBar() {
        return getSupportActionBar();
    }

    @Override
    public CharSequence getActionBarTitle() {
        return getTitle();
    }
}
