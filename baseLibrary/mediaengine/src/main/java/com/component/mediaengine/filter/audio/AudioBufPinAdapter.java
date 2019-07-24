package com.component.mediaengine.filter.audio;

import com.component.mediaengine.framework.AudioBufFrame;
import com.component.mediaengine.framework.PinAdapter;
import com.component.mediaengine.framework.SrcPin;

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
