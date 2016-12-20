package com.fsck.k9.activity;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;

/**
 * Created by andreaputzu on 12/12/16.
 */

public interface IDrawerActivityListener {
    int getSelectedTab();
    CharSequence getActionBarTitle();
    ActionBar getDrawerActivityActionBar();
    void onInvalidateOptionsMenu();
    DrawerLayout getDrawerLayout();
    RecyclerView getDrawerList();
}
