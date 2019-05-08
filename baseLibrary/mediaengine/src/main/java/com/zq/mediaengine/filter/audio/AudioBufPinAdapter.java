package com.zq.mediaengine.filter.audio;

import com.zq.mediaengine.framework.AudioBufFrame;
import com.zq.mediaengine.framework.PinAdapter;
import com.zq.mediaengine.framework.SrcPin;

/**
 * AudioBufFrame PinAdapter.
 *
 * @hide
 */

public class AudioBufPinAdapter extends PinAdapter<AudioBufFrame> {
    @Override
    protected SrcPin<AudioBufFrame> createSrcPin() {
        return new AudioBufSrcPin();
    }
}
