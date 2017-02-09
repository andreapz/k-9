package com.tiscali.appmail;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

/**
 * Created by andreaputzu on 09/02/17.
 */

public class KeystoreTest {


    private String ksPw = "mamadou";
    private String alias = "indoona";
    private File keystore;

    public void keystoreTest() {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

        try (FileInputStream fis = new FileInputStream(keystore)) {
            ks.load(fis, ksPw.toCharArray());
        }

        ks.getEntry(alias, new KeyStore.PasswordProtection(aliasPw.toCharArray()));
    }
}
