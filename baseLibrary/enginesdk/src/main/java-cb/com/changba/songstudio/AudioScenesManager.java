package com.changba.songstudio;

import com.changba.songstudio.audioeffect.AudioEffect;
import com.changba.songstudio.audioeffect.AudioEffectEQEnum;
import com.changba.songstudio.audioeffect.AudioEffectParamController;
import com.changba.songstudio.audioeffect.AudioEffectStyleEnum;
import com.changba.songstudio.audioeffect.AudioInfo;


public class AudioScenesManager {

    public static AudioEffect getAudioEffect(AudioEffectStyleEnum styleEnum) {
        if (styleEnum == null) {
            return null;
        }
        AudioEffect audioEffect = AudioEffectParamController.getInstance().extractParam(styleEnum,
                AudioEffectEQEnum.STANDARD);
        AudioInfo audioInfo = getAudioInfo();
        audioInfo.setAudioAGCVolume(1.0f);
        audioEffect.setAudioInfo(audioInfo);
        return audioEffect;
    }

    private static AudioEffect getPopularEffect() {
        AudioEffect audioEffect = AudioEffectParamController.getInstance().extractParam(AudioEffectStyleEnum.POPULAR,
                AudioEffectEQEnum.STANDARD);
        AudioInfo audioInfo = getAudioInfo();
        audioInfo.setAudioAGCVolume(1.9f);
        audioEffect.setAudioInfo(audioInfo);
        return audioEffect;
    }

    private static AudioInfo getAudioInfo() {
        int audioSampleRate = 44100;
        int channels = 1;
        float audioAGCVolume = 1.0f;
        float accompanyAGCVolume = 1.0f;
        return new AudioInfo(channels, audioSampleRate, audioAGCVolume, accompanyAGCVolume);
    }

}
