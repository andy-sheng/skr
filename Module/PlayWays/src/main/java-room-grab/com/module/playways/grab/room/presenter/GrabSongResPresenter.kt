package com.module.playways.grab.room.presenter

import android.os.Message

import com.common.callback.Callback
import com.common.log.MyLog
import com.common.utils.CustomHandlerThread
import com.common.utils.U
import com.module.playways.room.song.model.SongModel
import com.component.lyrics.utils.SongResUtils

class GrabSongResPresenter {

    internal var mCustomHandlerThread: CustomHandlerThread? = object : CustomHandlerThread("GrabSongResPresenter") {
        override fun processMessage(msg: Message) {
            if (msg.what == MSG_DOWNLOAD_ACC) {
                val accUrl = msg.obj as String
                val accFile = SongResUtils.getAccFileByUrl(accUrl)
                if (accFile != null && accFile.exists()) {
                    MyLog.w(TAG, "伴奏文件已存在$accUrl")
                } else {
                    U.getHttpUtils().downloadFileSync(accUrl, accFile, true, null)
                }

            }
        }
    }

    fun destroy() {
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread!!.destroy()
        }
    }

    fun tryDownloadAcc(preAccUrl: String) {
        val msg = mCustomHandlerThread!!.obtainMessage()
        msg.what = MSG_DOWNLOAD_ACC
        msg.obj = preAccUrl
        mCustomHandlerThread!!.sendMessage(msg)
    }

    companion object {
        val TAG = "GrabSongResPresenter"

        internal val MSG_DOWNLOAD_ACC = 10

        private fun test(bb: String, l: Callback<SongModel>?) {
            MyLog.d(TAG, "bb:$bb")
            val songModel = SongModel()
            l?.onCallback(0, songModel)
        }
    }
}
