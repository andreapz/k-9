package com.fsck.k9;

import android.content.Context;
import android.content.Intent;

import com.fsck.k9.fragment.MailPresenter;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by andreaputzu on 01/12/16.
 */

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

}

