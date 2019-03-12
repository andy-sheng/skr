package com.engine.effect;


import com.common.log.MyLog;
import com.common.utils.U;
import com.changba.songstudio.audioeffect.AudioEffect;
import com.changba.songstudio.audioeffect.AudioEffectEQEnum;
import com.changba.songstudio.audioeffect.AudioEffectParamController;
import com.changba.songstudio.audioeffect.AudioEffectStyleEnum;
import com.changba.songstudio.audioeffect.AudioInfo;


/**
 * Created by xiaokai.zhan on 2018/12/5.
 */

public class IFAudioEffectEngine {
    public final static String TAG = "IFAudioEffectEngine";

    static {
        try {
            MyLog.d(TAG, "loadLibrary");
            System.loadLibrary("native-lib1");
        } catch (Exception e) {
            MyLog.d(TAG, e);
            e.printStackTrace();
        }
    }

    public native void initAudioEffect(AudioEffect audioEffect);

    public native void destroyAudioEffect();

    public native void processAudioEffect(byte[] data, int len, int channel, int sampleRate);

    int mType = -1;

    public void load(int type) {
        AudioEffectParamController.getInstance().loadParamFromResource(U.app());
        AudioEffect audioEffect;
        if (type == 1) {
            audioEffect = AudioEffectParamController.getInstance().extractParam(AudioEffectStyleEnum.RNB,
                    AudioEffectEQEnum.STANDARD);
        } else if (type == 2) {
            audioEffect = AudioEffectParamController.getInstance().extractParam(AudioEffectStyleEnum.ROCK,
                    AudioEffectEQEnum.STANDARD);
        } else if (type == 3) {
            audioEffect = AudioEffectParamController.getInstance().extractParam(AudioEffectStyleEnum.POPULAR,
                    AudioEffectEQEnum.STANDARD);
        } else if (type == 4) {
            audioEffect = AudioEffectParamController.getInstance().extractParam(AudioEffectStyleEnum.DANCE,
                    AudioEffectEQEnum.STANDARD);
        } else if (type == 5) {
            audioEffect = AudioEffectParamController.getInstance().extractParam(AudioEffectStyleEnum.NEW_CENT,
                    AudioEffectEQEnum.STANDARD);
        } else if (type == 6) {
            audioEffect = AudioEffectParamController.getInstance().extractParam(AudioEffectStyleEnum.LIVE_ORIGINAL,
                    AudioEffectEQEnum.STANDARD);
        } else if (type == 7) {
            audioEffect = AudioEffectParamController.getInstance().extractParam(AudioEffectStyleEnum.LIVE_MAGIC,
                    AudioEffectEQEnum.STANDARD);
        } else if (type == 8) {
            audioEffect = AudioEffectParamController.getInstance().extractParam(AudioEffectStyleEnum.LIVE_SIGNER,
                    AudioEffectEQEnum.STANDARD);
        } else if (type == 9) {
            audioEffect = AudioEffectParamController.getInstance().extractParam(AudioEffectStyleEnum.LIVE_PROFFESSION,
                    AudioEffectEQEnum.STANDARD);
        } else if (type == 10) {
            audioEffect = AudioEffectParamController.getInstance().extractParam(AudioEffectStyleEnum.LIVE_GOD,
                    AudioEffectEQEnum.STANDARD);
        } else {
            audioEffect = AudioEffectParamController.getInstance().extractParam(AudioEffectStyleEnum.ORIGINAL,
                    AudioEffectEQEnum.STANDARD);
        }

        int audioSampleRate = 44100;
        int channels = 1;
        float audioAGCVolume = 1.0f;
        float accompanyAGCVolume = 1.0f;
        AudioInfo audioInfo = new AudioInfo(channels, audioSampleRate, audioAGCVolume, accompanyAGCVolume);

        audioInfo.setAudioAGCVolume(1.0f);
        audioEffect.setAudioInfo(audioInfo);
        this.initAudioEffect(audioEffect);
    }

    public int process(int type, byte[] samples, int length, int channels, int samplesPerSec) {
        if (type != mType) {
            mType = type;
            if (type == 1) {
                load(3);
            } else if (type == 2) {
                load(4);
            }
        }
        processAudioEffect(samples, length, channels, samplesPerSec);
        return 0;
    }

    public void destroy() {
        this.destroyAudioEffect();
    }

}
