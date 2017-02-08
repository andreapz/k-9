package com.tiscali.appmail.activity;

import com.tiscali.appmail.ApplicationComponent;

import dagger.Component;

/**
 * Created by andreaputzu on 01/12/16.
 */

@ActivityScope
@Component(modules = ActivityModule.class, dependencies = ApplicationComponent.class)
public interface NavigationDrawerActivityComponent {
    void inject(NavigationDrawerActivity activity);
}
