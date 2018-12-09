package media.ushow.audio_effect;

import android.content.Context;

import com.changba.songstudio.audioeffect.AudioEffect;
import com.changba.songstudio.audioeffect.AudioEffectParamController;

/**
 * Created by xiaokai.zhan on 2018/12/5.
 */

public class IFAudioEffectEngine {
    private static volatile boolean isLoadResFlag = false;
    public IFAudioEffectEngine(Context context, AudioEffect audioEffect) {
        if(!isLoadResFlag) {
            AudioEffectParamController.getInstance().loadParamFromResource(context);
            isLoadResFlag = true;
        }
        this.initAudioEffect(audioEffect);
    }

    public native void initAudioEffect(AudioEffect audioEffect);
    public native void setAudioEffect(AudioEffect audioEffect);
    public native void processAudioFrames(byte[] samples, int numOfSamples, int bytesPerSample, int channels, int samplesPerSec);
    public native void destroyAudioEffect();
}
