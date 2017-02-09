package com.tiscali.appmail.message;


import java.util.List;

import com.tiscali.appmail.crypto.MessageDecryptVerifier;
import com.tiscali.appmail.mail.Message;
import com.tiscali.appmail.mail.Part;


public class ComposePgpInlineDecider {
    public boolean shouldReplyInline(Message localMessage) {
        // TODO more criteria for this? maybe check the User-Agent header?
        return messageHasPgpInlineParts(localMessage);
    }

    private boolean messageHasPgpInlineParts(Message localMessage) {
        List<Part> inlineParts = MessageDecryptVerifier.findPgpInlineParts(localMessage);
        return !inlineParts.isEmpty();
    }
}
