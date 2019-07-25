package com.module.playways.room.room.score;

import com.alibaba.fastjson.JSON;
import com.common.log.MyLog;
import com.common.utils.HttpUtils;
import com.component.lyrics.utils.SongResUtils;
import com.common.utils.U;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;
import okio.Source;

public class RobotScoreHelper {

    public final String TAG = "RobotScoreHelper";

    MachineScoreModel mMachineScoreModel = new MachineScoreModel();

    MachineScoreModel mRobotScoreModel = new MachineScoreModel();

    long mBeginRecordTs = System.currentTimeMillis();

    public void add(MachineScoreItem machineScoreItem) {
        mMachineScoreModel.getDataList().add(machineScoreItem);
    }

    public void save(String filePath) {
        mMachineScoreModel.compute();
        String content = JSON.toJSONString(mMachineScoreModel);
        File file = new File(filePath);
        BufferedSink bufferedSink = null;
        try {
            Sink sink = Okio.sink(file);
            bufferedSink = Okio.buffer(sink);
            bufferedSink.writeString(content, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (null != bufferedSink) {
                bufferedSink.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean vilid() {
        return mMachineScoreModel.getDataList().size() > 2;
    }

    public boolean isScoreEnough() {
        int t = getAverageScore();
        boolean e = t > 0;
        MyLog.d(TAG, "isScoreEnough getAverageScore:" + t + " isScoreEnough:" + e);
        return e;
    }

    public void loadDataFromUrl(String midiUrl, int deep) {
        MyLog.d(TAG, "loadDataFromUrl" + " midiUrl=" + midiUrl + " deep=" + deep);
        if (deep > 5) {
            return;
        }
        File file = SongResUtils.getScoreFileByUrl(midiUrl);
        if (file != null && file.exists() && file.length() > 10) {
            loadDataFromFile(file.getAbsolutePath());
        } else {
            U.getHttpUtils().downloadFileSync(midiUrl, file,false, new HttpUtils.OnDownloadProgress() {
                @Override
                public void onDownloaded(long downloaded, long totalLength) {

                }

                @Override
                public void onCompleted(String localPath) {
                    //success
                    if (file != null) {
                        loadDataFromFile(file.getAbsolutePath());
                    }
                }

                @Override
                public void onCanceled() {

                }

                @Override
                public void onFailed() {
                    loadDataFromUrl(midiUrl, deep + 1);
                }
            });
        }
    }

    private void loadDataFromFile(String localPath) {
        Source source = null;
        BufferedSource bufferedSource = null;
        try {
            File file = new File(localPath);
            source = Okio.source(file);
            bufferedSource = Okio.buffer(source);
            String content = bufferedSource.readUtf8();

            MachineScoreModel machineScoreModel = JSON.parseObject(content, MachineScoreModel.class);
            if (machineScoreModel != null) {
                mRobotScoreModel = machineScoreModel;
                EventBus.getDefault().post(new RobotSongLineNum(mRobotScoreModel.getScoreLineNum()));
            }
        } catch (Exception e) {
            MyLog.e(TAG, e);
        } finally {
            try {
                bufferedSource.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 获取得分，算法为
     * curPostion 为当前播放的音频时间戳，一般为每一秒会取一次。
     * 则取 得分时间戳为  curPostion-1000<x<curPostion 的对应的得分即可
     * 保证 mMachineScoreModel 有序，采用二分法
     *
     * @param curPostion
     * @return
     */
    public int tryGetScoreByTs(long curPostion) {
        MyLog.d(TAG, "tryGetScoreByTs" + " curPostion=" + curPostion + " size=" + mRobotScoreModel.getDataList().size());
        MachineScoreItem machineScoreItem = mRobotScoreModel.findMatchingScoreItemByTs(curPostion);
        if (machineScoreItem != null) {
            return machineScoreItem.getScore();
        } else {
            return -1;
        }
    }

    public int tryGetScoreByLine(int lineNo) {
        MachineScoreItem machineScoreItem = mRobotScoreModel.findMatchingScoreItemByNo(lineNo);
        if (machineScoreItem != null) {
            return machineScoreItem.getScore();
        } else {
            return -1;
        }
    }

    /**
     * 得到<=lineNo所有的总分
     *
     * @param lineNo
     * @return
     */
    public int tryGetTotalScoreByLine(int lineNo) {
        return mRobotScoreModel.tryGetTotalScoreByLine(lineNo);
    }

    public int tryGetScoreLineNum() {
        int linenum = mRobotScoreModel.getScoreLineNum();
        if (linenum == 0) {
            MyLog.d(TAG, "tryGetScoreLineNum 行数为0？？？？？why？？？");
            return 1;
        }
        return linenum;
    }

    public int getAverageScore() {
        mMachineScoreModel.compute();
        return mMachineScoreModel.getAverageScore();
    }

    public void reset() {
        mMachineScoreModel.reset();
        mRobotScoreModel.reset();
        mBeginRecordTs = System.currentTimeMillis();
    }

    public void setBeginRecordTs(long ts) {
        mBeginRecordTs = ts;
    }

    public long getBeginRecordTs() {
        return mBeginRecordTs;
    }

    public static class RobotSongLineNum{
        public int lineNum;

        public RobotSongLineNum(int lineNum) {
            this.lineNum = lineNum;
        }
    }
}
