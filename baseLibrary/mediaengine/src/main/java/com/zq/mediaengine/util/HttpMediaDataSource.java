package com.zq.mediaengine.util;

import android.media.MediaDataSource;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@RequiresApi(api = Build.VERSION_CODES.M)
public class HttpMediaDataSource extends MediaDataSource {
    private static final String TAG = "HttpMediaDataSource";

    private URL mURL;
    private HttpURLConnection mURLConnection;
    private BufferedInputStream mInputStream;
    private int mConnectTimeout = 5 * 1000;
    private int mReadTimeout = 3 * 1000;
    private long mLength = -1;
    private long mPosition = 0;

    public HttpMediaDataSource(URL url) {
        mURL = url;
    }

    public HttpMediaDataSource(String urlString) throws IOException {
        this(new URL(urlString));
    }

    public void setConnectTimeout(int connectTimeout) {
        mConnectTimeout = connectTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        mReadTimeout = readTimeout;
    }

    @Override
    public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
        Log.d(TAG, "readAt: " + position + " size: " + size);
        if (mURLConnection == null || mPosition != position) {
            disconnect();
            connect(position);
        }
        if (size == 0) {
            return 0;
        }
        int result = mInputStream.read(buffer, offset, size);
        if (mPosition == position) {
            mPosition += result;
        } else {
            mPosition = position + result;
        }
        Log.d(TAG, "~readAt result: " + result);
        return result;
    }

    @Override
    public long getSize() throws IOException {
        connect(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mLength = mURLConnection.getContentLengthLong();
        } else {
            mLength = mURLConnection.getContentLength();
        }
        Log.i(TAG, "~getSize: " + mLength);
        return mLength > 0 ? mLength : -1;
    }

    @Override
    public void close() throws IOException {
        Log.i(TAG, "close");
        disconnect();
    }

    private void connect(long position) throws IOException {
        Log.d(TAG, "connect with range: " + position);
        mURLConnection = (HttpURLConnection) mURL.openConnection();
        mURLConnection.setUseCaches(true);
        mURLConnection.setRequestMethod("GET");
        mURLConnection.setRequestProperty("range", "bytes=" + position + "-");
        mURLConnection.setRequestProperty("Accept-Encoding", "identity");
        mURLConnection.setConnectTimeout(mConnectTimeout);
        mURLConnection.setReadTimeout(mReadTimeout);
        int responseCode = mURLConnection.getResponseCode();
        if (responseCode != 200 && responseCode != 206) {
            throw new IOException("http response code " + responseCode);
        }
        if (mInputStream == null) {
            mInputStream = new BufferedInputStream(mURLConnection.getInputStream());
        }
    }

    private void disconnect() throws IOException {
        Log.d(TAG, "disconnect");
        if (mInputStream != null) {
            mInputStream.close();
            mInputStream = null;
        }
        if (mURLConnection != null) {
            mURLConnection.disconnect();
            mURLConnection = null;
        }
    }
}
