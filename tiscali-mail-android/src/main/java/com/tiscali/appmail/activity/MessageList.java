package com.tiscali.appmail.activity;


import com.tiscali.appmail.K9;
import com.tiscali.appmail.K9.SplitViewMode;
import com.tiscali.appmail.R;
import com.tiscali.appmail.fragment.MailPresenter;
import com.tiscali.appmail.fragment.MessageListFragment;
import com.tiscali.appmail.search.SearchSpecification;
import com.tiscali.appmail.ui.messageview.MessageViewFragment;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import de.cketti.library.changelog.ChangeLog;


/**
 * MessageList is the primary user interface for the program. This Activity shows a list of
 * messages. From this Activity the user can perform all standard message operations.
 */
public class MessageList extends K9Activity
        implements MessageListFragment.MessageListFragmentGetListener,
        MessageViewFragment.MessageViewFragmentGetListener {

    // for this activity
    private static final String EXTRA_SEARCH = "search";
    private static final String EXTRA_NO_THREADING = "no_threading";

    private static final String ACTION_SHORTCUT = "shortcut";
    private static final String EXTRA_SPECIAL_FOLDER = "special_folder";

    private static final String EXTRA_MESSAGE_REFERENCE = "message_reference";


    public static final int REQUEST_MASK_PENDING_INTENT = 1 << 16;

    public static void actionDisplaySearch(Context context, SearchSpecification search,
            boolean noThreading, boolean newTask) {
        actionDisplaySearch(context, search, noThreading, newTask, true);
    }

    public static void actionDisplaySearch(Context context, SearchSpecification search,
            boolean noThreading, boolean newTask, boolean clearTop) {
        context.startActivity(intentDisplaySearch(context, search, noThreading, newTask, clearTop));
    }

    public static Intent intentDisplaySearch(Context context, SearchSpecification search,
            boolean noThreading, boolean newTask, boolean clearTop) {
        Intent intent = new Intent(context, MessageList.class);
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
        Intent intent = new Intent(context, MessageList.class);
        intent.setAction(ACTION_SHORTCUT);
        intent.putExtra(EXTRA_SPECIAL_FOLDER, specialFolder);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return intent;
    }

    public static Intent actionDisplayMessageIntent(Context context,
            MessageReference messageReference) {
        Intent intent = new Intent(context, MessageList.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(EXTRA_MESSAGE_REFERENCE, messageReference);
        return intent;
    }

    @Override
    public MessageListFragment.MessageListFragmentListener getMessageListFragmentListner() {
        if (mMailPresenter != null) {
            return mMailPresenter;
        }
        return null;
    }

    @Override
    public MessageViewFragment.MessageViewFragmentListener getMessageViewFragmentListner() {
        if (mMailPresenter != null) {
            return mMailPresenter;
        }
        return null;
    }

    private MailPresenter mMailPresenter;
    private MessageListFragment mMessageListFragment;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (UpgradeDatabases.actionUpgradeDatabases(this, getIntent())) {
            finish();
            return;
        }

        mMailPresenter = null; // new MailPresenter(this, getIntent());

        if (useSplitView()) {
            setContentView(R.layout.split_message_list);
        } else {
            setContentView(R.layout.message_list);
        }

        mMailPresenter.onCreateView();


        ChangeLog cl = new ChangeLog(this);
        if (cl.isFirstRun()) {
            cl.getLogDialog().show();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);

    }

    private boolean useSplitView() {
        SplitViewMode splitViewMode = K9.getSplitViewMode();
        int orientation = getResources().getConfiguration().orientation;

        return (splitViewMode == SplitViewMode.ALWAYS
                || (splitViewMode == SplitViewMode.WHEN_IN_LANDSCAPE
                        && orientation == Configuration.ORIENTATION_LANDSCAPE));
    }

    @Override
    public boolean onSearchRequested() {
        return mMailPresenter.getMessageListFragment().onSearchRequested();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean ret = false;
        if (KeyEvent.ACTION_DOWN == event.getAction()) {
            if (mMailPresenter != null) {
                ret = mMailPresenter.onCustomKeyDown(event.getKeyCode(), event);
            }
        }
        if (!ret) {
            ret = super.dispatchKeyEvent(event);
        }
        return ret;
    }

    /**
     * Activity
     */

    @Override
    public void onBackPressed() {
        if (mMailPresenter != null) {
            if (mMailPresenter.getDisplayMode() == MailPresenter.DisplayMode.MESSAGE_VIEW
                    && mMailPresenter.getMessageListWasDisplayed()) {
                mMailPresenter.showMessageList();
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void startIntentSenderForResult(IntentSender intent, int requestCode,
            Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags)
            throws SendIntentException {
        requestCode |= REQUEST_MASK_PENDING_INTENT;
        super.startIntentSenderForResult(intent, requestCode, fillInIntent, flagsMask, flagsValues,
                extraFlags);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode & REQUEST_MASK_PENDING_INTENT) == REQUEST_MASK_PENDING_INTENT) {
            requestCode ^= REQUEST_MASK_PENDING_INTENT;
            Toast.makeText(this, "onActivityResult not working", Toast.LENGTH_LONG);
            // if (mMessageViewFragment != null) {
            // mMessageViewFragment.onPendingIntentResult(requestCode, resultCode, data);
            // }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMailPresenter.onCreateOptionsMenu(menu, getMenuInflater());
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mMailPresenter.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return mMailPresenter.onPrepareOptionsMenu(menu);
    }
}
