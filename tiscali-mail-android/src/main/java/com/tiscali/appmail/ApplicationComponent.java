package com.tiscali.appmail;

import javax.inject.Singleton;

import com.tiscali.appmail.activity.MessageCompose;
import com.tiscali.appmail.activity.setup.AccountSetupBasics;
import com.tiscali.appmail.activity.setup.AccountSetupCheckSettings;
import com.tiscali.appmail.activity.setup.AccountSetupNames;
import com.tiscali.appmail.activity.setup.TiscaliAccountSetupUserPassword;

import dagger.Component;

/**
 * Created by andreaputzu on 01/12/16.
 */

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {
    void inject(MessageCompose activity);

    void inject(TiscaliAccountSetupUserPassword activity);

    void inject(AccountSetupBasics activity);

    void inject(AccountSetupNames activity);

    void inject(AccountSetupCheckSettings activity);
}
