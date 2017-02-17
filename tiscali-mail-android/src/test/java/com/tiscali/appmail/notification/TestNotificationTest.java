package com.tiscali.appmail.notification;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.tiscali.appmail.BuildConfig;

/**
 * Created by andreaputzu on 17/02/17.
 */

@Config(manifest = "src/main/AndroidManifest.xml", sdk = 21)
public class TestNotificationTest extends RobolectricTestRunner {


    /**
     * Creates a runner to run {@code testClass}. Looks in your working directory for your
     * AndroidManifest.xml file and res directory by default. Use the {@link Config} annotation to
     * configure.
     *
     * @param testClass the test class to be run
     * @throws InitializationError if junit says so
     */
    public TestNotificationTest(Class<?> testClass) throws InitializationError {
        super(testClass);
        String buildVariant = BuildConfig.BUILD_TYPE
                + (BuildConfig.FLAVOR.isEmpty() ? "" : "/" + BuildConfig.FLAVOR);
        System.setProperty("android.package", BuildConfig.APPLICATION_ID);
        System.setProperty("android.manifest",
                "build/intermediates/manifests/full/" + buildVariant + "/AndroidManifest.xml");
        System.setProperty("android.resources", "build/intermediates/res/" + buildVariant);
        System.setProperty("android.assets", "build/intermediates/assets/" + buildVariant);
    }

    @Test
    public void withPrivacyModeActive() throws Exception {
        assertEquals(1, 1);
    }
}
