package com.wali.live.utils;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.Base64Coder;
import com.base.utils.StringUtils;
import com.base.utils.network.Network;
import com.base.utils.sdcard.SDCardUtils;
import com.base.utils.version.VersionManager;
import com.xiaomi.accountsdk.request.AccessDeniedException;
import com.xiaomi.accountsdk.request.AuthenticationFailureException;
import com.xiaomi.accountsdk.request.SimpleRequest;
import com.xiaomi.accountsdk.request.SimpleRequest.MapContent;
import com.xiaomi.accountsdk.request.SimpleRequest.StringContent;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpUtils {
    public static ExecutorService ONLINE_FILE_TASK_EXECUTOR = (ExecutorService) Executors
            .newSingleThreadExecutor();

    public static final int CONNECTION_TIMEOUT = 10 * 1000;

    public static final int READ_TIMEOUT = 15 * 1000;

    private static final String SALT_P1 = "8007236f-";

    private static final String SALT_P2 = "a2d6-4847-ac83-";

    private static final String SALT_P3 = "c49395ad6d65";

    private static String USER_AGENT = null;
    public static final int DOWNLOAD_STATE_CANCEL = 1;
    public static final int DOWNLOAD_STATE_FAILED = 2;
    public static final int DOWNLOAD_STATE_SUCCESS = 3;
    /**
     * 对象缓冲，避免每次都调用判断代码
     */
    private static Boolean is_cmcc;

    private static Boolean is_unicom;

    private static Boolean is_telcom;

    private static String UserAgent;


    private static String getOperator(Context context) {
        TelephonyManager telManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String operator = telManager.getSimOperator();
        return operator;
    }


    private static int getMnc(String numeric) {
        if (TextUtils.isEmpty(numeric)) {
            return -1;
        }
        if (numeric.length() < 5) {
            return -1;
        }
        int ret = -1;
        try {
            ret = Integer.parseInt(numeric.substring(numeric.length()
                    - (numeric.length() > 5 ? 3 : 2)));
        } catch (Exception e) {
        }
        return ret;
    }


    private static String joinMap(Map<String, String> map, String sp) {
        if (map == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        Set<Map.Entry<String, String>> entries = map.entrySet();
        int i = 0;
        for (Map.Entry<String, String> entry : entries) {
            if (i > 0) {
                sb.append(sp);
            }
            final String key = entry.getKey();
            final String value = entry.getValue();
            sb.append(key);
            sb.append("=");
            sb.append(value);
            i++;
        }
        return sb.toString();
    }

    public static MapContent doV2Post(String url, List<NameValuePair> pairs)
            throws IOException, AccessDeniedException,
            AuthenticationFailureException {
        final long before = System.currentTimeMillis();
        pairs.add(new BasicNameValuePair("delayTime", String.valueOf(before)));
        final String md5 = getKeyFromParams(pairs);
        pairs.add(new BasicNameValuePair("s", md5));
        return SimpleRequest.postAsMap(url, pairsToMap(pairs), null, true);
    }


    public static StringContent doV2PostAsString(String url, List<NameValuePair> pairs)
            throws IOException, AccessDeniedException,
            AuthenticationFailureException {
        final long before = System.currentTimeMillis();
        pairs.add(new BasicNameValuePair("delayTime", String.valueOf(before)));
        final String md5 = getKeyFromParams(pairs);
        pairs.add(new BasicNameValuePair("s", md5));
        return SimpleRequest.postAsString(url, pairsToMap(pairs), null, true);
    }

    public static StringContent doV2Get(String url, List<NameValuePair> pairs)
            throws IOException, AccessDeniedException,
            AuthenticationFailureException {
        final long before = System.currentTimeMillis();
        pairs.add(new BasicNameValuePair("delayTime", String.valueOf(before)));
        final String md5 = getKeyFromParams(pairs);
        pairs.add(new BasicNameValuePair("s", md5));
        StringContent content = SimpleRequest.getAsString(url,
                pairsToMap(pairs), null, true);
//        MyLog.info("http v2 get " + url + " result: " + content.toString());
        return content;
    }

    public static String getKeyFromParams(
            final List<NameValuePair> nameValuePairs) {
        Collections.sort(nameValuePairs, new Comparator<NameValuePair>() {

            @Override
            public int compare(final NameValuePair p1, final NameValuePair p2) {
                return p1.getName().compareTo(p2.getName());
            }
        });

        final StringBuilder keyBuilder = new StringBuilder();
        boolean isFirst = true;
        for (final NameValuePair nvp : nameValuePairs) {

            if (!isFirst) {
                keyBuilder.append("&");
            }

            keyBuilder.append(nvp.getName()).append("=").append(nvp.getValue());
            isFirst = false;
        }

        keyBuilder.append("&").append(SALT_P1);
        keyBuilder.append(SALT_P2);
        keyBuilder.append(SALT_P3);

        final String key = keyBuilder.toString();
        final byte[] keyBytes = StringUtils.getBytes(key);
        return StringUtils.getMd5Digest(new String(Base64Coder
                .encode(keyBytes)));
    }

    private static Map<String, String> pairsToMap(List<NameValuePair> pairs) {
        Map<String, String> map = new HashMap<String, String>();
        for (NameValuePair pair : pairs) {
            map.put(pair.getName(), pair.getValue());
        }
        return map;
    }

    public interface OnDownloadProgress {
        void onDownloaded(long downloaded, long totalLength);

        void onCompleted(String localPath);

        // 这里 canceled 的 语义为 用户手动点击的已知的 停止，暂停，取消
        void onCanceled();

        void onFailed();
    }

    public static boolean downloadFile(String urlStr, final File outputFile,
                                       OnDownloadProgress progress) {
        InputStream input = null;
        OutputStream output = null;
        try {
            output = new FileOutputStream(outputFile);
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            HttpURLConnection.setFollowRedirects(true);
            conn.connect();
            input = conn.getInputStream();

            byte[] buffer = new byte[1024];
            int count;
            long downloaded = 0;
            long totalLength = conn.getContentLength();
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
                downloaded += count;
                if(null != progress) {
                    progress.onDownloaded(downloaded, totalLength);
                }
            }
            if(null != progress) {
                progress.onCompleted(outputFile.getAbsolutePath());
            }
            return true;
        } catch (IOException e) {
//            VoipLog.e("error while download file" + e);

            if(null != progress) {
                progress.onFailed();
            }
        } catch (Throwable e) {
//            VoipLog.e("error while download file" + e);

            if(null != progress) {
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
        return false;
    }

    /**
     * copy com.xiaomi.accountsdk.utils.CloudCoder 的hashDeviceInfo函数
     * 与miui.cloud.CloudManager.getHashedDeviceId使用相同的SHA1算法，确保与方流统计组使用同样算法。
     * 增强了异常情况的处理。输入null时，返回""。 保证不会出现闪退。 *
     */
    public static String miuiSHA1(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            return android.util.Base64.encodeToString(
                    md.digest(plain.getBytes()), android.util.Base64.URL_SAFE)
                    .substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void setConnectionTimeout(final HttpURLConnection conn) {
        conn.setConnectTimeout(Network.CONNECTION_TIMEOUT);
        conn.setReadTimeout(Network.READ_TIMEOUT);
    }

    /**
     * 从指定的URL下载文件到输出文件。 如果outputFile大小是0字节，不会在http请求head中加入range字段。 如果将来需要返回更多的信心，这个方法可以考虑返回一个class，其中包含responseCode,
     * state, bytes等信息，而不是数组。
     *
     * @param append 如果是false，直接覆盖所outputFile中的内容。如果是true，将下载内容append在outputFile文件的后面 。
     * @return 一个包含两个元素的数组，第一个元素是http请求的返回值，第二个元素是下载的状态（DOWNLOAD_STATE_XXX）， 第三个元素是下载的字节数。不会返回null
     */
    public static DownloadResponse downloadFile(final Context context, final String resId, final String urlStr, final String setHost,
                                                final File outputFile,
                                                final OnDownloadProgress progress, final boolean checkCancel, boolean append) {
        if (SDCardUtils.isSDCardBusy() || !Network.hasNetwork(context)) {
            if (progress != null) {
                progress.onFailed();
            }
            return new DownloadResponse(0, DOWNLOAD_STATE_FAILED, 0, null);
        }

        boolean cancelled = false;
        boolean succeeded = false;

        int responseCode = 0;
        long downloadBytes = 0;

        FileOutputStream fos = null;
        InputStream input = null;

        int contentBytes = 0;

        final long time = System.currentTimeMillis();
        String oriHost = null;
        Exception failedException = null;
        try {
            URL url = new URL(urlStr);
            oriHost = url.getHost();
            HttpURLConnection.setFollowRedirects(true);
            HttpURLConnection conn = null;
            if (Network.isCmwap(context)) {
                final String cmwapUrl = Network.getCMWapUrl(url);
                url = new URL(cmwapUrl);
                oriHost = url.getHost();
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty(Network.CMWAP_HEADER_HOST_KEY, oriHost);
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }

            conn.setRequestProperty(Network.USER_AGENT, buildUserAgent(context));
            conn.setRequestProperty("Connection", "Keep-Alive");
            if (!TextUtils.isEmpty(setHost)) {
                conn.setRequestProperty("Host", setHost);
            }
            long range = outputFile.length();
            if (append) {
                // append操作，需要从当前file.size开始下载。
                if (range != 0) {
                    conn.setRequestProperty("Range", String.format("bytes=%d-", range));
                    MyLog.v("Range" + ":" + String.format("bytes=%d-", range));
                    downloadBytes = range;
                }
            }

            setConnectionTimeout(conn);
            conn.connect();
            responseCode = conn.getResponseCode();
            contentBytes = conn.getContentLength();
            MyLog.v("the response code is " + responseCode + ", connected in "
                    + (System.currentTimeMillis() - time));
//            ApiCallLog apiCallLog = new ApiCallLog("MFS:", (responseCode == 200) ? 0 : responseCode, url, time, System.currentTimeMillis(),
//                    url.toString().getBytes().length, contentBytes, 0);
//
//            LinkMonitor.getInstance().trace(context, apiCallLog);
            if (responseCode == -1) {

                return new DownloadResponse(
                        -1, DOWNLOAD_STATE_FAILED, 0, null);
            }
            if (append && (range > 0) && (responseCode != 206)) {
                MyLog.w("expected response code is 206 while actual code is " + responseCode
                        + " give up append");
                append = false;
                range = 0;
                downloadBytes = 0;
                // 下载失败的情况下，删除这个这个临时文件，避免引起下次下载时候会误以为需要在这个文件上断点续传。
                if (outputFile.exists() && outputFile.isFile()) {
                    outputFile.delete();
                }
            }

            // 严格的讲，content length是本次的字节数，不一定是真实read出来的字节数（可能被gzip）了。
            if (append) {
                contentBytes = conn.getContentLength() + (int) range;
            }
            if (progress != null) {
                progress.onDownloaded(range, contentBytes);
            }

            input = new Network.DoneHandlerInputStream(conn.getInputStream());
            MyLog.v("content bytes " + contentBytes);

            // 一般不需要这么大的缓存，这里是为了减小界面刷新频率。
            final byte[] buffer = new byte[10 * 1024];
            int count;
            fos = new FileOutputStream(outputFile, append);
            while ((count = input.read(buffer)) != -1) {
//                if (checkCancel && (networkCallback != null)
//                        && !networkCallback.continueDownloading(resId)) {
//                    cancelled = true;
//                    break;
//                }
                fos.write(buffer, 0, count);
                downloadBytes += count;
                if (progress != null) {
                    progress.onDownloaded(downloadBytes, contentBytes);
                }
            }

            if (!cancelled) {
                succeeded = true;
            }
        } catch (final IOException e) {
            failedException = e;
            MyLog.e("error to call url:" + urlStr + " error:" + e.getMessage(), e);
        } finally {
            final long cost = System.currentTimeMillis() - time;
            MyLog.v("http downloadFile to " + urlStr + " cost " + cost
                    + "ms, total size = " + outputFile.length());
            // if (!MLBuildSettings.IsDebugBuild) {
            // MyLog.warn("http download for url:" + urlStr);
            // }
            if (input != null) {
                try {
                    input.close();
                } catch (final IOException e) {
                    MyLog.e(e);
                }
            }

            if (fos != null) {
                try {
                    fos.close();
                } catch (final IOException e) {
                    MyLog.e(e);
                    // 文件下载结束，但是关闭输出流出错了，此时文件内容有误，当做下载失败。
                    succeeded = false;
                }
            }
        }

        if (progress != null) {
            if (cancelled) {
                progress.onCanceled();
            } else if (!succeeded) {
                progress.onFailed();
            } else {
                progress.onCompleted(outputFile.getAbsolutePath());
            }
        }
        return new DownloadResponse(responseCode,
                (cancelled ? DOWNLOAD_STATE_CANCEL : (succeeded ? DOWNLOAD_STATE_SUCCESS
                        : DOWNLOAD_STATE_FAILED)), (int) downloadBytes, failedException);
    }

    public synchronized static String buildUserAgent(Context context) {
        if (USER_AGENT == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("MiLive ");
            sb.append(VersionManager.getCurrentVersionCode(GlobalData.app()));
            sb.append(" (");
            sb.append(XMStringUtils.join(new String[]{
                    Build.MODEL, String.valueOf(Build.VERSION.SDK_INT), Locale.getDefault().getLanguage()
            }, ";"));
            sb.append(") ");
            sb.append(Build.VERSION.RELEASE);
            USER_AGENT = sb.toString();
        }
        return USER_AGENT;
    }

    public static class DownloadResponse {

        public static final int dontAllowCallOnFailureTimesLimit = -2;
        public int responseCode;
        public int result;
        public int downloadBytes;
        public Exception e;

        public DownloadResponse(int responseCode, int result, int downloadBytes, Exception e) {
            this.responseCode = responseCode;
            this.result = result;
            this.downloadBytes = downloadBytes;
            this.e = e;
        }
    }

}
