
package com.fsck.k9.activity.setup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.Log;
import com.fsck.k9.*;
import com.fsck.k9.activity.FolderInfoHolder;
import com.fsck.k9.activity.K9PreferenceActivity;
import com.fsck.k9.mail.Folder;
import com.fsck.k9.mail.Folder.FolderClass;

import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.Store;
import com.fsck.k9.mailstore.LocalFolder;
import com.fsck.k9.mailstore.LocalStore;
import com.fsck.k9.service.MailService;

public class FolderSettings extends K9PreferenceActivity {

    private static final String EXTRA_FOLDER_NAME = "com.fsck.k9.folderName";
    private static final String EXTRA_ACCOUNT = "com.fsck.k9.account";

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

        String folderName = (String)getIntent().getSerializableExtra(EXTRA_FOLDER_NAME);
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
