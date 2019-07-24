package com.zq.mediaengine.framework;

/**
 * Simple transfer data from SinkPin to SrcPin.
 */
public class PinAdapter<T extends AVFrameBase> {
    public SinkPin<T> mSinkPin;
    public SrcPin<T> mSrcPin;

    public PinAdapter() {
        mSrcPin = createSrcPin();
        mSinkPin = new SinkPin<T>() {
            @Override
            public void onFormatChanged(Object format) {
                mSrcPin.onFormatChanged(format);
            }

            @Override
            public void onFrameAvailable(T frame) {
                mSrcPin.onFrameAvailable(frame);
            }

            @Override
            public void onDisconnect(boolean recursive) {
                super.onDisconnect(recursive);
                PinAdapter.this.onDisconnect(recursive);
                if (recursive) {
                    mSrcPin.disconnect(true);
                }
            }
        };
    }

    protected SrcPin<T> createSrcPin() {
        return new SrcPin<>();
    }

    public void onDisconnect(boolean recursive) {
    }
}
