package org.consensusj.jsonrpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A Trust Manager that adds a second trust store (key store) to the default trust store.
 * See: https://stackoverflow.com/questions/48790981/load-java-trust-store-at-runtime-after-jvm-have-been-launched/58695061#58695061
 */
public class CompositeTrustManager implements X509TrustManager {
    private static final Logger log = LoggerFactory.getLogger(CompositeTrustManager.class);
    private static final char[] password = new char[]{'c','h','a','n','g','e','i','t'};

    private final List<X509TrustManager> trustManagerList;

    /**
     * @param trustStoreStream Input stream for Java keystore with additional certificates
     */
    public CompositeTrustManager(InputStream trustStoreStream) {
        List<X509TrustManager> trustManagers = new ArrayList<>();
        try {
            trustManagers.add(getCustomTrustManager(trustStoreStream));
            trustManagers.add(getDefaultTrustManager());
        } catch (Exception e) {
            log.error("Exception: ", e);
            throw new RuntimeException(e);
        } finally {
            try {
                trustStoreStream.close();
            } catch (IOException e) {
                log.error("Exception: ", e);
            }
        }
        trustManagerList = trustManagers;
    }

    /**
     * @param trustStorePath Path to a Java keystore with additional certificates
     * @throws FileNotFoundException If the file doesn't exist
     */
    public CompositeTrustManager(Path trustStorePath) throws FileNotFoundException {
        this(new FileInputStream(trustStorePath.toFile()));
    }

    /**
     * Used to create an SSLContext using {@link CompositeTrustManager}
     * See: https://stackoverflow.com/questions/859111/how-can-i-use-different-certificates-on-specific-connections
     */
    public static SSLContext getCompositeSSLContext(Path trustStorePath) throws NoSuchAlgorithmException, KeyManagementException, FileNotFoundException {
        TrustManager tm = new CompositeTrustManager(trustStorePath);
        return getSSLContext(tm);
    }


    /**
     * Used to create an SSLContext using {@link CompositeTrustManager}
     * See: https://stackoverflow.com/questions/859111/how-can-i-use-different-certificates-on-specific-connections
     */
    public static SSLContext getCompositeSSLContext(InputStream trustStoreStream) throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager tm = new CompositeTrustManager(trustStoreStream);
        return getSSLContext(tm);
    }

    /**
     * Create an SSLContext using an alternative Trust Store
     */
    public static SSLContext getAlternateSSLContext(Path trustStorePath) throws KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException, CertificateException {
        return getAlternateSSLContext(new FileInputStream(trustStorePath.toFile()));
    }

    /**
     * Create an SSLContext using an alternative Trust Store
     * @param trustStoreStream inputStream with Trust Store (password must be 'changeit')
     */
    public static SSLContext getAlternateSSLContext(InputStream trustStoreStream) throws KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException, CertificateException {
        X509TrustManager trustManager = getCustomTrustManager(trustStoreStream);
        return getSSLContext( trustManager );
    }

    private static SSLContext getSSLContext(TrustManager trustManager) throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, new TrustManager[] {trustManager}, null);
        return ctx;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        for (X509TrustManager trustManager : trustManagerList) {
            try {
                trustManager.checkClientTrusted(chain, authType);
                return;
            } catch (CertificateException e) {
                // maybe the next trust manager will trust it, don't break the loop
            }
        }
        throw new CertificateException("None of the TrustManagers trust this certificate chain");
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        for (X509TrustManager trustManager : trustManagerList) {
            try {
                trustManager.checkServerTrusted(chain, authType);
                return;
            } catch (CertificateException e) {
                // maybe the next trust manager will trust it, don't break the loop
            }
        }
        throw new CertificateException("None of the TrustManagers trust this certificate chain");
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        List<X509Certificate> certs = new ArrayList<>();
        for (X509TrustManager trustManager : trustManagerList) {
            certs.addAll(Arrays.asList(trustManager.getAcceptedIssuers()));
        }
        return certs.toArray(new X509Certificate[0]);
    }

    private static X509TrustManager getCustomTrustManager(InputStream trustStream) throws CertificateException, KeyStoreException, NoSuchAlgorithmException, IOException {
        return createTrustManager(trustStream);
    }

    private static X509TrustManager getDefaultTrustManager() throws CertificateException, KeyStoreException, NoSuchAlgorithmException, IOException {
        return createTrustManager(null);
    }

    private static X509TrustManager createTrustManager(InputStream trustStream) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        // Now get trustStore
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());

        // load the stream to your store
        trustStore.load(trustStream, password);

        // initialize a trust manager factory with the trusted store
        TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustFactory.init(trustStore);

        // get the trust managers from the factory
        TrustManager[] trustManagers = trustFactory.getTrustManagers();
        for (TrustManager trustManager : trustManagers) {
            if (trustManager instanceof X509TrustManager) {
                return (X509TrustManager) trustManager;
            }
        }
        throw new CertificateException("No X509TrustManager available");
    }
}
