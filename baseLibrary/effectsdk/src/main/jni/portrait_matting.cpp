
#include "bef_effect_ai_portrait_matting.h"
#include "byted_effect.h"
#include <jni.h>
#include <string.h>
#include <cstdlib>

static const char *PortraitCLASS = "com/bytedance/labcv/effectsdk/PortraitMatting";


static struct {
  jclass clazz;
  jfieldID buffer;
  jfieldID width;
  jfieldID height;
} MATTING_MASK;


static jint
nativeCreateHandle(JNIEnv *env, jobject thiz) {
  jclass clazz = env->FindClass(PortraitCLASS);
  jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
  env->DeleteLocalRef(clazz);
  bef_effect_handle_t handle = NULL;
  bef_effect_result_t result = bef_effect_ai_portrait_matting_create(&handle);
  if (result == BEF_RESULT_SUC) {
    env->SetLongField(thiz, field, (jlong) (handle));
  }
  else
  {
    LOGE("native create portraitmatting handle error %d \n", result);
  }
  return result;
}

static jint
nativeInit(JNIEnv *env, jobject thiz, jstring strModelFile, jint  modelType) {

  const char *modelFile = env->GetStringUTFChars(strModelFile, 0);

  jclass clazz = env->FindClass(PortraitCLASS);
  jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
  env->DeleteLocalRef(clazz);
  bef_effect_handle_t handle = (bef_effect_handle_t) env->GetLongField(thiz, field);
  if (handle == NULL) {
    return BEF_RESULT_FAIL;
  }
  bef_effect_result_t result =  bef_effect_ai_portrait_matting_init_model(handle, (bef_ai_matting_model_type)modelType, modelFile);

  env->ReleaseStringUTFChars(strModelFile, modelFile);

  return result;
}


static jint
nativeMatting(JNIEnv *env, jobject thiz, jobject buffer, jint pixel_format,
               jint image_width, jint image_height,
               jint image_stride, jint orientation, jboolean maskflip,
               jobject outputMask) {

  jclass clazz = env->FindClass(PortraitCLASS);
  jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
  env->DeleteLocalRef(clazz);

  bef_effect_handle_t handle = (bef_effect_handle_t) env->GetLongField(thiz, field);

  if (handle == NULL) {
    return BEF_RESULT_INVALID_EFFECT_HANDLE;
  }

  const unsigned char *image = (const unsigned char *) env->GetDirectBufferAddress(buffer);
  bef_ai_matting_ret mattingRet;
  int outputWidth = 0;
  int outputHeight = 0;
  bef_effect_result_t result = bef_effect_ai_portrait_get_output_shape(handle,image_width, image_height,&outputWidth, &outputHeight );
  if (result != BEF_RESULT_SUC) {
    LOGE("bef_effect_ai_portrait_get_output_shape return %d", result);
    return result;
  }
  mattingRet.alpha = new unsigned char[outputWidth * outputHeight];
  result = bef_effect_ai_portrait_matting_do_detect(handle,image, (bef_ai_pixel_format)pixel_format, image_width, image_height,image_stride,(bef_ai_rotate_type)orientation,maskflip , &mattingRet);
  if(result == BEF_RESULT_SUC){
    jbyteArray maskbufferobj = env->NewByteArray(mattingRet.width * mattingRet.height);
    env->SetByteArrayRegion(maskbufferobj, 0, mattingRet.width * mattingRet.height, (jbyte *)mattingRet.alpha);
    delete [] mattingRet.alpha;
    env->SetObjectField(outputMask, MATTING_MASK.buffer, maskbufferobj);
    env->SetIntField(outputMask, MATTING_MASK.width, mattingRet.width);
    env->SetIntField(outputMask, MATTING_MASK.height, mattingRet.height);
    env->DeleteLocalRef(maskbufferobj);
  } else{
    LOGE("bef_effect_ai_portrait_matting_do_detect return %d", result);
  }
  return result;
}


static jint JNICALL
nativeCheckLicense(JNIEnv *env,  jobject thiz, jobject context, jstring strLicense) {
  const char *license = env->GetStringUTFChars(strLicense, 0);

  jclass clazz = env->FindClass(PortraitCLASS);
  jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
  env->DeleteLocalRef(clazz);
  bef_effect_handle_t handle = (bef_effect_handle_t) env->GetLongField(thiz, field);

  if (handle == NULL) {
    return BEF_RESULT_INVALID_EFFECT_HANDLE;
  }

  bef_effect_result_t result = bef_effect_ai_matting_check_license(env, context, handle, license);

  env->ReleaseStringUTFChars(strLicense, license);

  return  result;
}

static jint
nativeSetParam(JNIEnv *env, jobject thiz, jint paramType, jint paramValue) {
  jclass clazz = env->FindClass(PortraitCLASS);
  jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
  env->DeleteLocalRef(clazz);
  bef_effect_handle_t handle = (bef_effect_handle_t) env->GetLongField(thiz, field);

  if (handle == NULL) {
    return BEF_RESULT_INVALID_EFFECT_HANDLE;
  }
  return bef_effect_ai_portrait_matting_set_param(handle, (bef_ai_matting_param_type)paramType, paramValue);
}


static void
nativeRelease(JNIEnv *env, jobject thiz) {
  jclass clazz = env->FindClass(PortraitCLASS);
  jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
  env->DeleteLocalRef(clazz);

  bef_effect_handle_t handle = (bef_effect_handle_t) env->GetLongField(thiz, field);

  if (handle != NULL) {
    bef_effect_ai_portrait_matting_destroy(handle);
    env->SetLongField(thiz, field, 0);
  }
}

static JNINativeMethod gPortraitMethods[] = {
    {"nativeCreateHandle",   "()I",  (void *) nativeCreateHandle},
    {"nativeInit",   "(Ljava/lang/String;I)I",  (void *) nativeInit},
    {"nativeMatting", "(Ljava/nio/ByteBuffer;IIIIIZLcom/bytedance/labcv/effectsdk/PortraitMatting$MattingMask;)I", (void *) nativeMatting},
    {"nativeRelease", "()I",  (void *) nativeRelease},
    {"nativeCheckLicense", "(Landroid/content/Context;Ljava/lang/String;)I", (void *) nativeCheckLicense},
    {"nativeSetParam", "(II)I", (void *) nativeSetParam},
};


int  register_portait_matting_detect(JNIEnv* env)
{
  jclass clazz = env->FindClass(PortraitCLASS);

  jclass mattingmaskClazz = env->FindClass("com/bytedance/labcv/effectsdk/PortraitMatting$MattingMask");
  MATTING_MASK.clazz = (jclass) env->NewGlobalRef(mattingmaskClazz);
  MATTING_MASK.buffer = env->GetFieldID(MATTING_MASK.clazz, "buffer","[B");
  MATTING_MASK.width = env->GetFieldID(MATTING_MASK.clazz, "width","I");
  MATTING_MASK.height = env->GetFieldID(MATTING_MASK.clazz, "height","I");
  env->DeleteLocalRef(mattingmaskClazz);
  return env->RegisterNatives(clazz, gPortraitMethods, NELEMS(gPortraitMethods));
}