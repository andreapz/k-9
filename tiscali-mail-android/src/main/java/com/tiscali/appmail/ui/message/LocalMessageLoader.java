package com.tiscali.appmail.ui.message;


import com.tiscali.appmail.Account;
import com.tiscali.appmail.K9;
import com.tiscali.appmail.activity.MessageReference;
import com.tiscali.appmail.controller.MessagingController;
import com.tiscali.appmail.mail.MessagingException;
import com.tiscali.appmail.mailstore.LocalMessage;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;


public class LocalMessageLoader extends AsyncTaskLoader<LocalMessage> {
    private final MessagingController controller;
    private final Account account;
    private final MessageReference messageReference;
    private LocalMessage message;

    public LocalMessageLoader(Context context, MessagingController controller, Account account,
            MessageReference messageReference) {
        super(context);
        this.controller = controller;
        this.account = account;
        this.messageReference = messageReference;
    }

    @Override
    protected void onStartLoading() {
        if (message != null) {
            super.deliverResult(message);
        }

        if (takeContentChanged() || message == null) {
            forceLoad();
        }
    }

    @Override
    public void deliverResult(LocalMessage message) {
        this.message = message;
        super.deliverResult(message);
    }

    @Override
    public LocalMessage loadInBackground() {
        try {
            return loadMessageFromDatabase();
        } catch (Exception e) {
            Log.e(K9.LOG_TAG, "Error while loading message from database", e);
            return null;
        }
    }

    private LocalMessage loadMessageFromDatabase() throws MessagingException {
        return controller.loadMessage(account, messageReference.getFolderName(),
                messageReference.getUid());
    }

    public boolean isCreatedFor(MessageReference messageReference) {
        return this.messageReference.equals(messageReference);
    }
}
