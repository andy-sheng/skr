package media.ushow.audio_effect;

import com.changba.songstudio.audioeffect.AudioEffect;

/**
 * Created by xiaokai.zhan on 2018/12/2.
 */

public class AudioEffectProcessor {
    public static native int process(String inputFilePath, String outputFilePath);

    public static native int processWithEffect(AudioEffect audioEffect, String inputFilePath, String outputFilePath);

    public static native int processWithExternalEffect(AudioEffect audioEffect, String inputFilePath, String outputFilePath);
}
