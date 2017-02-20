package com.wali.live.network;

import android.net.Uri;
import android.text.TextUtils;

import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.producers.BaseNetworkFetcher;
import com.facebook.imagepipeline.producers.BaseProducerContextCallbacks;
import com.facebook.imagepipeline.producers.Consumer;
import com.facebook.imagepipeline.producers.FetchState;
import com.facebook.imagepipeline.producers.NetworkFetcher;
import com.facebook.imagepipeline.producers.ProducerContext;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.wali.live.statistics.StatisticUtils;
import com.wali.live.statistics.StatisticsKey;
import com.base.utils.network.Network;
import com.wali.live.utils.HttpUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by linjinbin on 16/8/29.
 *
 * @module 图片下载
 */
public class MiLiveNetworkFetcher extends BaseNetworkFetcher<FetchState> {
    private static final int NUM_NETWORK_THREADS = 5;
    private final ExecutorService mExecutorService = Executors.newFixedThreadPool(5);
    private boolean isPreferUseHttpDNS = false;
    private int mHttpDNSFailureTimes = 0;
    private int mDNSPODFailureTimes = 0;

    public MiLiveNetworkFetcher() {

    }

    public FetchState createFetchState(Consumer<EncodedImage> consumer, ProducerContext context) {
        return new FetchState(consumer, context);
    }

    public void fetch(final FetchState fetchState, final NetworkFetcher.Callback callback) {
        final Future future = this.mExecutorService.submit(new Runnable() {
            public void run() {
                HttpURLConnection connection = null;
                Uri uri = fetchState.getUri();
                String scheme = uri.getScheme();
                String uriString = uri.toString();
                String host = "";
                URL url = null;
                boolean isStatisticFailure = true;
                while (true) {
                    try {
                        url = new URL(uriString);
                        host = url.getHost();
                        String urlWithIp = ImageUrlDNSManager.getAvailableUrl(uriString);//先用ip下载

                        if (connection != null) {
                            connection.disconnect();
                            connection = null;
                        }
                        if (!uriString.equals(urlWithIp)) {
                            url = new URL(urlWithIp);
                        }
                        connection = (HttpURLConnection) url.openConnection();
                        setConnectionTimeout(connection);
                        if (!TextUtils.isEmpty(host) && !urlWithIp.contains(host)) {
                            connection.setRequestProperty("Host", host);
                        }
                        connection.setRequestProperty(Network.USER_AGENT, HttpUtils.buildUserAgent(GlobalData.app()));
                        connection.setRequestProperty("Connection", "Keep-Alive");
                        //connection.setRequestProperty("Connection", "close");
                        String nextUriString = connection.getHeaderField("Location");//处理302跳转
                        String nextScheme = nextUriString == null ? null : Uri.parse(nextUriString).getScheme();
                        if (nextUriString == null || nextScheme.equals(scheme)) {
                            InputStream is = connection.getInputStream();
                            callback.onResponse(is, -1);
                            isStatisticFailure = false;
                            break;
                        }

                        uriString = nextUriString;
                        scheme = nextScheme;
                    } catch (Exception ex1) {

                        if (Network.hasNetwork(GlobalData.app())) {

                            String nextUrl;
                            nextUrl = uriString;//直接用原始url下载
                            if (connection != null) {
                                connection.disconnect();
                                connection = null;
                            }
                            try {
                                url = new URL(nextUrl);
                                connection = (HttpURLConnection) url.openConnection();
                                setConnectionTimeout(connection);
                                connection.setRequestProperty(Network.USER_AGENT, HttpUtils.buildUserAgent(GlobalData.app()));
                                connection.setRequestProperty("Connection", "Keep-Alive");

                                if (!TextUtils.isEmpty(host) && !nextUrl.contains(host)) {
                                    connection.setRequestProperty("Host", host);
                                }
                                String nextUriString = connection.getHeaderField("Location");
                                String nextScheme = nextUriString == null ? null : Uri.parse(nextUriString).getScheme();
                                if (nextUriString == null || nextScheme.equals(scheme)) {
                                    InputStream is = connection.getInputStream();
                                    callback.onResponse(is, -1);
                                    isStatisticFailure = false;
                                    break;
                                }
                                uriString = nextUriString;
                                scheme = nextScheme;
                            } catch (Exception ex) {

                                MyLog.w("Fresco MLNetWorkFetcher fetch failed Exception=" + ex.toString()
                                        + ", failed nextUrl=" + nextUrl);
                                callback.onFailure(ex);
                                break;
                            }
                        } else {
                            callback.onFailure(ex1);
                            isStatisticFailure = false;
                            break;
                        }
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                            connection = null;
                        }

                    }
                }
                StatisticUtils.addToMiLinkMonitor(StatisticsKey.KEY_DOWNLOAD_IMG, isStatisticFailure ? StatisticUtils.FAILED : StatisticUtils.SUCCESS);
            }
        });
        fetchState.getContext().addCallbacks(new BaseProducerContextCallbacks() {
            public void onCancellationRequested() {
                if (future.cancel(false)) {
                    callback.onCancellation();
                }
            }
        });
    }

    public static void setConnectionTimeout(final HttpURLConnection conn) {
        conn.setConnectTimeout(Network.CONNECTION_TIMEOUT);
        conn.setReadTimeout(Network.READ_TIMEOUT);
    }
}
