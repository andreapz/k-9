package com.tiscali.appmail.fragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tiscali.appmail.Account;
import com.tiscali.appmail.K9;
import com.tiscali.appmail.Preferences;
import com.tiscali.appmail.R;
import com.tiscali.appmail.activity.Accounts;
import com.tiscali.appmail.activity.ActivityListener;
import com.tiscali.appmail.activity.FolderInfoHolder;
import com.tiscali.appmail.activity.FolderList;
import com.tiscali.appmail.activity.INavigationDrawerActivityListener;
import com.tiscali.appmail.activity.MessageReference;
import com.tiscali.appmail.activity.NavigationDrawerActivity;
import com.tiscali.appmail.activity.TiscaliUtility;
import com.tiscali.appmail.activity.compose.MessageActions;
import com.tiscali.appmail.activity.misc.SwipeGestureDetector.OnSwipeGestureListener;
import com.tiscali.appmail.activity.setup.AccountSettings;
import com.tiscali.appmail.activity.setup.AccountSetupBasics;
import com.tiscali.appmail.activity.setup.Prefs;
import com.tiscali.appmail.adapter.AccountsAdapterClickListener;
import com.tiscali.appmail.adapter.MailAdapterClickListener;
import com.tiscali.appmail.controller.MessagingController;
import com.tiscali.appmail.fragment.MessageListFragment.MessageListFragmentListener;
import com.tiscali.appmail.helper.SizeFormatter;
import com.tiscali.appmail.mail.Message;
import com.tiscali.appmail.mailstore.LocalFolder;
import com.tiscali.appmail.mailstore.StorageManager;
import com.tiscali.appmail.presenter.PresenterLifeCycle;
import com.tiscali.appmail.provider.TiscaliSearchRecentSuggestionsProvider;
import com.tiscali.appmail.search.LocalSearch;
import com.tiscali.appmail.search.SearchAccount;
import com.tiscali.appmail.search.SearchSpecification;
import com.tiscali.appmail.ui.messageview.MessageViewFragment;
import com.tiscali.appmail.ui.messageview.MessageViewFragment.MessageViewFragmentListener;
import com.tiscali.appmail.view.MessageHeader;
import com.tiscali.appmail.view.MessageTitleView;
import com.tiscali.appmail.view.ViewSwitcher;
import com.tiscali.appmail.view.ViewSwitcher.OnSwitchCompleteListener;
import com.tiscali.appmail.view.holder.AccountViewHolder;
import com.tiscali.appmail.view.holder.FolderViewHolder;
import com.tiscali.appmail.view.holder.HeaderViewHolder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Created by andreaputzu on 22/11/16.
 */

@Singleton
public class MailPresenter implements MessageListFragmentListener, MessageViewFragmentListener,
        OnBackStackChangedListener, OnSwipeGestureListener, OnSwitchCompleteListener,
        PresenterLifeCycle {

    private static final String ARG_ACTION = "ARG_ACTION";
    private static final String ARG_URI = "ARG_URI";
    private static final String ARG_SHORTCUT = "shortcut";
    private static final String EXTRA_SPECIAL_FOLDER = "special_folder";
    private static final String ARG_SEARCH = "search";
    private static final String ARG_NO_THREADING = "no_threading";
    private static final String ARG_EXTRAS = "ARG_EXTRAS";

    private static final String ACTION_NULL = "ACTION_NULL";

    private static final boolean REFRESH_REMOTE = true;

    // used for remote search
    public static final String EXTRA_SEARCH_ACCOUNT = "com.tiscali.appmail.search_account";
    private static final String EXTRA_SEARCH_FOLDER = "com.tiscali.appmail.search_folder";

    private static final String EXTRA_MESSAGE_REFERENCE = "message_reference";
    public static final String ARG_ACCOUNT = "account";
    public static final String ARG_FOLDER = "folder";

    // Used for navigating to next/previous message
    private static final int PREVIOUS = 1;
    private static final int NEXT = 2;

    private static final int MAX_MESSAGES_COUNT = 99;
    private final Activity mContext;
    private final INavigationDrawerActivityListener mListener;
    private Intent mIntent;

    private boolean mStarted = false;

    private LayoutInflater mInflater;
    private Menu mMenu;
    private ActionBar mActionBar;
    private View mActionBarMessageList;
    private View mActionBarMessageView;
    private MessageTitleView mActionBarSubject;
    private TextView mActionBarTitle;
    private TextView mActionBarSubTitle;
    private TextView mActionBarUnread;
    private ProgressBar mActionBarProgress;
    private View mActionButtonIndeterminateProgress;
    private MessageReference mMessageReference;
    private LocalSearch mSearch;
    private boolean mNoThreading;
    private boolean mSingleAccountMode;
    private Account mAccount;
    private boolean mSingleFolderMode;
    private String mFolderName;
    private MessageListFragment mMessageListFragment;
    private MessageViewFragment mMessageViewFragment;
    private DisplayMode mDisplayMode;
    private int mFirstBackStackId = -1;

    private ViewGroup mMessageViewContainer;
    private View mMessageViewPlaceHolder;
    private MenuItem mMenuButtonCheckMail;

    private StorageManager.StorageListener mStorageListener = new StorageListenerImplementation();

    private int mLastDirection = (K9.messageViewShowNext()) ? NEXT : PREVIOUS;

    private boolean isMediaListFragmentAttached = false;

    // private Bundle mSavedInstanceState;

    /**
     * {@code true} when the message list was displayed once. This is used in
     * {@link # onBackPressed()} to decide whether to go from the message view to the message list
     * or finish the activity.
     */
    private boolean mMessageListWasDisplayed = false;
    private ViewSwitcher mViewSwitcher;

    private static final String MAIL_PREFIX = "MAIL";
    private static final String MAIL_DISPLAY_MODE = MAIL_PREFIX + "DisplayMode";
    private static final String MAIL_MESSAGE_LIST_WAS_DISPLAYED =
            MAIL_PREFIX + "MessageListWasDisplayed";
    private static final String MAIL_FIRST_BACKSTACK_ID = MAIL_PREFIX + "FirstBackstackId";
    private static final String MAIL_ACCOUNT_UUID = MAIL_PREFIX + "AccountUuid";

    private MailAdapter mMailAdapter;
    private AccountsAdapter mAccountsAdapter;
    List<FolderInfoHolder> mFolders = new ArrayList<>();
    private MailPresenterHandler mHandler = new MailPresenterHandler();
    private String mAccountUuid;
    private SearchView mSearchView;

    private ActivityListener mMessagingListener = new ActivityListener() {
        @Override
        public void informUserOfStatus() {
            mHandler.dataChanged();
        }

        @Override
        public void listFoldersStarted(Account account) {
            if (account.equals(mAccount)) {
                // mHandler.progress(true);
            }
            super.listFoldersStarted(account);
        }

        @Override
        public void listFoldersFailed(Account account, String message) {
            if (account.equals(mAccount)) {
                // mHandler.progress(false);
            }
            super.listFoldersFailed(account, message);
        }

        @Override
        public void listFoldersFinished(Account account) {
            if (account.equals(mAccount)) {
                // mHandler.progress(false);
                MessagingController.getInstance(mContext).refreshListener(this);
                mHandler.dataChanged();
            }
            super.listFoldersFinished(account);

        }

        @Override
        public void listFolders(Account account, List<LocalFolder> folders) {
            if (account.equals(mAccount)) {

                List<FolderInfoHolder> newFolders = new LinkedList<>();
                List<FolderInfoHolder> topFolders = new LinkedList<>();

                for (LocalFolder folder : folders) {

                    FolderInfoHolder holder = mMailAdapter.getFolder(folder.getName());

                    if (holder == null) {
                        holder = new FolderInfoHolder(mContext, folder, mAccount, -1);
                    } else {
                        holder.populate(mContext, folder, mAccount, -1);

                    }
                    if (folder.isInTopGroup()
                            || TiscaliUtility.isFolderInTopGroup(mContext, folder.getName())) {
                        topFolders.add(holder);
                    } else {
                        newFolders.add(holder);
                    }
                }
                Collections.sort(newFolders);
                TiscaliUtility.sortFoldersInTopGroup(mContext, topFolders);
                topFolders.addAll(newFolders);
                mHandler.newFolders(topFolders);
            }
            super.listFolders(account, folders);
        }

        @Override
        public void synchronizeMailboxStarted(Account account, String folder) {
            super.synchronizeMailboxStarted(account, folder);
            if (account.equals(mAccount)) {
                // mHandler.progress(true);
                mHandler.folderLoading(folder, true);
                mHandler.dataChanged();
            }

        }

        @Override
        public void synchronizeMailboxFinished(Account account, String folder,
                int totalMessagesInMailbox, int numNewMessages) {
            super.synchronizeMailboxFinished(account, folder, totalMessagesInMailbox,
                    numNewMessages);
            if (account.equals(mAccount)) {
                // mHandler.progress(false);
                mHandler.folderLoading(folder, false);

                refreshFolder(account, folder);
            }

        }

        private void refreshFolder(Account account, String folderName) {
            // There has to be a cheaper way to get at the localFolder object than this
            LocalFolder localFolder = null;
            try {
                if (account != null && folderName != null) {
                    if (!account.isAvailable(mContext)) {
                        Log.i(K9.LOG_TAG, "not refreshing folder of unavailable account");
                        return;
                    }
                    localFolder = account.getLocalStore().getFolder(folderName);
                    FolderInfoHolder folderHolder = mMailAdapter.getFolder(folderName);
                    if (folderHolder != null) {
                        folderHolder.populate(mContext, localFolder, mAccount, -1);
                        folderHolder.flaggedMessageCount = -1;

                        mHandler.dataChanged();
                    }
                }
            } catch (Exception e) {
                Log.e(K9.LOG_TAG, "Exception while populating folder", e);
            } finally {
                if (localFolder != null) {
                    localFolder.close();
                }
            }

        }

        @Override
        public void synchronizeMailboxFailed(Account account, String folder, String message) {
            super.synchronizeMailboxFailed(account, folder, message);
            if (!account.equals(mAccount)) {
                return;
            }

            // mHandler.progress(false);

            mHandler.folderLoading(folder, false);

            // String mess = truncateStatus(message);

            // mHandler.folderStatus(folder, mess);
            FolderInfoHolder holder = mMailAdapter.getFolder(folder);

            if (holder != null) {
                holder.lastChecked = 0;
            }

            mHandler.dataChanged();

        }

        @Override
        public void setPushActive(Account account, String folderName, boolean enabled) {
            if (!account.equals(mAccount)) {
                return;
            }
            FolderInfoHolder holder = mMailAdapter.getFolder(folderName);

            if (holder != null) {
                holder.pushActive = enabled;

                mHandler.dataChanged();
            }
        }

        @Override
        public void messageDeleted(Account account, String folder, Message message) {
            synchronizeMailboxRemovedMessage(account, folder, message);
        }

        @Override
        public void emptyTrashCompleted(Account account) {
            if (account.equals(mAccount)) {
                refreshFolder(account, mAccount.getTrashFolderName());
            }
        }

        @Override
        public void folderStatusChanged(Account account, String folderName,
                int unreadMessageCount) {
            if (account.equals(mAccount)) {
                refreshFolder(account, folderName);
                informUserOfStatus();
            }
        }

        @Override
        public void sendPendingMessagesCompleted(Account account) {
            super.sendPendingMessagesCompleted(account);
            if (account.equals(mAccount)) {
                refreshFolder(account, mAccount.getOutboxFolderName());
            }
        }

        @Override
        public void sendPendingMessagesStarted(Account account) {
            super.sendPendingMessagesStarted(account);

            if (account.equals(mAccount)) {
                mHandler.dataChanged();
            }
        }

        @Override
        public void sendPendingMessagesFailed(Account account) {
            super.sendPendingMessagesFailed(account);
            if (account.equals(mAccount)) {
                refreshFolder(account, mAccount.getOutboxFolderName());
            }
        }

        @Override
        public void accountSizeChanged(Account account, long oldSize, long newSize) {
            if (account.equals(mAccount)) {
                mHandler.accountSizeChanged(oldSize, newSize);
            }
        }
    };

    public DisplayMode getDisplayMode() {
        return mDisplayMode;
    }

    public boolean getMessageListWasDisplayed() {
        return mMessageListWasDisplayed;
    }

    public MessageListFragment getMessageListFragment() {
        return mMessageListFragment;
    }

    public enum DisplayMode {
        MESSAGE_LIST, MESSAGE_VIEW, SPLIT_VIEW
    }

    @Inject
    public MailPresenter(INavigationDrawerActivityListener listener, Intent intent) {
        mListener = listener;
        mContext = listener.getActivity();
        mIntent = intent;
        // mWebtrekk = Webtrekk.getInstance();
    }

    public void setIntent(Intent intent) {
        mIntent = intent;
    }

    private void onRefresh(final boolean forceRemote) {
        if (mAccount != null) {
            MessagingController.getInstance(mContext).listFolders(mAccount, forceRemote,
                    mMessagingListener);
        }
    }

    @Nullable
    public void onCreateView() {
        mStarted = true;
        mInflater = mContext.getLayoutInflater();

        FrameLayout container = mListener.getContainer();

        if (useSplitView()) {
            mInflater.inflate(R.layout.split_message_list, container, true);
        } else {
            mInflater.inflate(R.layout.message_list, container, true);
            mViewSwitcher = (ViewSwitcher) container.findViewById(R.id.container);
            mViewSwitcher.setFirstInAnimation(
                    AnimationUtils.loadAnimation(mContext, R.anim.slide_in_left));
            mViewSwitcher.setFirstOutAnimation(
                    AnimationUtils.loadAnimation(mContext, R.anim.slide_out_right));
            mViewSwitcher.setSecondInAnimation(
                    AnimationUtils.loadAnimation(mContext, R.anim.slide_in_right));
            mViewSwitcher.setSecondOutAnimation(
                    AnimationUtils.loadAnimation(mContext, R.anim.slide_out_left));
            mViewSwitcher.setOnSwitchCompleteListener(this);
        }

        initializeActionBar();

        if (!decodeExtras(mIntent)) {
            Toast.makeText(mContext, "RETURN FRAGMENT", Toast.LENGTH_LONG);
        }

        // if (mSavedInstanceState != null) {
        // mAccountUuid = mSavedInstanceState.getString(MAIL_ACCOUNT_UUID);
        // } else {
        String[] accountUuids = mSearch.getAccountUuids();
        mAccountUuid = accountUuids[0];
        // }

        mAccount = Preferences.getPreferences(mContext).getAccount(mAccountUuid);

        findFragments();
        initializeDisplayMode();
        initializeLayout();
        initializeFragments();
        displayViews();
        // setupGestureDetector(this);

        mMailAdapter = new MailAdapter();
        mListener.setDrawerListAdapter(mMailAdapter);
        String folderName = null;
        if (mSearch.getFolderNames() != null && mSearch.getFolderNames().size() > 0) {
            folderName = mSearch.getFolderNames().get(0);
        }

        mMailAdapter.setSelectedPos(folderName);

        mAccountsAdapter = new AccountsAdapter();
    }

    public void showFolder(LocalSearch search) {

        mSearch = search;
        mMessageReference = null;

        if (updateDataWithNewSearch()) {
            updateFragments();
            displayViews();
        }
    }

    private void updateFragments() {

        boolean hasMessageListFragment = (mMessageListFragment != null);

        if (hasMessageListFragment) {
            mMessageListFragment.updateContent(mSearch);
            // TODO: update content for mMessageViewFragment if split view is restored
        }
    }

    // private void resetView() {
    // // if (mFirstBackStackId >= 0) {
    // // getFragmentManager().popBackStackImmediate(mFirstBackStackId,
    // // FragmentManager.POP_BACK_STACK_INCLUSIVE);
    // // mFirstBackStackId = -1;
    // // }
    // removeMessageListFragment();
    // removeMessageViewFragment();
    //
    // mMessageReference = null;
    // mSearch = null;
    // mFolderName = null;
    //
    // if (!decodeExtras(mIntent)) {
    // return;
    // }
    //
    // initializeDisplayMode(null);
    // initializeFragments();
    // displayViews();
    // }

    private void findFragments() {
        FragmentManager fragmentManager = mContext.getFragmentManager();
        mMessageListFragment =
                (MessageListFragment) fragmentManager.findFragmentById(R.id.message_list_container);
        mMessageViewFragment =
                (MessageViewFragment) fragmentManager.findFragmentById(R.id.message_view_container);
    }

    private void removeMessageListFragment() {
        if (mMessageListFragment != null) {
            hideFragment(mMessageListFragment);
        }
    }

    /**
     * Create fragment instances if necessary.
     *
     * @see #findFragments()
     */
    private void initializeFragments() {
        FragmentManager fragmentManager = mContext.getFragmentManager();
        fragmentManager.addOnBackStackChangedListener(this);

        boolean hasMessageListFragment = (mMessageListFragment != null);

        if (!hasMessageListFragment) {
            mMessageListFragment = MessageListFragment.newInstance(mSearch, false,
                    (K9.isThreadedViewEnabled() && !mNoThreading));
        }

        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (isMediaListFragmentAttached) {
            ft.show(mMessageListFragment);
        } else {
            ft.add(R.id.message_list_container, mMessageListFragment);
            isMediaListFragmentAttached = true;
        }
        ft.commit();

        // Check if the fragment wasn't restarted and has a MessageReference in the arguments. If
        // so, open the referenced message.
        // should not be possible: bottom bar hidden in detail message view
        if (!hasMessageListFragment && mMessageViewFragment == null && mMessageReference != null) {
            openMessage(mMessageReference);
        }
    }

    /**
     * Set the initial display mode (message list, message view, or split view).
     * <p>
     * <p>
     * <strong>Note:</strong> This method has to be called after {@link #findFragments()} because
     * the result depends on the availability of a {@link MessageViewFragment} instance.
     * </p>
     *
     */
    private void initializeDisplayMode() {
        if (useSplitView()) {
            mDisplayMode = DisplayMode.SPLIT_VIEW;
            return;
        }

        // if (savedInstanceState != null) {
        // DisplayMode savedDisplayMode =
        // (DisplayMode) savedInstanceState.getSerializable(MAIL_DISPLAY_MODE);
        // if (savedDisplayMode != null && savedDisplayMode != DisplayMode.SPLIT_VIEW) {
        // mDisplayMode = savedDisplayMode;
        // return;
        // }
        // }

        if (mMessageViewFragment != null || mMessageReference != null) {
            mDisplayMode = DisplayMode.MESSAGE_VIEW;
        } else {
            mDisplayMode = DisplayMode.MESSAGE_LIST;
        }
    }

    private void initializeLayout() {
        mMessageViewContainer = (ViewGroup) mContext.findViewById(R.id.message_view_container);

        mMessageViewPlaceHolder =
                mInflater.inflate(R.layout.empty_message_view, mMessageViewContainer, false);
    }

    private boolean useSplitView() {
        K9.SplitViewMode splitViewMode = K9.getSplitViewMode();
        int orientation = mContext.getResources().getConfiguration().orientation;

        return (splitViewMode == K9.SplitViewMode.ALWAYS
                || (splitViewMode == K9.SplitViewMode.WHEN_IN_LANDSCAPE
                        && orientation == Configuration.ORIENTATION_LANDSCAPE));
    }

    private void initializeActionBar() {
        mActionBar = ((AppCompatActivity) mContext).getSupportActionBar();

        mActionBar.setDisplayShowCustomEnabled(true);
        mActionBar.setCustomView(R.layout.actionbar_custom);

        View customView = mActionBar.getCustomView();
        mActionBarMessageList = customView.findViewById(R.id.actionbar_message_list);
        mActionBarMessageView = customView.findViewById(R.id.actionbar_message_view);
        mActionBarSubject = (MessageTitleView) customView.findViewById(R.id.message_title_view);
        mActionBarTitle = (TextView) customView.findViewById(R.id.actionbar_title_first);
        mActionBarSubTitle = (TextView) customView.findViewById(R.id.actionbar_title_sub);
        mActionBarUnread = (TextView) customView.findViewById(R.id.actionbar_unread_count);
        mActionBarProgress = (ProgressBar) customView.findViewById(R.id.actionbar_progress);
        mActionButtonIndeterminateProgress = getActionButtonIndeterminateProgress();

        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void displayViews() {
        switch (mDisplayMode) {
            case MESSAGE_LIST: {
                showMessageList();
                break;
            }
            case MESSAGE_VIEW: {
                showMessageView();
                break;
            }
            case SPLIT_VIEW: {
                mMessageListWasDisplayed = true;
                if (mMessageViewFragment == null) {
                    showMessageViewPlaceHolder();
                } else {
                    MessageReference activeMessage = mMessageViewFragment.getMessageReference();
                    if (activeMessage != null) {
                        mMessageListFragment.setActiveMessage(activeMessage);
                    }
                }
                break;
            }
        }
    }

    private void showMessageViewPlaceHolder() {
        removeMessageViewFragment();

        // Add placeholder view if necessary
        if (mMessageViewPlaceHolder.getParent() == null) {
            mMessageViewContainer.addView(mMessageViewPlaceHolder);
        }

        mMessageListFragment.setActiveMessage(null);
    }

    /**
     * Remove MessageViewFragment if necessary.
     */
    private void removeMessageViewFragment() {
        if (mMessageViewFragment != null) {
            removeFragment(mMessageViewFragment);
            mMessageViewFragment = null;

            showDefaultTitleView();
        }
    }

    @SuppressLint("InflateParams")
    private View getActionButtonIndeterminateProgress() {
        return mInflater.inflate(R.layout.actionbar_indeterminate_progress_actionview, null);
    }

    private boolean decodeExtras(Intent intent) {

        String action = intent.getAction();

        if (Intent.ACTION_VIEW.equals(action)) {
            Uri uri = intent.getData();
            if (uri != null) {
                List<String> segmentList = uri.getPathSegments();

                String accountId = segmentList.get(0);
                Collection<Account> accounts =
                        Preferences.getPreferences(mContext).getAvailableAccounts();
                for (Account account : accounts) {
                    if (String.valueOf(account.getAccountNumber()).equals(accountId)) {
                        String folderName = segmentList.get(1);
                        String messageUid = segmentList.get(2);
                        mMessageReference = new MessageReference(account.getUuid(), folderName,
                                messageUid, null);
                        break;
                    }
                }
            }
        } else if (ARG_SHORTCUT.equals(action)) {
            // Handle shortcut intents
            String specialFolder = intent.getStringExtra(EXTRA_SPECIAL_FOLDER);
            if (SearchAccount.UNIFIED_INBOX.equals(specialFolder)) {
                mSearch = SearchAccount.createUnifiedInboxAccount(mContext).getRelatedSearch();
            } else if (SearchAccount.ALL_MESSAGES.equals(specialFolder)) {
                mSearch = SearchAccount.createAllMessagesAccount(mContext).getRelatedSearch();
            }
        } else if (intent.getStringExtra(SearchManager.QUERY) != null) {
            // check if this intent comes from the system search ( remote )
            if (Intent.ACTION_SEARCH.equals(action)) {
                // hide toggle
                setActionBarUp();

                // Query was received from Search Dialog
                String query = intent.getStringExtra(SearchManager.QUERY).trim();
                TiscaliSearchRecentSuggestionsProvider.saveSearch(mContext, query);

                // use this if generic title for search results is required
                // mSearch = new LocalSearch(mContext.getString(R.string.search_results));
                // use this if searched string as toolbar title is required
                mSearch = new LocalSearch(query);

                mSearch.setManualSearch(true);
                mNoThreading = true;

                mSearch.or(new SearchSpecification.SearchCondition(
                        SearchSpecification.SearchField.SENDER,
                        SearchSpecification.Attribute.CONTAINS, query));
                mSearch.or(new SearchSpecification.SearchCondition(
                        SearchSpecification.SearchField.SUBJECT,
                        SearchSpecification.Attribute.CONTAINS, query));
                mSearch.or(new SearchSpecification.SearchCondition(
                        SearchSpecification.SearchField.MESSAGE_CONTENTS,
                        SearchSpecification.Attribute.CONTAINS, query));

                Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);
                if (appData != null) {
                    mSearch.addAccountUuid(appData.getString(EXTRA_SEARCH_ACCOUNT));
                    // searches started from a folder list activity will provide an account, but no
                    // folder
                    if (appData.getString(EXTRA_SEARCH_FOLDER) != null) {
                        mSearch.addAllowedFolder(appData.getString(EXTRA_SEARCH_FOLDER));
                    }
                } else {
                    mSearch.addAccountUuid(LocalSearch.ALL_ACCOUNTS);
                }
            }
        } else {
            // regular LocalSearch object was passed
            mSearch = intent.getParcelableExtra(ARG_SEARCH);
            mNoThreading = intent.getBooleanExtra(ARG_NO_THREADING, false);
        }

        if (mMessageReference == null) {
            mMessageReference = intent.getParcelableExtra(EXTRA_MESSAGE_REFERENCE);
        }

        if (mMessageReference != null) {
            mSearch = new LocalSearch();
            mSearch.addAccountUuid(mMessageReference.getAccountUuid());
            mSearch.addAllowedFolder(mMessageReference.getFolderName());
        }

        if (mSearch == null) {
            // We've most likely been started by an old unread widget
            String accountUuid = intent.getStringExtra(ARG_ACCOUNT);
            String folderName = intent.getStringExtra(ARG_FOLDER);

            mSearch = new LocalSearch(folderName);
            mSearch.addAccountUuid((accountUuid == null) ? "invalid" : accountUuid);
            if (folderName != null) {
                mSearch.addAllowedFolder(folderName);
            }
        }

        return updateDataWithNewSearch();
    }

    private boolean updateDataWithNewSearch() {

        Preferences prefs = Preferences.getPreferences(mContext.getApplicationContext());

        String[] accountUuids = mSearch.getAccountUuids();
        if (mSearch.searchAllAccounts()) {
            List<Account> accounts = prefs.getAccounts();
            mSingleAccountMode = (accounts.size() == 1);
        } else {
            mSingleAccountMode = (accountUuids.length == 1);
        }
        mSingleFolderMode = mSingleAccountMode && (mSearch.getFolderNames().size() == 1);

        if (mSingleAccountMode && (mAccount == null || !mAccount.isAvailable(mContext))) {
            Log.i(K9.LOG_TAG, "not opening MessageList of unavailable account");
            // onAccountUnavailable();
            return false;
        }

        if (mSingleFolderMode) {
            mFolderName = mSearch.getFolderNames().get(0);
        }

        return true;
    }

    @Override
    public void openMessage(MessageReference messageReference) {
        Preferences prefs = Preferences.getPreferences(mContext.getApplicationContext());
        Account account = prefs.getAccount(messageReference.getAccountUuid());
        String folderName = messageReference.getFolderName();

        if (folderName.equals(account.getDraftsFolderName())) {
            MessageActions.actionEditDraft(mContext, messageReference);
        } else {
            mMessageViewContainer.removeView(mMessageViewPlaceHolder);

            if (mMessageListFragment != null) {
                mMessageListFragment.setActiveMessage(messageReference);
            }

            MessageViewFragment fragment = MessageViewFragment.newInstance(messageReference);
            FragmentTransaction ft = mContext.getFragmentManager().beginTransaction();
            ft.replace(R.id.message_view_container, fragment);
            mMessageViewFragment = fragment;
            ft.commit();

            if (mDisplayMode != DisplayMode.SPLIT_VIEW) {
                showMessageView();
            }
        }
    }

    private void showMessageView() {
        mDisplayMode = DisplayMode.MESSAGE_VIEW;

        if (!mMessageListWasDisplayed) {
            mViewSwitcher.setAnimateFirstView(false);
        }
        mViewSwitcher.showSecondView();

        showMessageTitleView();
        configureMenu(mMenu);
    }

    private void showDefaultTitleView() {
        mActionBarMessageView.setVisibility(View.GONE);
        mActionBarMessageList.setVisibility(View.VISIBLE);

        if (mMessageListFragment != null) {
            mMessageListFragment.updateTitle();
        }

        mActionBarSubject.setMessageHeader(null);
    }

    private void showMessageTitleView() {
        mActionBarMessageList.setVisibility(View.GONE);
        mActionBarMessageView.setVisibility(View.VISIBLE);

        if (mMessageViewFragment != null) {
            displayMessageSubject(null);
            mMessageViewFragment.updateTitle();
        }
    }

    public void showMessageList() {
        mMessageListWasDisplayed = true;
        mDisplayMode = DisplayMode.MESSAGE_LIST;
        mViewSwitcher.showFirstView();

        mMessageListFragment.setActiveMessage(null);

        showDefaultTitleView();
        configureMenu(mMenu);
    }

    /**
     * Hide menu items not appropriate for the current context.
     * <p>
     * <p>
     * <strong>Note:</strong> Please adjust the comments in {@code res/menu/message_list_option.xml}
     * if you change the visibility of a menu item in this method.
     * </p>
     *
     * @param menu The {@link Menu} instance that should be modified. May be {@code null}; in that
     *        case the method does nothing and immediately returns.
     */
    private boolean configureMenu(Menu menu) {
        if (menu == null) {
            return false;
        }

        /*
         * Set visibility of menu items related to the message view
         */

        if (mDisplayMode == DisplayMode.MESSAGE_LIST || mMessageViewFragment == null
                || !mMessageViewFragment.isInitialized()) {
            menu.findItem(R.id.next_message).setVisible(false);
            menu.findItem(R.id.previous_message).setVisible(false);
            menu.findItem(R.id.single_message_options).setVisible(false);
            menu.findItem(R.id.delete).setVisible(false);
            menu.findItem(R.id.archive).setVisible(false);
            menu.findItem(R.id.move).setVisible(false);
            menu.findItem(R.id.copy).setVisible(false);
            menu.findItem(R.id.spam).setVisible(false);
            menu.findItem(R.id.refile).setVisible(false);
            menu.findItem(R.id.toggle_unread).setVisible(false);
            menu.findItem(R.id.select_text).setVisible(false);
            menu.findItem(R.id.toggle_message_view_theme).setVisible(false);
            menu.findItem(R.id.show_headers).setVisible(false);
            menu.findItem(R.id.hide_headers).setVisible(false);
        } else {
            // hide prev/next buttons in split mode
            if (mDisplayMode != DisplayMode.MESSAGE_VIEW) {
                menu.findItem(R.id.next_message).setVisible(false);
                menu.findItem(R.id.previous_message).setVisible(false);
            } else {
                MessageReference ref = mMessageViewFragment.getMessageReference();
                boolean initialized =
                        (mMessageListFragment != null && mMessageListFragment.isLoadFinished());
                boolean canDoPrev = (initialized && !mMessageListFragment.isFirst(ref));
                boolean canDoNext = (initialized && !mMessageListFragment.isLast(ref));

                MenuItem prev = menu.findItem(R.id.previous_message);
                prev.setVisible(false);
                prev.setEnabled(false); // canDoPrev
                prev.getIcon().setAlpha(canDoPrev ? 255 : 127);

                MenuItem next = menu.findItem(R.id.next_message);
                next.setVisible(false);
                next.setEnabled(false); // canDoPrev
                next.getIcon().setAlpha(canDoNext ? 255 : 127);
            }

            MenuItem toggleTheme = menu.findItem(R.id.toggle_message_view_theme);
            if (K9.useFixedMessageViewTheme()) {
                toggleTheme.setVisible(false);
            } else {
                // Set title of menu item to switch to dark/light theme
                if (K9.getK9MessageViewTheme() == K9.Theme.DARK) {
                    toggleTheme.setTitle(R.string.message_view_theme_action_light);
                } else {
                    toggleTheme.setTitle(R.string.message_view_theme_action_dark);
                }
                toggleTheme.setVisible(true);
            }

            // Jellybean has built-in long press selection support
            menu.findItem(R.id.select_text).setVisible(Build.VERSION.SDK_INT < 16);

            menu.findItem(R.id.delete).setVisible(K9.isMessageViewDeleteActionVisible());

            /*
             * Set visibility of copy, move, archive, spam in action bar and refile submenu
             */
            if (mMessageViewFragment.isCopyCapable()) {
                menu.findItem(R.id.copy).setVisible(K9.isMessageViewCopyActionVisible());
                menu.findItem(R.id.refile_copy).setVisible(true);
            } else {
                menu.findItem(R.id.copy).setVisible(false);
                menu.findItem(R.id.refile_copy).setVisible(false);
            }

            if (mMessageViewFragment.isMoveCapable()) {
                boolean canMessageBeArchived = mMessageViewFragment.canMessageBeArchived();
                boolean canMessageBeMovedToSpam = mMessageViewFragment.canMessageBeMovedToSpam();

                menu.findItem(R.id.move).setVisible(K9.isMessageViewMoveActionVisible());
                menu.findItem(R.id.archive)
                        .setVisible(canMessageBeArchived && K9.isMessageViewArchiveActionVisible());
                menu.findItem(R.id.spam)
                        .setVisible(canMessageBeMovedToSpam && K9.isMessageViewSpamActionVisible());

                menu.findItem(R.id.refile_move).setVisible(true);
                menu.findItem(R.id.refile_archive).setVisible(canMessageBeArchived);
                menu.findItem(R.id.refile_spam).setVisible(canMessageBeMovedToSpam);
            } else {
                menu.findItem(R.id.move).setVisible(false);
                menu.findItem(R.id.archive).setVisible(false);
                menu.findItem(R.id.spam).setVisible(false);

                menu.findItem(R.id.refile).setVisible(false);
            }

            if (mMessageViewFragment.allHeadersVisible()) {
                menu.findItem(R.id.show_headers).setVisible(false);
            } else {
                menu.findItem(R.id.hide_headers).setVisible(false);
            }
        }

        /*
         * Set visibility of menu items related to the message list
         */

        // Hide both search menu items by default and enable one when appropriate
        menu.findItem(R.id.search).setVisible(false);
        menu.findItem(R.id.search_remote).setVisible(false);

        if (mDisplayMode == DisplayMode.MESSAGE_VIEW || mMessageListFragment == null
                || !mMessageListFragment.isInitialized() || mMessageListFragment.isHidden()) {
            menu.findItem(R.id.check_mail).setVisible(false);
            menu.findItem(R.id.set_sort).setVisible(false);
            menu.findItem(R.id.select_all).setVisible(false);
            menu.findItem(R.id.send_messages).setVisible(false);
            menu.findItem(R.id.expunge).setVisible(false);
            menu.findItem(R.id.mark_all_as_read).setVisible(false);
            menu.findItem(R.id.show_folder_list).setVisible(false);
        } else {
            menu.findItem(R.id.set_sort).setVisible(true);
            menu.findItem(R.id.select_all).setVisible(true);
            menu.findItem(R.id.mark_all_as_read)
                    .setVisible(mMessageListFragment.isMarkAllAsReadSupported());

            if (!mMessageListFragment.isSingleAccountMode()) {
                menu.findItem(R.id.expunge).setVisible(false);
                menu.findItem(R.id.send_messages).setVisible(false);
                menu.findItem(R.id.show_folder_list).setVisible(false);
            } else {
                menu.findItem(R.id.send_messages).setVisible(mMessageListFragment.isOutbox());
                menu.findItem(R.id.expunge).setVisible(mMessageListFragment.isRemoteFolder()
                        && mMessageListFragment.isAccountExpungeCapable());
                menu.findItem(R.id.show_folder_list).setVisible(true);
            }

            menu.findItem(R.id.check_mail).setVisible(mMessageListFragment.isCheckMailSupported());

            // If this is an explicit local search, show the option to search on the server
            if (!mMessageListFragment.isRemoteSearch()
                    && mMessageListFragment.isRemoteSearchAllowed()) {
                menu.findItem(R.id.search_remote).setVisible(true);
            } else if (!mMessageListFragment.isManualSearch()) {
                menu.findItem(R.id.search).setVisible(true);
            }
        }
        return true;
    }

    private final class StorageListenerImplementation implements StorageManager.StorageListener {
        @Override
        public void onUnmount(String providerId) {
            if (mAccount != null && providerId.equals(mAccount.getLocalStorageProviderId())) {

                mContext.

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                onAccountUnavailable();
                            }
                        }

                );
            }
        }

        @Override
        public void onMount(String providerId) {
            // no-op
        }
    }

    protected void onAccountUnavailable() {
        Toast.makeText(mContext, "Account Unavaible Finish Activity", Toast.LENGTH_LONG);
        mContext.finish();
        // TODO inform user about account unavailability using Toast
        Accounts.listAccounts(mContext);
    }

    @Override
    public void onPause() {

        if (!mStarted) {
            return;
        }

        StorageManager.getInstance((mContext).getApplication()).removeListener(mStorageListener);

        MessagingController.getInstance(mContext).removeListener(mMessagingListener);
        mMessagingListener.onPause(mContext);
    }

    @Override
    public void onResume() {

        if (!mStarted) {
            return;
        }

        if (mAccount != null && !mAccount.isAvailable(mContext)) {
            onAccountUnavailable();
            return;
        }
        StorageManager.getInstance(mContext.getApplication()).addListener(mStorageListener);

        MessagingController.getInstance(mContext).addListener(mMessagingListener);

        onRefresh(!REFRESH_REMOTE);

        if (mAccount != null) {
            MessagingController.getInstance(mContext).cancelNotificationsForAccount(mAccount);
        }
        mMessagingListener.onResume(mContext);
    }

    @Override
    public void onDetach() {
        if (!mStarted) {
            return;
        }
        removeMessageListFragment();
        removeMessageViewFragment();

        mMessageReference = null;
        mSearch = null;
        mFolderName = null;

        mContext.invalidateOptionsMenu();

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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // outState.putSerializable(MAIL_DISPLAY_MODE, mDisplayMode);
        // outState.putBoolean(MAIL_MESSAGE_LIST_WAS_DISPLAYED, mMessageListWasDisplayed);
        // outState.putInt(MAIL_FIRST_BACKSTACK_ID, mFirstBackStackId);
        // outState.putString(MAIL_ACCOUNT_UUID, mAccountUuid);
        // mSavedInstanceState = outState;
    }

    @Override
    public void setStartInstanceState(Bundle savedInstanceState) {
        // mSavedInstanceState = savedInstanceState;
    }


    @Override
    public void onNewIntent(Intent intent) {

        // mIntent = intent;

        removeMessageListFragment();
        removeMessageViewFragment();

        mMessageReference = null;
        mSearch = null;
        mFolderName = null;

        if (!decodeExtras(intent)) {
            return;
        }

        initializeDisplayMode();
        initializeFragments();
        displayViews();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mMessageListWasDisplayed =
                    savedInstanceState.getBoolean(MAIL_MESSAGE_LIST_WAS_DISPLAYED);
            mFirstBackStackId = savedInstanceState.getInt(MAIL_FIRST_BACKSTACK_ID);
        }
    }

    // @Override
    // public void onBackPressed() {
    // if (mDisplayMode == MessageList.DisplayMode.MESSAGE_VIEW && mMessageListWasDisplayed) {
    // showMessageList();
    // } else {
    // super.onBackPressed();
    // }
    // }


    /**
     * Handle hotkeys
     * <p>
     * <p>
     * This method is called by {@link # dispatchKeyEvent(KeyEvent)} before any view had the chance
     * to
     * </p>
     * consume this key event.
     *
     * @param keyCode The value in {@code event.getKeyCode()}.
     * @param event Description of the key event.
     * @return {@code true} if this event was consumed.
     */
    public boolean onCustomKeyDown(final int keyCode, final KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP: {
                if (mMessageViewFragment != null && mDisplayMode != DisplayMode.MESSAGE_LIST
                        && K9.useVolumeKeysForNavigationEnabled()) {
                    showPreviousMessage();
                    return true;
                } else if (mDisplayMode != DisplayMode.MESSAGE_VIEW
                        && K9.useVolumeKeysForListNavigationEnabled()) {
                    mMessageListFragment.onMoveUp();
                    return true;
                }

                break;
            }
            case KeyEvent.KEYCODE_VOLUME_DOWN: {
                if (mMessageViewFragment != null && mDisplayMode != DisplayMode.MESSAGE_LIST
                        && K9.useVolumeKeysForNavigationEnabled()) {
                    showNextMessage();
                    return true;
                } else if (mDisplayMode != DisplayMode.MESSAGE_VIEW
                        && K9.useVolumeKeysForListNavigationEnabled()) {
                    mMessageListFragment.onMoveDown();
                    return true;
                }

                break;
            }
            case KeyEvent.KEYCODE_C: {
                mMessageListFragment.onCompose();
                return true;
            }
            case KeyEvent.KEYCODE_Q: {
                if (mMessageListFragment != null && mMessageListFragment.isSingleAccountMode()) {
                    onShowFolderList();
                }
                return true;
            }
            case KeyEvent.KEYCODE_O: {
                mMessageListFragment.onCycleSort();
                return true;
            }
            case KeyEvent.KEYCODE_I: {
                mMessageListFragment.onReverseSort();
                return true;
            }
            case KeyEvent.KEYCODE_DEL:
            case KeyEvent.KEYCODE_D: {
                if (mDisplayMode == DisplayMode.MESSAGE_LIST) {
                    mMessageListFragment.onDelete();
                } else if (mMessageViewFragment != null) {
                    mMessageViewFragment.onDelete();
                }
                return true;
            }
            case KeyEvent.KEYCODE_S: {
                mMessageListFragment.toggleMessageSelect();
                return true;
            }
            case KeyEvent.KEYCODE_G: {
                if (mDisplayMode == DisplayMode.MESSAGE_LIST) {
                    mMessageListFragment.onToggleFlagged();
                } else if (mMessageViewFragment != null) {
                    mMessageViewFragment.onToggleFlagged();
                }
                return true;
            }
            case KeyEvent.KEYCODE_M: {
                if (mDisplayMode == DisplayMode.MESSAGE_LIST) {
                    mMessageListFragment.onMove();
                } else if (mMessageViewFragment != null) {
                    mMessageViewFragment.onMove();
                }
                return true;
            }
            case KeyEvent.KEYCODE_V: {
                if (mDisplayMode == DisplayMode.MESSAGE_LIST) {
                    mMessageListFragment.onArchive();
                } else if (mMessageViewFragment != null) {
                    mMessageViewFragment.onArchive();
                }
                return true;
            }
            case KeyEvent.KEYCODE_Y: {
                if (mDisplayMode == DisplayMode.MESSAGE_LIST) {
                    mMessageListFragment.onCopy();
                } else if (mMessageViewFragment != null) {
                    mMessageViewFragment.onCopy();
                }
                return true;
            }
            case KeyEvent.KEYCODE_Z: {
                if (mDisplayMode == DisplayMode.MESSAGE_LIST) {
                    mMessageListFragment.onToggleRead();
                } else if (mMessageViewFragment != null) {
                    mMessageViewFragment.onToggleRead();
                }
                return true;
            }
            case KeyEvent.KEYCODE_F: {
                if (mMessageViewFragment != null) {
                    mMessageViewFragment.onForward();
                }
                return true;
            }
            case KeyEvent.KEYCODE_A: {
                if (mMessageViewFragment != null) {
                    mMessageViewFragment.onReplyAll();
                }
                return true;
            }
            case KeyEvent.KEYCODE_R: {
                if (mMessageViewFragment != null) {
                    mMessageViewFragment.onReply();
                }
                return true;
            }
            case KeyEvent.KEYCODE_J:
            case KeyEvent.KEYCODE_P: {
                if (mMessageViewFragment != null) {
                    showPreviousMessage();
                }
                return true;
            }
            case KeyEvent.KEYCODE_N:
            case KeyEvent.KEYCODE_K: {
                if (mMessageViewFragment != null) {
                    showNextMessage();
                }
                return true;
            }
            /*
             * FIXME case KeyEvent.KEYCODE_Z: { mMessageViewFragment.zoom(event); return true; }
             */
            case KeyEvent.KEYCODE_H: {
                Toast toast = Toast.makeText(mContext,
                        mContext.getString(R.string.message_list_help_key), Toast.LENGTH_LONG);
                toast.show();
                return true;
            }
            case KeyEvent.KEYCODE_DPAD_LEFT: {
                if (mMessageViewFragment != null && mDisplayMode == DisplayMode.MESSAGE_VIEW) {
                    return showPreviousMessage();
                }
                return false;
            }
            case KeyEvent.KEYCODE_DPAD_RIGHT: {
                if (mMessageViewFragment != null && mDisplayMode == DisplayMode.MESSAGE_VIEW) {
                    return showNextMessage();
                }
                return false;
            }

        }

        return false;
    }

    // @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Swallow these events too to avoid the audible notification of a volume change
        if (K9.useVolumeKeysForListNavigationEnabled()) {
            if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)
                    || (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
                if (K9.DEBUG)
                    Log.v(K9.LOG_TAG, "Swallowed key up.");
                return true;
            }
        }
        Toast.makeText(mContext, "onKeyUp fragment", Toast.LENGTH_LONG);
        return true;
        // return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean showNextMessage() {
        MessageReference ref = mMessageViewFragment.getMessageReference();
        if (ref != null) {
            if (mMessageListFragment.openNext(ref)) {
                mLastDirection = NEXT;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean showPreviousMessage() {
        MessageReference ref = mMessageViewFragment.getMessageReference();
        if (ref != null) {
            if (mMessageListFragment.openPrevious(ref)) {
                mLastDirection = PREVIOUS;
                return true;
            }
        }
        return false;
    }

    private void onShowFolderList() {
        FolderList.actionHandleAccount(mContext, mAccount);
        mContext.finish();
    }

    private void onAccounts() {
        Accounts.listAccounts(mContext);
        mContext.finish();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!mStarted) {
            return;
        }

        mContext.getMenuInflater().inflate(R.menu.message_list_option, menu);
        mMenu = menu;
        mMenuButtonCheckMail = menu.findItem(R.id.check_mail);

        mSearchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
        SearchManager searchManager =
                (SearchManager) mContext.getSystemService(Context.SEARCH_SERVICE);
        // NavigationDrawerActivity is search activity
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(mContext.getComponentName()));
        mSearchView.setMaxWidth(Integer.MAX_VALUE);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!mStarted) {
            return false;
        }

        return configureMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!mStarted) {
            return false;
        }

        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home: {
                goBack();
                return true;
            }
            case R.id.toggle_message_view_theme: {
                onToggleTheme();
                return true;
            }
            // MessageList
            case R.id.check_mail: {
                mMessageListFragment.checkMail();
                return true;
            }
            case R.id.set_sort_date: {
                mMessageListFragment.changeSort(Account.SortType.SORT_DATE);
                return true;
            }
            case R.id.set_sort_arrival: {
                mMessageListFragment.changeSort(Account.SortType.SORT_ARRIVAL);
                return true;
            }
            case R.id.set_sort_subject: {
                mMessageListFragment.changeSort(Account.SortType.SORT_SUBJECT);
                return true;
            }
            case R.id.set_sort_sender: {
                mMessageListFragment.changeSort(Account.SortType.SORT_SENDER);
                return true;
            }
            case R.id.set_sort_flag: {
                mMessageListFragment.changeSort(Account.SortType.SORT_FLAGGED);
                return true;
            }
            case R.id.set_sort_unread: {
                mMessageListFragment.changeSort(Account.SortType.SORT_UNREAD);
                return true;
            }
            case R.id.set_sort_attach: {
                mMessageListFragment.changeSort(Account.SortType.SORT_ATTACHMENT);
                return true;
            }
            case R.id.select_all: {
                mMessageListFragment.selectAll();
                return true;
            }
            case R.id.search: {
                mMessageListFragment.onSearchRequested();
                return true;
            }
            case R.id.search_remote: {
                mMessageListFragment.onRemoteSearch();
                return true;
            }
            case R.id.mark_all_as_read: {
                mMessageListFragment.confirmMarkAllAsRead();
                return true;
            }
            case R.id.show_folder_list: {
                onShowFolderList();
                return true;
            }
            // MessageView
            case R.id.next_message: {
                showNextMessage();
                return true;
            }
            case R.id.previous_message: {
                showPreviousMessage();
                return true;
            }
            case R.id.delete: {
                mMessageViewFragment.onDelete();
                return true;
            }
            case R.id.reply: {
                mMessageViewFragment.onReply();
                return true;
            }
            case R.id.reply_all: {
                mMessageViewFragment.onReplyAll();
                return true;
            }
            case R.id.forward: {
                mMessageViewFragment.onForward();
                return true;
            }
            case R.id.share: {
                mMessageViewFragment.onSendAlternate();
                return true;
            }
            case R.id.toggle_unread: {
                mMessageViewFragment.onToggleRead();
                return true;
            }
            case R.id.archive:
            case R.id.refile_archive: {
                mMessageViewFragment.onArchive();
                return true;
            }
            case R.id.spam:
            case R.id.refile_spam: {
                mMessageViewFragment.onSpam();
                return true;
            }
            case R.id.move:
            case R.id.refile_move: {
                mMessageViewFragment.onMove();
                return true;
            }
            case R.id.copy:
            case R.id.refile_copy: {
                mMessageViewFragment.onCopy();
                return true;
            }
            case R.id.select_text: {
                mMessageViewFragment.onSelectText();
                return true;
            }
            case R.id.show_headers:
            case R.id.hide_headers: {
                mMessageViewFragment.onToggleAllHeadersView();
                updateMenu();
                return true;
            }
        }

        if (!mSingleFolderMode) {
            // None of the options after this point are "safe" for search results
            // TODO: This is not true for "unread" and "starred" searches in regular folders
            return false;
        }

        switch (itemId) {
            case R.id.send_messages: {
                mMessageListFragment.onSendPendingMessages();
                return true;
            }
            case R.id.expunge: {
                mMessageListFragment.onExpunge();
                return true;
            }
            default: {
                return true; // super.onOptionsItemSelected(item);
            }
        }
    }

    private void onToggleTheme() {
        Toast.makeText(mContext, "TOGGLE THEME NOT WORKING", Toast.LENGTH_LONG);
        // if (K9.getK9MessageViewTheme() == K9.Theme.DARK) {
        // K9.setK9MessageViewThemeSetting(K9.Theme.LIGHT);
        // } else {
        // K9.setK9MessageViewThemeSetting(K9.Theme.DARK);
        // }
        //
        // new Thread(new Runnable() {
        // @Override
        // public void run() {
        // Context appContext = mContext.getApplicationContext();
        // Preferences prefs = Preferences.getPreferences(appContext);
        // StorageEditor editor = prefs.getStorage().edit();
        // K9.save(editor);
        // editor.commit();
        // }
        // }).start();

        // recreate();
    }

    // @Override
    // public boolean onSearchRequested() {
    // return mMessageListFragment.onSearchRequested();
    // }

    @Override
    public void onSwitchComplete(int displayedChild) {
        if (displayedChild == 0) {
            removeMessageViewFragment();
            if (Intent.ACTION_SEARCH.equals(mIntent.getAction())) {
                setActionBarUp();
            } else {
                setActionBarToggle();
            }
        } else {
            setActionBarUp();
        }
    }

    @Override
    public void updateMenu() {
        Toast.makeText(mContext, "invalidateOptionsMenu", Toast.LENGTH_LONG);
        // invalidateOptionsMenu();
        if (mMenu == null) {
            return;
        }

        if (mDisplayMode == DisplayMode.MESSAGE_VIEW) {
            // Set title of menu item to toggle the read state of the currently displayed message
            if (mMessageViewFragment.isMessageRead()) {
                int[] attrs = new int[] {R.attr.iconActionMarkAsUnread};
                TypedArray ta = mContext.obtainStyledAttributes(attrs);
                mMenu.findItem(R.id.toggle_unread).setIcon(ta.getDrawable(0));
            } else {
                int[] attrs = new int[] {R.attr.iconActionMarkAsRead};
                TypedArray ta = mContext.obtainStyledAttributes(attrs);
                mMenu.findItem(R.id.toggle_unread).setIcon(ta.getDrawable(0));
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

    @Override
    public void disableDeleteAction() {
        mMenu.findItem(R.id.delete).setEnabled(false);
    }

    @Override
    public void showNextMessageOrReturn() {
        if (K9.messageViewReturnToList() || !showLogicalNextMessage()) {
            if (mDisplayMode == DisplayMode.SPLIT_VIEW) {
                showMessageViewPlaceHolder();
            } else {
                showMessageList();
            }
        }
    }

    /**
     * Shows the next message in the direction the user was displaying messages.
     *
     * @return {@code true}
     */
    private boolean showLogicalNextMessage() {
        boolean result = false;
        if (mLastDirection == NEXT) {
            result = showNextMessage();
        } else if (mLastDirection == PREVIOUS) {
            result = showPreviousMessage();
        }

        if (!result) {
            result = showNextMessage() || showPreviousMessage();
        }

        return result;
    }

    @Override
    public void messageHeaderViewAvailable(MessageHeader header) {
        mActionBarSubject.setMessageHeader(header);
    }

    // @Override
    // public void setProgress(boolean enable) {
    // setProgressBarIndeterminateVisibility(enable);
    // }

    @Override
    public void displayMessageSubject(String subject) {
        if (mDisplayMode == DisplayMode.MESSAGE_VIEW) {
            mActionBarSubject.setText(subject);
        }
    }

    @Override
    public void setProgress(boolean b) {
        Toast.makeText(mContext, "setProgressBarIndeterminateVisibility not working",
                Toast.LENGTH_LONG);
        // setProgressBarIndeterminateVisibility(enable);
    }

    @Override
    public void enableActionBarProgress(boolean enable) {
        if (mMenuButtonCheckMail != null && mMenuButtonCheckMail.isVisible()) {
            mActionBarProgress.setVisibility(ProgressBar.GONE);
            if (enable) {
                mMenuButtonCheckMail.setActionView(mActionButtonIndeterminateProgress);
            } else {
                mMenuButtonCheckMail.setActionView(null);
            }
        } else {
            if (mMenuButtonCheckMail != null)
                mMenuButtonCheckMail.setActionView(null);
            if (enable) {
                mActionBarProgress.setVisibility(ProgressBar.VISIBLE);
            } else {
                mActionBarProgress.setVisibility(ProgressBar.GONE);
            }
        }
    }

    public void setActionBarTitle(String title) {
        mActionBarTitle.setText(title);
    }

    public void setActionBarSubTitle(String subTitle) {
        mActionBarSubTitle.setText(subTitle);
    }

    public void setActionBarUnread(int unread) {
        if (unread == 0) {
            mActionBarUnread.setVisibility(View.GONE);
        } else {
            mActionBarUnread.setVisibility(View.VISIBLE);
            mActionBarUnread.setText(String.format("%d", unread));
        }
    }

    @Override
    public void setMessageListTitle(String title) {
        setActionBarTitle(title);
    }

    @Override
    public void setMessageListSubTitle(String subTitle) {
        if (subTitle != null) {
            mActionBarSubTitle.setVisibility(View.VISIBLE);
            setActionBarSubTitle(subTitle);
        } else {
            mActionBarSubTitle.setVisibility(View.GONE);
        }
    }

    @Override
    public void setUnreadCount(int unread) {
        setActionBarUnread(unread);
    }

    @Override
    public void setMessageListProgress(int progress) {
        Toast.makeText(mContext, "setProgress NOT WORKING", Toast.LENGTH_LONG);
        // setProgress(progress);
    }

    @Override
    public void onResendMessage(MessageReference messageReference) {
        MessageActions.actionEditDraft(mContext, messageReference);
    }

    @Override
    public void onForward(MessageReference messageReference) {
        onForward(messageReference, null);
    }

    @Override
    public void onForward(MessageReference messageReference, Parcelable decryptionResultForReply) {
        MessageActions.actionForward(mContext, messageReference, decryptionResultForReply);
    }

    @Override
    public void onReply(MessageReference messageReference) {
        onReply(messageReference, null);
    }

    @Override
    public void onReply(MessageReference messageReference, Parcelable decryptionResultForReply) {
        MessageActions.actionReply(mContext, messageReference, false, decryptionResultForReply);
    }

    @Override
    public void onReplyAll(MessageReference messageReference) {
        onReplyAll(messageReference, null);
    }

    @Override
    public void onReplyAll(MessageReference messageReference, Parcelable decryptionResultForReply) {
        MessageActions.actionReply(mContext, messageReference, true, decryptionResultForReply);
    }

    @Override
    public void onCompose(Account account) {
        MessageActions.actionCompose(mContext, account);
    }

    @Override
    public void showMoreFromSameSender(String senderAddress) {
        LocalSearch tmpSearch = new LocalSearch("From " + senderAddress);
        tmpSearch.addAccountUuids(mSearch.getAccountUuids());
        tmpSearch.and(SearchSpecification.SearchField.SENDER, senderAddress,
                SearchSpecification.Attribute.CONTAINS);

        MessageListFragment fragment = MessageListFragment.newInstance(tmpSearch, false, false);

        addMessageListFragment(fragment, true);
    }

    private void addMessageListFragment(MessageListFragment fragment, boolean addToBackStack) {
        FragmentTransaction ft = mContext.getFragmentManager().beginTransaction();

        ft.replace(R.id.message_list_container, fragment);
        if (addToBackStack) {
            ft.addToBackStack(null);
        }

        mMessageListFragment = fragment;

        int transactionId = ft.commit();
        if (transactionId >= 0 && mFirstBackStackId < 0) {
            mFirstBackStackId = transactionId;
        }
    }

    @Override
    public void onBackStackChanged() {
        findFragments();

        if (mDisplayMode == DisplayMode.SPLIT_VIEW) {
            showMessageViewPlaceHolder();
        }

        configureMenu(mMenu);
    }

    @Override
    public void onSwipeRightToLeft(MotionEvent e1, MotionEvent e2) {
        if (mMessageListFragment != null && mDisplayMode != DisplayMode.MESSAGE_VIEW) {
            mMessageListFragment.onSwipeRightToLeft(e1, e2);
        }
    }

    @Override
    public void onSwipeLeftToRight(MotionEvent e1, MotionEvent e2) {
        if (mMessageListFragment != null && mDisplayMode != DisplayMode.MESSAGE_VIEW) {
            mMessageListFragment.onSwipeLeftToRight(e1, e2);
        }
    }

    @Override
    public boolean startSearch(Account account, String folderName) {
        // If this search was started from a MessageList of a single folder, pass along that folder
        // info
        // so that we can enable remote search.
        if (account != null && folderName != null) {
            final Bundle appData = new Bundle();
            appData.putString(EXTRA_SEARCH_ACCOUNT, account.getUuid());
            appData.putString(EXTRA_SEARCH_FOLDER, folderName);
            mSearchView.setAppSearchData(appData);
        } else {
            // TODO Handle the case where we're searching from within a search result.
            // mContext.startSearch(null, false, null, false);
        }
        Toast.makeText(mContext, "startSearch not working", Toast.LENGTH_LONG);


        return true;
    }

    @Override
    public void showThread(Account account, String folderName, long threadRootId) {
        showMessageViewPlaceHolder();

        LocalSearch tmpSearch = new LocalSearch();
        tmpSearch.addAccountUuid(account.getUuid());
        tmpSearch.and(SearchSpecification.SearchField.THREAD_ID, String.valueOf(threadRootId),
                SearchSpecification.Attribute.EQUALS);

        MessageListFragment fragment = MessageListFragment.newInstance(tmpSearch, true, false);
        addMessageListFragment(fragment, true);
    }

    @Override
    public void remoteSearchStarted() {
        // Remove action button for remote search
        configureMenu(mMenu);
    }

    @Override
    public void goBack() {
        FragmentManager fragmentManager = mContext.getFragmentManager();
        if (mDisplayMode == DisplayMode.MESSAGE_VIEW) {
            showMessageList();
        } else if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else if (mMessageListFragment.isManualSearch()) {
            mContext.finish();
        } else if (!mSingleFolderMode) {
            onAccounts();
        } else {
            onShowFolderList();
        }
    }

    private void createFlaggedSearch(Account account, FolderInfoHolder folder) {
        String searchTitle = mContext.getString(
                R.string.search_title, mContext.getString(R.string.message_list_title,
                        account.getDescription(), folder.displayName),
                mContext.getString(R.string.flagged_modifier));

        LocalSearch search = new LocalSearch(searchTitle);
        search.and(SearchSpecification.SearchField.FLAGGED, "1",
                SearchSpecification.Attribute.EQUALS);
        search.addAllowedFolder(folder.name);
        search.addAccountUuid(account.getUuid());
        showFolder(search);
        mListener.closeDrawer();
    }

    private void createUnreadSearch(Account account, FolderInfoHolder folder) {
        String searchTitle = mContext.getString(
                R.string.search_title, mContext.getString(R.string.message_list_title,
                        account.getDescription(), folder.displayName),
                mContext.getString(R.string.unread_modifier));

        LocalSearch search = new LocalSearch(searchTitle);
        search.and(SearchSpecification.SearchField.READ, "1",
                SearchSpecification.Attribute.NOT_EQUALS);

        search.addAllowedFolder(folder.name);
        search.addAccountUuid(account.getUuid());
        showFolder(search);
        mListener.closeDrawer();
    }

    public void showDialogSettings(final Account account) {
        mListener.closeDrawer();
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setItems(R.array.settings_titles, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    // account settings
                    case 0:
                        AccountSettings.actionSettings(mListener.getActivity(), account);
                        break;
                    // global settings
                    case 1:
                        Prefs.actionPrefs(mListener.getActivity());
                        break;
                    // update account
                    case 2:
                        if (mContext instanceof INavigationDrawerActivityListener) {
                            ((INavigationDrawerActivityListener) mContext).updateAccount(mAccount);
                        }
                        break;
                    // delete account
                    case 3:
                        showDeleteAccountDialog();
                        break;
                    // informations
                    case 4:
                        if (mContext instanceof INavigationDrawerActivityListener) {
                            ((INavigationDrawerActivityListener) mContext).showInformations();
                        }
                        break;
                }
            }
        });
        builder.create().show();
    }

    private void showDeleteAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.account_delete_dlg_title);
        builder.setMessage(mContext.getString(R.string.account_delete_dlg_instructions_fmt,
                mAccount.getDescription()));
        builder.setPositiveButton(R.string.okay_action, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                boolean isMainAccount =
                        Preferences.getPreferences(mContext).getAccounts().get(0).equals(mAccount)
                                ? true : false;
                deleteAccount(isMainAccount);
                refreshView();
            }
        });
        builder.setNegativeButton(R.string.cancel_action, null);
        builder.create().show();
    }

    private void deleteAccount(boolean isMainAccount) {
        try {
            // delete from db
            if (isMainAccount) {
                for (Account account : Preferences.getPreferences(mContext).getAccounts()) {
                    account.getLocalStore().delete();
                }
            } else {
                mAccount.getLocalStore().delete();
            }
        } catch (Exception e) {
            // Ignore, this may lead to localStores on sd-cards that
            // are currently not inserted to be left
        }
        if (isMainAccount) {
            for (Account account : Preferences.getPreferences(mContext).getAccounts()) {
                MessagingController.getInstance(mContext).deleteAccount(account);
                Preferences.getPreferences(mContext).deleteAccount(account);
            }
        } else {
            MessagingController.getInstance(mContext).deleteAccount(mAccount);
            Preferences.getPreferences(mContext).deleteAccount(mAccount);
        }
        K9.setServicesEnabled(mContext);
    }

    private void refreshView() {
        NavigationDrawerActivity.listMessage(mContext, null);
    }

    public class MailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int HEADER = 0;
        private static final int FOLDER = 1;

        List<FolderInfoHolder> mItems = new ArrayList<>();
        private int mSelectedPos = 1; // default inbox

        class HeaderMenu extends FolderInfoHolder {

        }

        MailAdapterClickListener mClickListener = new MailAdapterClickListener() {
            @Override
            public void onSettingsClick() {
                super.onSettingsClick();
                showDialogSettings(mAccount);
            }

            @Override
            public void onFolderClick(Account account, FolderInfoHolder folder) {
                super.onFolderClick(account, folder);
                LocalSearch search = new LocalSearch(folder.name);
                search.addAllowedFolder(folder.name);
                search.addAccountUuid(account.getUuid());
                showFolder(search);
                setSelectedPos(folder.name);
                notifyDataSetChanged();
                mListener.closeDrawer();
            }
        };

        public void setSelectedPos(String folderName) {

            if (folderName == null) {
                return;
            }

            for (int i = 0; i < mItems.size(); i++) {
                FolderInfoHolder folder = mItems.get(i);
                if (folderName.equals(folder.name)) {
                    mSelectedPos = i;
                }
            }
        }

        public void updateData() {
            mItems.clear();
            // header
            mItems.add(new HeaderMenu());
            mItems.addAll(mFolders);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, final int type) {

            View view;
            LayoutInflater inflater = (LayoutInflater) parent.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            switch (type) {
                case HEADER:
                    view = inflater.inflate(R.layout.nav_drawer_menu_header, parent, false);
                    return new HeaderViewHolder(view);
                case FOLDER:
                    view = inflater.inflate(R.layout.folder_holder, parent, false);
                    return new FolderViewHolder(view);
            }
            return null;
        }

        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof HeaderViewHolder) {
                final HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                if (mAccount != null) {
                    headerViewHolder.mSectionName.setText(mContext.getString(R.string.tab_mail),
                            TextView.BufferType.NORMAL);
                    headerViewHolder.mAccountTv.setText(mAccount.getEmail());
                    headerViewHolder.mAccountDisplayNameTv.setText(mAccount.getName());
                }
                headerViewHolder.mExpandMenuIconIv
                        .setImageResource(R.drawable.ic_arrow_drop_down_white_24dp);

                headerViewHolder.mAccountContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mListener.setDrawerListAdapter(mAccountsAdapter);
                    }
                });

                headerViewHolder.mSettingsIv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mClickListener.onSettingsClick();
                    }
                });
            } else if (holder instanceof FolderViewHolder) {
                final FolderInfoHolder folder = getItem(position);
                FolderViewHolder mailViewHolder = (FolderViewHolder) holder;
                mailViewHolder.itemView.setSelected(mSelectedPos == position);

                // row background
                if (mailViewHolder.itemView.isSelected()) {
                    mailViewHolder.mContainerRl.setBackgroundColor(
                            ContextCompat.getColor(mContext, R.color.colorItemSelected));
                } else {
                    mailViewHolder.mContainerRl.setBackgroundColor(
                            ContextCompat.getColor(mContext, android.R.color.transparent));
                }

                // icon
                if (mailViewHolder.itemView.isSelected()) {
                    mailViewHolder.mFolderIconIv.setImageAlpha(255);
                } else {
                    mailViewHolder.mFolderIconIv.setImageAlpha(138);
                }

                // Title
                if (folder.displayName != null) {
                    mailViewHolder.mFolderNameTv.setText(folder.displayName);
                }

                // unread messages
                if (folder.unreadMessageCount == -1) {
                    folder.unreadMessageCount = 0;
                    try {
                        folder.unreadMessageCount = folder.folder.getUnreadMessageCount();
                    } catch (Exception e) {
                        Log.e(K9.LOG_TAG, "Unable to get unreadMessageCount for "
                                + mAccount.getDescription() + ":" + folder.name);
                    }
                }
                setWidthForMaxCount(mailViewHolder.mNewMessageCountTv);
                if (folder.unreadMessageCount <= MAX_MESSAGES_COUNT) {
                    mailViewHolder.mNewMessageCountTv
                            .setText(String.format("%d", folder.unreadMessageCount));
                }

                // new messages icon gone
                mailViewHolder.mNewMessageCountIconIv.setVisibility(View.GONE);
                // mailViewHolder.mNewMessageCountIconIv.setBackgroundDrawable(
                // mAccount.generateColorChip(false, false, false, false, false).drawable());

                mailViewHolder.mNewMessageCountWrapperV
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (folder.unreadMessageCount > 0) {
                                    createUnreadSearch(mAccount, folder);
                                }
                            }
                        });

                // flagged messages
                if (folder.flaggedMessageCount == -1) {
                    folder.flaggedMessageCount = 0;
                    try {
                        folder.flaggedMessageCount = folder.folder.getFlaggedMessageCount();
                    } catch (Exception e) {
                        Log.e(K9.LOG_TAG, "Unable to get flaggedMessageCount for "
                                + mAccount.getDescription() + ":" + folder.name);
                    }

                }
                if (folder.flaggedMessageCount <= MAX_MESSAGES_COUNT) {
                    mailViewHolder.mFlaggedMessageCountTv
                            .setText(String.format("%d", folder.flaggedMessageCount));
                } else {
                    mailViewHolder.mFlaggedMessageCountTv
                            .setText(String.format("+%d", MAX_MESSAGES_COUNT));
                }

                mailViewHolder.mFlaggedMessageCountIconIv.setBackgroundDrawable(
                        mAccount.generateColorChip(false, false, false, false, true).drawable());
                mailViewHolder.mFlaggedMessageCountWrapperV
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (folder.flaggedMessageCount > 0) {
                                    createFlaggedSearch(mAccount, folder);
                                }
                            }
                        });

                // click listener
                mailViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mClickListener.onFolderClick(mAccount, folder);
                    }
                });
            }
        }

        private void setWidthForMaxCount(TextView tv) {
            String maxCount = String.format("+%d", MAX_MESSAGES_COUNT);
            tv.setText(maxCount);
            tv.measure(0, 0);
            ViewGroup.LayoutParams params = tv.getLayoutParams();
            params.width = tv.getMeasuredWidth();
            tv.setLayoutParams(params);
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? HEADER : FOLDER;
        }

        private FolderInfoHolder getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public int getFolderIndex(String folder) {
            FolderInfoHolder searchHolder = new FolderInfoHolder();
            searchHolder.name = folder;
            return mItems.indexOf(searchHolder);
        }

        public FolderInfoHolder getFolder(String folder) {
            FolderInfoHolder holder;

            int index = getFolderIndex(folder);
            if (index >= 0) {
                holder = mItems.get(index);
                if (holder != null) {
                    return holder;
                }
            }
            return null;
        }
    }


    public class AccountsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int HEADER = 0;
        private static final int ACCOUNT = 1;
        private static final int ADD_ACCOUNT_POSITION = 1;

        List<Account> mItems = new ArrayList<>();

        class HeaderMenu extends Account {

            protected HeaderMenu(Context context) {
                super(context);
            }
        }

        class AddAccountItem extends Account {

            protected AddAccountItem(Context context) {
                super(context);
            }
        }

        public AccountsAdapter() {
            updateData();
        }

        AccountsAdapterClickListener mClickListener = new AccountsAdapterClickListener() {
            @Override
            public void onSettingsClick() {
                super.onSettingsClick();
                showDialogSettings(mAccount);
            }

            @Override
            public void onAccountClick(Account account) {
                super.onAccountClick(account);
                // update current account
                mAccount = account;
                mAccountUuid = mAccount.getUuid();
                // update accounts list
                updateData();
                // set mail adapter as current list adapter
                mFolders.clear();
                mListener.setDrawerListAdapter(mMailAdapter);
                // update folders for new current account
                if (mContext instanceof INavigationDrawerActivityListener) {
                    ((INavigationDrawerActivityListener) mContext).updateAccount(mAccount);
                }
                // show default folder for new current account
                LocalSearch search = new LocalSearch(account.getAutoExpandFolderName());
                search.addAllowedFolder(account.getAutoExpandFolderName());
                search.addAccountUuid(account.getUuid());
                showFolder(search);
                // close drawer
                mListener.closeDrawer();
            }

            @Override
            public void onAddAccountClick() {
                super.onAddAccountClick();
                AccountSetupBasics.actionNewAccount(mContext);
            }
        };

        public void updateData() {
            mItems.clear();
            // header
            mItems.add(new HeaderMenu(mContext));
            // add account
            mItems.add(new AddAccountItem(mContext));
            mItems.addAll(Preferences.getPreferences(mContext).getAccounts());
            if (mAccount != null) {
                mItems.remove(mAccount);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, final int type) {

            View view;
            LayoutInflater inflater = (LayoutInflater) parent.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            switch (type) {
                case HEADER:
                    view = inflater.inflate(R.layout.nav_drawer_menu_header, parent, false);
                    return new HeaderViewHolder(view);
                case ACCOUNT:
                    view = inflater.inflate(R.layout.account_holder, parent, false);
                    return new AccountViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof HeaderViewHolder) {
                final HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                if (mAccount != null) {
                    headerViewHolder.mAccountTv.setText(mAccount.getEmail());
                    headerViewHolder.mAccountDisplayNameTv.setText(mAccount.getName());
                }
                headerViewHolder.mExpandMenuIconIv
                        .setImageResource(R.drawable.ic_arrow_drop_up_white_24dp);

                headerViewHolder.mAccountContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mListener.setDrawerListAdapter(mMailAdapter);
                    }
                });

                headerViewHolder.mSettingsIv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mClickListener.onSettingsClick();
                    }
                });
            } else if (holder instanceof AccountViewHolder) {
                AccountViewHolder accountViewHolder = (AccountViewHolder) holder;

                if (position == ADD_ACCOUNT_POSITION) {
                    // icon
                    accountViewHolder.mAccountIconIv.setImageResource(R.drawable.ic_add_black_24dp);
                    accountViewHolder.mAccountNameTv.setText(R.string.add_account_action);
                    accountViewHolder.itemView.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            mClickListener.onAddAccountClick();
                        }
                    });

                } else {
                    final Account account = getItem(position);
                    // icon
                    accountViewHolder.mAccountIconIv
                            .setImageResource(R.drawable.ic_email_black_24dp);

                    // Name
                    if (mAccount.getEmail() != null) {
                        accountViewHolder.mAccountNameTv.setText(account.getEmail());
                    }
                    // click listener
                    accountViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mClickListener.onAccountClick(account);
                        }
                    });
                }
            }
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        private Account getItem(int position) {
            return mItems.get(position);
        }


        @Override
        public int getItemViewType(int position) {
            return position == 0 ? HEADER : ACCOUNT;
        }
    }


    class MailPresenterHandler extends Handler {

        public void newFolders(final List<FolderInfoHolder> newFolders) {
            mContext.runOnUiThread(new Runnable() {
                public void run() {
                    if (!mFolders.isEmpty()) {
                        mFolders.clear();
                    }
                    mFolders.addAll(newFolders);
                    mMailAdapter.updateData();
                    mHandler.dataChanged();
                }
            });
        }

        public void workingAccount(final int res) {
            mContext.runOnUiThread(new Runnable() {
                public void run() {
                    String toastText = mContext.getString(res, mAccount.getDescription());
                    Toast toast = Toast.makeText(mContext, toastText, Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
        }

        public void accountSizeChanged(final long oldSize, final long newSize) {
            mContext.runOnUiThread(new Runnable() {
                public void run() {
                    String toastText = mContext.getString(R.string.account_size_changed,
                            mAccount.getDescription(), SizeFormatter.formatSize(mContext, oldSize),
                            SizeFormatter.formatSize(mContext, newSize));

                    Toast toast = Toast.makeText(mContext, toastText, Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        }

        public void folderLoading(final String folder, final boolean loading) {
            mContext.runOnUiThread(new Runnable() {
                public void run() {
                    FolderInfoHolder folderHolder = mMailAdapter.getFolder(folder);


                    if (folderHolder != null) {
                        folderHolder.loading = loading;
                    }

                }
            });
        }

        // public void progress(final boolean progress) {
        // // Make sure we don't try this before the menu is initialized
        // // this could happen while the activity is initialized.
        // if (mRefreshMenuItem == null) {
        // return;
        // }
        //
        // runOnUiThread(new Runnable() {
        // public void run() {
        // if (progress) {
        // mRefreshMenuItem.setActionView(mActionBarProgressView);
        // } else {
        // mRefreshMenuItem.setActionView(null);
        // }
        // }
        // });
        //
        // }

        public void dataChanged() {
            mContext.runOnUiThread(new Runnable() {
                public void run() {
                    mMailAdapter.notifyDataSetChanged();
                }
            });
        }
    }
}
