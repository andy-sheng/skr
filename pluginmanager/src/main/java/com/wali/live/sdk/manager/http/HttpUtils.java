package com.wali.live.sdk.manager.http;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.wali.live.sdk.manager.http.SimpleRequest.StringContent;
import com.wali.live.sdk.manager.http.bean.BasicNameValuePair;
import com.wali.live.sdk.manager.http.bean.NameValuePair;
import com.wali.live.sdk.manager.http.exception.AccessDeniedException;
import com.wali.live.sdk.manager.http.exception.AuthenticationFailureException;
import com.wali.live.sdk.manager.http.utils.Base64Coder;
import com.wali.live.sdk.manager.http.utils.StringUtils;

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

    public interface OnDownloadProgress {
        void onDownloaded(long downloaded, long totalLength);

        void onCompleted(String localPath);

        // 这里 canceled 的 语义为 用户手动点击的已知的 停止，暂停，取消
        void onCanceled();

        void onFailed();
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

    public static StringContent doV2Get(String url, List<NameValuePair> pairs)
            throws IOException, AccessDeniedException,
            AuthenticationFailureException {
        final long before = System.currentTimeMillis();
        pairs.add(new BasicNameValuePair("delayTime", String.valueOf(before)));
        final String md5 = getKeyFromParams(pairs);
        pairs.add(new BasicNameValuePair("s", md5));
        StringContent content = SimpleRequest.getAsString(url,
                pairsToMap(pairs), null, true);
        return content;
    }


    public static boolean downloadFile(String urlStr, final File outputFile,
                                       OnDownloadProgress progress) {
        if (!outputFile.exists()) {
            try {
                outputFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
                if (null != progress) {
                    progress.onDownloaded(downloaded, totalLength);
                }
            }
            if (null != progress) {
                progress.onCompleted(outputFile.getAbsolutePath());
            }
            return true;
        } catch (IOException e) {
            if (null != progress) {
                progress.onFailed();
            }
        } catch (Throwable e) {
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
