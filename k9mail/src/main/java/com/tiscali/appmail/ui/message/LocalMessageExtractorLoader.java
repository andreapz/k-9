package com.tiscali.appmail.ui.message;


import com.tiscali.appmail.K9;
import com.tiscali.appmail.mail.Message;
import com.tiscali.appmail.mailstore.LocalMessage;
import com.tiscali.appmail.mailstore.MessageViewInfo;
import com.tiscali.appmail.mailstore.MessageViewInfoExtractor;
import com.tiscali.appmail.ui.crypto.MessageCryptoAnnotations;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;


public class LocalMessageExtractorLoader extends AsyncTaskLoader<MessageViewInfo> {
    private static final MessageViewInfoExtractor messageViewInfoExtractor =
            MessageViewInfoExtractor.getInstance();


    private final Message message;
    private MessageViewInfo messageViewInfo;
    @Nullable
    private MessageCryptoAnnotations annotations;

    public LocalMessageExtractorLoader(Context context, Message message,
            @Nullable MessageCryptoAnnotations annotations) {
        super(context);
        this.message = message;
        this.annotations = annotations;
    }

    @Override
    protected void onStartLoading() {
        if (messageViewInfo != null) {
            super.deliverResult(messageViewInfo);
        }

        if (takeContentChanged() || messageViewInfo == null) {
            forceLoad();
        }
    }

    @Override
    public void deliverResult(MessageViewInfo messageViewInfo) {
        this.messageViewInfo = messageViewInfo;
        super.deliverResult(messageViewInfo);
    }

    @Override
    @WorkerThread
    public MessageViewInfo loadInBackground() {
        try {
            return messageViewInfoExtractor.extractMessageForView(message, annotations);
        } catch (Exception e) {
            Log.e(K9.LOG_TAG, "Error while decoding message", e);
            return null;
        }
    }

    public boolean isCreatedFor(LocalMessage localMessage,
            MessageCryptoAnnotations messageCryptoAnnotations) {
        return annotations == messageCryptoAnnotations && message.equals(localMessage);
    }
}
