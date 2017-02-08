package com.tiscali.appmail.fragment;

import javax.inject.Inject;

import com.tiscali.appmail.activity.INavigationDrawerActivityListener;

import android.content.Intent;

/**
 * Created by andreaputzu on 25/01/17.
 */

public class MediaImpPresenter extends MediaPresenter {

    Type mType;

    @Inject
    public MediaImpPresenter(INavigationDrawerActivityListener listener, Intent intent, Type type) {
        super(listener, intent);
        this.mType = type;
    }

    @Override
    public Type getType() {
        return mType;
    }
}
