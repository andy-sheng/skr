package com.component.mediaengine.kit.filter;

import com.engine.effect.IFAudioEffectEngine;
import com.component.mediaengine.filter.audio.AudioFilterBase;
import com.component.mediaengine.framework.AudioBufFormat;
import com.component.mediaengine.framework.AudioBufFrame;

public class CbAudioEffectFilter extends AudioFilterBase {
    private final static String TAG = "CbAudioEffectFilter";

    private int mType;
    private IFAudioEffectEngine mAudioEffectEngine;

    public CbAudioEffectFilter(int type) {
        mType = type;
        mAudioEffectEngine = new IFAudioEffectEngine();
    }

    @Override
    protected AudioBufFormat doFormatChanged(AudioBufFormat format) {
        return format;
    }

    @Override
    protected AudioBufFrame doFilter(AudioBufFrame frame) {
        mAudioEffectEngine.process(mType, frame.buf, frame.buf.limit(),
                frame.format.channels, frame.format.sampleRate);
        return frame;
    }

    @Override
    protected void doRelease() {
        mAudioEffectEngine.destroy();
    }
}
