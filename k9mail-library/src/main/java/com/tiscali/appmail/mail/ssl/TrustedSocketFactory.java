package com.tiscali.appmail.mail.ssl;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import com.tiscali.appmail.mail.MessagingException;

public interface TrustedSocketFactory {
    Socket createSocket(Socket socket, String host, int port, String clientCertificateAlias)
            throws NoSuchAlgorithmException, KeyManagementException, MessagingException,
            IOException;
}
