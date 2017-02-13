package com.tiscali.appmail.activity.setup.migrations;

import com.tiscali.appmail.Account;
import com.tiscali.appmail.K9;
import com.tiscali.appmail.Preferences;
import com.tiscali.appmail.preferences.Storage;
import com.tiscali.appmail.preferences.StorageEditor;

/**
 * Created by Annalisa Sini on 13/02/2017.
 */

public class SettingsMigrations {

    private static final String MAIL_DEFAULT_PREVIEW_LINES = "MAIL_DEFAULT_PREVIEW_LINES";

    public static void upgradeSettings(Preferences prefs, Account account) {

        Storage storage = prefs.getStorage();
        StorageEditor editor = storage.edit();

        upgradeGeneralSettings(storage, editor);

        upgradeAccountSettings(storage, editor);
    }

    private static void upgradeGeneralSettings(Storage storage, StorageEditor editor) {
        upgradeDefaultPreviewLines(storage, editor);

        K9.save(editor);
        editor.commit();
    }

    private static void upgradeDefaultPreviewLines(Storage storage, StorageEditor editor) {
        if (storage.getString(MAIL_DEFAULT_PREVIEW_LINES, null) == null) {
            // force mail default preview lines
            K9.setMessageListPreviewLines(K9.DEFAULT_MAIL_PREVIEW_LINES);
            // do it once
            editor.putString(MAIL_DEFAULT_PREVIEW_LINES, MAIL_DEFAULT_PREVIEW_LINES);
        }
    }

    private static void upgradeAccountSettings(Storage storage, StorageEditor editor) {

    }
}
