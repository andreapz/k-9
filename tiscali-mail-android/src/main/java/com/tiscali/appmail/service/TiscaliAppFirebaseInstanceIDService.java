package com.tiscali.appmail.service;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.tiscali.appmail.preferences.FirebasePreference;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by thomascastangia on 09/02/17.
 */
public class TiscaliAppFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = TiscaliAppFirebaseInstanceIDService.class.getName();
    public static final String TOKEN_BROADCAST = "token_broadcast";
    public static final String FIREBASE_PUSH_TOKEN = "FIREBASE_PUSH_TOKEN";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        Intent intent = new Intent(TOKEN_BROADCAST);
        intent.putExtra(FIREBASE_PUSH_TOKEN, refreshedToken);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        storeToken(refreshedToken);
    }

    private void storeToken(String token) {
        FirebasePreference.getInstance(getApplicationContext()).storeToken(token);
    }
}
