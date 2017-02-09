package com.tiscali.appmail.mail.store.imap;

import com.tiscali.appmail.mail.MessagingException;

class NegativeImapResponseException extends MessagingException {
    private static final long serialVersionUID = 3725007182205882394L;


    private final String alertText;


    public NegativeImapResponseException(String message, String alertText) {
        super(message, true);
        this.alertText = alertText;
    }

    public String getAlertText() {
        return alertText;
    }
}
