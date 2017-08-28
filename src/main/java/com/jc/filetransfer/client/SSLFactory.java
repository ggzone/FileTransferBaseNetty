package com.jc.filetransfer.client;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * Created by Hunter on 2017/04/14.
 */
public class SSLFactory {
    private static final Logger logger = LoggerFactory.getLogger(SSLFactory.class);

    public static SSLEngine createSSLEngine(String storeFile, String storePass, String keyPass, boolean clientMode) throws Exception {
        try {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream in = new FileInputStream(storeFile);
            ks.load(in, storePass.toCharArray());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, keyPass.toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);

            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            SSLEngine sslEngine = ctx.createSSLEngine();
            sslEngine.setUseClientMode(clientMode);
            return sslEngine;
        } catch (Exception ex) {
            logger.error("Create SSLEngine failed! cause" + ex.toString());
            throw new Exception("Create SSLEngine failed! cause" + ex.toString());
        }
    }


}
