package com.tiscali.appmail;

import javax.inject.Singleton;

import com.tiscali.appmail.activity.MessageCompose;

import dagger.Component;

/**
 * Created by andreaputzu on 01/12/16.
 */

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {
    void inject(MessageCompose activity);
}
