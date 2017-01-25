package com.fsck.k9.fragment;

import com.fsck.k9.activity.INavigationDrawerActivityListener;

import android.content.Intent;

/**
 * Created by andreaputzu on 25/01/17.
 */

public class VideoPresenter extends MediaImpPresenter {


    public VideoPresenter(INavigationDrawerActivityListener listener, Intent intent, Type type) {
        super(listener, intent, type);
    }
}
