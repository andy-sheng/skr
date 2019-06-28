// Copyright (C) 2019 Beijing Bytedance Network Technology Co., Ltd.

#include "byted_effect.h"
#include <jni.h>
#include <string.h>
#include <bef_effect_ai_hand.h>


static const char *CLASS = "com/bytedance/labcv/effectsdk/HandDetect";

static struct{
    jclass clazz;

    jfieldID hands;
    jfieldID count;
}HAND_INFO;

static struct{
    jclass clazz;
    jmethodID constructor;

    jfieldID ID;
    jfieldID rect;
    jfieldID action;
    jfieldID rotAngle;
    jfieldID score;
    jfieldID rotAngleBothhand;
    jfieldID keyPoints;
    jfieldID keyPointsExt;
    jfieldID seqAction;
}HAND;

static struct {
    jclass clazz;
    jmethodID constructor;
}RECT;

static struct {
    jclass clazz;
    jmethodID constructor;
}KEY_POINT;

bef_effect_handle_t getRefHandle(JNIEnv *jenv, jobject thiz){
    jclass clazz = jenv->FindClass(CLASS);
    jfieldID field = jenv->GetFieldID(clazz, "mNativePtr", "J");
    jenv->DeleteLocalRef(clazz);
    return (bef_effect_handle_t) jenv->GetLongField(thiz, field);
}


static void setHandField(JNIEnv *env, jobject handObj, bef_ai_hand *hand){
    // 设置ID
    env->SetIntField(handObj, HAND.ID,hand->id);
    // 设置rect
    bef_ai_rect rect = hand->rect;
    jobject rectObj = env->NewObject(RECT.clazz, RECT.constructor, rect.left, rect.top,
                                     rect.right, rect.bottom);
    env->SetObjectField(handObj, HAND.rect, rectObj);
    env->DeleteLocalRef(rectObj);

    // 设置action
    env->SetIntField(handObj,HAND.action, hand->action);

    // 设置rotAngle
    env->SetFloatField(handObj, HAND.rotAngle, hand->rot_angle);

    // 设置scrore
    env->SetFloatField(handObj, HAND.score, hand->score);

    // 设置rotAngleBothhand
    env->SetFloatField(handObj,HAND.rotAngleBothhand, hand->rot_angle_bothhand);
    // 设置手部关键点
    bef_ai_tt_key_point *keyPoints = hand->key_points;
    jobjectArray pointArray = env->NewObjectArray(BEF_HAND_KEY_POINT_NUM,KEY_POINT.clazz,NULL);
    for (int i = 0; i < BEF_HAND_KEY_POINT_NUM; i++) {
        jobject pointObj = env->NewObject(KEY_POINT.clazz, KEY_POINT.constructor, keyPoints[i].x,
                                          keyPoints[i].y, keyPoints[i].is_detect);
        env->SetObjectArrayElement(pointArray, i, pointObj);
        env->DeleteLocalRef(pointObj);
    }
    env->SetObjectField(handObj, HAND.keyPoints, pointArray);
    // 设置手部拓展点
    bef_ai_tt_key_point *keyPointsExt = hand->key_points_extension;
    jobjectArray pointExtArray = env->NewObjectArray(BEF_HAND_KEY_POINT_NUM_EXTENSION,KEY_POINT.clazz,NULL);
    for (int i = 0; i < BEF_HAND_KEY_POINT_NUM_EXTENSION; i++) {
        jobject pointExtObj = env->NewObject(KEY_POINT.clazz, KEY_POINT.constructor, keyPointsExt[i].x,
                                             keyPointsExt[i].y, keyPointsExt[i].is_detect);
        env->SetObjectArrayElement(pointExtArray, i, pointExtObj);
        env->DeleteLocalRef(pointExtObj);
    }
    env->SetObjectField(handObj, HAND.keyPointsExt, pointExtArray);
    env->SetIntField(handObj, HAND.seqAction, hand->seq_action);

}

static JNICALL jint nativeCreateHandler(
        JNIEnv *env,
        jobject thiz) {

    jclass clazz = env->FindClass(CLASS);
    jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
    env->DeleteLocalRef(clazz);

    bef_effect_handle_t handle = NULL;

    bef_effect_result_t result = bef_effect_ai_hand_detect_create(&handle, 1);
    if (result == BEF_RESULT_SUC) {
        // 保存handle
        env->SetLongField(thiz, field, (jlong) handle);
    }
    return result;

}

static jint nativeSetModel(
        JNIEnv *env,
        jobject thiz,
        jlong type,
        jstring param_path
        ) {
    bef_effect_handle_t handle = getRefHandle(env, thiz);

    if (handle == NULL) {
        return BEF_RESULT_FAIL;
    }
    const char *s_param_path = env->GetStringUTFChars(param_path, 0);

    int ret = bef_effect_ai_hand_detect_setmodel(handle, (bef_ai_hand_model_type)type, s_param_path);

    env->ReleaseStringUTFChars(param_path, s_param_path);

    return ret;


}

static jint nativeCheckLicense(
        JNIEnv *env,
        jobject thiz,
        jobject context,
        jstring strLicense) {

    bef_effect_handle_t handle = getRefHandle(env, thiz);

    if (handle == NULL) {
        return BEF_RESULT_INVALID_EFFECT_HANDLE;
    }
    const char *license = env->GetStringUTFChars(strLicense, 0);

    bef_effect_result_t result = bef_effect_ai_hand_check_license(env, context, handle, license
                                                                  );

    env->ReleaseStringUTFChars(strLicense, license);

    return  result;
}

static jint nativeSetParam(
        JNIEnv *env,
        jobject thiz,
        jint paramType,
        jfloat paramValue) {
    bef_effect_handle_t handle = getRefHandle(env, thiz);

    if (handle == NULL) {
        return BEF_RESULT_INVALID_EFFECT_HANDLE;
    }

    int ret = bef_effect_ai_hand_detect_setparam(handle, (bef_ai_hand_param_type)paramType, paramValue);
    return ret;

}

static jint nativeDetect(
        JNIEnv *env,
        jobject thiz,
        jobject buffer,
        jint pixel_format,
        jint image_width,
        jint image_height,
        jint image_stride,
        jint orientation,
        jlong detection_config,
        jobject info,
        jint delay_frame_count) {
    bef_effect_handle_t handle = getRefHandle(env, thiz);

    if (handle == NULL) {
        return BEF_RESULT_INVALID_EFFECT_HANDLE;
    }
    bef_ai_hand_info hand_info;

    memset(&hand_info, 0, sizeof(bef_ai_hand_info));

    const unsigned char *image = (const unsigned char *) env->GetDirectBufferAddress(buffer);

    bef_effect_result_t result = bef_effect_ai_hand_detect(handle, image,
                                                           (bef_ai_pixel_format) pixel_format,
                                                           image_width, image_height,
                                                           image_stride,
                                                           (bef_ai_rotate_type) orientation,
                                                           detection_config, &hand_info,delay_frame_count);

    if (result != BEF_RESULT_SUC) {
        return result;
    }
    jobjectArray befHands = env->NewObjectArray(hand_info.hand_count, HAND.clazz, NULL);
    env->SetObjectField(info,HAND_INFO.hands, befHands);

    env->SetIntField(info,HAND_INFO.count, hand_info.hand_count);
    for (int i = 0; i < hand_info.hand_count; ++i) {
        jobject handObject = env->NewObject(HAND.clazz, HAND.constructor);
        env->SetObjectArrayElement(befHands, i, handObject);
        setHandField(env, handObject, &hand_info.p_hands[i]);
        env->DeleteLocalRef(handObject);
    }
    env->DeleteLocalRef(befHands);
    return BEF_RESULT_SUC;
}

static void nativeRelease(
        JNIEnv *env,
        jobject thiz
        ) {
    jclass clazz = env->FindClass(CLASS);
    jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
    env->DeleteLocalRef(clazz);

    bef_effect_handle_t handle = (bef_effect_handle_t) env->GetLongField(thiz, field);

    if (handle != NULL) {
        bef_effect_ai_hand_detect_destroy(handle);
        env->SetLongField(thiz, field, 0);
    }

}

static JNINativeMethod gMethods[] = {
        {"nativeCreateHandler", "()I", (void *) nativeCreateHandler},
        {"nativeSetModel",   "(JLjava/lang/String;)I", (void *) nativeSetModel},
        {"nativeDetect", "(Ljava/nio/ByteBuffer;IIIIIJLcom/bytedance/labcv/effectsdk/BefHandInfo;I)I", (void *) nativeDetect},
        {"nativeRelease", "()V",                                                                              (void *) nativeRelease},
        {"nativeCheckLicense", "(Landroid/content/Context;Ljava/lang/String;)I",
                (void *) nativeCheckLicense},
        {"nativeSetParam", "(IF)I",
                (void *) nativeSetParam},
};

int register_hand_detect(JNIEnv *env) {
    jclass clazz = env->FindClass(CLASS);

    jclass classBefHandInfo = env->FindClass("com/bytedance/labcv/effectsdk/BefHandInfo");
    HAND_INFO.clazz = (jclass) env->NewGlobalRef(classBefHandInfo);
    HAND_INFO.hands = env->GetFieldID(classBefHandInfo, "hands",
                                      "[Lcom/bytedance/labcv/effectsdk/BefHandInfo$BefHand;");
    HAND_INFO.count = env->GetFieldID(classBefHandInfo, "handCount", "I");
    env->DeleteLocalRef(classBefHandInfo);

    jclass classBefHand = env->FindClass("com/bytedance/labcv/effectsdk/BefHandInfo$BefHand");
    HAND.clazz = (jclass) env->NewGlobalRef(classBefHand);
    HAND.constructor = env->GetMethodID(HAND.clazz, "<init>", "()V");
    HAND.ID = env->GetFieldID(HAND.clazz, "id", "I");
    HAND.rect = env->GetFieldID(HAND.clazz, "rect","Landroid/graphics/Rect;");
    HAND.action = env->GetFieldID(HAND.clazz, "action", "I");
    HAND.rotAngle =  env->GetFieldID(HAND.clazz, "rotAngle", "F");
    HAND.score =  env->GetFieldID(HAND.clazz, "score", "F");
    HAND.rotAngleBothhand =  env->GetFieldID(HAND.clazz, "rotAngleBothhand", "F");
    HAND.keyPoints =  env->GetFieldID(HAND.clazz, "keyPoints", "[Lcom/bytedance/labcv/effectsdk/BefHandInfo$BefKeyPoint;");
    HAND.keyPointsExt =  env->GetFieldID(HAND.clazz, "keyPointsExt", "[Lcom/bytedance/labcv/effectsdk/BefHandInfo$BefKeyPoint;");
    HAND.seqAction = env->GetFieldID(HAND.clazz, "seqAction","I");
    env->DeleteLocalRef(classBefHand);

    jclass rectClazz = env->FindClass("android/graphics/Rect");
    RECT.clazz = (jclass) env->NewGlobalRef(rectClazz);
    RECT.constructor = env->GetMethodID(RECT.clazz, "<init>", "(IIII)V");
    env->DeleteLocalRef(rectClazz);

    jclass pointClazz = env->FindClass("com/bytedance/labcv/effectsdk/BefHandInfo$BefKeyPoint");
    KEY_POINT.clazz = (jclass) env->NewGlobalRef(pointClazz);
    KEY_POINT.constructor = env->GetMethodID(KEY_POINT.clazz, "<init>", "(FFZ)V");
    env->DeleteLocalRef(pointClazz);
    return env->RegisterNatives(clazz, gMethods, NELEMS(gMethods));


}




