package com.module.playways.grab.room.presenter;

import android.os.Message;

import com.common.anim.ObjectPlayControlTemplate;
import com.common.log.MyLog;
import com.common.utils.CustomHandlerThread;
import com.common.utils.U;
import com.zq.lyrics.utils.SongResUtils;

import java.io.File;

public class GrabSongResPresenter {
    public final static String TAG = "GrabSongResPresenter";

    static final int MSG_DOWNLOAD_ACC = 10;

    CustomHandlerThread mCustomHandlerThread = new CustomHandlerThread("GrabSongResPresenter") {
        @Override
        protected void processMessage(Message msg) {
            if(msg.what == MSG_DOWNLOAD_ACC){
                String accUrl = (String) msg.obj;
                File accFile = SongResUtils.getAccFileByUrl(accUrl);
                if(accFile!=null && accFile.exists()){
                    MyLog.w(TAG,"伴奏文件已存在"+accUrl);
                }else{
                    U.getHttpUtils().downloadFileSync(accUrl,accFile,true,null);
                }

            }
        }
    };

    public void destroy() {
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.destroy();
        }
    }

    public void tryDownloadAcc(String preAccUrl) {
        Message msg = mCustomHandlerThread.obtainMessage();
        msg.what = MSG_DOWNLOAD_ACC;
        msg.obj = preAccUrl;
        mCustomHandlerThread.sendMessage(msg);
    }
}
