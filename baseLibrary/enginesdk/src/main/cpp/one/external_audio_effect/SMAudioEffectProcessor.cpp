//
// Created by lixianfeng on 25/7/17.
//

#include "SMAudioEffectProcessor.h"

SMAudioEffectProcessor::SMAudioEffectProcessor() {
    customeReverb = NULL;
    isCustomReverbEnable = false;
    mChannelCount = 0;
}
SMAudioEffectProcessor::~SMAudioEffectProcessor() {
    if (customeReverb) {
        customeReverb->Uninit();
        delete customeReverb;
        customeReverb = NULL;
    }
}

void SMAudioEffectProcessor::initEffect(int samplerate, int channelCount, int buffersize) {
    mChannelCount = channelCount;
    if (customeReverb == NULL) {
        customeReverb = new CReverb4();
        customeReverb->Init(samplerate, channelCount);
    }
}
void SMAudioEffectProcessor::onEffectSelect(int effect) {
    isCustomReverbEnable = false;
    needAutoTune = false;
    switch (effect) {
        case EFFECT_AUTOTUNE:
            /*打开AUTOTUNE功能,因为auto tune可能会和其他音效叠加
             * 并且处理方式和其他音效不一样，所以增加一个开关*/
            needAutoTune = true;
           // autotune->enableRetune(true);
            break;
        case EFFECT_KTV:
            isCustomReverbEnable = true;
            customeReverb->SetTypeId(KALA_VB_KTV_V40_QUICKLY);
            break;
        case EFFECT_DISTANT:
            isCustomReverbEnable = true;
            customeReverb->SetTypeId(KALA_VB_OLD_DISTANT_QUICKLY);
            break;
        case EFFECT_WARM:
            isCustomReverbEnable = true;
            customeReverb->SetTypeId(KALA_VB_WARM_QUICKLY);
            break;
        case EFFECT_PHONOGRAPH:
            isCustomReverbEnable = true;
            customeReverb->SetTypeId(KALA_VB_PHONOGRAPH_QUICKLY);
            break;
        case EFFECT_MAGNETIC:
            isCustomReverbEnable = true;
            customeReverb->SetTypeId(KALA_VB_MAGNETIC_QUICKLY);
            break;
        case EFFECT_ETHEREAL:
            isCustomReverbEnable = true;
            customeReverb->SetTypeId(KALA_VB_ETHEREAL_QUICKLY);
            break;
        case EFFECT_DIZZY:
            isCustomReverbEnable = true;
            customeReverb->SetTypeId(KALA_VB_DIZZY_QUICKLY);
            break;
        case EFFECT_NEW_DISTANT:
            isCustomReverbEnable = true;
            customeReverb->SetTypeId(KALA_VB_DISTANT_QUICKLY);
            break;
    };
}
void SMAudioEffectProcessor::Process(float* buffer, int32_t bufferSize, double displayPositionMs) {
    if (isCustomReverbEnable) {
        customeReverb->Process(buffer, bufferSize, buffer, bufferSize);
    }
}