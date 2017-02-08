package com.tiscali.appmail.activity;

import com.tiscali.appmail.ApplicationComponent;

import dagger.Component;

/**
 * Created by thomascastangia on 01/02/17.
 */


@ActivityScope
@Component(modules = ActivityModule.class, dependencies = ApplicationComponent.class)
public interface WelcomeActivityComponent {
    void inject(WelcomeActivity activity);
}
