#include "./audio_effect_filter_factory.h"

#define LOG_TAG "AudioEffectFilterFactory"

AudioEffectFilterFactory::AudioEffectFilterFactory() {
}

AudioEffectFilterFactory::~AudioEffectFilterFactory() {
}
//初始化静态成员
AudioEffectFilterFactory* AudioEffectFilterFactory::instance = new AudioEffectFilterFactory();
AudioEffectFilterFactory* AudioEffectFilterFactory::GetInstance() {
	return instance;
}

AudioEffectFilter* AudioEffectFilterFactory::buildFilter(EffectFilterType type) {
	AudioEffectFilter* ret = NULL;
	switch (type) {
	case VocalAGCVolumeAdjustEffectFilterType:
		//人声自动音量控制
		ret = new VocalAGCVolumeAdjustEffectFilter();
		break;
	case AccompanyAGCVolumeAdjustEffectFilterType:
		//伴奏自动音量控制
		ret = new AccompanyAGCVolumeAdjustEffectFilter();
		break;
	case CompressorFilterType:
		//压缩效果器
		ret = new CompressorEffectFilter();
		break;
	case EqualizerFilterType:
		//均衡效果器
		ret = new EqualizerEffectFilter();
		break;
	case ReverbEchoFilterType:
		//混响+Echo效果器
		ret = new ReverbEffectFilter();
		break;
	case Mono2StereoFilterType:
		//单声道转双声道效果器
		ret = new Mono2StereoEffectFilter();
		break;
	case VocalVolumeAdjustFilterType:
		//人声音量增益效果器
		ret = new VocalVolumeAdjustEffectFilter();
		break;
	case AccompanyVolumeAdjustFilterType:
		//伴奏音量增益效果器
		ret = new AccompanyVolumeAdjustEffectFilter();
		break;
	case FadeOutEffectFilterType:
		//淡出效果器
		ret = new FadeOutEffectFilter();
		break;
	case VocalVolumeBalanceAdjustFilterType:
		//7.0新增人声音量增益效果器
		ret = new VocalVolumeBalenceAdjustEffectFilter();
		break;
	case AccompanyVolumeBalanceAdjustFilterType:
		//7.0新增伴奏音量增益效果器
		ret = new AccompanyVolumeBalenceAdjustEffectFilter();
		break;
	case StereoReverbEchoFilterType:
		ret = new StereoReverbEffectFilter();
		break;
	}
	return ret;
}
