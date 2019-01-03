package com.component.busilib;

public class SkrConfig {

    private boolean mNeedUploadAudioForAI = true;

    private static class SkrConfigHolder {
        private static final SkrConfig INSTANCE = new SkrConfig();
    }

    private SkrConfig() {

    }

    public static final SkrConfig getInstance() {
        return SkrConfigHolder.INSTANCE;
    }

    public boolean isNeedUploadAudioForAI() {
        return mNeedUploadAudioForAI;
    }

    public void setNeedUploadAudioForAI(boolean needUploadAudioForAI) {
        mNeedUploadAudioForAI = needUploadAudioForAI;
    }
}
