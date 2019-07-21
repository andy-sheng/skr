package com.common.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Pair;

import com.common.download.DownloadTask;
import com.common.log.MyLog;
import com.common.statistics.StatisticsAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

public class HttpUtils {
    public final String TAG = "HttpUtils";

    private String USER_AGENT = null;

    HttpUtils() {
    }


    /**
     * 得到一批域名的ip地址
     *
     * @param host
     * @return
     */
    public static List<String> getAddressByHost(String host) {
        List<String> address = new ArrayList<>();
        if (!TextUtils.isEmpty(host)) {
            //DnsPod
            String dnsPodStr = DnsPodUtils.getAddressByHostDnsPod(host);
            if (!TextUtils.isEmpty(dnsPodStr)) {
                dnsPodStr = dnsPodStr.split(",")[0];
                String[] dnsPodStrs = dnsPodStr.split(";");
                MyLog.w("HttpDns", "domain=" + host + ",ipsList=" + dnsPodStr);
                for (int i = dnsPodStrs.length - 1; i >= 0; i--) {
                    String ipStr = dnsPodStrs[i];
                    if (!address.contains(ipStr)) {
                        address.add(0, ipStr);
                    }
                }
            }

            InetAddress[] addr = null;
            try {
                addr = InetAddress.getAllByName(host);
                if (addr != null && addr.length > 0) {
                    for (InetAddress item : addr) {
                        String hostAddress = item.getHostAddress();
                        if (!address.contains(hostAddress)) {
                            address.add(hostAddress);
                        }
                    }
                }
            } catch (Exception e) {
                MyLog.e("getAddressByHost error = " + e);
            }

        }
        return address;
    }

    /**
     * http 头里带的一些信息
     *
     * @return
     */
    public String buildUserAgent() {
        if (USER_AGENT == null) {
            StringBuilder sb = new StringBuilder();
            /**
             * 注意这个 如果是 中文名字 崩溃
             */
//            sb.append(U.getAppInfoUtils().getAppName()).append(" ");
            sb.append("LIVE").append(" ");
            sb.append(U.getAppInfoUtils().getVersionCode());
            sb.append(" (");
            sb.append(U.getStringUtils().join(new String[]{
                    Build.MODEL, String.valueOf(Build.VERSION.SDK_INT), U.getAppInfoUtils().getLanguageCode()
            }, ";"));
            sb.append(") ");
            sb.append(Build.VERSION.RELEASE);
            USER_AGENT = sb.toString();
        }
        return USER_AGENT;
    }

    public boolean isCtwap(Context context) {
        // 如果不是中国sim卡，直接返回否
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String countryISO = tm.getSimCountryIso();
        if (!"CN".equalsIgnoreCase(countryISO)) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return false;
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null)
            return false;
        String extraInfo = info.getExtraInfo();
        if (TextUtils.isEmpty(extraInfo) || (extraInfo.length() < 3))
            return false;
        return extraInfo.contains("ctwap");
    }

    public boolean isCmwap(Context context) {
        // 如果不是中国sim卡，直接返回否
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String countryISO = tm.getSimCountryIso();
        if (!"CN".equalsIgnoreCase(countryISO)) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return false;
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null)
            return false;
        String extraInfo = info.getExtraInfo();
        if (TextUtils.isEmpty(extraInfo) || (extraInfo.length() < 3))
            return false;
        if (extraInfo.contains("ctwap")) {
            return false;
        }
        return extraInfo.regionMatches(true, extraInfo.length() - 3, "wap", 0, 3);
    }

    public String getCMWapUrl(URL oriUrl) {
        StringBuilder gatewayBuilder = new StringBuilder();
        gatewayBuilder.append(oriUrl.getProtocol()).append("://").append("10.0.0.172").append(oriUrl.getPath());
        if (!TextUtils.isEmpty(oriUrl.getQuery())) {
            gatewayBuilder.append("?").append(oriUrl.getQuery());
        }
        return gatewayBuilder.toString();
    }

    private HttpURLConnection getHttpUrlConnection(Context context, URL url) throws IOException {
        if (isCtwap(context)) {
            java.net.Proxy proxy = new java.net.Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.0.0.200", 80));
            return (HttpURLConnection) url.openConnection(proxy);
        }
        if (!isCmwap(context)) {
            return (HttpURLConnection) url.openConnection();
        } else {
            String host = url.getHost();
            String cmwapUrl = getCMWapUrl(url);
            URL gatewayUrl = new URL(cmwapUrl);
            HttpURLConnection conn = (HttpURLConnection) gatewayUrl.openConnection();
            conn.addRequestProperty("X-Online-Host", host);
            return conn;
        }
    }


    private String fromParamListToString(List<Pair<String, String>> nameValuePairs) {
        StringBuffer params = new StringBuffer();
        for (Pair<String, String> pair : nameValuePairs) {
            try {
                if (pair.first == null)
                    continue;
                params.append(URLEncoder.encode(pair.first, "UTF-8"));
                params.append("=");
                params.append(URLEncoder.encode(pair.second, "UTF-8"));
                params.append("&");
            } catch (UnsupportedEncodingException e) {
                MyLog.i(TAG, "Failed to convert from param list to string: " + e.toString());
                MyLog.i(TAG, "pair: " + pair.toString());
                return null;
            }
        }
        if (params.length() > 0) {
            params = params.deleteCharAt(params.length() - 1);
        }
        return params.toString();
    }

    /**
     * 进行一个 post 请求
     *
     * @param context
     * @param url
     * @param nameValuePairs
     * @param headers
     * @param userAgent
     * @param cookie
     * @return
     * @throws IOException
     */
    public String doHttpPost(Context context, String url,
                             List<Pair<String, String>> nameValuePairs,
                             Map<String, String> headers, String userAgent, String cookie) throws IOException {
        if (TextUtils.isEmpty(url))
            throw new IllegalArgumentException("url");
        String responseContent = "";
        InputStream is = null;
        HttpURLConnection conn = null;
        try {
            conn = getHttpUrlConnection(context, new URL(url));
            conn.setConnectTimeout(10 * 1000);
            conn.setReadTimeout(15 * 1000);
            conn.setRequestMethod("POST");
            if (!TextUtils.isEmpty(userAgent)) {
                conn.setRequestProperty("http.useragent", userAgent);
            }
            if (cookie != null) {
                conn.setRequestProperty("Cookie", cookie);
            }

            String strParams = fromParamListToString(nameValuePairs);
            MyLog.i("Dozen", " url : " + url + ", str params : " + strParams);
            if (null == strParams) {
                throw new IllegalArgumentException("nameValuePairs");
            }

            conn.setDoOutput(true);
            byte[] b = strParams.getBytes();
            conn.getOutputStream().write(b, 0, b.length);
            conn.getOutputStream().flush();
            conn.getOutputStream().close();
            int statusCode = conn.getResponseCode();
            MyLog.i(TAG, "http POST Response Code: " + statusCode);
            is = conn.getInputStream();
            if (null != is) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(new DoneHandlerInputStream(is)));
                String tempLine = rd.readLine();
                StringBuffer tempStr = new StringBuffer();
                String crlf = System.getProperty("line.separator");
                while (tempLine != null) {
                    tempStr.append(tempLine);
                    tempStr.append(crlf);
                    tempLine = rd.readLine();
                }
                responseContent = tempStr.toString();
                rd.close();
                if (headers != null) {
                    for (int i = 0; ; i++) {
                        String name = conn.getHeaderFieldKey(i);
                        String value = conn.getHeaderField(i);
                        if (name == null && value == null) {
                            break;
                        }
                        headers.put(name, value);
                        i++;
                    }
                }
            }
        } catch (Exception e) {
            MyLog.e(e);
        } finally {
            if (null != is) {
                is.close();
            }
            if (null != conn) {
                conn.disconnect();
                conn = null;
            }
        }
        return responseContent;
    }


    /***********http下载*************/

    HashMap<String, DownloadParams> mDownLoadMap = new HashMap<>();

    /**
     * 取消某个下载
     *
     * @param url
     */
    public void cancelDownload(String url) {
        DownloadParams downloadParams = mDownLoadMap.get(url);
        if (downloadParams != null) {
            // 下载取消了
            downloadParams.hasCancel = true;
        }
    }

    public void cancelDownload(String bucketName, String objectkey) {
        String urlStr = bucketName + "_" + objectkey;
        DownloadParams downloadParams = mDownLoadMap.get(urlStr);
        if (downloadParams != null) {
            // 下载取消了
            downloadParams.hasCancel = true;
        }
    }

    /**
     * oss文件下载接口，异步
     *
     * @param outputFile
     * @param progress
     */
    public void downloadFileFromOssAsync(String bucketName, String objectkey, final File outputFile,
                                         OnDownloadProgress progress) {
        DownloadTask downloadTask = new DownloadTask(bucketName, objectkey, new DownloadTask.OssDownloadAdapter() {
            boolean cancel = false;

            @Override
            public boolean isCancel() {
                return cancel;
            }

            @Override
            public void onGetResult(InputStream input, long totalLength) {
                if (!outputFile.exists()) {
                    File parentFile = outputFile.getParentFile();
                    if (parentFile != null && !parentFile.exists()) {
                        parentFile.mkdirs();
                    }
                    try {
                        outputFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                String urlStr = bucketName + "_" + objectkey;
                OutputStream output = null;
                try {
                    output = new FileOutputStream(outputFile);
                    mDownLoadMap.put(urlStr, new DownloadParams(urlStr));
                    byte[] buffer = new byte[1024];
                    int count;
                    long downloaded = 0;
                    DownloadParams downloadParams = mDownLoadMap.get(urlStr);
                    while ((count = input.read(buffer)) != -1) {
                        output.write(buffer, 0, count);
                        if (downloadParams != null && downloadParams.hasCancel) {
                            // 下载取消了
                            cancel = true;
                            if (null != progress) {
                                progress.onCanceled();
                            }
                            mDownLoadMap.remove(urlStr);
                            return;
                        }
                        downloaded += count;
                        if (null != progress) {
                            progress.onDownloaded(downloaded, totalLength);
                        }
                    }
                    if (null != progress) {
                        progress.onCompleted(outputFile.getAbsolutePath());
                    }
                    mDownLoadMap.remove(urlStr);
                    return;
                } catch (Exception e) {
                    if (null != progress) {
                        progress.onFailed();
                    }
                } finally {
                    if (input != null) {
                        try {
                            input.close();
                        } catch (IOException e) {
                            //
                        }
                    }
                    if (output != null) {
                        try {
                            output.close();
                        } catch (IOException e) {
                            //
                        }
                    }
                }
                mDownLoadMap.remove(urlStr);
            }
        });
        downloadTask.downloadAsync();
    }

    /**
     * 唯一的下载接口，异步
     *
     * @param urlStr
     * @param outputFile
     * @param progress
     */
    public void downloadFileAsync(String urlStr, final File outputFile,
                                  boolean needTempFile, OnDownloadProgress progress) {
        io.reactivex.Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                downloadFileSync(urlStr, outputFile, needTempFile,progress);
                emitter.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    public long getFileLength(String urlStr) {
        long length = 0;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10 * 1000);
            conn.setReadTimeout(15 * 1000);
            HttpURLConnection.setFollowRedirects(true);
            conn.connect();
            length = conn.getContentLength();
        } catch (Exception e) {
            MyLog.e(TAG, "getFileLength error:" + e);
        } finally {

        }

        return length;
    }

    /**
     * 唯一的下载接口，同步
     *
     * @param urlStr
     * @param outputFile
     * @param needTempFile 是否生成一个下载中间temp文件，只有下载成功outputFile才有值
     * @param progress
     * @return
     */
    public boolean downloadFileSync(String urlStr, final File outputFile,
                                    boolean needTempFile, OnDownloadProgress progress) {
        MyLog.d(TAG,"downloadFileSync" + " urlStr="+urlStr+" out="+outputFile.getAbsolutePath());
        if (Looper.getMainLooper() == Looper.myLooper()) {
            throw new IllegalThreadStateException("cannot downloadFile on mainthread");
        }
        File outputFile2 = outputFile;
        if (needTempFile) {
            outputFile2 = new File(outputFile.getParentFile(), "temp" + outputFile.getName());
        }
        if (!outputFile2.exists()) {
            File parentFile = outputFile2.getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                parentFile.mkdirs();
            }
            try {
                outputFile2.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        InputStream input = null;
        OutputStream output = null;
        long startDownloadMs = System.currentTimeMillis();
        try {
            output = new FileOutputStream(outputFile2);
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10 * 1000);
            conn.setReadTimeout(15 * 1000);
            HttpURLConnection.setFollowRedirects(true);
            conn.connect();

            mDownLoadMap.put(urlStr, new DownloadParams(urlStr));

            input = conn.getInputStream();

            byte[] buffer = new byte[1024];
            int count;
            long downloaded = 0;
            long totalLength = conn.getContentLength();
            DownloadParams downloadParams = mDownLoadMap.get(urlStr);
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
                if (downloadParams != null && downloadParams.hasCancel) {
                    // 下载取消了
                    if (null != progress) {
                        progress.onCanceled();
                        // 下载取消打点
                        long cancelDownMs = System.currentTimeMillis();
                        HashMap param = new HashMap();
                        param.put("downUrl", urlStr);
                        StatisticsAdapter.recordCalculateEvent("download", "cancel", cancelDownMs - startDownloadMs, param);
                    }
                    mDownLoadMap.remove(urlStr);
                    return false;
                }
                downloaded += count;
                if (null != progress) {
                    progress.onDownloaded(downloaded, totalLength);
                }
                //Thread.sleep(1000);
            }
            if (needTempFile) {
                if (outputFile2.renameTo(outputFile)) {
                    MyLog.w(TAG, urlStr+" 下载成功");
                } else {
                    MyLog.w(TAG, "重命名失败");
                }
            }
            if (null != progress) {
                progress.onCompleted(outputFile.getAbsolutePath());
                // 下载完成打点
                long compleDownMs = System.currentTimeMillis();
                StatisticsAdapter.recordCalculateEvent("download", "success", compleDownMs - startDownloadMs, null);
            }
            mDownLoadMap.remove(urlStr);
            return true;
        } catch (Exception e) {
            if (null != progress) {
                progress.onFailed();
                // 下载失败打点
                long failDownMs = System.currentTimeMillis();
                HashMap param = new HashMap();
                param.put("downUrl", urlStr);
                param.put("exception", e.toString());
                StatisticsAdapter.recordCalculateEvent("download", "failed", failDownMs - startDownloadMs, param);
            }
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    //
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    //
                }
            }
        }
        mDownLoadMap.remove(urlStr);
        return false;
    }

    private static class DownloadParams {
        public String url;
        public boolean hasCancel = false;

        public DownloadParams(String url) {
            this.url = url;
        }
    }

    public interface OnDownloadProgress {
        void onDownloaded(long downloaded, long totalLength);

        void onCompleted(String localPath);

        // 这里 canceled 的 语义为 用户手动点击的已知的 停止，暂停，取消
        void onCanceled();

        void onFailed();
    }

    /**
     * This input stream won't read() after the underlying stream is exhausted.
     * http://code.google.com/p/android/issues/detail?id=14562
     */
    public final static class DoneHandlerInputStream extends FilterInputStream {
        private boolean done;

        public DoneHandlerInputStream(InputStream stream) {
            super(stream);
        }

        @Override
        public int read(byte[] bytes, int offset, int count) throws IOException {
            if (!done) {
                int result = super.read(bytes, offset, count);
                if (result != -1) {
                    return result;
                }
            }
            done = true;
            return -1;
        }
    }
}
