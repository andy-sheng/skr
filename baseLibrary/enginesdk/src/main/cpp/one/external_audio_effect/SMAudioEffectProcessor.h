#ifndef STARMAKER_ANDROID_CLIENT_SMAUDIOEFFECT_H
#define STARMAKER_ANDROID_CLIENT_SMAUDIOEFFECT_H

#include <cstring>
#include "reverb_inc/CReverb4.h"

enum ExternalAudioEffectType {
    EFFECT_NONE             = 0,
    EFFECT_FILTER           = 1,
    EFFECT_ROLL             = 2,
    EFFECT_FLANGER          = 3,
    EFFECT_REVERB           = 4,
    EFFECT_AUTOTUNE         = 5,
    EFFECT_KTV              = 6,
    EFFECT_PHONOGRAPH       = 7,
    EFFECT_DISTANT          = 8,
    EFFECT_WARM             = 9,
    EFFECT_MAGNETIC         = 10,
    EFFECT_ETHEREAL         = 11,
    EFFECT_DIZZY            = 12,
    EFFECT_NEW_DISTANT      = 13,//混响效果趋于KTV和DISTANT之间的音效
};

class SMAudioEffectProcessor {
public:
    SMAudioEffectProcessor();
    ~SMAudioEffectProcessor();
    //初始化音效
    void initEffect(int samplerate, int channelCount, int buffersize);
    //选择音效
    void onEffectSelect(int type);
    //执行音效合成
    void Process(float* buffer, int32_t numberOfSamples, double displayPositionMs);

private:
    bool isCustomReverbEnable;//是否开启音效（除去auto tune）
    bool needAutoTune;
    int mChannelCount = 0;
    CReverb4 *customeReverb;
};


#endif //STARMAKER_ANDROID_CLIENT_SMAUDIOEFFECT_H
