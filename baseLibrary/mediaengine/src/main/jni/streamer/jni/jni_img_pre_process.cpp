//
// Created by 昝晓飞 on 16/7/26.
//
#include "jni_img_pre_process.h"
#include "util/DataConvertUtility.h"
#include "filter/img_buf/img_pre_process.h"
#include "log.h"

jlong JNICALL Java_com_zq_mediaengine_filter_imgbuf_ImgPreProcessWrap_create
  (JNIEnv *, jobject)
{
    ImgPreProcess* imgProcess = new ImgPreProcess();
    return (jlong)(intptr_t)imgProcess;
}

jobject JNICALL Java_com_zq_mediaengine_filter_imgbuf_ImgPreProcessWrap_doScale
  (JNIEnv *env, jobject thiz, jlong instance, jobject srcData)
{
    ImgPreProcess* imgPreProcess = (ImgPreProcess*)(intptr_t)instance;
    if(imgPreProcess == NULL) {
        LOGE("[doScale] do not create ImgPreProcess");
        return NULL;
    }

    ImageBufFrame* imageBufFrame = GetDataConvertUtilityInstance()->ConvertJImgBuf(env, srcData);
    ImageBufFrame* scaleBufFrame  = imgPreProcess->ProcessScale(imageBufFrame);
    imgPreProcess = NULL;
    return GetDataConvertUtilityInstance()->ConvertSTImgBuf(env, scaleBufFrame);
}

jobject JNICALL Java_com_zq_mediaengine_filter_imgbuf_ImgPreProcessWrap_doScaleAndConvert2RGBA
  (JNIEnv *env, jobject thiz, jlong instance, jobject srcData)
{
    ImgPreProcess* imgPreProcess = (ImgPreProcess*)(intptr_t)instance;
    if(imgPreProcess == NULL) {
        LOGE("[doScale] do not create ImgPreProcess");
        return NULL;
    }

    ImageBufFrame* imageBufFrame = GetDataConvertUtilityInstance()->ConvertJImgBuf(env, srcData);
    ImageBufFrame* scaleBufFrame  = imgPreProcess->ProcessScale(imageBufFrame);
    ImageBufFrame* rgbaBufFrame  = imgPreProcess->ConvertI420ToRGBA(scaleBufFrame);
    imgPreProcess = NULL;
    return GetDataConvertUtilityInstance()->ConvertSTImgBuf(env, rgbaBufFrame);
}

jobject JNICALL Java_com_zq_mediaengine_filter_imgbuf_ImgPreProcessWrap_doBeauty
  (JNIEnv *env, jobject thiz, jlong instance, jobject srcData)
{
    ImgPreProcess* imgPreProcess = (ImgPreProcess*)(intptr_t)instance;
    if(imgPreProcess == NULL) {
        LOGE("[doBeauty] do not create ImgPreProcess");
        return NULL;
    }

    ImageBufFrame* imageBufFrame = GetDataConvertUtilityInstance()->ConvertJImgBuf(env, srcData);
    ImageBufFrame* beautyBufFrame  = imgPreProcess->ProcessBeauty(imageBufFrame);
    imgPreProcess = NULL;
    return GetDataConvertUtilityInstance()->ConvertSTImgBuf(env, beautyBufFrame);
}

jobject JNICALL Java_com_zq_mediaengine_filter_imgbuf_ImgPreProcessWrap_doMixer
  (JNIEnv *env, jobject thiz, jlong instance, jobjectArray srcBufs, jint srcNum,jobjectArray
  srcConfigs, jint configNum)
{
    ImgPreProcess* imgPreProcess = (ImgPreProcess*)(intptr_t)instance;
    if(imgPreProcess == NULL) {
        LOGE("[doMixer] do not create ImgPreProcess");
        return NULL;
    }

   jobject jObj = NULL;
   ImageBufFrame* imageBufFrames[srcNum];
   for (int i = 0; i < srcNum; i++) {
        jObj = (env)->GetObjectArrayElement(srcBufs, i);
        if(jObj != NULL) {
            ImageBufFrame* imageBufFrame = GetDataConvertUtilityInstance()->ConvertJImgBuf(env, jObj);
            imageBufFrames[i] = imageBufFrame;
        } else {
            imageBufFrames[i] = NULL;
        }
        (env)->DeleteLocalRef(jObj);
   }

   ImageMixerConfig* imageMixerConfigs[configNum];
   for (int i = 0; i < configNum; i++) {
        jObj = (env)->GetObjectArrayElement(srcConfigs, i);
        if(jObj != NULL) {
            ImageMixerConfig* imgMixConfig = GetDataConvertUtilityInstance()->ConvertJImgMixConfig(env, jObj);
            imageMixerConfigs[i] = imgMixConfig;
        } else {
            imageMixerConfigs[i] = NULL;
        }
        (env)->DeleteLocalRef(jObj);
   }

   ImageBufFrame* mixerBufFrame =  imgPreProcess->ProcessMixer(imageBufFrames, srcNum, imageMixerConfigs, configNum);
   imgPreProcess = NULL;
   return GetDataConvertUtilityInstance()->ConvertSTImgBuf(env, mixerBufFrame);
}

void JNICALL Java_com_zq_mediaengine_filter_imgbuf_ImgPreProcessWrap_setTargetSize
  (JNIEnv *env, jobject thiz, jlong instance, jint w, jint h)
{
    ImgPreProcess* imgPreProcess = (ImgPreProcess*)(intptr_t)instance;
    if(imgPreProcess == NULL) {
        LOGE("[setTargetSize] do not create ImgPreProcess");
        return;
    }

    int width = w;
    int height = h;
    imgPreProcess->SetTargetSize(width, height);
    imgPreProcess = NULL;
    return;
}

void JNICALL Java_com_zq_mediaengine_filter_imgbuf_ImgPreProcessWrap_updateIsFrontMirror
(JNIEnv *env, jobject thiz, jlong instance, jboolean isFrontCamera)
{
    ImgPreProcess* imgPreProcess = (ImgPreProcess*)(intptr_t)instance;
    if(imgPreProcess == NULL) {
        LOGE("[updateIsFrontMirror] do not create ImgPreProcess");
        return;
    }
    imgPreProcess->SetIsFrontCameraMirror(isFrontCamera);
    imgPreProcess = NULL;
    return;
}

void JNICALL Java_com_zq_mediaengine_filter_imgbuf_ImgPreProcessWrap_setBeautyInfo
(JNIEnv *env, jobject thiz, jlong instance, jint jbeautyLevel)
{
    ImgPreProcess* imgPreProcess = (ImgPreProcess*)(intptr_t)instance;
    if(imgPreProcess == NULL) {
        LOGE("[setBeautyInfo] do not create ImgPreProcess");
        return;
    }

    int beautyLevel = jbeautyLevel;
    imgPreProcess->SetBeautyLevel(beautyLevel);
    imgPreProcess = NULL;
    return;
}

void JNICALL Java_com_zq_mediaengine_filter_imgbuf_ImgPreProcessWrap_releaseInfo
  (JNIEnv *env, jobject thiz, jlong instance)
{
    ImgPreProcess* imgPreProcess = (ImgPreProcess*)(intptr_t)instance;
    if(imgPreProcess == NULL) {
        LOGE("[releaseInfo] do not create ImgPreProcess");
        return;
    }

    imgPreProcess->Release();

    delete imgPreProcess;
    imgPreProcess = NULL;
    return;
}

void JNICALL Java_com_zq_mediaengine_filter_imgbuf_ImgPreProcessWrap_priteByteBuffer
  (JNIEnv *env, jobject thiz, jlong instance, jobject jextra)
{
     uint8_t* extra = (uint8_t*)env->GetDirectBufferAddress(jextra);
     int extraSize = (int)env->GetDirectBufferCapacity(jextra);

     LOGE("=================[priteByteBuffer]bufSize %d ", extraSize);
     for(int i = 0; i< extraSize; i++) {
          LOGE("=================[priteByteBuffer][ %d ]:%d",i, extra[i]);
     }
     return;
}
 
jobject JNICALL Java_com_zq_mediaengine_filter_imgbuf_ImgPreProcessWrap_convertI420ToNv21
 (JNIEnv *env, jobject thiz, jlong instance, jobject srcData)
{
    ImgPreProcess* imgPreProcess = (ImgPreProcess*)(intptr_t)instance;
    if(imgPreProcess == NULL) {
        LOGE("[convertI420ToNv21] do not create ImgPreProcess");
        return NULL;
    }

    ImageBufFrame* imageBufFrame = GetDataConvertUtilityInstance()->ConvertJImgBuf(env, srcData);
    ImageBufFrame* scaleBufFrame  = imgPreProcess->ConvertI420ToNV21(imageBufFrame);
    imgPreProcess = NULL;
    return GetDataConvertUtilityInstance()->ConvertSTImgBuf(env, scaleBufFrame);
}
 
void JNICALL Java_com_zq_mediaengine_filter_imgbuf_ImgPreProcessWrap_debugScaleFlag
   (JNIEnv *env, jobject thiz, jlong instance, jboolean isDebug)
{
    ImgPreProcess* imgPreProcess = (ImgPreProcess*)(intptr_t)instance;
    if(imgPreProcess == NULL) {
        LOGE("[debugScaleFlag] do not create ImgPreProcess");
        return;
    }

    imgPreProcess->DebugScale(isDebug);
    imgPreProcess = NULL;
    return;
}

void JNICALL Java_com_zq_mediaengine_filter_imgbuf_ImgPreProcessWrap_debugBeautyFlag
(JNIEnv *env, jobject thiz, jlong instance, jboolean isDebug)
{
    ImgPreProcess* imgPreProcess = (ImgPreProcess*)(intptr_t)instance;
    if(imgPreProcess == NULL) {
        LOGE("[debugScaleFlag] do not create ImgPreProcess");
        return;
    }

    imgPreProcess->DebugBeatuy(isDebug);
    imgPreProcess = NULL;
    return;
}

void JNICALL Java_com_zq_mediaengine_filter_imgbuf_ImgPreProcessWrap_debugMixerFlag
(JNIEnv *env, jobject thiz, jlong instance, jboolean isDebug)
{
    ImgPreProcess* imgPreProcess = (ImgPreProcess*)(intptr_t)instance;
    if(imgPreProcess == NULL) {
        LOGE("[debugScaleFlag] do not create ImgPreProcess");
        return;
    }

    imgPreProcess->DebugMixer(isDebug);
    imgPreProcess = NULL;
    return;
}