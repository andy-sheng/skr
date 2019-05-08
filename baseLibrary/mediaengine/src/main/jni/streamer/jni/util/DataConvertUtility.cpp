//
// Created by 昝晓飞 on 16/7/26.
//
#include <malloc.h>
#include <stdio.h> 
#include "DataConvertUtility.h"
#include "jni/util/jni_cache.h"
#include "log.h"

DataConvertUtility * DataConvertUtility::GetInstance( void )
{
    static DataConvertUtility instance;
    return &instance;
}

DataConvertUtility::DataConvertUtility( void )
{	

}

void DataConvertUtility::Release(void)
{

}

/*
*convert java ImgBufFrme to c ImageBufFrame
*/
ImageBufFrame* DataConvertUtility::ConvertJImgBuf(JNIEnv *env, jobject jImgBufFrame)
{
    if(env == NULL || jImgBufFrame == NULL) {
        LOGE("[DataConvertUtility][ConvertJImgBuf] the env or jImgBufFrame is NULL");
        return NULL;
    }
    GetJniCacheInstance()->CacheClass(env, JAVA_CLASS_PATH_IMGBUFFRAME_FORMAT);
    GetJniCacheInstance()->CacheClass(env, JAVA_CLASS_PATH_IMGBUFFRAME);

    ImageBufFrame* imagebufFrame = new ImageBufFrame();
    imagebufFrame->pts = JCOM_GET_FIELD_J(env, jImgBufFrame, JAVA_CLASS_PATH_IMGBUFFRAME, JAVA_IMGBUFFRAME_FIELD_PTS);
    imagebufFrame->dts = JCOM_GET_FIELD_J(env, jImgBufFrame, JAVA_CLASS_PATH_IMGBUFFRAME, JAVA_IMGBUFFRAME_FIELD_DTS);
    imagebufFrame->flags = JCOM_GET_FIELD_I(env, jImgBufFrame, JAVA_CLASS_PATH_IMGBUFFRAME, JAVA_IMGBUFFRAME_FIELD_FLAGS);
    //buf
    jobject  jextra = JCOM_GET_FIELD_L(env, jImgBufFrame, JAVA_CLASS_PATH_IMGBUFFRAME, JAVA_IMGBUFFRAME_FIELD_BUF);
    
    if(jextra == NULL) {
        return NULL;
    }

    uint8_t* extra = (uint8_t*)env->GetDirectBufferAddress(jextra);
    int extraSize = (int)env->GetDirectBufferCapacity(jextra);

    imagebufFrame->buf = extra;
    imagebufFrame->buf_size = extraSize;//JCOM_GET_FIELD_I(env, jImgBufFrame, JAVA_CLASS_PATH_IMGBUFFRAME, JAVA_IMGBUFFRAME_FIELD_BUFSIZE);

    jobject jformat = JCOM_GET_FIELD_L(env, jImgBufFrame, JAVA_CLASS_PATH_IMGBUFFRAME, JAVA_IMGBUFFRAME_FIELD_FORMAT);

    if(jformat == NULL) {
        return NULL;
    }
    imagebufFrame->width = JCOM_GET_FIELD_I(env, jformat, JAVA_CLASS_PATH_IMGBUFFRAME_FORMAT, JAVA_IMGBUFFRAME_FORMAT_FIELD_WIDTH);
    imagebufFrame->height = JCOM_GET_FIELD_I(env, jformat, JAVA_CLASS_PATH_IMGBUFFRAME_FORMAT, JAVA_IMGBUFFRAME_FORMAT_FIELD_HEIGHT);
    imagebufFrame->orientation = JCOM_GET_FIELD_I(env, jformat, JAVA_CLASS_PATH_IMGBUFFRAME_FORMAT, JAVA_IMGBUFFRAME_FORMAT_FIELD_ORIENTATION);
    imagebufFrame->format = JCOM_GET_FIELD_I(env, jformat, JAVA_CLASS_PATH_IMGBUFFRAME_FORMAT, JAVA_IMGBUFFRAME_FORMAT_FIELD_FORMAT);

    if(imagebufFrame->format == FMT_NV21){
        imagebufFrame->channels = 2; //TODO,其它format和channel的对照关系FMT_NV21??
    }

    int strideNum = JCOM_GET_FIELD_I(env, jformat, JAVA_CLASS_PATH_IMGBUFFRAME_FORMAT, JAVA_IMGBUFFRAME_FORMAT_FIELD_STRIDENUMER);
    if(strideNum > 0) {
        jintArray jStrideArray = (jintArray)JCOM_GET_FIELD_L(env, jformat, JAVA_CLASS_PATH_IMGBUFFRAME_FORMAT, JAVA_IMGBUFFRAME_FORMAT_FIELD_STRIDE);
        if(jStrideArray != NULL) {
            jint* stride = env->GetIntArrayElements(jStrideArray, NULL);
            imagebufFrame->stride = (int*)malloc(sizeof(int)*strideNum);
            for(int i = 0; i < strideNum; i++) {
                imagebufFrame->stride[i] = stride[i];
            }

            env->ReleaseIntArrayElements(jStrideArray,stride,0);
            stride = NULL;
        }
        imagebufFrame->strideNum = strideNum;
    } else {
        imagebufFrame->stride = NULL;
        imagebufFrame->strideNum = 0;
    }
    
    extra = NULL;
    return imagebufFrame;
}

/**
* convert java ImgBufferConfig to c ImageMixerConfig
*/
ImageMixerConfig* DataConvertUtility::ConvertJImgMixConfig(JNIEnv *env, jobject jconfig)
{
    if(env == NULL || jconfig == NULL) {
        LOGE("[DataConvertUtility][ConvertJImgMixConfig] the env or jconfig is null");
        return NULL;
    }

    GetJniCacheInstance()->CacheClass(env, JAVA_CLASS_PATH_IMGBUFMIXERCONFIG);
    ImageMixerConfig* config = new ImageMixerConfig();
    config->x = JCOM_GET_FIELD_I(env, jconfig, JAVA_CLASS_PATH_IMGBUFMIXERCONFIG, JAVA_IMGBUFMIXERCONFIG_FIELD_X);
    config->y = JCOM_GET_FIELD_I(env, jconfig, JAVA_CLASS_PATH_IMGBUFMIXERCONFIG, JAVA_IMGBUFMIXERCONFIG_FIELD_Y);
    config->w = JCOM_GET_FIELD_I(env, jconfig, JAVA_CLASS_PATH_IMGBUFMIXERCONFIG, JAVA_IMGBUFMIXERCONFIG_FIELD_W);
    config->h = JCOM_GET_FIELD_I(env, jconfig, JAVA_CLASS_PATH_IMGBUFMIXERCONFIG, JAVA_IMGBUFMIXERCONFIG_FIELD_H);
    config->alpha = JCOM_GET_FIELD_I(env, jconfig, JAVA_CLASS_PATH_IMGBUFMIXERCONFIG, JAVA_IMGBUFMIXERCONFIG_FIELD_ALPHA);
    return config;
}

/**
* convert java AudioBufFrame to c AudioResampleBuf
*/
AudioResampleBuf* DataConvertUtility::ConvertJAudioBuf(JNIEnv *env, jobject inFormat, jobject outFormat)
{
    if(env == NULL || inFormat == NULL || outFormat == NULL) {
        LOGE("[DataConvertUtility][ConvertJAudioBuf] the env or jconfig is null");
    }
    GetJniCacheInstance()->CacheClass(env, JAVA_CLASS_PATH_AUDIOBUFFORMAT);

    AudioResampleBuf* audioBuf = new AudioResampleBuf();

    audioBuf->inSampleFmt = JCOM_GET_FIELD_I(env, inFormat, JAVA_CLASS_PATH_AUDIOBUFFORMAT, JAVA_AUDIOBUFFORMAT_FIELD_FORMAT);
    audioBuf->inSampleRate = JCOM_GET_FIELD_I(env, inFormat, JAVA_CLASS_PATH_AUDIOBUFFORMAT, JAVA_AUDIOBUFFORMAT_FIELD_RATE);
    audioBuf->inChnNum = JCOM_GET_FIELD_I(env, inFormat, JAVA_CLASS_PATH_AUDIOBUFFORMAT, JAVA_AUDIOBUFFORMAT_FIELD_CHANNELS);
    audioBuf->outSampleFmt = JCOM_GET_FIELD_I(env, outFormat, JAVA_CLASS_PATH_AUDIOBUFFORMAT, JAVA_AUDIOBUFFORMAT_FIELD_FORMAT);
    audioBuf->outSampleRate = JCOM_GET_FIELD_I(env, outFormat, JAVA_CLASS_PATH_AUDIOBUFFORMAT, JAVA_AUDIOBUFFORMAT_FIELD_RATE);
    audioBuf->outChnNum = JCOM_GET_FIELD_I(env, outFormat, JAVA_CLASS_PATH_AUDIOBUFFORMAT, JAVA_AUDIOBUFFORMAT_FIELD_CHANNELS);

    return audioBuf;
}

/**
* convert java AudioBufFrame to c AudioBufFrame
*/
AudioBufFormat* DataConvertUtility::ConvertJAudioBuf(JNIEnv *env, jobject inFormat)
{
    if(env == NULL || inFormat == NULL) {
            LOGE("[DataConvertUtility][ConvertJAudioBuf] the env or inFormat is null");
    }

    GetJniCacheInstance()->CacheClass(env, JAVA_CLASS_PATH_AUDIOBUFFORMAT);

    AudioBufFormat* audioBuf = new AudioBufFormat();
    audioBuf->sampleFormat = JCOM_GET_FIELD_I(env, inFormat, JAVA_CLASS_PATH_AUDIOBUFFORMAT,
                                JAVA_AUDIOBUFFORMAT_FIELD_FORMAT);
    audioBuf->sampleRate = JCOM_GET_FIELD_I(env, inFormat, JAVA_CLASS_PATH_AUDIOBUFFORMAT,
                                JAVA_AUDIOBUFFORMAT_FIELD_RATE);
    audioBuf->channels = JCOM_GET_FIELD_I(env, inFormat, JAVA_CLASS_PATH_AUDIOBUFFORMAT,
                                JAVA_AUDIOBUFFORMAT_FIELD_CHANNELS);
    audioBuf->codecId = JCOM_GET_FIELD_I(env, inFormat, JAVA_CLASS_PATH_AUDIOBUFFORMAT,
                                    JAVA_AUDIOBUFFORMAT_FIELD_CODECID);

    return audioBuf;
}

/**
* convert c ImageBufFrame to java ImageBufFrame
*/
jobject DataConvertUtility::ConvertSTImgBuf(JNIEnv *env, ImageBufFrame* stImagBuf)
{
    if(env == NULL || stImagBuf == NULL) {
        LOGW("DataConvertUtility::ConvertSTImgBuf env or stImageBuf is null");
        return NULL;
    }

    GetJniCacheInstance()->CacheClass(env, JAVA_CLASS_PATH_IMGBUFFRAME);
    GetJniCacheInstance()->CacheClass(env, JAVA_CLASS_PATH_IMGBUFFRAME_FORMAT);

    jclass jImgBuf = JCOM_FIND_CLASS(env, JAVA_CLASS_PATH_IMGBUFFRAME);
    jclass jImgBufFormat = JCOM_FIND_CLASS(env, JAVA_CLASS_PATH_IMGBUFFRAME_FORMAT);

    jmethodID jmImgBufConstruct = JCOM_GET_METHOD_ID(env, JAVA_CLASS_PATH_IMGBUFFRAME, JAVA_IMGBUFFRAME_METHOD_CONSTRUCT);
    jmethodID jmImgBufFormatConstruct = JCOM_GET_METHOD_ID(env, JAVA_CLASS_PATH_IMGBUFFRAME_FORMAT, JAVA_IMGBUFFRAME_FORMAT_METHOD_CONSTRUCT);


    jobject imgBuf = env->NewObject(jImgBuf, jmImgBufConstruct);
    jobject imgBufFormat = env->NewObject(jImgBufFormat, jmImgBufFormatConstruct);

    JCOM_SET_FIELD_J(env, imgBuf, JAVA_CLASS_PATH_IMGBUFFRAME, JAVA_IMGBUFFRAME_FIELD_PTS, stImagBuf->pts);
    JCOM_SET_FIELD_J(env, imgBuf, JAVA_CLASS_PATH_IMGBUFFRAME, JAVA_IMGBUFFRAME_FIELD_DTS, stImagBuf->dts);
    JCOM_SET_FIELD_I(env, imgBuf, JAVA_CLASS_PATH_IMGBUFFRAME, JAVA_IMGBUFFRAME_FIELD_FLAGS, stImagBuf->flags);

    JCOM_SET_FIELD_I(env, imgBufFormat, JAVA_CLASS_PATH_IMGBUFFRAME_FORMAT, JAVA_IMGBUFFRAME_FORMAT_FIELD_WIDTH, stImagBuf->width);
    JCOM_SET_FIELD_I(env, imgBufFormat, JAVA_CLASS_PATH_IMGBUFFRAME_FORMAT, JAVA_IMGBUFFRAME_FORMAT_FIELD_HEIGHT, stImagBuf->height);
    JCOM_SET_FIELD_I(env, imgBufFormat, JAVA_CLASS_PATH_IMGBUFFRAME_FORMAT, JAVA_IMGBUFFRAME_FORMAT_FIELD_ORIENTATION, stImagBuf->orientation);
    JCOM_SET_FIELD_I(env, imgBufFormat, JAVA_CLASS_PATH_IMGBUFFRAME_FORMAT, JAVA_IMGBUFFRAME_FORMAT_FIELD_FORMAT, stImagBuf->format);

    jintArray stride = env->NewIntArray(stImagBuf->strideNum);
    env->SetIntArrayRegion(stride, 0, stImagBuf->strideNum, stImagBuf->stride);
    JCOM_SET_FIELD_L(env, imgBufFormat, JAVA_CLASS_PATH_IMGBUFFRAME_FORMAT, JAVA_IMGBUFFRAME_FORMAT_FIELD_STRIDE, stride);
    JCOM_SET_FIELD_I(env, imgBufFormat, JAVA_CLASS_PATH_IMGBUFFRAME_FORMAT, JAVA_IMGBUFFRAME_FORMAT_FIELD_STRIDENUMER, stImagBuf->strideNum);

    JCOM_SET_FIELD_L(env, imgBuf, JAVA_CLASS_PATH_IMGBUFFRAME, JAVA_IMGBUFFRAME_FIELD_FORMAT, imgBufFormat);
    //set Buf
    jobject outBuffer = env->NewDirectByteBuffer(stImagBuf->buf, stImagBuf->buf_size);

    if(outBuffer != NULL) {
        JCOM_SET_FIELD_L(env, imgBuf, JAVA_CLASS_PATH_IMGBUFFRAME, JAVA_IMGBUFFRAME_FIELD_BUF, outBuffer);
        //JCOM_SET_FIELD_I(env, imgBuf, JAVA_CLASS_PATH_IMGBUFFRAME, JAVA_IMGBUFFRAME_FIELD_BUFSIZE, stImagBuf->buf_size);
    }

    //delete local ref
    env->DeleteLocalRef(stride);
    env->DeleteLocalRef(jImgBuf);
    env->DeleteLocalRef(jImgBufFormat);
    return imgBuf;
}

DataConvertUtility *GetDataConvertUtilityInstance()
{
   return DataConvertUtility::GetInstance();
}
