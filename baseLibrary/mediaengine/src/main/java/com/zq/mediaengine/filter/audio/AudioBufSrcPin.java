package com.zq.mediaengine.filter.audio;

import android.support.annotation.Nullable;

import com.zq.mediaengine.framework.AVConst;
import com.zq.mediaengine.framework.AudioBufFormat;
import com.zq.mediaengine.framework.AudioBufFrame;
import com.zq.mediaengine.framework.SinkPin;
import com.zq.mediaengine.framework.SrcPin;

/**
 * AudioBufFrame source pin.
 *
 * @hide
 */

public class AudioBufSrcPin extends SrcPin<AudioBufFrame> {

    @Override
    public synchronized void onFrameAvailable(AudioBufFrame frame) {
        super.onFrameAvailable(frame);
        if ((frame.flags & AVConst.FLAG_DETACH_NATIVE_MODULE) != 0) {
            format = null;
        }
    }

    @Override
    public synchronized void disconnect(@Nullable SinkPin<AudioBufFrame> sinkPin,
                                        boolean recursive) {
        if (format != null) {
            AudioBufFrame frame = new AudioBufFrame((AudioBufFormat) format, null, 0);
            frame.flags |= AVConst.FLAG_DETACH_NATIVE_MODULE;

            if (sinkPin != null) {
                sinkPin.onFrameAvailable(frame);
            } else {
                for (SinkPin<AudioBufFrame> sink : sinkPins) {
                    sink.onFrameAvailable(frame);
                }
            }
        }
        super.disconnect(sinkPin, recursive);
    }
}
