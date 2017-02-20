package com.mi.live.data.report;

import com.base.log.MyLog;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;

import java.util.concurrent.ExecutorService;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * @module MiLink打点
 * <p>
 * Created by yangli on 16-7-6.
 */
public class StatReportController implements IStatReport, IReporter {
    private static final String TAG = "StatReportController";

    private ExecutorService mReportExecutor;

    public StatReportController() {
        mReportExecutor = null;
    }

    public StatReportController(ExecutorService executorService) {
        mReportExecutor = executorService;
    }

    @Override
    public boolean reportByMiLink(byte[] data) {
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_STAT_REPORT);
        packetData.setData(data);
        PacketData response = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
        return response != null && response.getMnsCode() == 0;
    }

    @Override
    public Subscription reportPushStream(
            String userId,
            String streamUrl,
            String streamDomain,
            String streamIp,
            int status,
            int errCode,
            String errMsg
    ) {
        MyLog.w(TAG, "reportPushStream errCode=" + errCode + ", errMsg: " + errMsg);
        return StreamFactory.createReportPushStream(userId, streamUrl, streamDomain,
                streamIp, status, errCode, errMsg, this, mReportExecutor)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        MyLog.w(TAG, "reportPushStream " + (result ? "success" : "failed"));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "reportPushStream, exception=" + throwable);
                    }
                });
    }

    @Override
    public Subscription reportPullStream(
            String userId,
            String anchorId,
            String streamUrl,
            String streamDomain,
            String streamIp,
            int status,
            int errCode,
            String errMsg
    ) {
        MyLog.w(TAG, "reportPullStream errCode=" + errCode + ", errMsg: " + errMsg);
        return StreamFactory.createReportPullStream(userId, anchorId, streamUrl, streamDomain,
                streamIp, status, errCode, errMsg, this, mReportExecutor)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        MyLog.w(TAG, "reportPullStream " + (result ? "success" : "failed"));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "reportPullStream, exception=" + throwable);
                    }
                });
    }

    @Override
    public Subscription reportFirstFrameDelay(String userId, String streamUrl, int delay, long timeStamp) {
        MyLog.w(TAG, "reportFirstFrameDelay delay=" + delay);
        return StreamFactory.createReportFirstFrameDelay(userId, streamUrl, delay, timeStamp, this, mReportExecutor)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        MyLog.w(TAG, "reportFirstFrameDelay " + (result ? "success" : "failed"));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "reportFirstFrameDelay, exception=" + throwable);
                    }
                });
    }

    @Override
    public Subscription reportPushStutter(String userId, String streamUrl, int delay, long timeStamp) {
        MyLog.w(TAG, "reportPushStutter delay=" + delay);
        return StutterFactory.createReportPushStutter(userId, streamUrl, delay, timeStamp, this, mReportExecutor)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        MyLog.w(TAG, "reportPushStutter " + (result ? "success" : "failed"));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "reportPushStutter, exception=" + throwable);
                    }
                });
    }

    @Override
    public Subscription reportPullStutter(String userId, String anchorId, String streamUrl, int delay, long timeStamp) {
        MyLog.w(TAG, "reportPullStutter delay=" + delay);
        return StutterFactory.createReportPullStutter(userId, anchorId, streamUrl, delay, timeStamp, this, mReportExecutor)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        MyLog.w(TAG, "reportPullStutter " + (result ? "success" : "failed"));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "reportPullStutter, exception=" + throwable);
                    }
                });
    }

    @Override
    public Subscription reportPlaybackStutter(String userId, String anchorId, String streamUrl, int delay, long timeStamp) {
        MyLog.w(TAG, "reportPlaybackStutter delay=" + delay);
        return StutterFactory.createReportPlaybackStutter(userId, anchorId, streamUrl, delay, timeStamp, this, mReportExecutor)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        MyLog.w(TAG, "reportPlaybackStutter " + (result ? "success" : "failed"));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "reportPlaybackStutter, exception=" + throwable);
                    }
                });
    }

    @Override
    public Subscription reportSeekDelay(String userId, String anchorId, String streamUrl, int delay, long timeStamp) {
        MyLog.w(TAG, "reportSeekDelay delay=" + delay);
        return StutterFactory.createReportSeekDelayStutter(userId, anchorId, streamUrl, delay, timeStamp, this, mReportExecutor)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        MyLog.w(TAG, "reportSeekDelay " + (result ? "success" : "failed"));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "reportSeekDelay, exception=" + throwable);
                    }
                });
    }

    @Override
    public Subscription reportPushRate(String userId, String streamUrl, String rate, long timeStamp) {
        MyLog.w(TAG, "reportPushRate rate: \"" + rate + "\"");
        return RateFactory.createReportPushRate(userId, streamUrl, rate, timeStamp, this, mReportExecutor)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        MyLog.w(TAG, "reportPushRate " + (result ? "success" : "failed"));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "reportPushRate, exception=" + throwable);
                    }
                });
    }

    @Override
    public Subscription reportPullRate(String userId, String streamUrl, String rate, long timeStamp) {
        MyLog.w(TAG, "reportPullRate rate: \"" + rate + "\"");
        return RateFactory.createReportPullRate(userId, streamUrl, rate, timeStamp, this, mReportExecutor)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        MyLog.w(TAG, "reportPullRate " + (result ? "success" : "failed"));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "reportPullRate, exception=" + throwable);
                    }
                });
    }
}
