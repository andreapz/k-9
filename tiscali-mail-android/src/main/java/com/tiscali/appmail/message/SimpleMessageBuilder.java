package com.tiscali.appmail.message;


import com.tiscali.appmail.Globals;
import com.tiscali.appmail.mail.BoundaryGenerator;
import com.tiscali.appmail.mail.MessagingException;
import com.tiscali.appmail.mail.internet.MessageIdGenerator;
import com.tiscali.appmail.mail.internet.MimeMessage;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.VisibleForTesting;


public class SimpleMessageBuilder extends MessageBuilder {

    public static SimpleMessageBuilder newInstance() {
        Context context = Globals.getContext();
        MessageIdGenerator messageIdGenerator = MessageIdGenerator.getInstance();
        BoundaryGenerator boundaryGenerator = BoundaryGenerator.getInstance();
        return new SimpleMessageBuilder(context, messageIdGenerator, boundaryGenerator);
    }

    @VisibleForTesting
    SimpleMessageBuilder(Context context, MessageIdGenerator messageIdGenerator,
            BoundaryGenerator boundaryGenerator) {
        super(context, messageIdGenerator, boundaryGenerator);
    }

    @Override
    protected void buildMessageInternal() {
        try {
            MimeMessage message = build();
            queueMessageBuildSuccess(message);
        } catch (MessagingException me) {
            queueMessageBuildException(me);
        }
    }

    @Override
    protected void buildMessageOnActivityResult(int requestCode, Intent data) {
        throw new UnsupportedOperationException();
    }
}
