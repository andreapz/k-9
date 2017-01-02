package com.fsck.k9.api;

import com.fsck.k9.api.model.Config;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import rx.Observable;

/**
 * Created by andreaputzu on 02/01/17.
 */

public class MainConfigurationTest {

    @Test
    public void getConfig() throws Exception {

        Observable<Config> mainconfig = MainConfigurationApiAdapter.getConfig();

        Assert.assertNotNull(mainconfig);

    }
}
