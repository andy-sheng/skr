package com.zq.mediaengine.kit.filter;

import com.engine.effect.ITbAgcProcessor;
import com.zq.mediaengine.filter.audio.AudioFilterBase;
import com.zq.mediaengine.framework.AudioBufFormat;
import com.zq.mediaengine.framework.AudioBufFrame;

public class TbAudioAgcFilter extends AudioFilterBase {
    private final static String TAG = "TbAudioEffectFilter";

    private ITbAgcProcessor mAgcProcessor;

    public TbAudioAgcFilter() {
        mAgcProcessor = new ITbAgcProcessor();
        mAgcProcessor.init();
    }

    @Override
    protected AudioBufFormat doFormatChanged(AudioBufFormat format) {
        return format;
    }

    @Override
    protected AudioBufFrame doFilter(AudioBufFrame frame) {
        mAgcProcessor.processV1(frame.buf, frame.buf.limit(),
                frame.format.channels, frame.format.sampleRate);
        return frame;
    }

    @Override
    protected void doRelease() {
        mAgcProcessor.destroyAgcProcessor();
    }
}
