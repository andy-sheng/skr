// Copyright (C) 2018 Beijing Bytedance Network Technology Co., Ltd.

#include "byted_effect.h"
#include <jni.h>
#include <string.h>
#include <algorithm>
#include "bef_effect_ai_face_verify.h"
#include "bef_effect_ai_public_define.h"

static const char *CLASS = "com/bytedance/labcv/effectsdk/FaceVerify";

static struct {
    jclass clazz;

    jfieldID face106s;
    jfieldID features;
    jfieldID validNum;
} FACE_VERIFY_INFO;

static struct {
    jclass clazz;
    jmethodID constructor;

    jfieldID rect;
    jfieldID score;
    jfieldID points_array;
    jfieldID visibility_array;
    jfieldID yaw;
    jfieldID pitch;
    jfieldID roll;
    jfieldID eye_dist;
    jfieldID action;
    jfieldID ID;
} FACE106;


static struct {
    jclass clazz;
    jmethodID constructor;
} FACE_RECT;

static struct {
    jclass clazz;
    jmethodID constructor;
} FACE_POINT;

static void
setFace106(JNIEnv *env, jobject face106obj, bef_ai_face_106 *face106) {
    // 设置rect
    bef_ai_rect rect = face106->rect;
    jobject rectObj = env->NewObject(FACE_RECT.clazz, FACE_RECT.constructor, rect.left, rect.top,
                                     rect.right, rect.bottom);
    env->SetObjectField(face106obj, FACE106.rect, rectObj);
    env->DeleteLocalRef(rectObj);

    // 设置score
    env->SetFloatField(face106obj, FACE106.score, face106->score);

    // 设置points_array
    bef_ai_fpoint *points = face106->points_array;
    jobjectArray pointsObj = env->NewObjectArray(106, FACE_POINT.clazz, NULL);
    for (int i = 0; i < 106; i++) {
        jobject pointObj = env->NewObject(FACE_POINT.clazz, FACE_POINT.constructor, points[i].x,
                                          points[i].y);
        env->SetObjectArrayElement(pointsObj, i, pointObj);
        env->DeleteLocalRef(pointObj);
    }
    env->SetObjectField(face106obj, FACE106.points_array, pointsObj);
    env->DeleteLocalRef(pointsObj);

    // 设置visibility_array
    float *visibility_array = face106->visibility_array;
    jfloatArray visibilityObjs = env->NewFloatArray(106);
    jfloat values[106];
    for (int i = 0; i < 106; i++) {
        values[i] = visibility_array[i];
    }
    env->SetFloatArrayRegion(visibilityObjs, 0, 106, values);
    env->SetObjectField(face106obj, FACE106.visibility_array, visibilityObjs);
    env->DeleteLocalRef(visibilityObjs);

    // 设置yaw
    env->SetFloatField(face106obj, FACE106.yaw, face106->yaw);

    // 设置pitch
    env->SetFloatField(face106obj, FACE106.pitch, face106->pitch);

    // 设置roll
    env->SetFloatField(face106obj, FACE106.roll, face106->roll);

    // 设置eye_dist
    env->SetFloatField(face106obj, FACE106.eye_dist, face106->eye_dist);

    // 设置action
    env->SetIntField(face106obj, FACE106.action, face106->action);

    // 设置ID
    env->SetIntField(face106obj, FACE106.ID, face106->ID);
}


static jint
nativeCreateHandle(JNIEnv *env, jobject thiz, jstring strModel106, jstring strModelVerify, jint maxNum) {
    jclass clazz = env->FindClass(CLASS);
    jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
    env->DeleteLocalRef(clazz);

    const char *modelVerify = env->GetStringUTFChars(strModelVerify, 0);
    const char *model106 = env->GetStringUTFChars(strModel106, 0);

    bef_effect_handle_t handle = NULL;

    bef_effect_result_t result = bef_effect_ai_face_verify_create(model106,modelVerify, maxNum, &handle);
    if (result == BEF_RESULT_SUC) {
        env->SetLongField(thiz, field, (jlong) handle);
    }

    env->ReleaseStringUTFChars(strModelVerify, modelVerify);


    return result;
}

static void
setFeatures(JNIEnv *env,jobject info, bef_ai_face_verify_info face_verify_info){
    jclass floatArrayClass = env->FindClass("[F");
    jobjectArray featuresArray = env->NewObjectArray(face_verify_info.valid_face_num, floatArrayClass,
                                                     nullptr);
    jfloat temp[BEF_AI_FACE_FEATURE_DIM];

    for (int i = 0; i < face_verify_info.valid_face_num; ++i) {
        jfloatArray colArr = env->NewFloatArray(BEF_AI_FACE_FEATURE_DIM);

        for (int j = 0; j < BEF_AI_FACE_FEATURE_DIM ; ++j) {
            temp[j] = face_verify_info.features[i][j];
        }
        env->SetFloatArrayRegion(colArr, 0, BEF_AI_FACE_FEATURE_DIM, temp);
        env->SetObjectArrayElement(featuresArray,i,colArr);
        env->DeleteLocalRef(colArr);
    }
    env->SetObjectField(info,FACE_VERIFY_INFO.features, featuresArray);
    env->DeleteLocalRef(featuresArray);

}


static jint
extractFeature(JNIEnv *env, jobject thiz, jobject buffer, jint pixel_format,
           jint image_width, jint image_height,
           jint image_stride, jint orientation, jobject info) {
    bef_ai_face_verify_info face_verify_info;

    jclass clazz = env->FindClass(CLASS);
    jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
    // end face attibute
    env->DeleteLocalRef(clazz);

    bef_effect_handle_t handle = (bef_effect_handle_t) env->GetLongField(thiz, field);

    if (handle == NULL) {
        return BEF_RESULT_INVALID_EFFECT_HANDLE;
    }
    const unsigned char *image = (const unsigned char *) env->GetDirectBufferAddress(buffer);

    if (image == NULL) {
        return BEF_RESULT_INVALID_IMAGE_DATA;
    }

    bef_effect_result_t result = bef_effect_ai_face_extract_feature(handle, image,
                                      (bef_ai_pixel_format) pixel_format,
                                                           image_width, image_height,
                                                           image_stride,
                                      (bef_ai_rotate_type) orientation,
                                       &face_verify_info);

    if (result != BEF_RESULT_SUC) {
        return result;
    }

    jobjectArray face106s = env->NewObjectArray(face_verify_info.valid_face_num, FACE106.clazz, NULL);
    env->SetObjectField(info, FACE_VERIFY_INFO.face106s, face106s);
    env->SetIntField(info, FACE_VERIFY_INFO.validNum, face_verify_info.valid_face_num);
    setFeatures(env, info, face_verify_info);

    for (int i = 0; i < face_verify_info.valid_face_num; i++) {
        jobject face106obj = env->NewObject(FACE106.clazz, FACE106.constructor);
        env->SetObjectArrayElement(face106s, i, face106obj);
        setFace106(env, face106obj, &face_verify_info.base_infos[i]);
        env->DeleteLocalRef(face106obj);
    }
    env->DeleteLocalRef(face106s);

    return result;
}

static jdouble
verify(JNIEnv *env, jobject thiz,jfloatArray feature1, jfloatArray feature2){
    jsize size1 = env->GetArrayLength(feature1);
    jsize size2 = env->GetArrayLength(feature2);
    if (size1 != size2)
        return -1;
    jfloat * ptrFeature1 = env->GetFloatArrayElements(feature1, JNI_FALSE);
    jfloat * ptrFeature2 = env->GetFloatArrayElements(feature2, JNI_FALSE);
    double dist = bef_effect_ai_face_verify(ptrFeature1,ptrFeature2, size1);
    env->ReleaseFloatArrayElements(feature1, ptrFeature1, 0);
    env->ReleaseFloatArrayElements(feature2, ptrFeature2, 0);
    return dist;

}

static void
nativeRelease(JNIEnv *env, jobject thiz) {
    jclass clazz = env->FindClass(CLASS);
    jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
    env->DeleteLocalRef(clazz);

    bef_effect_handle_t handle = (bef_effect_handle_t) env->GetLongField(thiz, field);

    if (handle != NULL) {
        bef_effect_ai_face_verify_destroy(handle);
        env->SetLongField(thiz, field, 0);
    }
}


jint JNICALL
nativeCheckLicense(JNIEnv *env,  jobject thiz, jobject context, jstring strLicense) {
    const char *license = env->GetStringUTFChars(strLicense, 0);

    jclass clazz = env->FindClass(CLASS);
    jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
    env->DeleteLocalRef(clazz);
    bef_effect_handle_t handle = (bef_effect_handle_t) env->GetLongField(thiz, field);

    if (handle == NULL) {
        return BEF_RESULT_INVALID_EFFECT_HANDLE;
    }
    bef_effect_result_t result = bef_effect_ai_face_verify_check_license(env, context, handle, license);
    env->ReleaseStringUTFChars(strLicense, license);

    return  result;
}

jdouble toScore(JNIEnv *env,  jobject thiz, jdouble d){
    return bef_effect_ai__dist2score(d);
}

static JNINativeMethod gMethods[] = {
        {"nativeCreateHandle",   "(Ljava/lang/String;Ljava/lang/String;I)I", (void *) nativeCreateHandle},
        {"nativeExtractFeature", "(Ljava/nio/ByteBuffer;IIIIILcom/bytedance/labcv/effectsdk/BefFaceFeature;)I", (void *) extractFeature},
        {"nativeVerify","([F[F)D",(void *) verify},
        {"nativeDistanceToScore","(D)D",(void *) toScore},
        {"nativeRelease", "()V",                                                                      (void *) nativeRelease},
        {"nativeCheckLicense", "(Landroid/content/Context;Ljava/lang/String;)I",
                (void *) nativeCheckLicense}
};

int register_face_verify(JNIEnv *env) {
    jclass clazz = env->FindClass(CLASS);

    jclass faceInfoClazz = env->FindClass("com/bytedance/labcv/effectsdk/BefFaceFeature");
    FACE_VERIFY_INFO.clazz = (jclass) env->NewGlobalRef(faceInfoClazz);
    FACE_VERIFY_INFO.face106s = env->GetFieldID(FACE_VERIFY_INFO.clazz, "baseInfo",
                                         "[Lcom/bytedance/labcv/effectsdk/BefFaceInfo$Face106;");
    FACE_VERIFY_INFO.features = env->GetFieldID(FACE_VERIFY_INFO.clazz, "features", "[[F");
    FACE_VERIFY_INFO.validNum = env->GetFieldID(FACE_VERIFY_INFO.clazz, "validFaceNum", "I");
    env->DeleteLocalRef(faceInfoClazz);

    jclass faceClazz = env->FindClass("com/bytedance/labcv/effectsdk/BefFaceInfo$Face106");
    FACE106.clazz = (jclass) env->NewGlobalRef(faceClazz);
    FACE106.constructor = env->GetMethodID(FACE106.clazz, "<init>", "()V");
    FACE106.rect = env->GetFieldID(FACE106.clazz, "rect",
                                   "Lcom/bytedance/labcv/effectsdk/BefFaceInfo$FaceRect;");
    FACE106.score = env->GetFieldID(FACE106.clazz, "score", "F");
    FACE106.points_array = env->GetFieldID(FACE106.clazz, "points_array",
                                           "[Lcom/bytedance/labcv/effectsdk/BefFaceInfo$FacePoint;");
    FACE106.visibility_array = env->GetFieldID(FACE106.clazz, "visibility_array", "[F");
    FACE106.yaw = env->GetFieldID(FACE106.clazz, "yaw", "F");
    FACE106.pitch = env->GetFieldID(FACE106.clazz, "pitch", "F");
    FACE106.roll = env->GetFieldID(FACE106.clazz, "roll", "F");
    FACE106.eye_dist = env->GetFieldID(FACE106.clazz, "eye_dist", "F");
    FACE106.action = env->GetFieldID(FACE106.clazz, "action", "I");
    FACE106.ID = env->GetFieldID(FACE106.clazz, "ID", "I");
    env->DeleteLocalRef(faceClazz);

    jclass rectClazz = env->FindClass("com/bytedance/labcv/effectsdk/BefFaceInfo$FaceRect");
    FACE_RECT.clazz = (jclass) env->NewGlobalRef(rectClazz);
    FACE_RECT.constructor = env->GetMethodID(FACE_RECT.clazz, "<init>", "(IIII)V");
    env->DeleteLocalRef(rectClazz);

    jclass pointClazz = env->FindClass(
            "com/bytedance/labcv/effectsdk/BefFaceInfo$FacePoint");
    FACE_POINT.clazz = (jclass) env->NewGlobalRef(pointClazz);
    FACE_POINT.constructor = env->GetMethodID(FACE_POINT.clazz, "<init>", "(FF)V");
    env->DeleteLocalRef(pointClazz);


    return env->RegisterNatives(clazz, gMethods, NELEMS(gMethods));
}