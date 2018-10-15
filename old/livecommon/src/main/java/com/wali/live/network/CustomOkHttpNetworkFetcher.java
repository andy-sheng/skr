package com.wali.live.network;

import android.net.Uri;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;

import com.base.log.MyLog;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpNetworkFetcher;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.producers.BaseNetworkFetcher;
import com.facebook.imagepipeline.producers.BaseProducerContextCallbacks;
import com.facebook.imagepipeline.producers.Consumer;
import com.facebook.imagepipeline.producers.FetchState;
import com.facebook.imagepipeline.producers.ProducerContext;

import com.wali.live.statistics.StatisticUtils;
import com.wali.live.statistics.StatisticsKey;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @module OkHttp的图片加载
 */
public class CustomOkHttpNetworkFetcher extends BaseNetworkFetcher<OkHttpNetworkFetcher.OkHttpNetworkFetchState> {
    private static final String TAG = "CustomOkHttpNetworkFetcher";
    private static final String QUEUE_TIME = "queue_time";
    private static final String FETCH_TIME = "fetch_time";
    private static final String TOTAL_TIME = "total_time";
    private static final String IMAGE_SIZE = "image_size";
    private final OkHttpClient mOkHttpClient;
    private Executor mCancellationExecutor;

    public CustomOkHttpNetworkFetcher(OkHttpClient okHttpClient) {
        this.mOkHttpClient = okHttpClient;
        this.mCancellationExecutor = okHttpClient.dispatcher().executorService();
    }

    public OkHttpNetworkFetcher.OkHttpNetworkFetchState createFetchState(Consumer<EncodedImage> consumer, ProducerContext context) {
        return new OkHttpNetworkFetcher.OkHttpNetworkFetchState(consumer, context);
    }

    public void fetch(final OkHttpNetworkFetcher.OkHttpNetworkFetchState fetchState, final Callback callback) {
        fetchState.submitTime = SystemClock.elapsedRealtime();
        Uri uri = fetchState.getUri();
        String url = uri.toString();
        work(fetchState, callback, url, 0);
    }

    private void work(final OkHttpNetworkFetcher.OkHttpNetworkFetchState fetchState, final Callback callback, final String url, final int index) {
        MyLog.d(TAG, "当前进程名1:" + Thread.currentThread().getName() + "url:" + url);
        String host = Uri.parse(url).getHost();
        String urlWithIp = url;
        if (index == 0) {
            urlWithIp = ImageUrlDNSManager.getAvailableUrl(url);
        } else {
            String nextUrl = ImageUrlDNSManager.getNextAvailableUrl(url, index);
            if (TextUtils.isEmpty(nextUrl)) {
                urlWithIp = url;
            }
        }
        MyLog.d(TAG, "当前进程名2:" + Thread.currentThread().getName() + "urlWithIp:" + urlWithIp);
        Request request = (new Request.Builder()).cacheControl((new CacheControl.Builder()).noStore().build())
                .header("host", host)
                .url(urlWithIp).get()
                .build();
        final Call call = this.mOkHttpClient.newCall(request);
        fetchState.getContext().addCallbacks(new BaseProducerContextCallbacks() {
            public void onCancellationRequested() {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    call.cancel();
                } else {
                    CustomOkHttpNetworkFetcher.this.mCancellationExecutor.execute(new Runnable() {
                        public void run() {
                            call.cancel();
                        }
                    });
                }
            }
        });
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 如果有保底ip，再尝试
                String ip = ImageUrlDNSManager.getNextAvailableUrl(url, index + 1);
                if (!TextUtils.isEmpty(ip)) {
                    work(fetchState, callback, url, index + 1);
                } else {
                    StatisticUtils.addToMiLinkMonitor(StatisticsKey.KEY_DOWNLOAD_IMG, StatisticUtils.FAILED, url + ":" + e.getMessage());
                    CustomOkHttpNetworkFetcher.this.handleException(call, e, callback);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                fetchState.responseTime = SystemClock.elapsedRealtime();
                ResponseBody body = response.body();
                try {
                    MyLog.d(TAG, "onResponse 当前进程名:" + Thread.currentThread().getName() + ", ResponseBody=" + body.toString());
                    long e = body.contentLength();
                    if (e < 0L) {
                        e = 0L;
                    }
                    String nextUriString = response.header("Location");//处理302跳转
                    String nextScheme = nextUriString == null ? null : Uri.parse(nextUriString).getScheme();
                    if (nextUriString == null || nextScheme == null) {
                        callback.onResponse(body.byteStream(), (int) e);
                        StatisticUtils.addToMiLinkMonitor(StatisticsKey.KEY_DOWNLOAD_IMG, StatisticUtils.SUCCESS);
                    } else {
                        MyLog.w(TAG, "download with 302 nextUriString=" + nextUriString);
                        work(fetchState, callback, nextUriString, 0);
                    }
                } catch (Exception var13) {
                    // 如果有保底ip，再尝试
                    String ip = ImageUrlDNSManager.getNextAvailableUrl(url, index + 1);
                    if (!TextUtils.isEmpty(ip)) {
                        work(fetchState, callback, url, index + 1);
                    } else {
                        CustomOkHttpNetworkFetcher.this.handleException(call, var13, callback);
                        StatisticUtils.addToMiLinkMonitor(StatisticsKey.KEY_DOWNLOAD_IMG, StatisticUtils.FAILED, url + ":" + var13.getMessage());
                    }
                } finally {
                    try {
                        body.close();
                    } catch (Exception var12) {
                        MyLog.w("OkHttpNetworkFetchProducer", "Exception when closing response body", var12);
                    }
                }
            }
        });
    }

    public void onFetchCompletion(OkHttpNetworkFetcher.OkHttpNetworkFetchState fetchState, int byteSize) {
        fetchState.fetchCompleteTime = SystemClock.elapsedRealtime();
    }

    public Map<String, String> getExtraMap(OkHttpNetworkFetcher.OkHttpNetworkFetchState fetchState, int byteSize) {
        HashMap extraMap = new HashMap(4);
        extraMap.put("queue_time", Long.toString(fetchState.responseTime - fetchState.submitTime));
        extraMap.put("fetch_time", Long.toString(fetchState.fetchCompleteTime - fetchState.responseTime));
        extraMap.put("total_time", Long.toString(fetchState.fetchCompleteTime - fetchState.submitTime));
        extraMap.put("image_size", Integer.toString(byteSize));
        return extraMap;
    }

    private void handleException(Call call, Exception e, Callback callback) {
        MyLog.d(TAG, "handleException 当前进程名:" + Thread.currentThread().getName());
        if (call.isCanceled()) {
            callback.onCancellation();
        } else {
            callback.onFailure(e);
        }

    }

    public static class OkHttpNetworkFetchState extends FetchState {
        public long submitTime;
        public long responseTime;
        public long fetchCompleteTime;

        public OkHttpNetworkFetchState(Consumer<EncodedImage> consumer, ProducerContext producerContext) {
            super(consumer, producerContext);
        }
    }
}
