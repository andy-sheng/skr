package com.wali.live.watchsdk.log;

import android.os.Message;

import com.base.preference.PreferenceUtils;
import com.mi.live.data.milink.callback.MiLinkPacketDispatcher;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.milink.sdk.aidl.PacketData;
import com.mi.milink.sdk.base.CustomHandlerThread;
import com.mi.milink.sdk.proto.DataExtraProto;
import com.wali.live.watchsdk.init.InitManager;

/**
 * LogHandler是接到MnsCommand.COMMAND_VOIP_AUTO_UPLOAD_LOG命令之后，上传日志
 * Created by yaojian on 15-5-5.
 */
public class LogHandler implements MiLinkPacketDispatcher.PacketDataHandler {

    private final static String TAG = "LogHandler";

    public static final String SP_KEY_LOG_REVERT_TS = "keyLogRevertTs";       //日志

    private int mUploadTimes = 0;

    private final static int MSG_CHECK_REVERT_LOG_LEVEL = 101;      //检查
    private CustomHandlerThread mCustomHandlerThread = new CustomHandlerThread(TAG) {
        @Override
        protected void processMessage(Message msg) {
            switch (msg.what) {
                case MSG_CHECK_REVERT_LOG_LEVEL:
                    processMsgCheckRevertLogLevel();
                    break;

                default:

                    break;

            }
        }
    };

    public LogHandler() {

        //启动时去检查回滚log级别
        Message message = mCustomHandlerThread.obtainMessage();
        message.what = MSG_CHECK_REVERT_LOG_LEVEL;
        mCustomHandlerThread.sendMessage(message);
    }

    @Override
    public boolean processPacketData(PacketData data) {

        //WARNNING 必须要异步, 在一个新线程中去做
        if (mUploadTimes < 50) {
            mUploadTimes++;
//            UpLoadLogTask upLoadLogTask = new UpLoadLogTask(false, null);
//            AsyncTaskUtils.exe(upLoadLogTask);
        }

        if (data == null) {
            return false;
        }

        String command = data.getCommand();
        if (command.equals(MiLinkCommand.COMMAND_PUSH_LOGLEVEL)) {    //接收到push, 设置log级别
            processCommandPushLogLevel(data);
        }


        return false;
    }

    @Override
    public String[] getAcceptCommand() {
        return new String[]{
                MiLinkCommand.COMMAND_VOIP_AUTO_UPLOAD_LOG,
                MiLinkCommand.COMMAND_PUSH_LOGLEVEL
        };
    }

    /**
     * 处理MiLinkCommand.COMMAND_PUSH_LOGLEVEL
     *
     * @param data
     */
    private void processCommandPushLogLevel(PacketData data) {
        if (data == null || data.getData() == null) {
            return;
        }
        try {
            DataExtraProto.DataLoglevel dataLoglevel = DataExtraProto.DataLoglevel.parseFrom(data.getData());
            if (dataLoglevel == null) {
                return;
            } else {
                //设置log level
                int logLevel = dataLoglevel.getLoglevel();
                InitManager.setAppAndMilinkLogLevel(logLevel,logLevel);

                //产品要求在多少秒之后要恢复之前的log级别
                long timeDelay = dataLoglevel.getTimeLong();
                long revertTimestamp = System.currentTimeMillis() + timeDelay;

                PreferenceUtils.setSettingLong(SP_KEY_LOG_REVERT_TS, revertTimestamp);

                //去检查回滚log级别
                Message message = mCustomHandlerThread.obtainMessage();
                message.what = MSG_CHECK_REVERT_LOG_LEVEL;
                mCustomHandlerThread.sendMessage(message);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 处理MSG_CHECK_REVERT_LOG_LEVEL, 恢复日志级别
     */
    private void processMsgCheckRevertLogLevel() {
        long revertTimestamp = PreferenceUtils.getSettingLong(SP_KEY_LOG_REVERT_TS, 0);
        long now = System.currentTimeMillis();

        if (now >= revertTimestamp) {     //需要恢复日志级别
            InitManager.initLogger();
            PreferenceUtils.setSettingLong(SP_KEY_LOG_REVERT_TS, 0);

            mCustomHandlerThread.removeMessage(MSG_CHECK_REVERT_LOG_LEVEL);
        } else {
            long delay = revertTimestamp - now;
            Message message = mCustomHandlerThread.obtainMessage();
            message.what = MSG_CHECK_REVERT_LOG_LEVEL;
            mCustomHandlerThread.sendMessageDelayed(message, delay);
        }
    }

}
