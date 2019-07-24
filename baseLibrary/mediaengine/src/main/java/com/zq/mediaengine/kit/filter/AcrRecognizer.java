package com.zq.mediaengine.kit.filter;

import com.engine.Params;
import com.engine.arccloud.AcrCloudManager;
import com.engine.arccloud.AcrRecognizeListener;
import com.engine.arccloud.RecognizeConfig;
import com.zq.mediaengine.filter.audio.AudioFilterBase;
import com.zq.mediaengine.framework.AudioBufFormat;
import com.zq.mediaengine.framework.AudioBufFrame;

public class AcrRecognizer extends AudioFilterBase {
    private static final String TAG = "CbAudioScorer";

    private Params mConfig;
    private AcrCloudManager mAcrCloudManager;
    private byte[] mAudioData;

    public AcrRecognizer() {
    }

    public void init(Params config) {
        mConfig = config;
    }

    private void tryInitArcManager() {
        if (mAcrCloudManager == null) {
            synchronized (this) {
                if (mAcrCloudManager == null) {
                    mAcrCloudManager = new AcrCloudManager();
                }
            }
        }
    }

    public void startRecognize(RecognizeConfig recognizeConfig) {
        tryInitArcManager();
        mAcrCloudManager.startRecognize(recognizeConfig);
    }

    public void setRecognizeListener(AcrRecognizeListener recognizeConfig) {
        if (mAcrCloudManager != null) {
            mAcrCloudManager.setRecognizeListener(recognizeConfig);
        }
    }

    public void stopRecognize() {
        if (mAcrCloudManager != null) {
            mAcrCloudManager.stopRecognize();
        }
    }

    public void recognizeInManualMode(int lineNo) {
        if (mAcrCloudManager != null) {
            mAcrCloudManager.recognizeInManualMode(lineNo);
        }
    }

    @Override
    protected AudioBufFormat doFormatChanged(AudioBufFormat format) {
        return format;
    }

    @Override
    protected AudioBufFrame doFilter(AudioBufFrame frame) {
        if (mAcrCloudManager != null && mConfig != null) {
            if (mConfig.isMixMusicPlaying() && mConfig.getLrcHasStart() || mConfig.isGrabSingNoAcc()) {
                int len = frame.buf.limit();
                if (mAudioData == null || mAudioData.length < len) {
                    mAudioData = new byte[len];
                }
                frame.buf.get(mAudioData, 0, len);
                frame.buf.rewind();
                mAcrCloudManager.putPool(mAudioData, frame.format.sampleRate, frame.format.channels);
            }
        }
        return frame;
    }

    @Override
    protected void doRelease() {
        mAudioData = null;
        if (mAcrCloudManager != null) {
            mAcrCloudManager.destroy();
        }
    }
}
