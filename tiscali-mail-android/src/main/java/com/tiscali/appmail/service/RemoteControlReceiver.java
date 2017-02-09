
package com.tiscali.appmail.service;

import static com.tiscali.appmail.remotecontrol.K9RemoteControl.K9_ACCOUNT_DESCRIPTIONS;
import static com.tiscali.appmail.remotecontrol.K9RemoteControl.K9_ACCOUNT_UUIDS;

import java.util.List;

import com.tiscali.appmail.Account;
import com.tiscali.appmail.K9;
import com.tiscali.appmail.Preferences;
import com.tiscali.appmail.remotecontrol.K9RemoteControl;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class RemoteControlReceiver extends CoreReceiver {
    @Override
    public Integer receive(Context context, Intent intent, Integer tmpWakeLockId) {
        if (K9.DEBUG)
            Log.i(K9.LOG_TAG, "RemoteControlReceiver.onReceive" + intent);

        if (K9RemoteControl.K9_SET.equals(intent.getAction())) {
            RemoteControlService.set(context, intent, tmpWakeLockId);
            tmpWakeLockId = null;
        } else if (K9RemoteControl.K9_REQUEST_ACCOUNTS.equals(intent.getAction())) {
            try {
                Preferences preferences = Preferences.getPreferences(context);
                List<Account> accounts = preferences.getAccounts();
                String[] uuids = new String[accounts.size()];
                String[] descriptions = new String[accounts.size()];
                for (int i = 0; i < accounts.size(); i++) {
                    // warning: account may not be isAvailable()
                    Account account = accounts.get(i);

                    uuids[i] = account.getUuid();
                    descriptions[i] = account.getDescription();
                }
                Bundle bundle = getResultExtras(true);
                bundle.putStringArray(K9_ACCOUNT_UUIDS, uuids);
                bundle.putStringArray(K9_ACCOUNT_DESCRIPTIONS, descriptions);
            } catch (Exception e) {
                Log.e(K9.LOG_TAG, "Could not handle K9_RESPONSE_INTENT", e);
            }

        }

        return tmpWakeLockId;
    }

}
