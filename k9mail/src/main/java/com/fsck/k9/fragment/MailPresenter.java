package com.fsck.k9.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
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

import com.fsck.k9.Account;
import com.fsck.k9.K9;
import com.fsck.k9.Preferences;
import com.fsck.k9.R;
import com.fsck.k9.activity.Accounts;
import com.fsck.k9.activity.ActivityListener;
import com.fsck.k9.activity.FolderInfoHolder;
import com.fsck.k9.activity.FolderList;
import com.fsck.k9.activity.INavigationDrawerActivityListener;
import com.fsck.k9.activity.MessageReference;
import com.fsck.k9.activity.Search;
import com.fsck.k9.activity.TiscaliUtility;
import com.fsck.k9.activity.compose.MessageActions;
import com.fsck.k9.activity.misc.SwipeGestureDetector.OnSwipeGestureListener;
import com.fsck.k9.adapter.BaseNavDrawerMenuAdapter;
import com.fsck.k9.adapter.MailNavDrawerClickListener;
import com.fsck.k9.controller.MessagingController;
import com.fsck.k9.fragment.MessageListFragment.MessageListFragmentListener;
import com.fsck.k9.helper.SizeFormatter;
import com.fsck.k9.mail.Message;
import com.fsck.k9.mailstore.LocalFolder;
import com.fsck.k9.mailstore.StorageManager;
import com.fsck.k9.presenter.PresenterLifeCycle;
import com.fsck.k9.search.LocalSearch;
import com.fsck.k9.search.SearchAccount;
import com.fsck.k9.search.SearchSpecification;
import com.fsck.k9.ui.messageview.MessageViewFragment;
import com.fsck.k9.ui.messageview.MessageViewFragment.MessageViewFragmentListener;
import com.fsck.k9.view.MessageHeader;
import com.fsck.k9.view.MessageTitleView;
import com.fsck.k9.view.ViewSwitcher;
import com.fsck.k9.view.ViewSwitcher.OnSwitchCompleteListener;
import com.fsck.k9.view.holder.FolderViewHolder;
import com.fsck.k9.view.holder.HeaderViewHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;


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
    public static final String EXTRA_SEARCH_ACCOUNT = "com.fsck.k9.search_account";
    private static final String EXTRA_SEARCH_FOLDER = "com.fsck.k9.search_folder";

    private static final String EXTRA_MESSAGE_REFERENCE = "message_reference";
    public static final String ARG_ACCOUNT = "account";
    public static final String ARG_FOLDER = "folder";

    // Used for navigating to next/previous message
    private static final int PREVIOUS = 1;
    private static final int NEXT = 2;
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

    private Bundle mSavedInstanceState;

    /**
     * {@code true} when the message list was displayed once. This is used in
     * {@link # onBackPressed()} to decide whether to go from the message view to the message list or
     * finish the activity.
     */
    private boolean mMessageListWasDisplayed = false;
    private ViewSwitcher mViewSwitcher;

    private static final String STATE_DISPLAY_MODE = "displayMode";
    private static final String STATE_MESSAGE_LIST_WAS_DISPLAYED = "messageListWasDisplayed";
    private static final String STATE_FIRST_BACK_STACK_ID = "firstBackstackId";

    private MailAdapter mMailAdapter;
    List<FolderInfoHolder> mFolders = new ArrayList<>();
    private MailPresenterHandler mHandler = new MailPresenterHandler();

    private ActivityListener mMessagingListener = new ActivityListener() {
        @Override
        public void informUserOfStatus() {
            mHandler.dataChanged();
        }

        @Override
        public void listFoldersStarted(Account account) {
            if (account.equals(mAccount)) {
//                mHandler.progress(true);
            }
            super.listFoldersStarted(account);
        }

        @Override
        public void listFoldersFailed(Account account, String message) {
            if (account.equals(mAccount)) {
//                mHandler.progress(false);
            }
            super.listFoldersFailed(account, message);
        }

        @Override
        public void listFoldersFinished(Account account) {
            if (account.equals(mAccount)) {
//                mHandler.progress(false);
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
                    if (folder.isInTopGroup() || TiscaliUtility.isFolderInTopGroup(mContext, folder.getName())) {
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
//                mHandler.progress(true);
                mHandler.folderLoading(folder, true);
                mHandler.dataChanged();
            }

        }

        @Override
        public void synchronizeMailboxFinished(Account account, String folder, int totalMessagesInMailbox, int numNewMessages) {
            super.synchronizeMailboxFinished(account, folder, totalMessagesInMailbox, numNewMessages);
            if (account.equals(mAccount)) {
//                mHandler.progress(false);
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

//            mHandler.progress(false);

            mHandler.folderLoading(folder, false);

            //   String mess = truncateStatus(message);

            //   mHandler.folderStatus(folder, mess);
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
        public void folderStatusChanged(Account account, String folderName, int unreadMessageCount) {
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
        MESSAGE_LIST,
        MESSAGE_VIEW,
        SPLIT_VIEW
    }

    @Inject
    public MailPresenter(INavigationDrawerActivityListener listener, Intent intent) {
        mListener = listener;
        mContext = listener.getActivity();
        mIntent = intent;
    }

    public void setIntent(Intent intent) {
        mIntent = intent;
    }

    private void onRefresh(final boolean forceRemote) {
        if(mAccount != null) {
            MessagingController.getInstance(mContext).listFolders(mAccount, forceRemote, mMessagingListener);
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
            mViewSwitcher.setFirstInAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_in_left));
            mViewSwitcher.setFirstOutAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_out_right));
            mViewSwitcher.setSecondInAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_in_right));
            mViewSwitcher.setSecondOutAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_out_left));
            mViewSwitcher.setOnSwitchCompleteListener(this);
        }

        initializeActionBar();

        if (!decodeExtras()) {
            Toast.makeText(mContext,"RETURN FRAGMENT",Toast.LENGTH_LONG);
        }

        findFragments();
        initializeDisplayMode(mSavedInstanceState);
        initializeLayout();
        initializeFragments();
        displayViews();
        //setupGestureDetector(this);

        mMailAdapter = new MailAdapter();
        mListener.setDrawerListAdapter(mMailAdapter);

        // mail tab
        List<Account> accounts = Preferences.getPreferences(mContext).getAccounts();
        if(accounts != null && !accounts.isEmpty()) {
            mAccount = accounts.get(0);
        }
    }

    public void showFolder(LocalSearch search) {

        mSearch = search;
        mMessageReference = null;

        if(updateDataWithNewSearch()) {
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

    private void resetView() {
//        if (mFirstBackStackId >= 0) {
//            getFragmentManager().popBackStackImmediate(mFirstBackStackId,
//                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
//            mFirstBackStackId = -1;
//        }
        removeMessageListFragment();
        removeMessageViewFragment();

        mMessageReference = null;
        mSearch = null;
        mFolderName = null;

        if (!decodeExtras()) {
            return;
        }

        initializeDisplayMode(null);
        initializeFragments();
        displayViews();
    }

    private void findFragments() {
        FragmentManager fragmentManager = ((Activity)mContext).getFragmentManager();
        mMessageListFragment = (MessageListFragment) fragmentManager.findFragmentById(
                R.id.message_list_container);
        mMessageViewFragment = (MessageViewFragment) fragmentManager.findFragmentById(
                R.id.message_view_container);
    }

    private void removeMessageListFragment() {
        FragmentTransaction ft = mContext.getFragmentManager().beginTransaction();
        ft.remove(mMessageListFragment);
        mMessageListFragment = null;
        ft.commit();
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
            FragmentTransaction ft = fragmentManager.beginTransaction();
            mMessageListFragment = MessageListFragment.newInstance(mSearch, false,
                    (K9.isThreadedViewEnabled() && !mNoThreading));
            ft.add(R.id.message_list_container, mMessageListFragment);
            ft.commit();
        }

        // Check if the fragment wasn't restarted and has a MessageReference in the arguments. If
        // so, open the referenced message.
        if (!hasMessageListFragment && mMessageViewFragment == null &&
                mMessageReference != null) {
            openMessage(mMessageReference);
        }
    }

    /**
     * Set the initial display mode (message list, message view, or split view).
     *
     * <p><strong>Note:</strong>
     * This method has to be called after {@link #findFragments()} because the result depends on
     * the availability of a {@link MessageViewFragment} instance.
     * </p>
     *
     * @param savedInstanceState
     *         The saved instance state that was passed to the activity as argument to
     *         {@link # onCreateView(Bundle)}. May be {@code null}.
     */
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

        if (mMessageViewFragment != null || mMessageReference != null) {
            mDisplayMode = DisplayMode.MESSAGE_VIEW;
        } else {
            mDisplayMode = DisplayMode.MESSAGE_LIST;
        }
    }

    private void initializeLayout() {
        mMessageViewContainer = (ViewGroup) ((Activity)mContext).findViewById(R.id.message_view_container);

        mMessageViewPlaceHolder = mInflater.inflate(R.layout.empty_message_view, mMessageViewContainer, false);
    }

    private boolean useSplitView() {
        K9.SplitViewMode splitViewMode = K9.getSplitViewMode();
        int orientation = mContext.getResources().getConfiguration().orientation;

        return (splitViewMode == K9.SplitViewMode.ALWAYS ||
                (splitViewMode == K9.SplitViewMode.WHEN_IN_LANDSCAPE &&
                        orientation == Configuration.ORIENTATION_LANDSCAPE));
    }

    private void initializeActionBar() {
        mActionBar = ((AppCompatActivity)mContext).getSupportActionBar();

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
            FragmentTransaction ft = mContext.getFragmentManager().beginTransaction();
            ft.remove(mMessageViewFragment);
            mMessageViewFragment = null;
            ft.commit();

            showDefaultTitleView();
        }
    }

    @SuppressLint("InflateParams")
    private View getActionButtonIndeterminateProgress() {
        return mInflater.inflate(R.layout.actionbar_indeterminate_progress_actionview, null);
    }

    private boolean decodeExtras() {

        String action = mIntent.getAction();

        if (Intent.ACTION_VIEW.equals(action)) {
            Uri uri = mIntent.getData();
            if(uri != null) {
                List<String> segmentList = uri.getPathSegments();

                String accountId = segmentList.get(0);
                Collection<Account> accounts = Preferences.getPreferences(mContext).getAvailableAccounts();
                for (Account account : accounts) {
                    if (String.valueOf(account.getAccountNumber()).equals(accountId)) {
                        String folderName = segmentList.get(1);
                        String messageUid = segmentList.get(2);
                        mMessageReference = new MessageReference(account.getUuid(), folderName, messageUid, null);
                        break;
                    }
                }
            }
        } else if (ARG_SHORTCUT.equals(action)) {
            // Handle shortcut intents
            String specialFolder = mIntent.getStringExtra(EXTRA_SPECIAL_FOLDER);
            if (SearchAccount.UNIFIED_INBOX.equals(specialFolder)) {
                mSearch = SearchAccount.createUnifiedInboxAccount(mContext).getRelatedSearch();
            } else if (SearchAccount.ALL_MESSAGES.equals(specialFolder)) {
                mSearch = SearchAccount.createAllMessagesAccount(mContext).getRelatedSearch();
            }
        } else if (mIntent.getStringExtra(SearchManager.QUERY) != null) {
            // check if this intent comes from the system search ( remote )
            if (Intent.ACTION_SEARCH.equals(action)) {
                //Query was received from Search Dialog
                String query = mIntent.getStringExtra(SearchManager.QUERY).trim();

                mSearch = new LocalSearch(mContext.getString(R.string.search_results));
                mSearch.setManualSearch(true);
                mNoThreading = true;

                mSearch.or(new SearchSpecification.SearchCondition(SearchSpecification.SearchField.SENDER, SearchSpecification.Attribute.CONTAINS, query));
                mSearch.or(new SearchSpecification.SearchCondition(SearchSpecification.SearchField.SUBJECT, SearchSpecification.Attribute.CONTAINS, query));
                mSearch.or(new SearchSpecification.SearchCondition(SearchSpecification.SearchField.MESSAGE_CONTENTS, SearchSpecification.Attribute.CONTAINS, query));

                Bundle appData = mIntent.getBundleExtra(SearchManager.APP_DATA);
                if (appData != null) {
                    mSearch.addAccountUuid(appData.getString(EXTRA_SEARCH_ACCOUNT));
                    // searches started from a folder list activity will provide an account, but no folder
                    if (appData.getString(EXTRA_SEARCH_FOLDER) != null) {
                        mSearch.addAllowedFolder(appData.getString(EXTRA_SEARCH_FOLDER));
                    }
                } else {
                    mSearch.addAccountUuid(LocalSearch.ALL_ACCOUNTS);
                }
            }
        } else {
            // regular LocalSearch object was passed
            mSearch = mIntent.getParcelableExtra(ARG_SEARCH);
            mNoThreading = mIntent.getBooleanExtra(ARG_NO_THREADING, false);
        }

        if (mMessageReference == null) {
            mMessageReference = mIntent.getParcelableExtra(EXTRA_MESSAGE_REFERENCE);
        }

        if (mMessageReference != null) {
            mSearch = new LocalSearch();
            mSearch.addAccountUuid(mMessageReference.getAccountUuid());
            mSearch.addAllowedFolder(mMessageReference.getFolderName());
        }

        if (mSearch == null) {
            // We've most likely been started by an old unread widget
            String accountUuid = mIntent.getStringExtra(ARG_ACCOUNT);
            String folderName = mIntent.getStringExtra(ARG_FOLDER);

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
            if (mSingleAccountMode) {
                mAccount = accounts.get(0);
            }
        } else {
            mSingleAccountMode = (accountUuids.length == 1);
            if (mSingleAccountMode) {
                mAccount = prefs.getAccount(accountUuids[0]);
            }
        }
        mSingleFolderMode = mSingleAccountMode && (mSearch.getFolderNames().size() == 1);

        if (mSingleAccountMode && (mAccount == null || !mAccount.isAvailable(mContext))) {
            Log.i(K9.LOG_TAG, "not opening MessageList of unavailable account");
            //onAccountUnavailable();
            return false;
        }

        if (mSingleFolderMode) {
            mFolderName = mSearch.getFolderNames().get(0);
        }

        // now we know if we are in single account mode and need a subtitle
        mActionBarSubTitle.setVisibility((!mSingleFolderMode) ? View.GONE : View.VISIBLE);

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
            FragmentTransaction ft = ((Activity)mContext).getFragmentManager().beginTransaction();
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
     *
     * <p><strong>Note:</strong>
     * Please adjust the comments in {@code res/menu/message_list_option.xml} if you change the
     * visibility of a menu item in this method.
     * </p>
     *
     * @param menu
     *         The {@link Menu} instance that should be modified. May be {@code null}; in that case
     *         the method does nothing and immediately returns.
     */
    private boolean configureMenu(Menu menu) {
        if (menu == null) {
            return false;
        }

        /*
         * Set visibility of menu items related to the message view
         */

        if (mDisplayMode == DisplayMode.MESSAGE_LIST
                || mMessageViewFragment == null
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
                boolean initialized = (mMessageListFragment != null &&
                        mMessageListFragment.isLoadFinished());
                boolean canDoPrev = (initialized && !mMessageListFragment.isFirst(ref));
                boolean canDoNext = (initialized && !mMessageListFragment.isLast(ref));

                MenuItem prev = menu.findItem(R.id.previous_message);
                prev.setEnabled(canDoPrev);
                prev.getIcon().setAlpha(canDoPrev ? 255 : 127);

                MenuItem next = menu.findItem(R.id.next_message);
                next.setEnabled(canDoNext);
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

            // Set title of menu item to toggle the read state of the currently displayed message
            if (mMessageViewFragment.isMessageRead()) {
                menu.findItem(R.id.toggle_unread).setTitle(R.string.mark_as_unread_action);
            } else {
                menu.findItem(R.id.toggle_unread).setTitle(R.string.mark_as_read_action);
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
                menu.findItem(R.id.archive).setVisible(canMessageBeArchived &&
                        K9.isMessageViewArchiveActionVisible());
                menu.findItem(R.id.spam).setVisible(canMessageBeMovedToSpam &&
                        K9.isMessageViewSpamActionVisible());

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

        if (mDisplayMode == DisplayMode.MESSAGE_VIEW || mMessageListFragment == null ||
                !mMessageListFragment.isInitialized()) {
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
            menu.findItem(R.id.mark_all_as_read).setVisible(
                    mMessageListFragment.isMarkAllAsReadSupported());

            if (!mMessageListFragment.isSingleAccountMode()) {
                menu.findItem(R.id.expunge).setVisible(false);
                menu.findItem(R.id.send_messages).setVisible(false);
                menu.findItem(R.id.show_folder_list).setVisible(false);
            } else {
                menu.findItem(R.id.send_messages).setVisible(mMessageListFragment.isOutbox());
                menu.findItem(R.id.expunge).setVisible(mMessageListFragment.isRemoteFolder() &&
                        mMessageListFragment.isAccountExpungeCapable());
                menu.findItem(R.id.show_folder_list).setVisible(true);
            }

            menu.findItem(R.id.check_mail).setVisible(mMessageListFragment.isCheckMailSupported());

            // If this is an explicit local search, show the option to search on the server
            if (!mMessageListFragment.isRemoteSearch() &&
                    mMessageListFragment.isRemoteSearchAllowed()) {
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
                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onAccountUnavailable();
                    }
                });
            }
        }

        @Override
        public void onMount(String providerId) {
            // no-op
        }
    }

    protected void onAccountUnavailable() {
        Toast.makeText(mContext,"Account Unavaible Finish Activity", Toast.LENGTH_LONG);
        ((Activity)mContext).finish();
        // TODO inform user about account unavailability using Toast
        Accounts.listAccounts(mContext);
    }

    @Override
    public void onPause() {

        if(!mStarted) {
            return;
        }

        StorageManager.getInstance((mContext).getApplication()).removeListener(mStorageListener);

        MessagingController.getInstance(mContext).removeListener(mMessagingListener);
        mMessagingListener.onPause(mContext);
    }

    @Override
    public void onResume() {

        if(!mStarted) {
            return;
        }

        if (!(this instanceof Search)) {
            //necessary b/c no guarantee Search.onStop will be called before MessageList.onResume
            //when returning from search results
            Search.setActive(false);
        }

        if (mAccount != null && !mAccount.isAvailable(mContext)) {
            onAccountUnavailable();
            return;
        }
        StorageManager.getInstance(mContext.getApplication()).addListener(mStorageListener);

        MessagingController.getInstance(mContext).addListener(mMessagingListener);

        onRefresh(!REFRESH_REMOTE);

        MessagingController.getInstance(mContext).cancelNotificationsForAccount(mAccount);
        mMessagingListener.onResume(mContext);
    }

    @Override
    public void onDetach() {
        if(!mStarted) {
            return;
        }
        removeMessageListFragment();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(!mStarted) {
            return;
        }

        outState.putSerializable(STATE_DISPLAY_MODE, mDisplayMode);
        outState.putBoolean(STATE_MESSAGE_LIST_WAS_DISPLAYED, mMessageListWasDisplayed);
        outState.putInt(STATE_FIRST_BACK_STACK_ID, mFirstBackStackId);
    }

    @Override
    public void setStartInstanceState(Bundle savedInstanceState) {
        mSavedInstanceState = savedInstanceState;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mMessageListWasDisplayed = savedInstanceState.getBoolean(STATE_MESSAGE_LIST_WAS_DISPLAYED);
            mFirstBackStackId = savedInstanceState.getInt(STATE_FIRST_BACK_STACK_ID);
        }
    }

//    @Override
//    public void onBackPressed() {
//        if (mDisplayMode == MessageList.DisplayMode.MESSAGE_VIEW && mMessageListWasDisplayed) {
//            showMessageList();
//        } else {
//            super.onBackPressed();
//        }
//    }

    /**
     * Handle hotkeys
     *
     * <p>
     * This method is called by {@link # dispatchKeyEvent(KeyEvent)} before any view had the chance
     * to consume this key event.
     * </p>
     *
     * @param keyCode
     *         The value in {@code event.getKeyCode()}.
     * @param event
     *         Description of the key event.
     *
     * @return {@code true} if this event was consumed.
     */
    public boolean onCustomKeyDown(final int keyCode, final KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP: {
                if (mMessageViewFragment != null && mDisplayMode != DisplayMode.MESSAGE_LIST &&
                        K9.useVolumeKeysForNavigationEnabled()) {
                    showPreviousMessage();
                    return true;
                } else if (mDisplayMode != DisplayMode.MESSAGE_VIEW &&
                        K9.useVolumeKeysForListNavigationEnabled()) {
                    mMessageListFragment.onMoveUp();
                    return true;
                }

                break;
            }
            case KeyEvent.KEYCODE_VOLUME_DOWN: {
                if (mMessageViewFragment != null && mDisplayMode != DisplayMode.MESSAGE_LIST &&
                        K9.useVolumeKeysForNavigationEnabled()) {
                    showNextMessage();
                    return true;
                } else if (mDisplayMode != DisplayMode.MESSAGE_VIEW &&
                        K9.useVolumeKeysForListNavigationEnabled()) {
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
            /* FIXME
            case KeyEvent.KEYCODE_Z: {
                mMessageViewFragment.zoom(event);
                return true;
            }*/
            case KeyEvent.KEYCODE_H: {
                Toast toast = Toast.makeText(mContext, mContext.getString(R.string.message_list_help_key), Toast.LENGTH_LONG);
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

//    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Swallow these events too to avoid the audible notification of a volume change
        if (K9.useVolumeKeysForListNavigationEnabled()) {
            if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP) || (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
                if (K9.DEBUG)
                    Log.v(K9.LOG_TAG, "Swallowed key up.");
                return true;
            }
        }
        Toast.makeText(mContext,"onKeyUp fragment",Toast.LENGTH_LONG);
        return true;
//        return super.onKeyUp(keyCode, event);
    }

    private boolean showNextMessage() {
        MessageReference ref = mMessageViewFragment.getMessageReference();
        if (ref != null) {
            if (mMessageListFragment.openNext(ref)) {
                mLastDirection = NEXT;
                return true;
            }
        }
        return false;
    }

    private boolean showPreviousMessage() {
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
        if(!mStarted) {
            return;
        }

        mContext.getMenuInflater().inflate(R.menu.message_list_option, menu);
        mMenu = menu;
        mMenuButtonCheckMail= menu.findItem(R.id.check_mail);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(!mStarted) {
            return false;
        }
        return configureMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(!mStarted) {
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
            //TODO: This is not true for "unread" and "starred" searches in regular folders
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
                return true; //super.onOptionsItemSelected(item);
            }
        }
    }

    private void onToggleTheme() {
        Toast.makeText(mContext, "TOGGLE THEME NOT WORKING", Toast.LENGTH_LONG);
//        if (K9.getK9MessageViewTheme() == K9.Theme.DARK) {
//            K9.setK9MessageViewThemeSetting(K9.Theme.LIGHT);
//        } else {
//            K9.setK9MessageViewThemeSetting(K9.Theme.DARK);
//        }
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Context appContext = mContext.getApplicationContext();
//                Preferences prefs = Preferences.getPreferences(appContext);
//                StorageEditor editor = prefs.getStorage().edit();
//                K9.save(editor);
//                editor.commit();
//            }
//        }).start();

//        recreate();
    }

    //    @Override
//    public boolean onSearchRequested() {
//        return mMessageListFragment.onSearchRequested();
//    }

    @Override
    public void onSwitchComplete(int displayedChild) {
        if (displayedChild == 0) {
            removeMessageViewFragment();
            setActionBarToggle();
        }
        else {
            setActionBarUp();
        }
    }

    @Override
    public void updateMenu() {
        Toast.makeText(mContext, "invalidateOptionsMenu", Toast.LENGTH_LONG);
//        invalidateOptionsMenu();
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

//    @Override
//    public void setProgress(boolean enable) {
//        setProgressBarIndeterminateVisibility(enable);
//    }

    @Override
    public void displayMessageSubject(String subject) {
        if (mDisplayMode == DisplayMode.MESSAGE_VIEW) {
            mActionBarSubject.setText(subject);
        }
    }

    @Override
    public void setProgress(boolean b) {
        Toast.makeText(mContext,"setProgressBarIndeterminateVisibility not working", Toast.LENGTH_LONG);
//        setProgressBarIndeterminateVisibility(enable);
    }

    @Override
    public void enableActionBarProgress(boolean enable) {
        if (mMenuButtonCheckMail != null && mMenuButtonCheckMail.isVisible()) {
            mActionBarProgress.setVisibility(ProgressBar.GONE);
            if (enable) {
                mMenuButtonCheckMail
                        .setActionView(mActionButtonIndeterminateProgress);
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
        setActionBarSubTitle(subTitle);
    }

    @Override
    public void setUnreadCount(int unread) {
        setActionBarUnread(unread);
    }

    @Override
    public void setMessageListProgress(int progress) {
        Toast.makeText(mContext, "setProgress NOT WORKING", Toast.LENGTH_LONG);
//        setProgress(progress);
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
        tmpSearch.and(SearchSpecification.SearchField.SENDER, senderAddress, SearchSpecification.Attribute.CONTAINS);

        MessageListFragment fragment = MessageListFragment.newInstance(tmpSearch, false, false);

        addMessageListFragment(fragment, true);
    }

    private void addMessageListFragment(MessageListFragment fragment, boolean addToBackStack) {
        FragmentTransaction ft = ((Activity)mContext).getFragmentManager().beginTransaction();

        ft.replace(R.id.message_list_container, fragment);
        if (addToBackStack)
            ft.addToBackStack(null);

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
        // If this search was started from a MessageList of a single folder, pass along that folder info
        // so that we can enable remote search.
        if (account != null && folderName != null) {
            final Bundle appData = new Bundle();
            appData.putString(EXTRA_SEARCH_ACCOUNT, account.getUuid());
            appData.putString(EXTRA_SEARCH_FOLDER, folderName);
//            startSearch(null, false, appData, false);
        } else {
            // TODO Handle the case where we're searching from within a search result.
//            startSearch(null, false, null, false);
        }
        Toast.makeText(mContext,"startSearch not working", Toast.LENGTH_LONG);
        return true;
    }

    @Override
    public void showThread(Account account, String folderName, long threadRootId) {
        showMessageViewPlaceHolder();

        LocalSearch tmpSearch = new LocalSearch();
        tmpSearch.addAccountUuid(account.getUuid());
        tmpSearch.and(SearchSpecification.SearchField.THREAD_ID, String.valueOf(threadRootId), SearchSpecification.Attribute.EQUALS);

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
        FragmentManager fragmentManager = ((Activity)mContext).getFragmentManager();
        if (mDisplayMode == DisplayMode.MESSAGE_VIEW) {
            showMessageList();
        } else if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else if (mMessageListFragment.isManualSearch()) {
            ((Activity)mContext).finish();
        } else if (!mSingleFolderMode) {
            onAccounts();
        } else {
            onShowFolderList();
        }
    }

    private void createFlaggedSearch(Account account, FolderInfoHolder folder) {
        String searchTitle = mContext.getString(R.string.search_title,
                mContext.getString(R.string.message_list_title, account.getDescription(),
                        folder.displayName),
                mContext.getString(R.string.flagged_modifier));

        LocalSearch search = new LocalSearch(searchTitle);
        search.and(SearchSpecification.SearchField.FLAGGED, "1", SearchSpecification.Attribute.EQUALS);
        search.addAllowedFolder(folder.name);
        search.addAccountUuid(account.getUuid());
        showFolder(search);
        mListener.closeDrawer();
    }

    private void createUnreadSearch(Account account, FolderInfoHolder folder) {
        String searchTitle = mContext.getString(R.string.search_title,
                mContext.getString(R.string.message_list_title, account.getDescription(),
                        folder.displayName),
                mContext.getString(R.string.unread_modifier));

        LocalSearch search = new LocalSearch(searchTitle);
        search.and(SearchSpecification.SearchField.READ, "1", SearchSpecification.Attribute.NOT_EQUALS);

        search.addAllowedFolder(folder.name);
        search.addAccountUuid(account.getUuid());
        showFolder(search);
        mListener.closeDrawer();
    }

    public class MailAdapter extends BaseNavDrawerMenuAdapter {

        MailNavDrawerClickListener mClickListener = new MailNavDrawerClickListener() {
            @Override
            public void onSettingsClick() {
                super.onSettingsClick();
                mListener.showDialogSettings(mAccount);
            }

            @Override
            public void onFolderClick(Account account, FolderInfoHolder folder) {
                super.onFolderClick(account, folder);
                LocalSearch search = new LocalSearch(folder.name);
                search.addAllowedFolder(folder.name);
                search.addAccountUuid(account.getUuid());
                showFolder(search);
                mListener.closeDrawer();
            }
        };

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, final int type) {

            View view;
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            switch (type) {
                case HEADER:
                    view = inflater.inflate(R.layout.nav_drawer_menu_header, parent, false);
                    return new HeaderViewHolder(view);
                case ITEM:
                    view = inflater.inflate(R.layout.folder_holder, parent, false);
                    return new FolderViewHolder(view);
            }
            return null;
        }

        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if(holder instanceof HeaderViewHolder) {
                // TODO
                final HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                headerViewHolder.mAccountTv.setText("Name Surname");
                headerViewHolder.mAccountTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
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

                // Title
                if(folder.displayName != null) {
                    mailViewHolder.mFolderNameTv.setText(folder.displayName);
                }

                // unread messages
                if(folder.unreadMessageCount == -1) {
                    folder.unreadMessageCount = 0;
                    try {
                        folder.unreadMessageCount  = folder.folder.getUnreadMessageCount();
                    } catch (Exception e) {
                        Log.e(K9.LOG_TAG, "Unable to get unreadMessageCount for " + mAccount.getDescription() + ":"
                                + folder.name);
                    }
                }
                mailViewHolder.mNewMessageCountTv.setText(String.format("%d", folder.unreadMessageCount));
                // new messages icon gone
                mailViewHolder.mNewMessageCountIconIv.setVisibility(View.INVISIBLE);
//                mailViewHolder.mNewMessageCountIconIv.setBackgroundDrawable(
//                        mAccount.generateColorChip(false, false, false, false, false).drawable());

                mailViewHolder.mNewMessageCountWrapperV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(folder.unreadMessageCount > 0) {
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
                        Log.e(K9.LOG_TAG, "Unable to get flaggedMessageCount for " + mAccount.getDescription() + ":"
                                + folder.name);
                    }

                }
                mailViewHolder.mFlaggedMessageCountTv.setText(String.format("%d", folder.flaggedMessageCount));
                mailViewHolder.mFlaggedMessageCountIconIv.setBackgroundDrawable(
                        mAccount.generateColorChip(false, false, false, false,true).drawable());
                mailViewHolder.mFlaggedMessageCountWrapperV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (folder.flaggedMessageCount > 0) {
                            createFlaggedSearch(mAccount, folder);
                        }
                    }
                });

                //click listener
                mailViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mClickListener.onFolderClick(mAccount, folder);
                    }
                });
            }
        }

        @Override
        public int getChildrenCount(int position) {
            return 0;
        }

        @Override
        public int getItemDepth(int position) {
            return 0;
        }

        @Override
        public boolean isItemExpanded(int position) {
            return false;
        }

        @Override
        public int getItemViewType(int position) {
            // without header
//        return ITEM;
            // with header
            return position == 0 ? HEADER : ITEM;
        }

        private FolderInfoHolder getItem(int position) {
            // without header
//        return mFolders.get(position);
            // with header
            return mFolders.get(position - 1);
        }

        @Override
        public int getItemCount() {
            // without header
//        return mFolders.size();
            // with header
            return mFolders.size() + 1;
        }

        public int getFolderIndex(String folder) {
            FolderInfoHolder searchHolder = new FolderInfoHolder();
            searchHolder.name = folder;
            return  mFolders.indexOf(searchHolder);
        }

        public FolderInfoHolder getFolder(String folder) {
            FolderInfoHolder holder;

            int index = getFolderIndex(folder);
            if (index >= 0) {
                holder = mFolders.get(index);
                if (holder != null) {
                    return holder;
                }
            }
            return null;
        }
    }

    class MailPresenterHandler extends Handler {

        public void newFolders(final List<FolderInfoHolder> newFolders) {
            mContext.runOnUiThread(new Runnable() {
                public void run() {
                    mFolders.clear();
                    mFolders.addAll(newFolders);
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
                    String toastText = mContext.getString(R.string.account_size_changed, mAccount.getDescription(),
                            SizeFormatter.formatSize(mContext, oldSize), SizeFormatter.formatSize(mContext, newSize));

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

//        public void progress(final boolean progress) {
//            // Make sure we don't try this before the menu is initialized
//            // this could happen while the activity is initialized.
//            if (mRefreshMenuItem == null) {
//                return;
//            }
//
//            runOnUiThread(new Runnable() {
//                public void run() {
//                    if (progress) {
//                        mRefreshMenuItem.setActionView(mActionBarProgressView);
//                    } else {
//                        mRefreshMenuItem.setActionView(null);
//                    }
//                }
//            });
//
//        }

        public void dataChanged() {
            mContext.runOnUiThread(new Runnable() {
                public void run() {
                    mMailAdapter.notifyDataSetChanged();
                }
            });
        }
    }
}
