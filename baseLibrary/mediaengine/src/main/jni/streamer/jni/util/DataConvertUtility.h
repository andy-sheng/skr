//
//用于java类和c类之间互相转换
//

#ifndef KSYSTREAMERANDROID_DATACONVERTUTILITY_H
#define KSYSTREAMERANDROID_DATACONVERTUTILITY_H
#include <jni.h>
#include "framework/ImageBufDef.h"
#include "framework/ImageMixerConfig.h"
#include "framework/AudioResampleBuf.h"
#include "framework/AudioBufFormat.h"
#include "watermark.h"

class DataConvertUtility {
public:
    virtual ~DataConvertUtility( void ) {};

    static DataConvertUtility * GetInstance();
    void Release(void);

    ImageBufFrame* ConvertJImgBuf(JNIEnv *env, jobject object);
    ImageMixerConfig* ConvertJImgMixConfig(JNIEnv *env, jobject object);
    AudioResampleBuf* ConvertJAudioBuf(JNIEnv *env, jobject inFormat, jobject outFormat);
    AudioBufFormat* ConvertJAudioBuf(JNIEnv *env, jobject inFormat);

    jobject ConvertSTImgBuf(JNIEnv *env, ImageBufFrame* stImagBuf);
protected:
    DataConvertUtility( void );
};

DataConvertUtility *GetDataConvertUtilityInstance();
#endif //KSYSTREAMERANDROID_DATACONVERTUTILITY_H
