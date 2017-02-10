package com.tiscali.appmail.mailstore.migrations;

import com.tiscali.appmail.Account;
import com.tiscali.appmail.Preferences;
import com.tiscali.appmail.mail.ServerSettings;
import com.tiscali.appmail.mail.store.RemoteStore;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by andreaputzu on 10/02/17.
 */

public class MigrationToTiscali56 {
    public static void storePasswordAccount(SQLiteDatabase db, MigrationsHelper migrationsHelper) {
        Account account = migrationsHelper.getAccount();
        ServerSettings settings = RemoteStore.decodeStoreUri(account.getStoreUri());
        account.setPassword(settings.password);
        account.save(Preferences.getPreferences(migrationsHelper.getContext()));
    }
}
