package com.tiscali.appmail.mailstore.migrations;


import java.util.List;

import com.tiscali.appmail.K9;
import com.tiscali.appmail.mail.Folder;
import com.tiscali.appmail.mailstore.LocalFolder;
import com.tiscali.appmail.mailstore.LocalStore;
import com.tiscali.appmail.preferences.Storage;
import com.tiscali.appmail.preferences.StorageEditor;

import android.util.Log;


class MigrationTo42 {
    public static void from41MoveFolderPreferences(MigrationsHelper migrationsHelper) {
        try {
            LocalStore localStore = migrationsHelper.getLocalStore();
            Storage storage = migrationsHelper.getStorage();

            long startTime = System.currentTimeMillis();
            StorageEditor editor = storage.edit();

            List<? extends Folder> folders = localStore.getPersonalNamespaces(true);
            for (Folder folder : folders) {
                if (folder instanceof LocalFolder) {
                    LocalFolder lFolder = (LocalFolder) folder;
                    lFolder.save(editor);
                }
            }

            editor.commit();
            long endTime = System.currentTimeMillis();
            Log.i(K9.LOG_TAG, "Putting folder preferences for " + folders.size()
                    + " folders back into Preferences took " + (endTime - startTime) + " ms");
        } catch (Exception e) {
            Log.e(K9.LOG_TAG, "Could not replace Preferences in upgrade from DB_VERSION 41", e);
        }
    }
}
