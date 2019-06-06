package com.zq.mediaengine.kit.filter;

import com.engine.Params;
import com.engine.arccloud.ArcCloudManager;
import com.engine.arccloud.ArcRecognizeListener;
import com.engine.arccloud.RecognizeConfig;
import com.zq.mediaengine.filter.audio.AudioFilterBase;
import com.zq.mediaengine.framework.AudioBufFormat;
import com.zq.mediaengine.framework.AudioBufFrame;

public class AcrRecognizer extends AudioFilterBase {
    private static final String TAG = "CbAudioScorer";

    private Params mConfig;
    private ArcCloudManager mArcCloudManager;
    private byte[] mAudioData;

    public AcrRecognizer() {
    }

    public void init(Params config) {
        mConfig = config;
    }

    private void tryInitArcManager() {
        if (mArcCloudManager == null) {
            synchronized (this) {
                if (mArcCloudManager == null) {
                    mArcCloudManager = new ArcCloudManager();
                }
            }
        }
    }

    public void startRecognize(RecognizeConfig recognizeConfig) {
        tryInitArcManager();
        mArcCloudManager.startRecognize(recognizeConfig);
    }

    public void setRecognizeListener(ArcRecognizeListener recognizeConfig) {
        if (mArcCloudManager != null) {
            mArcCloudManager.setRecognizeListener(recognizeConfig);
        }
    }

    public void stopRecognize() {
        if (mArcCloudManager != null) {
            mArcCloudManager.stopRecognize();
        }
    }

    public void recognizeInManualMode(int lineNo) {
        if (mArcCloudManager != null) {
            mArcCloudManager.recognizeInManualMode(lineNo);
        }
    }

    @Override
    protected AudioBufFormat doFormatChanged(AudioBufFormat format) {
        return format;
    }

    @Override
    protected AudioBufFrame doFilter(AudioBufFrame frame) {
        if (mArcCloudManager != null && mConfig != null) {
            if (mConfig.isMixMusicPlaying() && mConfig.getLrcHasStart() || mConfig.isGrabSingNoAcc()) {
                int len = frame.buf.limit();
                if (mAudioData == null || mAudioData.length < len) {
                    mAudioData = new byte[len];
                }
                frame.buf.get(mAudioData, 0, len);
                frame.buf.rewind();
                mArcCloudManager.putPool(mAudioData, frame.format.sampleRate, frame.format.channels);
            }
        }
        return frame;
    }

    @Override
    protected void doRelease() {
        mAudioData = null;
        if (mArcCloudManager != null) {
            mArcCloudManager.destroy();
        }
    }
}
