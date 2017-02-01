package com.fsck.k9.activity.setup;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import com.fsck.k9.Account;
import com.fsck.k9.K9;
import com.fsck.k9.Preferences;
import com.fsck.k9.R;
import com.fsck.k9.activity.K9Activity;
import com.fsck.k9.helper.Utility;
import com.fsck.k9.mail.AuthType;
import com.fsck.k9.mail.ConnectionSecurity;
import com.fsck.k9.mail.ServerSettings;
import com.fsck.k9.mail.store.RemoteStore;
import com.fsck.k9.mail.store.imap.ImapStoreSettings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

// imported from Tiscali Mail

public class TiscaliAccountSetupUserPassword extends K9Activity implements OnClickListener {
    private static final String EXTRA_ACCOUNT =
            "com.tiscali.appmail.TiscaliAccountSetupUserPassword.account";
    private static final String STATE_KEY_CHECKED_INCOMING =
            "com.tiscali.appmail.TiscaliAccountSetupUserPassword.checkedIncoming";

    private EditText mUsernameView;
    private EditText mPasswordView;
    private CheckBox mShowPasswordCheckBox;
    private Button mNextButton;

    private Account mAccount;
    private boolean mCheckedIncoming = false;
    private String mEmailDomain;

    public static void actionEditUserPasswordSettings(Activity context, Account account) {
        context.startActivity(intentActionEditUserPasswordSettings(context, account));
    }

    public static Intent intentActionEditUserPasswordSettings(Context context, Account account) {
        Intent i = new Intent(context, TiscaliAccountSetupUserPassword.class);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        return i;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String accountUuid = getIntent().getStringExtra(EXTRA_ACCOUNT);
        mAccount = Preferences.getPreferences(this).getAccount(accountUuid);

        /*
         * If we're being reloaded we override the original account with the one we saved
         */
        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_ACCOUNT)) {
            accountUuid = savedInstanceState.getString(EXTRA_ACCOUNT);
            mAccount = Preferences.getPreferences(this).getAccount(accountUuid);
        }

        if (mAccount == null) {
            finish();

            return;
        }

        setContentView(R.layout.tiscali_account_setup_user_password);

        mEmailDomain = getResources().getString(R.string.account_setup_basics_domain_tiscali);

        mUsernameView = (EditText) findViewById(R.id.account_username);
        mPasswordView = (EditText) findViewById(R.id.account_password);
        TextView emailDomainTv = (TextView) findViewById(R.id.account_domain);
        emailDomainTv.setText(mEmailDomain);

        mShowPasswordCheckBox = (CheckBox) findViewById(R.id.show_password);
        mShowPasswordCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showPassword(isChecked);
            }
        });

        mNextButton = (Button) findViewById(R.id.next);
        mNextButton.setOnClickListener(this);

        /*
         * Calls validateFields() which enables or disables the Next button based on the fields'
         * validity.
         */
        TextWatcher validationTextWatcher = new TextWatcher() {
            public void afterTextChanged(Editable e) {
                validateFields();
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                /* unused */
            }

            public void onTextChanged(CharSequence c, int start, int before, int count) {
                /* unused */
            }
        };
        mUsernameView.addTextChangedListener(validationTextWatcher);
        mPasswordView.addTextChangedListener(validationTextWatcher);

        try {
            ServerSettings settings = RemoteStore.decodeStoreUri(mAccount.getStoreUri());

            if (settings.username != null)
                mUsernameView.setText(settings.username);

            if (settings.password != null)
                mPasswordView.setText(settings.password);

            if (!ServerSettings.Type.IMAP.equals(settings.type))
                throw new Exception("Unknown account type: " + mAccount.getStoreUri());

            validateFields();
        } catch (Exception e) {
            failure(e);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(EXTRA_ACCOUNT, mAccount.getUuid());
        outState.putBoolean(STATE_KEY_CHECKED_INCOMING, mCheckedIncoming);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mCheckedIncoming = savedInstanceState.getBoolean(STATE_KEY_CHECKED_INCOMING);

        showPassword(mShowPasswordCheckBox.isChecked());
    }

    private void showPassword(boolean showPassword) {
        mPasswordView.setInputType(showPassword ? InputType.TYPE_TEXT_VARIATION_PASSWORD
                : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mPasswordView.setSelection(mPasswordView.getText().length());
    }

    private void validateFields() {
        mNextButton.setEnabled(Utility.requiredFieldValid(mUsernameView)
                && Utility.requiredFieldValid(mPasswordView));

        Utility.setCompoundDrawablesAlpha(mNextButton, mNextButton.isEnabled() ? 255 : 128);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (!mCheckedIncoming) {
                /*
                 * Set the username and password for the outgoing settings to the username and
                 * password the user just set for incoming.
                 */
                try {
                    String usernameEnc =
                            URLEncoder.encode(mUsernameView.getText().toString(), "UTF-8");
                    String passwordEnc =
                            URLEncoder.encode(mPasswordView.getText().toString(), "UTF-8");
                    URI oldUri = new URI(mAccount.getTransportUri());
                    URI uri = new URI(oldUri.getScheme(),
                            usernameEnc + ":" + passwordEnc + ":CRAM_MD5", oldUri.getHost(),
                            oldUri.getPort(), null, null, null);

                    mAccount.setTransportUri(uri.toString());
                } catch (UnsupportedEncodingException enc) {
                    // This really shouldn't happen since the encoding is hardcoded to UTF-8
                    Log.e(K9.LOG_TAG, "Couldn't urlencode username or password.", enc);
                } catch (URISyntaxException use) {
                    /*
                     * If we can't set up the URL we just continue. It's only for convenience.
                     */
                }

                // We've successfully checked incoming. Now check outgoing.
                mCheckedIncoming = true;
                AccountSetupCheckSettings.actionCheckSettings(this, mAccount,
                        AccountSetupCheckSettings.CheckDirection.OUTGOING);

                mAccount.setEmail(mUsernameView.getText().toString() + mEmailDomain);
            } else {
                /*
                 * boolean isPushCapable = false; try { Store store = mAccount.getRemoteStore();
                 * isPushCapable = store.isPushCapable(); } catch (Exception e) { Log.e(K9.LOG_TAG,
                 * "Could not get remote store", e); } if (isPushCapable &&
                 * mAccount.getFolderPushMode() != FolderMode.NONE) {
                 * MailService.actionRestartPushers(this, null); }
                 */

                // We've successfully checked outgoing as well.
                mAccount.setDescription(mAccount.getEmail());
                mAccount.save(Preferences.getPreferences(this));

                K9.setServicesEnabled(this);
                AccountSetupNames.actionSetNames(this, mAccount);
                finish();
            }
        } else if (resultCode == AccountSetupCheckSettings.RESULT_ERROR_INVALID_USER_PASSWORD) {
            mCheckedIncoming = false;
        }
    }

    protected void onNext() {
        try {
            ServerSettings settings = RemoteStore.decodeStoreUri(mAccount.getStoreUri());
            if (!ServerSettings.Type.IMAP.equals(settings.type)) {
                throw new Exception("Unknown account type: " + mAccount.getStoreUri());
            }

            String username = mUsernameView.getText().toString();
            String password = mPasswordView.getText().toString();
            AuthType authType = settings.authenticationType;
            String host = settings.host;
            int port = settings.port;
            ConnectionSecurity connectionSecurity = settings.connectionSecurity;
            String clientCertificateAlias = settings.clientCertificateAlias;

            mAccount.deleteCertificate(host, port,
                    AccountSetupCheckSettings.CheckDirection.INCOMING);

            ImapStoreSettings imapSettings = (ImapStoreSettings) settings;

            Map<String, String> extra = new HashMap<String, String>();
            extra.put(ImapStoreSettings.AUTODETECT_NAMESPACE_KEY,
                    Boolean.toString(imapSettings.autoDetectNamespace));
            extra.put(ImapStoreSettings.PATH_PREFIX_KEY, imapSettings.pathPrefix);

            settings = new ServerSettings(ServerSettings.Type.IMAP, host, port, connectionSecurity,
                    authType, username, password, clientCertificateAlias);

            mAccount.setStoreUri(RemoteStore.createStoreUri(settings));

            AccountSetupCheckSettings.actionCheckSettings(this, mAccount,
                    AccountSetupCheckSettings.CheckDirection.INCOMING);
        } catch (Exception e) {
            failure(e);
        }
    }

    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.next:
                    onNext();
                    break;
            }
        } catch (Exception e) {
            failure(e);
        }
    }

    private void failure(Exception use) {
        Log.e(K9.LOG_TAG, "Failure", use);

        Toast toast = Toast.makeText(getApplication(),
                getString(R.string.account_setup_bad_uri, use.getMessage()), Toast.LENGTH_LONG);
        toast.show();
    }
}
