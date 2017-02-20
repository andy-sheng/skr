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
 * 推拉流统计
 * Created by yangli on 16-7-6.
 */
class StreamFactory {

    private static final String KEY_PUSH_STREAM = "push_stream";
    private static final String KEY_PULL_STREAM = "pull_stream";
    private static final String KEY_FIRST_FRAME_DELAY = "first_frame_delay";

    /** 推流成功/失败 */
    public static Observable<Boolean> createReportPushStream(
            final String userId,
            final String streamUrl,
            final String streamDomain,
            final String streamIp,
            final int status,
            final int errCode,
            final String errMsg,
            final IReporter reporter,
            ExecutorService executor
    ) {
        return Observable.create(new rx.Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                final String myUserId = !TextUtils.isEmpty(userId) ? userId : UserAccountManager.getInstance().getUuid();
                if (TextUtils.isEmpty(myUserId) || reporter == null) {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onError(new Exception("invalid input"));
                    }
                    return;
                }
                try {
                    StatReport.StatInfo.Builder builder = StatReport.StatInfo.newBuilder();
                    builder.setName(KEY_PUSH_STREAM)
                            .addItem(StatReport.StatItem.newBuilder().setKey(ReportProtocol.KEY_USER_ID).setVal(myUserId))
                            .addItem(StatReport.StatItem.newBuilder().setKey(ReportProtocol.KEY_STREAM_URL).setVal(streamUrl))
                            .addItem(StatReport.StatItem.newBuilder().setKey(ReportProtocol.KEY_STREAM_DOMAIN).setVal(streamDomain))
                            .addItem(StatReport.StatItem.newBuilder().setKey(ReportProtocol.KEY_STREAM_IP).setVal(streamIp))
                            .addItem(StatReport.StatItem.newBuilder().setKey(ReportProtocol.KEY_STATUS).setVal(String.valueOf(status)))
                            .addItem(StatReport.StatItem.newBuilder().setKey(ReportProtocol.KEY_ERR_CODE).setVal(String.valueOf(errCode)))
                            .addItem(StatReport.StatItem.newBuilder().setKey(ReportProtocol.KEY_ERR_MSG).setVal(errMsg));
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

    /** 拉流成功/失败 */
    public static Observable<Boolean> createReportPullStream(
            final String userId,
            final String anchorId,
            final String streamUrl,
            final String streamDomain,
            final String streamIp,
            final int status,
            final int errCode,
            final String errMsg,
            final IReporter reporter,
            ExecutorService executor
    ) {
        return Observable.create(new rx.Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                final String myUserId = !TextUtils.isEmpty(userId) ? userId : UserAccountManager.getInstance().getUuid();
                if (TextUtils.isEmpty(myUserId) || reporter == null) {
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onError(new Exception("invalid input"));
                    }
                    return;
                }
                try {
                    StatReport.StatInfo.Builder builder = StatReport.StatInfo.newBuilder();
                    builder.setName(KEY_PULL_STREAM)
                            .addItem(StatReport.StatItem.newBuilder().setKey(ReportProtocol.KEY_USER_ID).setVal(myUserId))
                            .addItem(StatReport.StatItem.newBuilder().setKey(ReportProtocol.KEY_ANCHOR_ID).setVal(anchorId))
                            .addItem(StatReport.StatItem.newBuilder().setKey(ReportProtocol.KEY_STREAM_URL).setVal(streamUrl))
                            .addItem(StatReport.StatItem.newBuilder().setKey(ReportProtocol.KEY_STREAM_DOMAIN).setVal(streamDomain))
                            .addItem(StatReport.StatItem.newBuilder().setKey(ReportProtocol.KEY_STREAM_IP).setVal(streamIp))
                            .addItem(StatReport.StatItem.newBuilder().setKey(ReportProtocol.KEY_STATUS).setVal(String.valueOf(status)))
                            .addItem(StatReport.StatItem.newBuilder().setKey(ReportProtocol.KEY_ERR_CODE).setVal(String.valueOf(errCode)))
                            .addItem(StatReport.StatItem.newBuilder().setKey(ReportProtocol.KEY_ERR_MSG).setVal(errMsg));
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

    /** 首帧延迟 */
    public static Observable<Boolean> createReportFirstFrameDelay(
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
                        subscriber.onError(new Exception("invalid input"));
                    }
                    return;
                }
                try {
                    StatReport.StatInfo.Builder builder = StatReport.StatInfo.newBuilder();
                    builder.setName(KEY_FIRST_FRAME_DELAY)
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

}
