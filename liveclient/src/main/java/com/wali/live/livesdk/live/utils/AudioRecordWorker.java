package com.wali.live.livesdk.live.utils;

import android.os.Message;

import com.mi.milink.sdk.base.CustomHandlerThread;

/**
 * Created by chenyong on 2016/11/25.
 */

public class AudioRecordWorker extends CustomHandlerThread {
    public AudioRecordWorker() {
        super("AudioRecordWorker", android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
    }

    @Override
    protected void processMessage(Message msg) {

    }
}
