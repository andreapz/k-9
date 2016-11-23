package com.fsck.k9.activity;


import com.fsck.k9.fragment.MailFragment;

public class Search extends MailFragment {
    protected static boolean isActive = false;

    public static boolean isActive() {
        return isActive;
    }

    public static void setActive(boolean val) {
        isActive = val;
    }

    @Override
    public void onStart() {
        setActive(true);
        super.onStart();
    }

    @Override
    public void onStop() {
        setActive(false);
        super.onStop();
    }



}
