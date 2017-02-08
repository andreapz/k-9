package com.tiscali.appmail.mail.store.imap;


import java.io.IOException;
import java.util.List;

import com.tiscali.appmail.mail.MessagingException;


interface ImapSearcher {
    List<ImapResponse> search() throws IOException, MessagingException;
}
