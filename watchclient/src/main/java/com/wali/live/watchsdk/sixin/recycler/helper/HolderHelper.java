package com.wali.live.watchsdk.sixin.recycler.helper;

import com.base.log.MyLog;
import com.wali.live.dao.SixinMessage;
import com.wali.live.watchsdk.sixin.cache.SendingMessageCache;
import com.wali.live.watchsdk.sixin.constant.SixinConstants;
import com.wali.live.watchsdk.sixin.data.SixinMessageCloudStore;
import com.wali.live.watchsdk.sixin.data.SixinMessageLocalStore;

/**
 * Created by lan on 2017/10/31.
 */
public class HolderHelper {
    private static final String TAG = SixinConstants.LOG_PREFIX + HolderHelper.class.getSimpleName();

    public static void resendMessage(long msgId) {
        if (msgId > 0) {
            SixinMessage sixinMessage = SixinMessageLocalStore.getSixinMessageByMsgId(msgId);
            if (sixinMessage != null
                    && null != sixinMessage.getSender()
                    && null != sixinMessage.getMsgTyppe()
                    && null != sixinMessage.getSenderMsgId()) {

                switch (sixinMessage.getMsgTyppe()) {
                    case SixinMessage.S_MSG_TYPE_TEXT:
                        SendingMessageCache.put(msgId, System.currentTimeMillis());
                        new SixinMessageCloudStore().send(sixinMessage);
                        break;
                    default:
                        MyLog.e(TAG, "resendMessage but sixinMessage type is illegal");
                        break;
                }
            } else {
                MyLog.e(TAG, "resendMessage but sixinMessage is null or illegal");
            }
        }
    }
}
