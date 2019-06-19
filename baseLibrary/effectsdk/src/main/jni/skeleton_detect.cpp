
#include "bef_effect_ai_skeleton.h"

#include "byted_effect.h"
#include <jni.h>
#include <string.h>
#include <cstdlib>

static const char *CLASS = "com/bytedance/labcv/effectsdk/SkeletonDetect";



static struct {
  jclass clazz;
  jfieldID skeletons;
  jfieldID skeletonNum;
} SKELETON_INFO;

static struct
{
  jclass clazz;
  jmethodID constructor;
  jfieldID keypoints;
} SKELETON;

static struct
{
  jclass clazz;
  jmethodID constructor;
} SKELETONPOINT;

static jint
nativeInit(JNIEnv *env, jobject thiz, jstring strModelFile) {
  jclass clazz = env->FindClass(CLASS);
  jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
  env->DeleteLocalRef(clazz);

  const char *modelFile = env->GetStringUTFChars(strModelFile, 0);


  bef_effect_handle_t handle = NULL;
  bef_effect_result_t result = bef_effect_ai_skeleton_create(modelFile, &handle);
  if (result == BEF_RESULT_SUC) {
    env->SetLongField(thiz, field, (jlong) (handle));
  }
  env->ReleaseStringUTFChars(strModelFile, modelFile);

  return result;
}

static void
nativeRelease(JNIEnv *env, jobject thiz) {
  jclass clazz = env->FindClass(CLASS);
  jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
  env->DeleteLocalRef(clazz);

  bef_effect_handle_t handle = (bef_effect_handle_t) env->GetLongField(thiz, field);

  if (handle != NULL) {
    bef_effect_ai_skeleton_destroy(handle);
    env->SetLongField(thiz, field, 0);
  }
}

static jint JNICALL
nativeCheckLicense(JNIEnv *env,  jobject thiz, jobject context, jstring strLicense) {
  const char *license = env->GetStringUTFChars(strLicense, 0);

  jclass clazz = env->FindClass(CLASS);
  jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
  env->DeleteLocalRef(clazz);
  bef_effect_handle_t handle = (bef_effect_handle_t) env->GetLongField(thiz, field);

  if (handle == NULL) {
    return BEF_RESULT_INVALID_EFFECT_HANDLE;
  }

  bef_effect_result_t result = bef_effect_ai_skeleton_check_license(env, context, handle, license);

  env->ReleaseStringUTFChars(strLicense, license);

  return  result;
}

static jint
nativeSetParam(JNIEnv *env, jobject thiz, jint width, jint height) {
  jclass clazz = env->FindClass(CLASS);
  jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
  env->DeleteLocalRef(clazz);
  bef_effect_handle_t handle = (bef_effect_handle_t) env->GetLongField(thiz, field);

  if (handle == NULL) {
    return BEF_RESULT_INVALID_EFFECT_HANDLE;
  }
  return bef_effect_ai_skeleton_setparam(handle, width, height);

}



static jint
detectSkeleton(JNIEnv *env, jobject thiz, jobject buffer, jint pixel_format,
    jint image_width, jint image_height,
    jint image_stride, jint orientation, jobject info) {

jclass clazz = env->FindClass(CLASS);
jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
env->DeleteLocalRef(clazz);

bef_effect_handle_t handle = (bef_effect_handle_t) env->GetLongField(thiz, field);

if (handle == NULL) {
return BEF_RESULT_INVALID_EFFECT_HANDLE;
}

const unsigned char *image = (const unsigned char *) env->GetDirectBufferAddress(buffer);
if (image == NULL){
    return BEF_RESULT_INVALID_IMAGE_DATA;

}
int skeletonNum = env->GetIntField(info, SKELETON_INFO.skeletonNum);
bef_skeleton_info *skeleton_info = nullptr;
bef_effect_result_t result = bef_effect_ai_skeleton_detect(handle, image,
                                                       (bef_ai_pixel_format) pixel_format,
                                                       image_width, image_height,
                                                       image_stride,
                                                       (bef_ai_rotate_type) orientation,
                                                       &skeletonNum, &skeleton_info);
  if(skeleton_info != nullptr) {
  env->SetIntField(info, SKELETON_INFO.skeletonNum, skeletonNum);
  jobjectArray  jskeletons = env->NewObjectArray( skeletonNum ,SKELETON.clazz, NULL);

  for(int i = 0; i < skeletonNum; i++)
  {
    jobject  jskeleton = env->NewObject(SKELETON.clazz, SKELETON.constructor, NULL);
    jobjectArray  jpoints = env->NewObjectArray(BEF_MAX_SKELETON_POINT_NUM, SKELETONPOINT.clazz, NULL);
    for(int j = 0; j < BEF_MAX_SKELETON_POINT_NUM; j++)
    {
      jobject jpoint = env->NewObject(SKELETONPOINT.clazz, SKELETONPOINT.constructor,
                                      skeleton_info[i].keyPointInfos[j].x, skeleton_info[i].keyPointInfos[j].y,
                                      skeleton_info[i].keyPointInfos[j].is_detect == 0 ? false : true);
      env->SetObjectArrayElement(jpoints, j, jpoint);
    }
    env->SetObjectField(jskeleton, SKELETON.keypoints, jpoints);
    env->SetObjectArrayElement(jskeletons, i, jskeleton);
  }
  env->SetObjectField(info, SKELETON_INFO.skeletons, jskeletons);
  if(skeleton_info != nullptr)
    delete[] (skeleton_info);
  skeleton_info = NULL;
}
return result;
}



static JNINativeMethod gMethods[] = {
    {"nativeInit",   "(Ljava/lang/String;)I",                                                            (void *) nativeInit},
   {"nativeDetect", "(Ljava/nio/ByteBuffer;IIIIILcom/bytedance/labcv/effectsdk/BefSkeletonInfo;)I", (void *) detectSkeleton},
    {"nativeRelease", "()V",                                                                              (void *) nativeRelease},
    {"nativeCheckLicense", "(Landroid/content/Context;Ljava/lang/String;)I",
                                                                                                          (void *) nativeCheckLicense},
    {"nativeSetParam", "(II)I",
                                                                                                          (void *) nativeSetParam},
};

int  register_skeleton_detect(JNIEnv* env)
{
  jclass clazz = env->FindClass(CLASS);

  jclass skeletonfoClazz = env->FindClass("com/bytedance/labcv/effectsdk/BefSkeletonInfo");
  SKELETON_INFO.clazz = (jclass) env->NewGlobalRef(skeletonfoClazz);
  SKELETON_INFO.skeletons = env->GetFieldID(SKELETON_INFO.clazz, "skeletons",
                                       "[Lcom/bytedance/labcv/effectsdk/BefSkeletonInfo$Skeleton;");
  SKELETON_INFO.skeletonNum = env->GetFieldID(SKELETON_INFO.clazz, "skeletonNum", "I");
  env->DeleteLocalRef(skeletonfoClazz);

  jclass skeletonClazz = env->FindClass("com/bytedance/labcv/effectsdk/BefSkeletonInfo$Skeleton");
  SKELETON.clazz = (jclass) env->NewGlobalRef(skeletonClazz);
  SKELETON.constructor = env->GetMethodID(SKELETON.clazz, "<init>", "()V");
  SKELETON.keypoints = env->GetFieldID(SKELETON.clazz, "keypoints",
                                 "[Lcom/bytedance/labcv/effectsdk/BefSkeletonInfo$SkeletonPoint;");
  env->DeleteLocalRef(skeletonClazz);

  jclass skeletonpointClazz = env->FindClass("com/bytedance/labcv/effectsdk/BefSkeletonInfo$SkeletonPoint");
  SKELETONPOINT.clazz = (jclass) env->NewGlobalRef(skeletonpointClazz);
  SKELETONPOINT.constructor = env->GetMethodID(SKELETONPOINT.clazz, "<init>", "(FFZ)V");
  env->DeleteLocalRef(skeletonpointClazz);

  return env->RegisterNatives(clazz, gMethods, NELEMS(gMethods));
}