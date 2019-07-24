package com.component.mediaengine.kit.filter;

import com.engine.effect.ITbEffectProcessor;
import com.component.mediaengine.filter.audio.AudioFilterBase;
import com.component.mediaengine.framework.AudioBufFormat;
import com.component.mediaengine.framework.AudioBufFrame;

public class TbAudioEffectFilter extends AudioFilterBase {
    private final static String TAG = "TbAudioEffectFilter";

    private int mType;
    private ITbEffectProcessor mEffectProcessor;

    public TbAudioEffectFilter(int type) {
        mType = type;
        mEffectProcessor = new ITbEffectProcessor();
        mEffectProcessor.init();
    }

    @Override
    protected AudioBufFormat doFormatChanged(AudioBufFormat format) {
        return format;
    }

    @Override
    protected AudioBufFrame doFilter(AudioBufFrame frame) {
        mEffectProcessor.process(mType, frame.buf, frame.buf.limit(),
                frame.format.channels, frame.format.sampleRate);
        return frame;
    }

    @Override
    protected void doRelease() {
        mEffectProcessor.destroyEffectProcessor();
    }
}
