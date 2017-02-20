package com.mi.live.data.report;

import rx.Subscription;

/**
 * Created by yangli on 16-7-6.
 */
public interface IStatReport {
    Subscription reportPushStream(
            String userId,
            String streamUrl,
            String streamDomain,
            String streamIp,
            int status,
            int errCode,
            String errMsg);

    Subscription reportPullStream(
            String userId,
            String anchorId,
            String streamUrl,
            String streamDomain,
            String streamIp,
            int status,
            int errCode,
            String errMsg
    );

    Subscription reportFirstFrameDelay(
            String userId,
            String streamUrl,
            int delay,
            long timeStamp);

    Subscription reportPushStutter(
            String userId,
            String streamUrl,
            int delay,
            long timeStamp);

    Subscription reportPullStutter(
            String userId,
            String anchorId,
            String streamUrl,
            int delay,
            long timeStamp);

    Subscription reportPlaybackStutter(
            String userId,
            String anchorId,
            String streamUrl,
            int delay,
            long timeStamp);

    Subscription reportSeekDelay(
            String userId,
            String anchorId,
            String streamUrl,
            int delay,
            long timeStamp);

    Subscription reportPushRate(
            String userId,
            String streamUrl,
            String rate,
            long timeStamp);

    Subscription reportPullRate(
            String userId,
            String streamUrl,
            String rate,
            long timeStamp);
}
