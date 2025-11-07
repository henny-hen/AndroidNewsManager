package com.example.newsmanagerproject.services;

import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class TrustModifier {
    private static final TrustingHostnameVerifier TRUSTING_HOSTNAME_VERIFIER = new TrustingHostnameVerifier();
    private static SSLContext SSL_CONTEXT;

    static {
        try {
            SSL_CONTEXT = SSLContext.getInstance("TLS");
            SSL_CONTEXT.init(null, new TrustManager[]{new AlwaysTrustManager()}, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public static void relaxHostChecking(HttpURLConnection conn) {
        if (conn instanceof HttpsURLConnection) {
            HttpsURLConnection httpsConnection = (HttpsURLConnection) conn;
            httpsConnection.setSSLSocketFactory(SSL_CONTEXT.getSocketFactory());
            httpsConnection.setHostnameVerifier(TRUSTING_HOSTNAME_VERIFIER);
        }
    }

    private static class TrustingHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    private static class AlwaysTrustManager implements X509TrustManager {
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) {
        }

        public void checkServerTrusted(X509Certificate[] arg0, String arg1) {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}