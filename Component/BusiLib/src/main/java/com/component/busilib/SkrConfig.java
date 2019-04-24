package com.component.busilib;

public class SkrConfig {

    /**
     * 机器人资源让ios的传吧 ，android 好多机型性能太差了，一录音更卡了
     */
    private boolean mNeedUploadAudioForAI = false;

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
