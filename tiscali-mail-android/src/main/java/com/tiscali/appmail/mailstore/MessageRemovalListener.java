package com.tiscali.appmail.mailstore;

import com.tiscali.appmail.mail.Message;

public interface MessageRemovalListener {
    public void messageRemoved(Message message);
}
