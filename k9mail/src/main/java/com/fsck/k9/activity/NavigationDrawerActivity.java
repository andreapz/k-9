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

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.fsck.k9.R;
import com.fsck.k9.adapter.NavDrawerMenuAdapter;
import com.fsck.k9.fragment.ContainerFragment;
import com.fsck.k9.listener.BottomNavigationViewListener;
import com.fsck.k9.listener.ContentFragmentListener;
import com.fsck.k9.model.NavDrawerMenuItem;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


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
public class NavigationDrawerActivity extends K9Activity implements BottomNavigationViewListener {

    public static final String SELECTED_TAB = "SELECTED_TAB";
    public static final int MAIL_TAB_SELECTED = 0;
    public static final int NEWS_TAB_SELECTED = 1;
    public static final int VIDEO_TAB_SELECTED = 2;
    public static final int OFFERS_TAB_SELECTED = 3;

    public static final int DEFAULT_SELECTED_TAB = NEWS_TAB_SELECTED;

    private DrawerLayout mDrawerLayout;
    private RecyclerView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private NavDrawerMenuAdapter mOffersAdapter;
    private NavDrawerMenuAdapter mNewsAdapter;
    private NavDrawerMenuAdapter mVideoAdapter;

    private int mSelectedTab;
    private ContentFragmentListener mContentFragmentListener;

    List<NavDrawerMenuItem> mOffersTabMenuItems;
    List<NavDrawerMenuItem> mNewsTabMenuItems;
    List<NavDrawerMenuItem> mVideoTabMenuItems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);

        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (RecyclerView) findViewById(R.id.left_drawer);

        initData();

        // set a custom shadow that overlays the main content when the drawer opens
//        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // improve performance by indicating the list if fixed size.
        mDrawerList.setHasFixedSize(true);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_menu_black_24dp,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
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
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // init values
        if (savedInstanceState == null) {
            mSelectedTab = DEFAULT_SELECTED_TAB;
            Bundle bundle = new Bundle();
            bundle.putInt(SELECTED_TAB, mSelectedTab);
            showContainerFragment(ContainerFragment.class.getCanonicalName(), bundle);
        } else {
            mSelectedTab = savedInstanceState.getInt(SELECTED_TAB);
            ContainerFragment bottomNavigationViewFragment = (ContainerFragment) getFragmentManager().findFragmentById(R.id.content_frame);
            if(bottomNavigationViewFragment != null) {
                mContentFragmentListener = bottomNavigationViewFragment;
            }
        }

        setAdapterBasedOnSelectedTab(mSelectedTab);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_TAB, mSelectedTab);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putInt(SELECTED_TAB, mSelectedTab);
    }

    private void setAdapterBasedOnSelectedTab(int selectedTab) {
        NavDrawerMenuAdapter selectedAdapter = null;
        switch (selectedTab) {
            case MAIL_TAB_SELECTED:
//                selectedAdapter = mOffersAdapter;
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

    private void showContainerFragment(String fragmentName, Bundle args){

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        Fragment fragment = Fragment.instantiate(this, fragmentName, args);
        ft.add(R.id.content_frame, fragment);
        ft.commit();

        if(fragment instanceof ContainerFragment) {
            ContainerFragment bottomNavigationViewFragment = (ContainerFragment) fragment;
            mContentFragmentListener = bottomNavigationViewFragment;
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
    private void initData() {

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

    @Override
    public void onMailTabClicked() {
        mSelectedTab = MAIL_TAB_SELECTED;
        setAdapterBasedOnSelectedTab(mSelectedTab);
        // TODO show content
    }

    @Override
    public void onNewsTabClicked() {
        mSelectedTab = NEWS_TAB_SELECTED;
        setAdapterBasedOnSelectedTab(mSelectedTab);
        // TODO show content
    }

    @Override
    public void onVideoTabClicked() {
        mSelectedTab = VIDEO_TAB_SELECTED;
        setAdapterBasedOnSelectedTab(mSelectedTab);
        // TODO show content
    }

    @Override
    public void onOffersTabClicked() {
        mSelectedTab = OFFERS_TAB_SELECTED;
        // TODO show content
    }
}
