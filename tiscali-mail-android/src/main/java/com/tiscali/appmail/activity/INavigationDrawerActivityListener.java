package com.tiscali.appmail.activity;

import com.tiscali.appmail.Account;
import com.tiscali.appmail.api.ApiController;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.widget.FrameLayout;


/**
 * Created by andreaputzu on 12/12/16.
 */

public interface INavigationDrawerActivityListener {
    Activity getActivity();

    FrameLayout getContainer();

    void setDrawerListAdapter(RecyclerView.Adapter<RecyclerView.ViewHolder> adapter);

    void closeDrawer();

    void setDrawerEnable(boolean isEnabled);

    void showBottomNav();

    void hideBottomNav();

    ApiController getApiController();

    void showInformations();

    void updateAccount(Account account);
}
