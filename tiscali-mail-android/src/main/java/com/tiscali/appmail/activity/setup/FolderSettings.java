
package com.tiscali.appmail.activity.setup;

import com.tiscali.appmail.Account;
import com.tiscali.appmail.K9;
import com.tiscali.appmail.Preferences;
import com.tiscali.appmail.R;
import com.tiscali.appmail.activity.FolderInfoHolder;
import com.tiscali.appmail.activity.K9PreferenceActivity;
import com.tiscali.appmail.mail.Folder;
import com.tiscali.appmail.mail.Folder.FolderClass;
import com.tiscali.appmail.mail.MessagingException;
import com.tiscali.appmail.mail.Store;
import com.tiscali.appmail.mailstore.LocalFolder;
import com.tiscali.appmail.mailstore.LocalStore;
import com.tiscali.appmail.service.MailService;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;

public class FolderSettings extends K9PreferenceActivity {

    private static final String EXTRA_FOLDER_NAME = "com.tiscali.appmail.folderName";
    private static final String EXTRA_ACCOUNT = "com.tiscali.appmail.account";

    private static final String PREFERENCE_TOP_CATERGORY = "folder_settings";

    private LocalFolder mFolder;

    public static void actionSettings(Context context, Account account, String folderName) {
        Intent i = new Intent(context, FolderSettings.class);
        i.putExtra(EXTRA_FOLDER_NAME, folderName);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        context.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String folderName = (String) getIntent().getSerializableExtra(EXTRA_FOLDER_NAME);
        String accountUuid = getIntent().getStringExtra(EXTRA_ACCOUNT);
        Account mAccount = Preferences.getPreferences(this).getAccount(accountUuid);

        try {
            LocalStore localStore = mAccount.getLocalStore();
            mFolder = localStore.getFolder(folderName);
            mFolder.open(Folder.OPEN_MODE_RW);
        } catch (MessagingException me) {
            Log.e(K9.LOG_TAG, "Unable to edit folder " + folderName + " preferences", me);
            return;
        }

        boolean isPushCapable = false;
        try {
            Store store = mAccount.getRemoteStore();
            isPushCapable = store.isPushCapable();
        } catch (Exception e) {
            Log.e(K9.LOG_TAG, "Could not get remote store", e);
        }

        addPreferencesFromResource(R.xml.folder_settings_preferences);

        String displayName = FolderInfoHolder.getDisplayName(this, mAccount, mFolder.getName());
        Preference category = findPreference(PREFERENCE_TOP_CATERGORY);
        category.setTitle(displayName);
    }

    private void saveSettings() throws MessagingException {

        FolderClass oldPushClass = mFolder.getPushClass();
        FolderClass oldDisplayClass = mFolder.getDisplayClass();

        mFolder.save();

        FolderClass newPushClass = mFolder.getPushClass();
        FolderClass newDisplayClass = mFolder.getDisplayClass();

        if (oldPushClass != newPushClass
                || (newPushClass != FolderClass.NO_CLASS && oldDisplayClass != newDisplayClass)) {
            MailService.actionRestartPushers(getApplication(), null);
        }
    }

    @Override
    public void onPause() {
        try {
            saveSettings();
        } catch (MessagingException e) {
            Log.e(K9.LOG_TAG, "Saving folder settings failed", e);
        }

        super.onPause();
    }
}
