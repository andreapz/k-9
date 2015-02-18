package com.fsck.k9.account;

import com.fsck.k9.Account.DeletePolicy;
import com.fsck.k9.mail.ServerSettings.Type;

import java.util.HashMap;
import java.util.Map;

/**
 * Deals with logic surrounding account creation.
 * <p/>
 * TODO Move much of the code from com.fsck.k9.activity.setup.* into here
 */
public class AccountCreator {

    private static Map<Type, DeletePolicy> defaults;

    public static DeletePolicy calculateDefaultDeletePolicy(Type type) {
        return getDefaults().get(type);
    }

    private static synchronized Map<Type, DeletePolicy> getDefaults() {
        if (null == defaults) {
            defaults = new HashMap<Type, DeletePolicy>();
            defaults.put(Type.IMAP, DeletePolicy.ON_DELETE);
            defaults.put(Type.POP3, DeletePolicy.NEVER);
            defaults.put(Type.WebDAV, DeletePolicy.ON_DELETE);
        }
        return defaults;
    }
}
