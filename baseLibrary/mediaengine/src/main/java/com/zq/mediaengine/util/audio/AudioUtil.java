package com.zq.mediaengine.util.audio;

import android.content.Context;
import android.media.AudioManager;

import com.common.log.MyLog;

/**
 * Audio utils.
 */

public class AudioUtil {
    public static int getNativeSampleRate(Context context) {
        int sampleRate = 44100;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build
                .VERSION_CODES.JELLY_BEAN_MR1) {
            AudioManager audioManager = (AudioManager) context.getSystemService(
                    Context.AUDIO_SERVICE);
            try {
                sampleRate = Integer.parseInt(audioManager.getProperty(
                        AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE));
            } catch (NumberFormatException e) {
                MyLog.w("AudioUtil", "NumberFormatException" + audioManager.getProperty(
                        AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE));
            }
        }
        return sampleRate;
    }

    public static int getNativeBufferSize(Context context, int sampleRate) {
        int atomSize = 1024;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build
                .VERSION_CODES.JELLY_BEAN_MR1) {
            AudioManager audioManager = (AudioManager) context.getSystemService(
                    Context.AUDIO_SERVICE);
            try {
                int nativeSampleRate = Integer.parseInt(audioManager.getProperty(
                        AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE));
                if (sampleRate == nativeSampleRate) {
                    atomSize = Integer.parseInt(audioManager.getProperty(AudioManager
                            .PROPERTY_OUTPUT_FRAMES_PER_BUFFER));
                }
            }catch (Exception e){
                MyLog.e(e);
            }
        }
        return atomSize;
    }
}
