package com.tiscali.appmail;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.tiscali.appmail.preferences.FirebasePreference;

import android.content.Intent;
import android.util.Log;

/**
 * Created by thomascastangia on 09/02/17.
 */
public class TiscaliFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "FBMsgServiceID";
    public static final String TOKEN_BROADCAST = "token_broadcast";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        getApplicationContext().sendBroadcast(new Intent(TOKEN_BROADCAST));
        storeToken(refreshedToken);
    }

    private void storeToken(String token) {
        FirebasePreference.getInstance(getApplicationContext()).storeToken(token);
    }
}
