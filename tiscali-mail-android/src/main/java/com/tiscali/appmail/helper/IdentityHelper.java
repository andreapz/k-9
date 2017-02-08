package com.tiscali.appmail.helper;


import com.tiscali.appmail.Account;
import com.tiscali.appmail.Identity;
import com.tiscali.appmail.mail.Address;
import com.tiscali.appmail.mail.Message;

public class IdentityHelper {

    /**
     * Find the identity a message was sent to.
     *
     * @param account The account the message belongs to.
     * @param message The message to get the recipients from.
     *
     * @return The identity the message was sent to, or the account's default identity if it
     *         couldn't be determined which identity this message was sent to.
     *
     * @see Account#findIdentity(com.tiscali.appmail.mail.Address)
     */
    public static Identity getRecipientIdentityFromMessage(Account account, Message message) {
        Identity recipient = null;

        for (Address address : message.getRecipients(Message.RecipientType.TO)) {
            Identity identity = account.findIdentity(address);
            if (identity != null) {
                recipient = identity;
                break;
            }
        }
        if (recipient == null) {
            Address[] ccAddresses = message.getRecipients(Message.RecipientType.CC);
            if (ccAddresses.length > 0) {
                for (Address address : ccAddresses) {
                    Identity identity = account.findIdentity(address);
                    if (identity != null) {
                        recipient = identity;
                        break;
                    }
                }
            }
        }

        if (recipient == null) {
            recipient = account.getIdentity(0);
        }

        return recipient;
    }
}
