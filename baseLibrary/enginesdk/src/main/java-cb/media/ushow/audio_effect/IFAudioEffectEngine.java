package media.ushow.audio_effect;

import android.text.TextUtils;

import com.changba.songstudio.audioeffect.AudioEffect;
import com.changba.songstudio.audioeffect.AudioEffectParamController;
import com.common.log.MyLog;
import com.common.utils.U;

import media.ushow.score.ScoreProcessor;

/**
 * Created by xiaokai.zhan on 2018/12/5.
 */

public class IFAudioEffectEngine {
    public final static String TAG = "IFAudioEffectEngine";

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

    private ScoreProcessor mScoreProcessor;

    private String mLastMelpPath;

    /**
     * @param samples 2048
     * @param numOfSamples 512
     * @param bytesPerSample 2
     * @param channels 2
     * @param samplesPerSec 44100
     * @param currentTimeMills 当前时间戳
     * @param melpPath 有的为null
     */
    public void processAudioBuffer(byte[] samples, int numOfSamples, int bytesPerSample, int channels, int samplesPerSec, long currentTimeMills, String melpPath) {
        //MyLog.d(TAG,"processAudioBuffer" + " samples.length=" + samples.length + " numOfSamples=" + numOfSamples + " bytesPerSample=" + bytesPerSample + " channels=" + channels + " samplesPerSec=" + samplesPerSec + " currentTimeMills=" + currentTimeMills + " melpPath=" + melpPath);
        if (null == mScoreProcessor ) {
            mScoreProcessor = new ScoreProcessor();
        }

        if(!TextUtils.isEmpty(melpPath) && !melpPath.equals(mLastMelpPath)){
            mScoreProcessor.init(samplesPerSec, 1, bytesPerSample * 8, numOfSamples, melpPath);
            mLastMelpPath = melpPath;
        }

        this.processAudioFrames(samples, numOfSamples, bytesPerSample, channels, samplesPerSec, currentTimeMills);
    }

    public int getLineScore() {
        if (null != mScoreProcessor) {
            return mScoreProcessor.getLineScore();
        }
        return -1;
    }

    private native void processAudioFrames(byte[] samples, int numOfSamples, int bytesPerSample, int channels, int samplesPerSec, long currentTimeMills);


    public native void initAudioEffect(AudioEffect audioEffect);

    public native void setAudioEffect(AudioEffect audioEffect);


    public void destroy() {
        if (null != mScoreProcessor) {
            mScoreProcessor.destroy();
            mScoreProcessor = null;
        }
        this.destroyAudioEffect();
    }

    private native void destroyAudioEffect();
}
