//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.utils;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.rong.imageloader.core.assist.ContentLengthInputStream;
import io.rong.imageloader.core.download.BaseImageDownloader;
import io.rong.imageloader.utils.IoUtils;

public class RongAuthImageDownloader extends BaseImageDownloader {
    private SSLSocketFactory mSSLSocketFactory;
    final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    public RongAuthImageDownloader(Context context) {
        super(context);
        SSLContext sslContext = this.sslContextForTrustedCertificates();
        this.mSSLSocketFactory = sslContext.getSocketFactory();
    }

    public RongAuthImageDownloader(Context context, int connectTimeout, int readTimeout) {
        super(context, connectTimeout, readTimeout);
        SSLContext sslContext = this.sslContextForTrustedCertificates();
        this.mSSLSocketFactory = sslContext.getSocketFactory();
    }

    protected InputStream getStreamFromNetwork(String imageUri, Object extra) throws IOException {
        URL url = null;

        try {
            url = new URL(imageUri);
        } catch (MalformedURLException var7) {
            var7.printStackTrace();
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(this.connectTimeout);
        conn.setReadTimeout(this.readTimeout);
        if (conn instanceof HttpsURLConnection) {
            ((HttpsURLConnection) conn).setSSLSocketFactory(this.mSSLSocketFactory);
            ((HttpsURLConnection) conn).setHostnameVerifier(this.DO_NOT_VERIFY);
        }

        conn.connect();

        InputStream imageStream;
        try {
            if (conn.getResponseCode() >= 300 && conn.getResponseCode() < 400) {
                String redirectUrl = conn.getHeaderField("Location");
                conn = (HttpURLConnection) (new URL(redirectUrl)).openConnection();
                conn.setConnectTimeout(this.connectTimeout);
                conn.setReadTimeout(this.readTimeout);
                if (conn instanceof HttpsURLConnection) {
                    ((HttpsURLConnection) conn).setSSLSocketFactory(this.mSSLSocketFactory);
                    ((HttpsURLConnection) conn).setHostnameVerifier(this.DO_NOT_VERIFY);
                }

                conn.connect();
            }

            imageStream = conn.getInputStream();
        } catch (IOException var8) {
            if (conn.getContentLength() <= 0 || !conn.getContentType().contains("image/")) {
                IoUtils.readAndCloseStream(conn.getErrorStream());
                throw var8;
            }

            imageStream = conn.getErrorStream();
        }

        if (!this.shouldBeProcessed(conn)) {
            IoUtils.closeSilently(imageStream);
            throw new IOException("Image request failed with response code " + conn.getResponseCode());
        } else {
            return new ContentLengthInputStream(new BufferedInputStream(imageStream, 32768), conn.getContentLength());
        }
    }

    private SSLContext sslContextForTrustedCertificates() {
        TrustManager[] trustAllCerts = new TrustManager[1];
        TrustManager tm = new io.rong.imkit.utils.RongAuthImageDownloader.miTM();
        trustAllCerts[0] = tm;
        SSLContext sc = null;

        try {
            sc = SSLContext.getInstance("SSL");
            sc.init((KeyManager[]) null, trustAllCerts, (SecureRandom) null);
            return sc;
        } catch (NoSuchAlgorithmException var9) {
            var9.printStackTrace();
            return sc;
        } catch (KeyManagementException var10) {
            var10.printStackTrace();
            return sc;
        } finally {
            ;
        }
    }

    class miTM implements TrustManager, X509TrustManager {
        miTM() {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
        }
    }
}
