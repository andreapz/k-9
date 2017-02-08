package com.tiscali.appmail.mailstore.migrations;


import java.util.List;

import com.tiscali.appmail.Account;
import com.tiscali.appmail.mail.Flag;
import com.tiscali.appmail.mailstore.LocalStore;
import com.tiscali.appmail.preferences.Storage;

import android.content.Context;


/**
 * Helper to allow accessing classes and methods that aren't visible or accessible to the
 * 'migrations' package
 */
public interface MigrationsHelper {
    LocalStore getLocalStore();

    Storage getStorage();

    Account getAccount();

    Context getContext();

    String serializeFlags(List<Flag> flags);
}
