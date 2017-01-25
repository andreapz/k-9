package com.fsck.k9.fragment;

import javax.inject.Inject;

import com.fsck.k9.activity.INavigationDrawerActivityListener;

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
