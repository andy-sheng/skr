package com.module.playways.rank.room.score;

import com.alibaba.fastjson.JSON;
import com.common.log.MyLog;
import com.common.utils.HttpUtils;
import com.common.utils.SongResUtils;
import com.common.utils.U;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;
import okio.Source;

public class RobotScoreHelper {

    public final static String TAG = "RobotScoreHelper";

    MachineScoreModel mMachineScoreModel = new MachineScoreModel();

    MachineScoreModel mRobotScoreModel = new MachineScoreModel();

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
        boolean e = t > 10;
        MyLog.d(TAG, "isScoreEnough getAverageScore:" + t + " isScoreEnough:" + e);
        return e;
    }

    public void loadDataFromUrl(String midiUrl, int deep) {
        MyLog.d(TAG, "loadDataFromUrl" + " midiUrl=" + midiUrl + " deep=" + deep);
        if (deep > 5) {
            return;
        }
        File file = SongResUtils.getScoreFileByUrl(midiUrl);
        if (file.exists() && file.length() > 10) {
            loadDataFromFile(file.getAbsolutePath());
        } else {
            U.getHttpUtils().downloadFileSync(midiUrl, file, new HttpUtils.OnDownloadProgress() {
                @Override
                public void onDownloaded(long downloaded, long totalLength) {

                }

                @Override
                public void onCompleted(String localPath) {
                    //success
                    loadDataFromFile(file.getAbsolutePath());
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
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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

    public int getAverageScore() {
        mMachineScoreModel.compute();
        return mMachineScoreModel.getAverageScore();
    }

}
