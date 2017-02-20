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
 * 码率统计
 * Created by yangli on 16-7-6.
 */
public class RateFactory {

    private static final String KEY_PUSH_RATE = "push_rate";
    private static final String KEY_PULL_RATE = "pull_rate";

    private static Observable<Boolean> createRateObservable(
            final String userId,
            final String streamUrl,
            final String rate,
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
                            .addItem(StatReport.StatItem.newBuilder().setKey(ReportProtocol.KEY_STREAM_URL).setVal(streamUrl))
                            .addItem(StatReport.StatItem.newBuilder().setKey(ReportProtocol.KEY_RATE).setVal(rate))
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

    /** 推流码率上报 */
    public static Observable<Boolean> createReportPushRate(
            String userId,
            String streamUrl,
            String rate,
            long timeStamp,
            IReporter reporter,
            ExecutorService executor
    ) {
        return createRateObservable(userId, streamUrl, rate, timeStamp, reporter, executor, KEY_PUSH_RATE);
    }

    /** 拉流码率上报 */
    public static Observable<Boolean> createReportPullRate(
            String userId,
            String streamUrl,
            String rate,
            long timeStamp,
            IReporter reporter,
            ExecutorService executor
    ) {
        return createRateObservable(userId, streamUrl, rate, timeStamp, reporter, executor, KEY_PULL_RATE);
    }

}
