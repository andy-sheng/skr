package media.ushow.audio_effect;

import com.changba.songstudio.audioeffect.AudioEffect;
import com.changba.songstudio.audioeffect.AudioEffectParamController;
import com.common.utils.U;

import media.ushow.score.ScoreProcessor;

/**
 * Created by xiaokai.zhan on 2018/12/5.
 */

public class IFAudioEffectEngine {
    private static volatile boolean isLoadResFlag = false;

    public static void load() {
        if (!isLoadResFlag) {
            AudioEffectParamController.getInstance().loadParamFromResource(U.app());
            isLoadResFlag = true;
        }
    }

    public IFAudioEffectEngine(AudioEffect audioEffect) {
        load();
        this.initAudioEffect(audioEffect);
    }

    private ScoreProcessor scoreProcessor;

    public void processAudioBuffer(byte[] samples, int numOfSamples, int bytesPerSample, int channels, int samplesPerSec, long currentTimeMills, String melpPath) {
        if (null == scoreProcessor && melpPath != null && melpPath.trim().length() > 0) {
            channels = 1;
            scoreProcessor = new ScoreProcessor(samplesPerSec, channels, bytesPerSample * 8, numOfSamples, melpPath);
        }
        this.processAudioFrames(samples, numOfSamples, bytesPerSample, channels, samplesPerSec, currentTimeMills);
    }

    public int getLineScore() {
        if (null != scoreProcessor) {
            return scoreProcessor.getLineScore();
        }
        return -1;
    }

    private native void processAudioFrames(byte[] samples, int numOfSamples, int bytesPerSample, int channels, int samplesPerSec, long currentTimeMills);


    public native void initAudioEffect(AudioEffect audioEffect);

    public native void setAudioEffect(AudioEffect audioEffect);


    public native void destroyAudioEffect();
}
