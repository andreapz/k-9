package com.tiscali.appmail.mail.store.imap;

interface UntaggedHandler {
    void handleAsyncUntaggedResponse(ImapResponse response);
}
