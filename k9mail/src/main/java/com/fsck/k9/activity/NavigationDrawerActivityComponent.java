package com.fsck.k9.activity;

import com.fsck.k9.ApplicationComponent;

import dagger.Component;

/**
 * Created by andreaputzu on 01/12/16.
 */

@ActivityScope
@Component(modules = ActivityModule.class, dependencies = ApplicationComponent.class)
public interface NavigationDrawerActivityComponent {
    void inject(NavigationDrawerActivity activity);
}
