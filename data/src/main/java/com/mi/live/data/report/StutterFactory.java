package com.mi.live.data.report;

import android.text.TextUtils;

import com.mi.live.data.account.UserAccountManager;
import com.wali.live.proto.StatReport;

import java.util.concurrent.ExecutorService;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * @module MiLink打点
 * 卡顿统计
 * Created by yangli on 16-7-6.
 */
public class StutterFactory {

    private static final String KEY_PUSH_STUTTER = "push_stutter";
    private static final String KEY_PULL_STUTTER = "pull_stutter";
    private static final String KEY_PLAYBACK_STUTTER = "playback_stutter";
    private static final String KEY_SEEK_DELAY = "seek_delay";

    /** 推流卡顿 */
    public static Observable<Boolean> createReportPushStutter(
            final String userId,
            final String streamUrl,
            final int delay,
            final long timeStamp,
            final IReporter reporter,
            ExecutorService executor
    ) {
        return Observable.create(new rx.Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                final String myUserId = !TextUtils.isEmpty(userId) ? userId : UserAccountManager.getInstance().getUuid();
                if (TextUtils.isEmpty(myUserId) || reporter == null) {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onError(new Throwable("invalid input"));
                    }
                    return;
                }
                try {
                    StatReport.StatInfo.Builder builder = StatReport.StatInfo.newBuilder();
                    builder.setName(KEY_PUSH_STUTTER)
                            .addItem(StatReport.StatItem.newBuilder().setKey(ReportProtocol.KEY_USER_ID).setVal(myUserId))
                            .addItem(StatReport.StatItem.newBuilder().setKey(ReportProtocol.KEY_STREAM_URL).setVal(streamUrl))
                            .addItem(StatReport.StatItem.newBuilder().setKey(ReportProtocol.KEY_DELAY).setVal(String.valueOf(delay)))
                            .addItem(StatReport.StatItem.newBuilder().setKey(ReportProtocol.KEY_TIME).setVal(String.valueOf(timeStamp)));
                    StatReport.StatInfo statInfo = builder.build();
                    boolean result = reporter.reportByMiLink(statInfo.toByteArray());
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(result);
                    }
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onCompleted();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onError(e);
                    }
                }
            }
        }).subscribeOn(executor != null ? Schedulers.from(executor) : Schedulers.immediate());
    }

    private static Observable<Boolean> createStutterObservable(
            final String userId,
            final String anchorId,
            final String streamUrl,
            final int delay,
            final long timeStamp,
            final IReporter reporter,
            ExecutorService executor,
            final String reportType
    ) {
        return Observable.create(new rx.Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                final String myUserId = !TextUtils.isEmpty(userId) ? userId : UserAccountManager.getInstance().getUuid();
                if (TextUtils.isEmpty(myUserId) || reporter == null) {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onError(new Throwable("invalid input"));
                    }
                    return;
                }
                try {
                    StatReport.StatInfo.Builder builder = StatReport.StatInfo.newBuilder();
                    builder.setName(reportType)
                            .addItem(StatReport.StatItem.newBuilder().setKey(ReportProtocol.KEY_USER_ID).setVal(myUserId))
                            .addItem(StatReport.StatItem.newBuilder().setKey(ReportProtocol.KEY_ANCHOR_ID).setVal(anchorId))
                            .addItem(StatReport.StatItem.newBuilder().setKey(ReportProtocol.KEY_STREAM_URL).setVal(streamUrl))
                            .addItem(StatReport.StatItem.newBuilder().setKey(ReportProtocol.KEY_DELAY).setVal(String.valueOf(delay)))
                            .addItem(StatReport.StatItem.newBuilder().setKey(ReportProtocol.KEY_TIME).setVal(String.valueOf(timeStamp)));
                    StatReport.StatInfo statInfo = builder.build();
                    boolean result = reporter.reportByMiLink(statInfo.toByteArray());
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onNext(result);
                    }
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onCompleted();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onError(e);
                    }
                }
            }
        }).subscribeOn(executor != null ? Schedulers.from(executor) : Schedulers.immediate());
    }

    /** 拉流卡顿 */
    public static Observable<Boolean> createReportPullStutter(
            String userId,
            String anchorId,
            String streamUrl,
            int delay,
            long timeStamp,
            IReporter reporter,
            ExecutorService executor
    ) {
        return createStutterObservable(userId, anchorId, streamUrl, delay, timeStamp, reporter, executor, KEY_PULL_STUTTER);
    }

    /** 回放卡顿 */
    public static Observable<Boolean> createReportPlaybackStutter(
            String userId,
            String anchorId,
            String streamUrl,
            int delay,
            long timeStamp,
            IReporter reporter,
            ExecutorService executor
    ) {
        return createStutterObservable(userId, anchorId, streamUrl, delay, timeStamp, reporter, executor, KEY_PLAYBACK_STUTTER);
    }

    /** seek耗时 */
    public static Observable<Boolean> createReportSeekDelayStutter(
            String userId,
            String anchorId,
            String streamUrl,
            int delay,
            long timeStamp,
            IReporter reporter,
            ExecutorService executor
    ) {
        return createStutterObservable(userId, anchorId, streamUrl, delay, timeStamp, reporter, executor, KEY_SEEK_DELAY);
    }
}
