#include "./audio_effect_processor_factory.h"

#define LOG_TAG "AudioEffectProcessorFactory"

AudioEffectProcessorFactory::AudioEffectProcessorFactory() {
}

AudioEffectProcessorFactory::~AudioEffectProcessorFactory() {
}
//初始化静态成员
AudioEffectProcessorFactory* AudioEffectProcessorFactory::instance = new AudioEffectProcessorFactory();
AudioEffectProcessorFactory* AudioEffectProcessorFactory::GetInstance() {
	return instance;
}

AudioEffectProcessor* AudioEffectProcessorFactory::buildAccompanyEffectProcessor() {
	return new AccompanyEffectProcessor();
}

AudioEffectProcessor* AudioEffectProcessorFactory::buildLiveAudioEffectProcessor() {
	return new AudioEffectLiveProcessor();
}
