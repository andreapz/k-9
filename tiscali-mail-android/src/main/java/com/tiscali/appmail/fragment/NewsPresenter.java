package com.tiscali.appmail.fragment;

import com.tiscali.appmail.activity.INavigationDrawerActivityListener;

import android.content.Intent;

/**
 * Created by andreaputzu on 25/01/17.
 */

public class NewsPresenter extends MediaImpPresenter {


    public NewsPresenter(INavigationDrawerActivityListener listener, Intent intent, Type type) {
        super(listener, intent, type);
    }
}
