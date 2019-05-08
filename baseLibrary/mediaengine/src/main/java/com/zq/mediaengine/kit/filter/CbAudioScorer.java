package com.zq.mediaengine.kit.filter;

import android.util.Log;

import com.engine.Params;
import com.engine.score.ICbScoreProcessor;
import com.engine.score.Score2Callback;
import com.zq.mediaengine.filter.audio.AudioFilterBase;
import com.zq.mediaengine.framework.AudioBufFormat;
import com.zq.mediaengine.framework.AudioBufFrame;

public class CbAudioScorer extends AudioFilterBase {
    private static final String TAG = "CbAudioScorer";

    private Params mConfig;
    private ICbScoreProcessor mProcessor;
    private byte[] mAudioData;

    public CbAudioScorer() {
        mProcessor = new ICbScoreProcessor();
    }

    public void init(Params config) {
        mConfig = config;
        mProcessor.init();
    }

    @Override
    protected AudioBufFormat doFormatChanged(AudioBufFormat format) {
        return format;
    }

    @Override
    protected AudioBufFrame doFilter(AudioBufFrame frame) {
        if (mConfig == null) {
            return frame;
        }

        // 这里重新拷贝一份数据，不再改ICbScoreProcessor内部实现去支持ByteBuffer
        int len = frame.buf.limit();
        if (mAudioData == null || mAudioData.length < len) {
            mAudioData = new byte[len];
        }
        frame.buf.get(mAudioData, 0, len);
        frame.buf.rewind();

        long ts = mConfig.getAccTs() - 40;
        ts = ts > 0 ? ts : 0;

        mProcessor.process(mAudioData, len, frame.format.channels, frame.format.sampleRate,
                ts, mConfig.getMidiPath());
        return frame;
    }

    @Override
    protected void doRelease() {
        mAudioData = null;
        mProcessor.destroy();
    }

    public int getScoreV1() {
        return mProcessor.getScoreV1();
    }

    public void getScoreV2(int lineNum, Score2Callback score2Callback) {
        mProcessor.getScoreV2(lineNum, score2Callback);
    }
}
