package com.zq.mediaengine.kit.filter;

import android.util.Log;

import com.engine.Params;
import com.engine.effect.ITbAgcProcessor;
import com.zq.mediaengine.filter.audio.AudioFilterBase;
import com.zq.mediaengine.framework.AudioBufFormat;
import com.zq.mediaengine.framework.AudioBufFrame;

public class TbAudioAgcFilter extends AudioFilterBase {
    private final static String TAG = "TbAudioEffectFilter";

    private ITbAgcProcessor mAgcProcessor;

    private Params mConfig;

    public TbAudioAgcFilter(Params params) {
        mAgcProcessor = new ITbAgcProcessor();
        mAgcProcessor.init();
        mConfig = params;
    }

    @Override
    protected AudioBufFormat doFormatChanged(AudioBufFormat format) {
        return format;
    }

    @Override
    protected AudioBufFrame doFilter(AudioBufFrame frame) {
        // 针对不同场景，处理agc
        switch (mConfig.getScene()) {
            case grab:
                if (mConfig.isGrabSingNoAcc()) {
                    // 只有单人清唱才走天宝的agc
                    mAgcProcessor.processV1(frame.buf, frame.buf.limit(),
                            frame.format.channels, frame.format.sampleRate);
                }
                break;
            case doubleChat:
                break;
            case voice:
                break;
            case rank:
            case audiotest:
                mAgcProcessor.processV1(frame.buf, frame.buf.limit(),
                        frame.format.channels, frame.format.sampleRate);
                break;
        }
        return frame;
    }

    @Override
    protected void doRelease() {
        mAgcProcessor.destroyAgcProcessor();
    }
}
