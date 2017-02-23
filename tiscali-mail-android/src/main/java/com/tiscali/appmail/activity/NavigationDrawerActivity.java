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

package com.tiscali.appmail.activity;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.smartadserver.android.library.SASBannerView;
import com.smartadserver.android.library.SASInterstitialView;
import com.smartadserver.android.library.model.SASAdElement;
import com.smartadserver.android.library.ui.SASAdView;
import com.smartadserver.android.library.ui.SASRotatingImageLoader;
import com.tiscali.appmail.Account;
import com.tiscali.appmail.ApplicationComponent;
import com.tiscali.appmail.K9;
import com.tiscali.appmail.Preferences;
import com.tiscali.appmail.R;
import com.tiscali.appmail.activity.misc.BottomNavigationViewHelper;
import com.tiscali.appmail.activity.setup.AccountSetupBasics;
import com.tiscali.appmail.activity.setup.migrations.SettingsMigrations;
import com.tiscali.appmail.analytics.LogManager;
import com.tiscali.appmail.api.ApiController;
import com.tiscali.appmail.api.model.DeviceRegister;
import com.tiscali.appmail.api.model.MainConfig;
import com.tiscali.appmail.api.model.Me;
import com.tiscali.appmail.controller.MessagingController;
import com.tiscali.appmail.fragment.MailPresenter;
import com.tiscali.appmail.fragment.MediaFragment;
import com.tiscali.appmail.fragment.MediaPresenter;
import com.tiscali.appmail.fragment.MessageListFragment;
import com.tiscali.appmail.fragment.NewsPresenter;
import com.tiscali.appmail.fragment.OffersPresenter;
import com.tiscali.appmail.fragment.VideoPresenter;
import com.tiscali.appmail.helper.CaptivePortalHelper;
import com.tiscali.appmail.helper.NetworkHelper;
import com.tiscali.appmail.preferences.FirebasePreference;
import com.tiscali.appmail.preferences.StorageEditor;
import com.tiscali.appmail.preferences.WelcomePreference;
import com.tiscali.appmail.search.LocalSearch;
import com.tiscali.appmail.search.SearchSpecification;
import com.tiscali.appmail.service.TiscaliAppFirebaseInstanceIDService;
import com.tiscali.appmail.service.TiscaliAppFirebaseMessagingService;
import com.tiscali.appmail.ui.messageview.MessageViewFragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


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

    private static final String ACTION_SHORTCUT = "shortcut";
    private static final String EXTRA_SPECIAL_FOLDER = "special_folder";

    private static final String EXTRA_SEARCH = "search";
    private static final String EXTRA_NO_THREADING = "no_threading";
    private static final String EXTRA_MESSAGE_REFERENCE = "message_reference";

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
    public static final String GET_PARAMS_VERSION = "?version=";
    public static final String GET_PARAMS_ID = "&UDID=";
    public static final String GET_PARAMS_PLATFORM = "&platform=";
    public static final String PLATFORM = "android";
    public static final int INTERSTITIAL_INTERVAL_TIME = 1000 * 60 * 5;

    public static int DEFAULT_SELECTED_TAB = NEWS_TAB_SELECTED;
    public static final String DEFAULT_TAB_KEY = "default_tab";

    public static final String INTERSTITIAL_TIME = "INTERSTITIAL_TIME";
    /*****************************************
     * Ad Constants
     *****************************************/
    private final static int SITE_ID = 104808; // 45409
    private final static String PAGE_ID = "663262";
    private final static int FORMAT_ID = 15140;
    private final static String TARGET = "";

    private final static int INTERSTITIAL_SITE_ID = 104808;
    private final static String INTERSTITIAL_PAGE_ID = "663264";
    private final static int INTERSTITIAL_FORMAT_ID = 12167;
    private final static String INTERSTITIAL_TARGET = "";

    private DrawerLayout mDrawerLayout;
    private RecyclerView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private int mSelectedTab;
    private BottomNavigationView mBottomNav;

    private SASBannerView mBannerView;
    private SASAdView.AdResponseHandler mBannerResponseHandler;
    private SASInterstitialView mInterstitialView;
    private SASAdView.AdResponseHandler interstitialResponseHandler;
    private int mInterstitialState;

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
    @Inject
    LogManager mLogManager;

    private FrameLayout mViewContainer;
    private BroadcastReceiver mBroadcastReceiver;
    private LocalBroadcastManager mLocalBroadcastManager;

    private Toolbar mToolbar;

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
    private LinearLayout mBannerContainer;
    private NavigationDrawerActivityComponent mComponent;

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

    public static Intent shortcutIntent(Context context, String specialFolder) {
        Intent intent = new Intent(context, NavigationDrawerActivity.class);
        intent.setAction(ACTION_SHORTCUT);
        intent.putExtra(EXTRA_SPECIAL_FOLDER, specialFolder);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return intent;
    }

    public static Intent actionDisplayMessageIntent(Context context,
                                                    MessageReference messageReference) {
        Intent intent = new Intent(context, NavigationDrawerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(EXTRA_MESSAGE_REFERENCE, messageReference);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_K9_Light_NoActionBar_Base);
        super.onCreate(savedInstanceState);

        Preferences pref = Preferences.getPreferences(this);

        // TEST
        NetworkHelper.getInstance(this);
        List<Account> accounts = pref.getAccounts();
        WelcomePreference prefManager = new WelcomePreference(this);
        Intent intent = getIntent();
        // see if we should show the welcome message
        if (ACTION_IMPORT_SETTINGS.equals(intent.getAction())) {
            onImport();
        } else if (prefManager.isFirstTimeLaunch()) {
            Intent welcomeIntent = new Intent(this, WelcomeActivity.class);
            getActivity().startActivity(welcomeIntent);
            finish();
            return;
        } else if (accounts.size() < 1) {
            AccountSetupBasics.actionNewAccount(this);
            finish();
            return;
        }

        if (UpgradeDatabases.actionUpgradeDatabases(this, intent)) {
            finish();
            return;
        }

        String accountUUid = intent.getStringExtra(EXTRA_ACCOUNT);
        Intent mailIntent;
        Account account = null;
        // mail search
        if (intent.getStringExtra(SearchManager.QUERY) != null) {
            mailIntent = intent;
            intent.putExtra(EXTRA_STARTUP, false);
        } else if (intent.getExtras() != null && intent.getExtras()
                .getString(TiscaliAppFirebaseMessagingService.NOTIFICATION_SECTION) != null) {
            mailIntent = intent;

        } else if (accountUUid != null) {
            account = pref.getAccount(accountUUid);
            mailIntent = getMailIntent(account);
        } else if (intent.getExtras() != null && intent.getExtras().get(EXTRA_SEARCH) != null) {
            mailIntent = intent;
            account = getAccount(accounts, (LocalSearch) intent.getExtras().get(EXTRA_SEARCH));
            if (account == null) {
                account = accounts.get(0);
            }
        } else {
            account = accounts.get(0);
            mailIntent = getMailIntent(account);
        }

        // upgrade settings from old Tiscali Mail to new Tiscali.it
        SettingsMigrations.upgradeSettings(pref, account);
        updateAccount(account);

        if (mNewsPresenter == null) {
            buildDaggerComponent(mailIntent);
        }

        setContentView(R.layout.activity_navigation_drawer);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (RecyclerView) findViewById(R.id.left_drawer);
        ViewGroup.LayoutParams params = mDrawerList.getLayoutParams();
        params.width = getNavigationDrawerWidth();
        mDrawerList.setLayoutParams(params);
        mBottomNav = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        BottomNavigationViewHelper.disableShiftMode(mBottomNav);
        mViewContainer = (FrameLayout) findViewById(R.id.content_frame);
        mBottomNav.setOnNavigationItemSelectedListener(mBottomNavigationItemSelectedListener);
        mBannerContainer = (LinearLayout) findViewById(R.id.banner_ll);
        initBannerView();
        initInterstitialView();

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
                logDrawerOpened();
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        DEFAULT_SELECTED_TAB = pref.getStorage().getInt(DEFAULT_TAB_KEY, DEFAULT_SELECTED_TAB);

        mSelectedTab = NONE_TAB_SELECTED;

        if (savedInstanceState == null) {
            int tempSelectedTab = DEFAULT_SELECTED_TAB;
            // open mail when coming from registration/change of password/account selection
            if (!intent.getBooleanExtra(EXTRA_STARTUP, true) || (intent.getExtras() != null
                    && intent.getExtras().get(EXTRA_SEARCH) != null)) {
                tempSelectedTab = MAIL_TAB_SELECTED;
            } else if (intent.getExtras() != null && intent.getExtras()
                    .getString(TiscaliAppFirebaseMessagingService.NOTIFICATION_SECTION) != null) {

                String extrasSection = intent.getExtras()
                        .getString(TiscaliAppFirebaseMessagingService.NOTIFICATION_SECTION);
                if (extrasSection != null) {
                    if (extrasSection
                            .equals(TiscaliAppFirebaseMessagingService.NOTIFICATION_SECTION_NEWS)) {
                        tempSelectedTab = NEWS_TAB_SELECTED;
                    } else if (extrasSection.equals(
                            TiscaliAppFirebaseMessagingService.NOTIFICATION_SECTION_VIDEO)) {
                        tempSelectedTab = VIDEO_TAB_SELECTED;
                    } else if (extrasSection.equals(
                            TiscaliAppFirebaseMessagingService.NOTIFICATION_SECTION_OFFERS)) {
                        tempSelectedTab = OFFERS_TAB_SELECTED;
                    }
                }

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

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                if (NetworkHelper.ACTION_NETWORK_CONNECTIVITY_CHANGE.equals(intent.getAction())) {
                    if (NetworkHelper.getInstance(getApplicationContext()).isConnected()) {
                        Observable.empty().observeOn(Schedulers.newThread())
                                .subscribe(new Subscriber<Object>() {
                                    @Override
                                    public void onCompleted() {
                                        if (CaptivePortalHelper.getInstance(getApplicationContext())
                                                .isCaptivePortalConnection()) {
                                            // show the login activity
                                            CaptivePortalHelper.getInstance(getApplicationContext())
                                                    .showLoginWebView();
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
                } else if (TiscaliAppFirebaseInstanceIDService.TOKEN_BROADCAST
                        .equals(intent.getAction())) {
                    if (intent.getStringExtra(
                            TiscaliAppFirebaseInstanceIDService.FIREBASE_PUSH_TOKEN) != null) {
                        String token = intent.getStringExtra(
                                TiscaliAppFirebaseInstanceIDService.FIREBASE_PUSH_TOKEN);
                        Log.i("APZ", "Push token: " + token);
                        mApiController.pushRegister(token,
                                TiscaliAppFirebaseInstanceIDService.FIREBASE_PLATFORM,
                                TiscaliAppFirebaseInstanceIDService.FIREBASE_ENVIRONMENT,
                                new Action1<DeviceRegister>() {
                                    @Override
                                    public void call(DeviceRegister register) {
                                        Log.i("APZ",
                                                "DeviceRegister Status: " + register.getStatus());
                                        Log.i("APZ",
                                                "DeviceRegister Device: " + register.getDevice());
                                    }
                                });
                    }
                } else if (TiscaliAppFirebaseMessagingService.TOKEN_VERIFY_BROADCAST
                        .equals(intent.getAction())) {
                    if (intent.getStringExtra(
                            TiscaliAppFirebaseMessagingService.FIREBASE_OTP_TOKEN) != null) {
                        String otp = intent.getStringExtra(
                                TiscaliAppFirebaseMessagingService.FIREBASE_OTP_TOKEN);
                        Log.i("APZ", "Push otp: " + otp);
                        mApiController.pushActivate(otp, new Action1<DeviceRegister>() {

                            @Override
                            public void call(DeviceRegister register) {
                                Log.i("APZ", "DeviceActivate Status: " + register.getStatus());
                                Log.i("APZ", "DeviceActivate Device: " + register.getDevice());
                                FirebasePreference.getInstance(getApplicationContext())
                                        .resetToken();
                            }
                        });
                    }
                }
            }

        };

        loadBannerAd();

        long startMillis = pref.getStorage().getLong(INTERSTITIAL_TIME, 0L);
        long nowMillis = System.currentTimeMillis();

        if (nowMillis - startMillis > INTERSTITIAL_INTERVAL_TIME) {

            loadInterstitialAd();

            StorageEditor editor = Preferences.getPreferences(this).getStorage().edit();
            editor.putLong(INTERSTITIAL_TIME, nowMillis);
            editor.commit();
        }
    }

    private void initBannerView() {
        // Fetch the SASBannerView inflated from the main.xml layout file
        mBannerView = (SASBannerView) this.findViewById(R.id.banner);

        // Add a loader view on the banner. This view covers the banner placement, to indicate
        // progress, whenever the banner is loading an ad.
        // This is optional
        View loader = new SASRotatingImageLoader(this);
        loader.setBackgroundColor(0x66000000);
        mBannerView.setLoaderView(loader);

        // Instantiate the response handler used when loading an ad on the banner
        mBannerResponseHandler = new SASAdView.AdResponseHandler() {
            public void adLoadingCompleted(SASAdElement adElement) {
                Log.i("APZ", "Banner loading completed");
            }

            public void adLoadingFailed(Exception e) {
                Log.i("APZ", "Banner loading failed: " + e.getMessage());
            }
        };
    }

    /**
     * Loads an ad on the banner
     */
    private void loadBannerAd() {
        // Load banner ad with appropriate parameters
        // (siteID,pageID,formatID,master,targeting,adResponseHandler)
        mBannerView.loadAd(SITE_ID, PAGE_ID, FORMAT_ID, true, TARGET, mBannerResponseHandler);
    }

    private Account getAccount(List<Account> accounts, LocalSearch localSearch) {
        for (Account cAccount : accounts) {
            for (String uuid : localSearch.getAccountUuids()) {
                if (uuid.equals(cAccount.getUuid())) {
                    return cAccount;
                }
            }
        }
        return null;
    }

    /**
     * initialize the SASInterstitialView instance of this Activity
     */
    private void initInterstitialView() {

        // Create SASInterstitialView instance
        mInterstitialView = new SASInterstitialView(this);

        // Add a loader view on the interstitial view. This view is displayed fullscreen, to
        // indicate progress,
        // whenever the interstitial is loading an ad.
        View loader = new SASRotatingImageLoader(this);
        loader.setBackgroundColor(Color.WHITE);
        mInterstitialView.setLoaderView(loader);

        // Add a state change listener on the SASInterstitialView instance to monitor MRAID states
        // changes.
        // Useful for instance to perform some actions as soon as the interstitial disappears.
        mInterstitialView.addStateChangeListener(new SASAdView.OnStateChangeListener() {
            public void onStateChanged(SASAdView.StateChangeEvent stateChangeEvent) {
                switch (stateChangeEvent.getType()) {
                    case SASAdView.StateChangeEvent.VIEW_DEFAULT:
                        mInterstitialState = SASAdView.StateChangeEvent.VIEW_DEFAULT;
                        // the MRAID Ad View is in default state
                        Log.i("APZ", "Interstitial MRAID state : DEFAULT");
                        Observable.empty().observeOn(Schedulers.newThread())
                                .delay(1500, TimeUnit.MILLISECONDS)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<Object>() {
                                    @Override
                                    public void onCompleted() {
                                        if (mInterstitialState == SASAdView.StateChangeEvent.VIEW_DEFAULT) {
                                            Log.i("APZ", "Interstitial FORCE CLOSE");
                                            mInterstitialView.onDestroy();
                                            initInterstitialView();
                                        }
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                    }

                                    @Override
                                    public void onNext(Object o) {
                                    }
                                });
                        break;
                    case SASAdView.StateChangeEvent.VIEW_EXPANDED:
                        // the MRAID Ad View is in expanded state
                        mInterstitialState = SASAdView.StateChangeEvent.VIEW_EXPANDED;
                        Log.i("APZ", "Interstitial MRAID state : EXPANDED");
                        break;
                    case SASAdView.StateChangeEvent.VIEW_HIDDEN:
                        // the MRAID Ad View is in hidden state
                        mInterstitialState = SASAdView.StateChangeEvent.VIEW_HIDDEN;
                        Log.i("APZ", "Interstitial MRAID state : HIDDEN");
                        break;
                }
            }
        });

        // Instantiate the response handler used when loading an interstitial ad
        interstitialResponseHandler = new SASAdView.AdResponseHandler() {

            public void adLoadingCompleted(SASAdElement adElement) {
                Log.i("APZ", "Interstitial loading completed");
            }

            public void adLoadingFailed(Exception e) {
                Log.i("APZ", "Interstitial loading failed: " + e.getMessage());
            }

        };
    }

    /**
     * Loads an interstitial ad
     */
    private void loadInterstitialAd() {
        // Load interstitial ad with appropriate parameters
        // (siteID,pageID,formatID,master,targeting,adResponseHandler)
        mInterstitialView.loadAd(INTERSTITIAL_SITE_ID, INTERSTITIAL_PAGE_ID, INTERSTITIAL_FORMAT_ID,
                true, INTERSTITIAL_TARGET, interstitialResponseHandler);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        if (intent.getExtras() != null && intent.getExtras()
                .getString(TiscaliAppFirebaseMessagingService.NOTIFICATION_SECTION) != null) {
            String extrasSection = intent.getExtras()
                    .getString(TiscaliAppFirebaseMessagingService.NOTIFICATION_SECTION);

            if (extrasSection
                    .equals(TiscaliAppFirebaseMessagingService.NOTIFICATION_SECTION_NEWS)) {
                mBottomNav.findViewById(mBottomNav.getMenu().getItem(NEWS_TAB_SELECTED).getItemId())
                        .performClick();
                mNewsPresenter.onNewIntent(intent);
                return;
            } else if (extrasSection
                    .equals(TiscaliAppFirebaseMessagingService.NOTIFICATION_SECTION_VIDEO)) {
                mBottomNav
                        .findViewById(mBottomNav.getMenu().getItem(VIDEO_TAB_SELECTED).getItemId())
                        .performClick();
                mVideoPresenter.onNewIntent(intent);
                return;
            } else if (extrasSection
                    .equals(TiscaliAppFirebaseMessagingService.NOTIFICATION_SECTION_OFFERS)) {
                mBottomNav
                        .findViewById(mBottomNav.getMenu().getItem(OFFERS_TAB_SELECTED).getItemId())
                        .performClick();
                mOffersPresenter.onNewIntent(intent);
                return;
            }
        }

        switch (mSelectedTab) {
            case MAIL_TAB_SELECTED:
                if (mMailPresenter != null) {

                    mBottomNav
                            .findViewById(
                                    mBottomNav.getMenu().getItem(MAIL_TAB_SELECTED).getItemId())
                            .performClick();

                    if (!Intent.ACTION_MAIN.equals(intent.getAction())) {
                        mMailPresenter.onNewIntent(intent);
                    }
                }
                break;

            case NEWS_TAB_SELECTED:
                mBottomNav.findViewById(mBottomNav.getMenu().getItem(NEWS_TAB_SELECTED).getItemId())
                        .performClick();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NetworkHelper.ACTION_NETWORK_CONNECTIVITY_CHANGE);

        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, intentFilter);
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver,
                new IntentFilter(TiscaliAppFirebaseInstanceIDService.TOKEN_BROADCAST));
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver,
                new IntentFilter(TiscaliAppFirebaseMessagingService.TOKEN_VERIFY_BROADCAST));
    }

    @Override
    protected void onStop() {
        mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
        super.onStop();

    }

    @Override
    public void updateAccount(Account account) {
        if (account != null) {
            MessagingController.getInstance(getApplication()).listFoldersSynchronous(account, true,
                    null);
            MessagingController.getInstance(getApplication()).checkMail(this, account, true, true,
                    null);
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

            showBottomNav();
        } else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            mDrawerToggle.onDrawerStateChanged(DrawerLayout.STATE_IDLE);
            mDrawerToggle.setDrawerIndicatorEnabled(false);
            mDrawerToggle.syncState();

            hideBottomNav();
        }
    }

    public void hideBottomNav() {
        mBannerContainer.animate().translationY(mBottomNav.getHeight())
                .setListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        // mBottomNav.setVisibility(View.GONE);
                    }

                });
    }

    public void showBottomNav() {
        mBannerContainer.animate().translationY(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                // mBottomNav.setVisibility(View.VISIBLE);
            }
        });
    }

    public NavigationDrawerActivityComponent getComponent() {
        return mComponent;
    }

    private void buildDaggerComponent(Intent intent) {
        ApplicationComponent component = ((K9) getApplicationContext()).getComponent();
        mComponent =
                DaggerNavigationDrawerActivityComponent.builder().applicationComponent(component)
                        .activityModule(new ActivityModule(this, intent)).build();
        mComponent.inject(this);
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

    @Override
    protected void onDestroy() {
        NetworkHelper.resetInstance();
        if (mInterstitialView != null) {
            mInterstitialView.onDestroy();
        }
        super.onDestroy();
    }

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
        // change toolbar height in landscape
        if (mToolbar != null) {
            LinearLayout.LayoutParams layoutParams =
                    (LinearLayout.LayoutParams) mToolbar.getLayoutParams();
            layoutParams.height = getResources().getDimensionPixelSize(R.dimen.action_bar_height);
            mToolbar.setLayoutParams(layoutParams);
        }
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

    public void logDrawerOpened() {
        String tag = "";

        switch (mSelectedTab) {
            case MAIL_TAB_SELECTED:
                tag = getResources().getString(R.string.com_tiscali_appmail_Drawer_Mail);
                break;
            case NEWS_TAB_SELECTED:
                tag = getResources().getString(R.string.com_tiscali_appmail_Drawer_News);
                break;
            case VIDEO_TAB_SELECTED:
                tag = getResources().getString(R.string.com_tiscali_appmail_Drawer_Video);
                break;
            case OFFERS_TAB_SELECTED:
                tag = getResources().getString(R.string.com_tiscali_appmail_Drawer_Promo);
                break;
        }

        mLogManager.track(tag);
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
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        if (action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
            if (mNewsPresenter != null
                    && mNewsPresenter.getDisplayMode() == MediaPresenter.DisplayMode.MEDIA_DETAIL) {
                mNewsPresenter.goBackOnHistory();
            } else if (mVideoPresenter != null && mVideoPresenter
                    .getDisplayMode() == MediaPresenter.DisplayMode.MEDIA_DETAIL) {
                mVideoPresenter.goBackOnHistory();
            } else if (mOffersPresenter != null && mOffersPresenter
                    .getDisplayMode() == MediaPresenter.DisplayMode.MEDIA_DETAIL) {
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
        } else if (action == KeyEvent.ACTION_DOWN) {
            if (mMailPresenter != null) {
                return mMailPresenter.onCustomKeyDown(keyCode, event);

            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void updateMainConfig(MainConfig mainConfig) {
        // Nop
    }

    @Override
    public ApiController getApiController() {
        return mApiController;
    }

    @Override
    public void showInformations() {
        MainConfig mainConfig = null;
        String url = null;
        if (getApiController() != null) {
            mainConfig = getApiController().getMainConfig();
        }
        if (mainConfig != null && mainConfig.getEndpoints() != null
                && mainConfig.getEndpoints().getInfoAbout() != null) {
            url = (mainConfig.getEndpoints().getInfoAbout().getUrl());
        }
        if (url != null) {
            Intent myIntent = new Intent(getActivity(), BrowserActivity.class);
            StringBuffer bufferUrl = new StringBuffer(url);
            String androidId = Settings.Secure.getString(
                    getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

            PackageInfo pInfo = null;
            String version = null;
            try {
                pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                version = pInfo.versionName;
                if (version != null) {
                    bufferUrl.append(GET_PARAMS_VERSION + version);
                    bufferUrl.append(GET_PARAMS_PLATFORM + PLATFORM);
                    bufferUrl.append(GET_PARAMS_ID + androidId);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            myIntent.putExtra(BrowserActivity.EXTRA_URL, bufferUrl.toString());

            this.startActivityForResult(myIntent,
                    MediaPresenter.MEDIA_PRESENTER_INFORMATION_SETTINGS);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MediaPresenter.MEDIA_PRESENTER_BROWSING) {
            if (resultCode == Activity.RESULT_OK) {
                if (mNewsPresenter != null) {
                    mNewsPresenter.onActivityResult();
                } else if (mVideoPresenter != null && mVideoPresenter
                        .getDisplayMode() == MediaPresenter.DisplayMode.MEDIA_DETAIL) {
                    mVideoPresenter.onActivityResult();
                } else if (mOffersPresenter != null && mOffersPresenter
                        .getDisplayMode() == MediaPresenter.DisplayMode.MEDIA_DETAIL) {
                    mOffersPresenter.onActivityResult();
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
