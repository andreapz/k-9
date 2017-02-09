package com.tiscali.appmail.preferences;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by thomascastangia on 09/02/17.
 */

public class FirebasePreference {
    private final static String PREF_NAME = "firebase_shared";
    private final static String ACCESS_TOKEN_KEY = "token";

    private static Context mContext;
    private static FirebasePreference mInstance;

    private FirebasePreference(Context context) {
        mContext = context;

    }

    public static synchronized FirebasePreference getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new FirebasePreference(context);
        }
        return mInstance;
    }

    public boolean storeToken(String token) {
        SharedPreferences preferences =
                mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(ACCESS_TOKEN_KEY, token);
        editor.apply();
        return true;
    }

    public String getToken() {
        SharedPreferences preferences =
                mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(ACCESS_TOKEN_KEY, null);
    }

}
