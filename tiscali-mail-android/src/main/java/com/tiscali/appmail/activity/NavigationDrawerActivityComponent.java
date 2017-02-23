package com.tiscali.appmail.activity;

import com.tiscali.appmail.ApplicationComponent;
import com.tiscali.appmail.fragment.MailPresenter;
import com.tiscali.appmail.fragment.MediaPresenter;

import dagger.Component;

/**
 * Created by andreaputzu on 01/12/16.
 */

@ActivityScope
@Component(modules = ActivityModule.class, dependencies = ApplicationComponent.class)
public interface NavigationDrawerActivityComponent {
    void inject(NavigationDrawerActivity activity);

    void injectMailPresenter(MailPresenter presenter);

    void injectMediaPresenter(MediaPresenter presenter);
}
