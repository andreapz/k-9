package com.tiscali.appmail.activity.setup;

import java.io.File;

import com.tiscali.appmail.K9;
import com.tiscali.appmail.K9.NotificationHideSubject;
import com.tiscali.appmail.K9.NotificationQuickDelete;
import com.tiscali.appmail.Preferences;
import com.tiscali.appmail.R;
import com.tiscali.appmail.activity.ColorPickerDialog;
import com.tiscali.appmail.activity.K9PreferenceActivity;
import com.tiscali.appmail.helper.FileBrowserHelper;
import com.tiscali.appmail.helper.FileBrowserHelper.FileBrowserFailOverCallback;
import com.tiscali.appmail.notification.NotificationController;
import com.tiscali.appmail.preferences.CheckBoxListPreference;
import com.tiscali.appmail.preferences.Storage;
import com.tiscali.appmail.preferences.StorageEditor;
import com.tiscali.appmail.preferences.TimePickerPreference;
import com.tiscali.appmail.provider.TiscaliSearchRecentSuggestionsProvider;
import com.tiscali.appmail.service.MailService;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.widget.Toast;


public class Prefs extends K9PreferenceActivity {

    /**
     * Immutable empty {@link CharSequence} array
     */
    private static final CharSequence[] EMPTY_CHAR_SEQUENCE_ARRAY = new CharSequence[0];

    /*
     * Keys of the preferences defined in res/xml/global_preferences.xml
     */
    private static final String PREFERENCE_LANGUAGE = "language";
    private static final String PREFERENCE_FONT_SIZE = "font_size";
    private static final String PREFERENCE_ANIMATIONS = "animations";
    private static final String PREFERENCE_GESTURES = "gestures";
    private static final String PREFERENCE_VOLUME_NAVIGATION = "volumeNavigation";
    // private static final String PREFERENCE_START_INTEGRATED_INBOX = "start_integrated_inbox";
    private static final String PREFERENCE_CONFIRM_ACTIONS = "confirm_actions";
    private static final String PREFERENCE_NOTIFICATION_HIDE_SUBJECT = "notification_hide_subject";
    private static final String PREFERENCE_MEASURE_ACCOUNTS = "measure_accounts";
    private static final String PREFERENCE_COUNT_SEARCH = "count_search";
    // private static final String PREFERENCE_MESSAGELIST_CHECKBOXES = "messagelist_checkboxes";
    private static final String PREFERENCE_MESSAGELIST_PREVIEW_LINES = "messagelist_preview_lines";
    private static final String PREFERENCE_MESSAGELIST_SENDER_ABOVE_SUBJECT =
            "messagelist_sender_above_subject";
    private static final String PREFERENCE_MESSAGELIST_STARS = "messagelist_stars";
    private static final String PREFERENCE_MESSAGELIST_SHOW_CORRESPONDENT_NAMES =
            "messagelist_show_correspondent_names";
    private static final String PREFERENCE_MESSAGELIST_SHOW_CONTACT_NAME =
            "messagelist_show_contact_name";
    // private static final String PREFERENCE_MESSAGELIST_CONTACT_NAME_COLOR =
    // "messagelist_contact_name_color";
    // private static final String PREFERENCE_MESSAGELIST_SHOW_CONTACT_PICTURE =
    // "messagelist_show_contact_picture";
    // private static final String PREFERENCE_MESSAGELIST_COLORIZE_MISSING_CONTACT_PICTURES =
    // "messagelist_colorize_missing_contact_pictures";
    private static final String PREFERENCE_MESSAGEVIEW_FIXEDWIDTH = "messageview_fixedwidth_font";
    private static final String PREFERENCE_MESSAGEVIEW_VISIBLE_REFILE_ACTIONS =
            "messageview_visible_refile_actions";

    private static final String PREFERENCE_MESSAGEVIEW_RETURN_TO_LIST =
            "messageview_return_to_list";
    private static final String PREFERENCE_MESSAGEVIEW_SHOW_NEXT = "messageview_show_next";
    private static final String PREFERENCE_QUIET_TIME_ENABLED = "quiet_time_enabled";
    private static final String PREFERENCE_DISABLE_NOTIFICATION_DURING_QUIET_TIME =
            "disable_notifications_during_quiet_time";
    private static final String PREFERENCE_QUIET_TIME_STARTS = "quiet_time_starts";
    private static final String PREFERENCE_QUIET_TIME_ENDS = "quiet_time_ends";
    private static final String PREFERENCE_NOTIF_QUICK_DELETE = "notification_quick_delete";
    private static final String PREFERENCE_LOCK_SCREEN_NOTIFICATION_VISIBILITY =
            "lock_screen_notification_visibility";

    private static final String PREFERENCE_AUTOFIT_WIDTH = "messageview_autofit_width";
    private static final String PREFERENCE_BACKGROUND_OPS = "background_ops";
    private static final String PREFERENCE_DEBUG_LOGGING = "debug_logging";
    private static final String PREFERENCE_SENSITIVE_LOGGING = "sensitive_logging";

    private static final String PREFERENCE_ATTACHMENT_DEF_PATH = "attachment_default_path";
    private static final String PREFERENCE_BACKGROUND_AS_UNREAD_INDICATOR =
            "messagelist_background_as_unread_indicator";
    private static final String PREFERENCE_THREADED_VIEW = "threaded_view";
    private static final String PREFERENCE_FOLDERLIST_WRAP_NAME = "folderlist_wrap_folder_name";
    // private static final String PREFERENCE_SPLITVIEW_MODE = "splitview_mode";
    private static final String PREFERENCE_CANCEL_RECENT_SEARCHES = "clear_recent_searches";

    private static final int ACTIVITY_CHOOSE_FOLDER = 1;

    // Named indices for the mVisibleRefileActions field
    private static final int VISIBLE_REFILE_ACTIONS_DELETE = 0;
    private static final int VISIBLE_REFILE_ACTIONS_ARCHIVE = 1;
    private static final int VISIBLE_REFILE_ACTIONS_MOVE = 2;
    private static final int VISIBLE_REFILE_ACTIONS_COPY = 3;
    private static final int VISIBLE_REFILE_ACTIONS_SPAM = 4;

    // private ListPreference mLanguage;
    private CheckBoxPreference mAnimations;
    private CheckBoxPreference mGestures;
    private CheckBoxListPreference mVolumeNavigation;
    // private CheckBoxPreference mStartIntegratedInbox;
    private CheckBoxListPreference mConfirmActions;
    private ListPreference mNotificationHideSubject;
    private CheckBoxPreference mMeasureAccounts;
    private CheckBoxPreference mCountSearch;
    private ListPreference mPreviewLines;
    private CheckBoxPreference mSenderAboveSubject;
    // private CheckBoxPreference mCheckboxes;
    // private CheckBoxPreference mStars;
    private CheckBoxPreference mShowCorrespondentNames;
    private CheckBoxPreference mShowContactName;
    // private CheckBoxPreference mChangeContactNameColor;
    // private CheckBoxPreference mShowContactPicture;
    // private CheckBoxPreference mColorizeMissingContactPictures;
    private CheckBoxPreference mFixedWidth;
    private CheckBoxPreference mReturnToList;
    private CheckBoxPreference mShowNext;
    private CheckBoxPreference mAutofitWidth;
    private ListPreference mBackgroundOps;
    private CheckBoxPreference mDebugLogging;
    private CheckBoxPreference mSensitiveLogging;
    private CheckBoxPreference mWrapFolderNames;
    private CheckBoxListPreference mVisibleRefileActions;

    private CheckBoxPreference mQuietTimeEnabled;
    private CheckBoxPreference mDisableNotificationDuringQuietTime;
    private com.tiscali.appmail.preferences.TimePickerPreference mQuietTimeStarts;
    private com.tiscali.appmail.preferences.TimePickerPreference mQuietTimeEnds;
    private ListPreference mNotificationQuickDelete;
    private ListPreference mLockScreenNotificationVisibility;
    private Preference mAttachmentPathPreference;

    // private CheckBoxPreference mBackgroundAsUnreadIndicator;
    private CheckBoxPreference mThreadedView;
    // private ListPreference mSplitViewMode;

    private Preference mCancelRecentSearches;


    public static void actionPrefs(Context context) {
        Intent i = new Intent(context, Prefs.class);
        context.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.global_preferences);

        // mLanguage = (ListPreference) findPreference(PREFERENCE_LANGUAGE);
        // List<CharSequence> entryVector = new
        // ArrayList<CharSequence>(Arrays.asList(mLanguage.getEntries()));
        // List<CharSequence> entryValueVector = new
        // ArrayList<CharSequence>(Arrays.asList(mLanguage.getEntryValues()));
        // String supportedLanguages[] = getResources().getStringArray(R.array.supported_languages);
        // Set<String> supportedLanguageSet = new
        // HashSet<String>(Arrays.asList(supportedLanguages));
        // for (int i = entryVector.size() - 1; i > -1; --i) {
        // if (!supportedLanguageSet.contains(entryValueVector.get(i))) {
        // entryVector.remove(i);
        // entryValueVector.remove(i);
        // }
        // }
        // initListPreference(mLanguage, K9.getK9Language(),
        // entryVector.toArray(EMPTY_CHAR_SEQUENCE_ARRAY),
        // entryValueVector.toArray(EMPTY_CHAR_SEQUENCE_ARRAY));

        findPreference(PREFERENCE_FONT_SIZE)
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        onFontSizeSettings();
                        return true;
                    }
                });

        mAnimations = (CheckBoxPreference) findPreference(PREFERENCE_ANIMATIONS);
        mAnimations.setChecked(K9.showAnimations());

        mGestures = (CheckBoxPreference) findPreference(PREFERENCE_GESTURES);
        mGestures.setChecked(K9.gesturesEnabled());

        mVolumeNavigation = (CheckBoxListPreference) findPreference(PREFERENCE_VOLUME_NAVIGATION);
        mVolumeNavigation
                .setItems(new CharSequence[] {getString(R.string.volume_navigation_message),
                        getString(R.string.volume_navigation_list)});
        mVolumeNavigation.setCheckedItems(new boolean[] {K9.useVolumeKeysForNavigationEnabled(),
                K9.useVolumeKeysForListNavigationEnabled()});

        // mStartIntegratedInbox =
        // (CheckBoxPreference)findPreference(PREFERENCE_START_INTEGRATED_INBOX);
        // mStartIntegratedInbox.setChecked(K9.startIntegratedInbox());

        mConfirmActions = (CheckBoxListPreference) findPreference(PREFERENCE_CONFIRM_ACTIONS);

        boolean canDeleteFromNotification =
                NotificationController.platformSupportsExtendedNotifications();
        CharSequence[] confirmActionEntries = new CharSequence[canDeleteFromNotification ? 5 : 4];
        boolean[] confirmActionValues = new boolean[canDeleteFromNotification ? 5 : 4];
        int index = 0;

        confirmActionEntries[index] = getString(R.string.global_settings_confirm_action_delete);
        confirmActionValues[index++] = K9.confirmDelete();
        confirmActionEntries[index] =
                getString(R.string.global_settings_confirm_action_delete_starred);
        confirmActionValues[index++] = K9.confirmDeleteStarred();
        if (canDeleteFromNotification) {
            confirmActionEntries[index] =
                    getString(R.string.global_settings_confirm_action_delete_notif);
            confirmActionValues[index++] = K9.confirmDeleteFromNotification();
        }
        confirmActionEntries[index] = getString(R.string.global_settings_confirm_action_spam);
        confirmActionValues[index++] = K9.confirmSpam();
        confirmActionEntries[index] = getString(R.string.global_settings_confirm_menu_discard);
        confirmActionValues[index++] = K9.confirmDiscardMessage();

        mConfirmActions.setItems(confirmActionEntries);
        mConfirmActions.setCheckedItems(confirmActionValues);

        mNotificationHideSubject = setupListPreference(PREFERENCE_NOTIFICATION_HIDE_SUBJECT,
                K9.getNotificationHideSubject().toString());

        mMeasureAccounts = (CheckBoxPreference) findPreference(PREFERENCE_MEASURE_ACCOUNTS);
        mMeasureAccounts.setChecked(K9.measureAccounts());

        mCountSearch = (CheckBoxPreference) findPreference(PREFERENCE_COUNT_SEARCH);
        mCountSearch.setChecked(K9.countSearchMessages());

        mPreviewLines = setupListPreference(PREFERENCE_MESSAGELIST_PREVIEW_LINES,
                Integer.toString(K9.messageListPreviewLines()));

        mSenderAboveSubject =
                (CheckBoxPreference) findPreference(PREFERENCE_MESSAGELIST_SENDER_ABOVE_SUBJECT);
        mSenderAboveSubject.setChecked(K9.messageListSenderAboveSubject());
        // mCheckboxes = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGELIST_CHECKBOXES);
        // mCheckboxes.setChecked(K9.messageListCheckboxes());

        // mStars = (CheckBoxPreference)findPreference(PREFERENCE_MESSAGELIST_STARS);
        // mStars.setChecked(K9.messageListStars());

        mShowCorrespondentNames = (CheckBoxPreference) findPreference(
                PREFERENCE_MESSAGELIST_SHOW_CORRESPONDENT_NAMES);
        mShowCorrespondentNames.setChecked(K9.showCorrespondentNames());

        mShowContactName =
                (CheckBoxPreference) findPreference(PREFERENCE_MESSAGELIST_SHOW_CONTACT_NAME);
        mShowContactName.setChecked(K9.showContactName());

        // mShowContactPicture =
        // (CheckBoxPreference)findPreference(PREFERENCE_MESSAGELIST_SHOW_CONTACT_PICTURE);
        // mShowContactPicture.setChecked(K9.showContactPicture());

        // mColorizeMissingContactPictures = (CheckBoxPreference)findPreference(
        // PREFERENCE_MESSAGELIST_COLORIZE_MISSING_CONTACT_PICTURES);
        // mColorizeMissingContactPictures.setChecked(K9.isColorizeMissingContactPictures());

        // mBackgroundAsUnreadIndicator =
        // (CheckBoxPreference) findPreference(PREFERENCE_BACKGROUND_AS_UNREAD_INDICATOR);
        // mBackgroundAsUnreadIndicator.setChecked(K9.useBackgroundAsUnreadIndicator());

        // mChangeContactNameColor =
        // (CheckBoxPreference)findPreference(PREFERENCE_MESSAGELIST_CONTACT_NAME_COLOR);
        // mChangeContactNameColor.setChecked(K9.changeContactNameColor());

        mThreadedView = (CheckBoxPreference) findPreference(PREFERENCE_THREADED_VIEW);
        mThreadedView.setChecked(K9.isThreadedViewEnabled());

        // if (K9.changeContactNameColor()) {
        // mChangeContactNameColor.setSummary(R.string.global_settings_registered_name_color_changed);
        // } else {
        // mChangeContactNameColor.setSummary(R.string.global_settings_registered_name_color_default);
        // }
        // mChangeContactNameColor.setOnPreferenceChangeListener(new
        // Preference.OnPreferenceChangeListener() {
        // public boolean onPreferenceChange(Preference preference, Object newValue) {
        // final Boolean checked = (Boolean) newValue;
        // if (checked) {
        // onChooseContactNameColor();
        // mChangeContactNameColor.setSummary(R.string.global_settings_registered_name_color_changed);
        // } else {
        // mChangeContactNameColor.setSummary(R.string.global_settings_registered_name_color_default);
        // }
        // mChangeContactNameColor.setChecked(checked);
        // return false;
        // }
        // });

        mFixedWidth = (CheckBoxPreference) findPreference(PREFERENCE_MESSAGEVIEW_FIXEDWIDTH);
        mFixedWidth.setChecked(K9.messageViewFixedWidthFont());

        mReturnToList = (CheckBoxPreference) findPreference(PREFERENCE_MESSAGEVIEW_RETURN_TO_LIST);
        mReturnToList.setChecked(K9.messageViewReturnToList());

        mShowNext = (CheckBoxPreference) findPreference(PREFERENCE_MESSAGEVIEW_SHOW_NEXT);
        mShowNext.setChecked(K9.messageViewShowNext());

        mAutofitWidth = (CheckBoxPreference) findPreference(PREFERENCE_AUTOFIT_WIDTH);
        mAutofitWidth.setChecked(K9.autofitWidth());

        mQuietTimeEnabled = (CheckBoxPreference) findPreference(PREFERENCE_QUIET_TIME_ENABLED);
        mQuietTimeEnabled.setChecked(K9.getQuietTimeEnabled());

        mDisableNotificationDuringQuietTime = (CheckBoxPreference) findPreference(
                PREFERENCE_DISABLE_NOTIFICATION_DURING_QUIET_TIME);
        mDisableNotificationDuringQuietTime.setChecked(!K9.isNotificationDuringQuietTimeEnabled());
        mQuietTimeStarts = (TimePickerPreference) findPreference(PREFERENCE_QUIET_TIME_STARTS);
        mQuietTimeStarts.setDefaultValue(K9.getQuietTimeStarts());
        mQuietTimeStarts.setSummary(K9.getQuietTimeStarts());
        mQuietTimeStarts.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final String time = (String) newValue;
                mQuietTimeStarts.setSummary(time);
                return false;
            }
        });

        mQuietTimeEnds = (TimePickerPreference) findPreference(PREFERENCE_QUIET_TIME_ENDS);
        mQuietTimeEnds.setSummary(K9.getQuietTimeEnds());
        mQuietTimeEnds.setDefaultValue(K9.getQuietTimeEnds());
        mQuietTimeEnds.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final String time = (String) newValue;
                mQuietTimeEnds.setSummary(time);
                return false;
            }
        });

        mNotificationQuickDelete = setupListPreference(PREFERENCE_NOTIF_QUICK_DELETE,
                K9.getNotificationQuickDeleteBehaviour().toString());
        if (!NotificationController.platformSupportsExtendedNotifications()) {
            PreferenceScreen prefs = (PreferenceScreen) findPreference("notification_preferences");
            prefs.removePreference(mNotificationQuickDelete);
            mNotificationQuickDelete = null;
        }

        mLockScreenNotificationVisibility =
                setupListPreference(PREFERENCE_LOCK_SCREEN_NOTIFICATION_VISIBILITY,
                        K9.getLockScreenNotificationVisibility().toString());
        if (!NotificationController.platformSupportsLockScreenNotifications()) {
            ((PreferenceScreen) findPreference("notification_preferences"))
                    .removePreference(mLockScreenNotificationVisibility);
            mLockScreenNotificationVisibility = null;
        }

        mBackgroundOps =
                setupListPreference(PREFERENCE_BACKGROUND_OPS, K9.getBackgroundOps().name());

        mDebugLogging = (CheckBoxPreference) findPreference(PREFERENCE_DEBUG_LOGGING);
        mSensitiveLogging = (CheckBoxPreference) findPreference(PREFERENCE_SENSITIVE_LOGGING);

        mDebugLogging.setChecked(K9.DEBUG);
        mSensitiveLogging.setChecked(K9.DEBUG_SENSITIVE);

        mAttachmentPathPreference = findPreference(PREFERENCE_ATTACHMENT_DEF_PATH);
        mAttachmentPathPreference.setSummary(K9.getAttachmentDefaultPath());
        mAttachmentPathPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                FileBrowserHelper.getInstance().showFileBrowserActivity(Prefs.this,
                        new File(K9.getAttachmentDefaultPath()), ACTIVITY_CHOOSE_FOLDER, callback);

                return true;
            }

            FileBrowserFailOverCallback callback = new FileBrowserFailOverCallback() {

                @Override
                public void onPathEntered(String path) {
                    mAttachmentPathPreference.setSummary(path);
                    K9.setAttachmentDefaultPath(path);
                }

                @Override
                public void onCancel() {
                    // canceled, do nothing
                }
            };
        });

        mWrapFolderNames = (CheckBoxPreference) findPreference(PREFERENCE_FOLDERLIST_WRAP_NAME);
        mWrapFolderNames.setChecked(K9.wrapFolderNames());

        mVisibleRefileActions = (CheckBoxListPreference) findPreference(
                PREFERENCE_MESSAGEVIEW_VISIBLE_REFILE_ACTIONS);
        CharSequence[] visibleRefileActionsEntries = new CharSequence[5];
        visibleRefileActionsEntries[VISIBLE_REFILE_ACTIONS_DELETE] =
                getString(R.string.delete_action);
        visibleRefileActionsEntries[VISIBLE_REFILE_ACTIONS_ARCHIVE] =
                getString(R.string.archive_action);
        visibleRefileActionsEntries[VISIBLE_REFILE_ACTIONS_MOVE] = getString(R.string.move_action);
        visibleRefileActionsEntries[VISIBLE_REFILE_ACTIONS_COPY] = getString(R.string.copy_action);
        visibleRefileActionsEntries[VISIBLE_REFILE_ACTIONS_SPAM] = getString(R.string.spam_action);

        boolean[] visibleRefileActionsValues = new boolean[5];
        visibleRefileActionsValues[VISIBLE_REFILE_ACTIONS_DELETE] =
                K9.isMessageViewDeleteActionVisible();
        visibleRefileActionsValues[VISIBLE_REFILE_ACTIONS_ARCHIVE] =
                K9.isMessageViewArchiveActionVisible();
        visibleRefileActionsValues[VISIBLE_REFILE_ACTIONS_MOVE] =
                K9.isMessageViewMoveActionVisible();
        visibleRefileActionsValues[VISIBLE_REFILE_ACTIONS_COPY] =
                K9.isMessageViewCopyActionVisible();
        visibleRefileActionsValues[VISIBLE_REFILE_ACTIONS_SPAM] =
                K9.isMessageViewSpamActionVisible();

        mVisibleRefileActions.setItems(visibleRefileActionsEntries);
        mVisibleRefileActions.setCheckedItems(visibleRefileActionsValues);

        // mSplitViewMode = (ListPreference) findPreference(PREFERENCE_SPLITVIEW_MODE);
        // initListPreference(mSplitViewMode, K9.getSplitViewMode().name(),
        // mSplitViewMode.getEntries(), mSplitViewMode.getEntryValues());

        mCancelRecentSearches = findPreference(PREFERENCE_CANCEL_RECENT_SEARCHES);
        mCancelRecentSearches.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // FIXME use getActivity() after PreferenceActivity dismissing
                TiscaliSearchRecentSuggestionsProvider.showCancelSearchHistoryDialog(Prefs.this);
                return true;
            }
        });
    }

    private void saveSettings() {
        Storage storage = Preferences.getPreferences(this).getStorage();

        // K9.setK9Language(mLanguage.getValue());

        K9.setAnimations(mAnimations.isChecked());
        K9.setGesturesEnabled(mGestures.isChecked());
        K9.setUseVolumeKeysForNavigation(mVolumeNavigation.getCheckedItems()[0]);
        K9.setUseVolumeKeysForListNavigation(mVolumeNavigation.getCheckedItems()[1]);
        // K9.setStartIntegratedInbox(!mHideSpecialAccounts.isChecked() &&
        // mStartIntegratedInbox.isChecked());
        K9.setNotificationHideSubject(
                NotificationHideSubject.valueOf(mNotificationHideSubject.getValue()));

        int index = 0;
        K9.setConfirmDelete(mConfirmActions.getCheckedItems()[index++]);
        K9.setConfirmDeleteStarred(mConfirmActions.getCheckedItems()[index++]);
        if (NotificationController.platformSupportsExtendedNotifications()) {
            K9.setConfirmDeleteFromNotification(mConfirmActions.getCheckedItems()[index++]);
        }
        K9.setConfirmSpam(mConfirmActions.getCheckedItems()[index++]);
        K9.setConfirmDiscardMessage(mConfirmActions.getCheckedItems()[index++]);

        K9.setMeasureAccounts(mMeasureAccounts.isChecked());
        K9.setCountSearchMessages(mCountSearch.isChecked());
        K9.setMessageListPreviewLines(Integer.parseInt(mPreviewLines.getValue()));
        // K9.setMessageListCheckboxes(mCheckboxes.isChecked());
        // K9.setMessageListStars(mStars.isChecked());
        K9.setShowCorrespondentNames(mShowCorrespondentNames.isChecked());
        K9.setMessageListSenderAboveSubject(mSenderAboveSubject.isChecked());
        K9.setShowContactName(mShowContactName.isChecked());
        // K9.setShowContactPicture(mShowContactPicture.isChecked());
        // K9.setColorizeMissingContactPictures(mColorizeMissingContactPictures.isChecked());
        // K9.setUseBackgroundAsUnreadIndicator(mBackgroundAsUnreadIndicator.isChecked());
        K9.setThreadedViewEnabled(mThreadedView.isChecked());
        // K9.setChangeContactNameColor(mChangeContactNameColor.isChecked());
        K9.setMessageViewFixedWidthFont(mFixedWidth.isChecked());
        K9.setMessageViewReturnToList(mReturnToList.isChecked());
        K9.setMessageViewShowNext(mShowNext.isChecked());
        K9.setAutofitWidth(mAutofitWidth.isChecked());
        K9.setQuietTimeEnabled(mQuietTimeEnabled.isChecked());

        boolean[] enabledRefileActions = mVisibleRefileActions.getCheckedItems();
        K9.setMessageViewDeleteActionVisible(enabledRefileActions[VISIBLE_REFILE_ACTIONS_DELETE]);
        K9.setMessageViewArchiveActionVisible(enabledRefileActions[VISIBLE_REFILE_ACTIONS_ARCHIVE]);
        K9.setMessageViewMoveActionVisible(enabledRefileActions[VISIBLE_REFILE_ACTIONS_MOVE]);
        K9.setMessageViewCopyActionVisible(enabledRefileActions[VISIBLE_REFILE_ACTIONS_COPY]);
        K9.setMessageViewSpamActionVisible(enabledRefileActions[VISIBLE_REFILE_ACTIONS_SPAM]);

        K9.setNotificationDuringQuietTimeEnabled(!mDisableNotificationDuringQuietTime.isChecked());
        K9.setQuietTimeStarts(mQuietTimeStarts.getTime());
        K9.setQuietTimeEnds(mQuietTimeEnds.getTime());
        K9.setWrapFolderNames(mWrapFolderNames.isChecked());

        if (mNotificationQuickDelete != null) {
            K9.setNotificationQuickDeleteBehaviour(
                    NotificationQuickDelete.valueOf(mNotificationQuickDelete.getValue()));
        }

        if (mLockScreenNotificationVisibility != null) {
            K9.setLockScreenNotificationVisibility(K9.LockScreenNotificationVisibility
                    .valueOf(mLockScreenNotificationVisibility.getValue()));
        }

        // K9.setSplitViewMode(SplitViewMode.valueOf(mSplitViewMode.getValue()));
        K9.setAttachmentDefaultPath(mAttachmentPathPreference.getSummary().toString());
        boolean needsRefresh = K9.setBackgroundOps(mBackgroundOps.getValue());

        if (!K9.DEBUG && mDebugLogging.isChecked()) {
            Toast.makeText(this, R.string.debug_logging_enabled, Toast.LENGTH_LONG).show();
        }
        K9.DEBUG = mDebugLogging.isChecked();
        K9.DEBUG_SENSITIVE = mSensitiveLogging.isChecked();

        StorageEditor editor = storage.edit();
        K9.save(editor);
        editor.commit();

        if (needsRefresh) {
            MailService.actionReset(this, null);
        }
    }

    @Override
    protected void onPause() {
        saveSettings();
        super.onPause();
    }

    private void onFontSizeSettings() {
        FontSizeSettings.actionEditSettings(this);
    }

    private void onChooseContactNameColor() {
        new ColorPickerDialog(this, new ColorPickerDialog.OnColorChangedListener() {
            public void colorChanged(int color) {
                K9.setContactNameColor(color);
            }
        }, K9.getContactNameColor()).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVITY_CHOOSE_FOLDER:
                if (resultCode == RESULT_OK && data != null) {
                    // obtain the filename
                    Uri fileUri = data.getData();
                    if (fileUri != null) {
                        String filePath = fileUri.getPath();
                        if (filePath != null) {
                            mAttachmentPathPreference.setSummary(filePath.toString());
                            K9.setAttachmentDefaultPath(filePath.toString());
                        }
                    }
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
