package com.fsck.k9.activity;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.widget.FrameLayout;

import com.fsck.k9.Account;
import com.fsck.k9.adapter.BaseNavDrawerMenuAdapter;
import com.fsck.k9.model.NavDrawerMenuItem;

import java.util.List;

/**
 * Created by andreaputzu on 12/12/16.
 */

public interface INavigationDrawerActivityListener {
    Activity getActivity();

    FrameLayout getContainer();
    void setDrawerListAdapter(BaseNavDrawerMenuAdapter adapter);
    void closeDrawer();
    void showDialogSettings(Account account);
    void showDialogCustomize(List<NavDrawerMenuItem> data);

    void setDrawerEnable(boolean isEnabled);
}
