package com.engine.arccloud;

public class RecognizeConfig {
    /**
     * 模式自动
     * 设置开始，积攒buffer，buffer达到阈值，自动识别
     * 模式手动
     * 设置开始，积攒buffer，buffer达到阈值，不识别，不停地滚动buffer，主动调用了识别，才识别
     */
    public static final int MODE_AUTO = 1;
    public static final int MODE_MANUAL = 2;

    int mode = MODE_MANUAL;
    String songName;
    int autoTimes = 1;

    boolean wantRecognizeInManualMode = false;

    ArcRecognizeListener resultListener;

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getAutoTimes() {
        return autoTimes;
    }

    public void setAutoTimes(int autoTimes) {
        this.autoTimes = autoTimes;
    }

    public boolean isWantRecognizeInManualMode() {
        return wantRecognizeInManualMode;
    }

    public void setWantRecognizeInManualMode(boolean wantRecognizeInManualMode) {
        this.wantRecognizeInManualMode = wantRecognizeInManualMode;
    }

    public ArcRecognizeListener getResultListener() {
        return resultListener;
    }

    public void setResultListener(ArcRecognizeListener resultListener) {
        this.resultListener = resultListener;
    }

    public static Builder newBuilder(){
        return new Builder();
    }

    public static class Builder {
        RecognizeConfig mParams = new RecognizeConfig();

        Builder() {
        }

        public Builder setSongName(String songName) {
            mParams.setSongName(songName);
            return this;
        }

        public Builder setMode(int mode) {
            mParams.setMode(mode);
            return this;
        }

        public Builder setAutoTimes(int autoTimes) {
            mParams.setAutoTimes(autoTimes);
            return this;
        }

        public Builder setMResultListener(ArcRecognizeListener mResultListener) {
            mParams.setResultListener(mResultListener);
            return this;
        }
        public RecognizeConfig build() {
            return mParams;
        }
    }
}
