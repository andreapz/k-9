package com.tiscali.appmail.activity;


public class Search extends NavigationDrawerActivity {
    protected static boolean isActive = false;

    // public Search(INavigationDrawerActivityListener listner, Intent intent) {
    // super(listner, intent);
    // }

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
