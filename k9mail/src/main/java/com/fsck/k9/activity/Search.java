package com.fsck.k9.activity;


import android.content.Context;
import android.content.Intent;

import com.fsck.k9.fragment.MailPresenter;

public class Search extends MailPresenter {
    protected static boolean isActive = false;

    public Search(Context context, Intent intent) {
        super(context, intent);
    }

    public static boolean isActive() {
        return isActive;
    }

    public static void setActive(boolean val) {
        isActive = val;
    }



//    @Override
//    public void onStart() {
//        setActive(true);
//        super.onStart();
//    }
//
//    @Override
//    public void onStop() {
//        setActive(false);
//        super.onStop();
//    }



}
