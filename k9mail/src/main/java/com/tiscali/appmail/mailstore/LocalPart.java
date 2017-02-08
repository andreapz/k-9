package com.tiscali.appmail.mailstore;


public interface LocalPart {
    String getAccountUuid();

    long getId();

    long getSize();

    LocalMessage getMessage();
}
