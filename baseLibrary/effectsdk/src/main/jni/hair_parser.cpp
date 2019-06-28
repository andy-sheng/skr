
#include "bef_effect_ai_hairparser.h"
#include "byted_effect.h"
#include <jni.h>
#include <string.h>

static const char *ParseHairCLASS = "com/bytedance/labcv/effectsdk/HairParser";


static struct {
    jclass clazz;
    jfieldID buffer;
    jfieldID width;
    jfieldID height;
    jfieldID channel;
} HAIR_MASK;

static jint
nativeCreateHandle(JNIEnv *env, jobject thiz) {
    jclass clazz = env->FindClass(ParseHairCLASS);
    jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
    env->DeleteLocalRef(clazz);
    bef_effect_handle_t handle = NULL;
    bef_effect_result_t result = bef_effect_ai_hairparser_create(&handle);
    if (result == BEF_RESULT_SUC) {
        env->SetLongField(thiz, field, (jlong) (handle));
    }
    return result;
}

static jint
nativeInit(JNIEnv *env, jobject thiz, jstring strModelFile) {

    const char *modelFile = env->GetStringUTFChars(strModelFile, 0);

    jclass clazz = env->FindClass(ParseHairCLASS);
    jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
    env->DeleteLocalRef(clazz);
    bef_effect_handle_t handle = (bef_effect_handle_t) env->GetLongField(thiz, field);
    if (handle == NULL) {
        return BEF_RESULT_INVALID_EFFECT_HANDLE;
    }
    bef_effect_result_t result =  bef_effect_ai_hairparser_init_model(handle, modelFile);

    env->ReleaseStringUTFChars(strModelFile, modelFile);

    return result;
}

static jint
nativeGetShape(JNIEnv *env, jobject thiz,jintArray array){
    jclass clazz = env->FindClass(ParseHairCLASS);
    jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
    env->DeleteLocalRef(clazz);
    bef_effect_handle_t handle = (bef_effect_handle_t) env->GetLongField(thiz, field);

    if (handle == NULL) {
        return BEF_RESULT_INVALID_EFFECT_HANDLE;
    }
    jint *pArray = env->GetIntArrayElements(array, NULL);
    bef_effect_result_t  ret = bef_effect_ai_hairparser_get_output_shape(handle,pArray, pArray+ 1, pArray + 2);
    env->ReleaseIntArrayElements(array,pArray, 0);
    return ret;

}


static jint
nativeParse(JNIEnv *env, jobject thiz, jobject buffer, jint pixel_format,
              jint image_width, jint image_height,
              jint image_stride, jint orientation, jboolean maskflip,
              jbyteArray outputMask) {

    jclass clazz = env->FindClass(ParseHairCLASS);
    jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
    env->DeleteLocalRef(clazz);

    bef_effect_handle_t handle = (bef_effect_handle_t) env->GetLongField(thiz, field);

    if (handle == NULL) {
        return BEF_RESULT_INVALID_EFFECT_HANDLE;
    }
    jbyte * pMask =env->GetByteArrayElements(outputMask, NULL);

    const unsigned char *image = (const unsigned char *) env->GetDirectBufferAddress(buffer);
    if (image == NULL){
        return BEF_RESULT_INVALID_IMAGE_DATA;
    }
    bef_effect_result_t result = bef_effect_ai_hairparser_do_detect(handle,image,(bef_ai_pixel_format)pixel_format,image_width, image_height, image_stride, (bef_ai_rotate_type)orientation,(unsigned char*)pMask, maskflip);


    return result;
}


static jint JNICALL
nativeCheckLicense(JNIEnv *env,  jobject thiz, jobject context, jstring strLicense) {
    const char *license = env->GetStringUTFChars(strLicense, 0);

    jclass clazz = env->FindClass(ParseHairCLASS);
    jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
    env->DeleteLocalRef(clazz);
    bef_effect_handle_t handle = (bef_effect_handle_t) env->GetLongField(thiz, field);

    if (handle == NULL) {
        return BEF_RESULT_INVALID_EFFECT_HANDLE;
    }

    bef_effect_result_t result = bef_effect_ai_hairparser_check_license(env, context, handle, license);

    env->ReleaseStringUTFChars(strLicense, license);

    return  result;
}

static jint
nativeSetParam(JNIEnv *env, jobject thiz, jint netInputWidth,jint netInputHeight,jboolean useTracking, jboolean useBlur) {
    jclass clazz = env->FindClass(ParseHairCLASS);
    jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
    env->DeleteLocalRef(clazz);
    bef_effect_handle_t handle = (bef_effect_handle_t) env->GetLongField(thiz, field);

    if (handle == NULL) {
        return BEF_RESULT_INVALID_EFFECT_HANDLE;
    }
    return bef_effect_ai_hairparser_set_param(handle, netInputWidth, netInputHeight, useTracking, useBlur);

}


static void
nativeRelease(JNIEnv *env, jobject thiz) {
    jclass clazz = env->FindClass(ParseHairCLASS);
    jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
    env->DeleteLocalRef(clazz);

    bef_effect_handle_t handle = (bef_effect_handle_t) env->GetLongField(thiz, field);

    if (handle != NULL) {
        bef_effect_ai_hairparser_destroy(handle);
        env->SetLongField(thiz, field, 0);
    }
}

static JNINativeMethod gPortraitMethods[] = {
        {"nativeCreateHandle",   "()I",  (void *) nativeCreateHandle},
        {"nativeInit",   "(Ljava/lang/String;)I",  (void *) nativeInit},
        {"nativeGetShape", "([I)I",  (void *) nativeGetShape},
        {"nativeParse", "(Ljava/nio/ByteBuffer;IIIIIZ[B)I", (void *) nativeParse},
        {"nativeRelease", "()I",  (void *) nativeRelease},
        {"nativeCheckLicense", "(Landroid/content/Context;Ljava/lang/String;)I", (void *) nativeCheckLicense},
        {"nativeSetParam", "(IIZZ)I", (void *) nativeSetParam},
};


int  register_hair_parser(JNIEnv* env)
{
    jclass clazz = env->FindClass(ParseHairCLASS);

    jclass hairmaskClazz = env->FindClass("com/bytedance/labcv/effectsdk/HairParser$HairMask");
    HAIR_MASK.clazz = (jclass) env->NewGlobalRef(hairmaskClazz);
    HAIR_MASK.buffer = env->GetFieldID(HAIR_MASK.clazz, "buffer","[B");
    HAIR_MASK.width = env->GetFieldID(HAIR_MASK.clazz, "width","I");
    HAIR_MASK.height = env->GetFieldID(HAIR_MASK.clazz, "height","I");
    HAIR_MASK.channel = env->GetFieldID(HAIR_MASK.clazz, "height","I");
    env->DeleteLocalRef(hairmaskClazz);
    return env->RegisterNatives(clazz, gPortraitMethods, NELEMS(gPortraitMethods));
}