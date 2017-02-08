package com.tiscali.appmail.notification;


import com.tiscali.appmail.Account;


public class NotificationGroupKeys {
    private static final String NOTIFICATION_GROUP_KEY_PREFIX = "newMailNotifications-";


    public static String getGroupKey(Account account) {
        return NOTIFICATION_GROUP_KEY_PREFIX + account.getAccountNumber();
    }
}
