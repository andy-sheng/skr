// Copyright (C) 2018 Beijing Bytedance Network Technology Co., Ltd.

#include "bef_effect_ai_face_detect.h"
#include "bef_effect_ai_face_attribute.h"
#include "byted_effect.h"
#include <jni.h>
#include <string.h>
#include <algorithm>
#include "bef_effect_ai_public_define.h"

static const char *CLASS = "com/bytedance/labcv/effectsdk/FaceDetect";


static struct {
    jclass clazz;

    jfieldID face106s;
    jfieldID extras;
    jfieldID attris;
} FACE_INFO;

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

    jfieldID eye_count;
    jfieldID eyebrow_count;
    jfieldID lips_count;
    jfieldID iris_count;

    jfieldID eye_left;
    jfieldID eye_right;
    jfieldID eyebrow_left;
    jfieldID eyebrow_right;
    jfieldID lips;
    jfieldID left_iris;
    jfieldID right_iris;
} FACE_EXT;


static struct {
  jclass clazz;
  jmethodID constructor;

  jfieldID age;
  jfieldID boy_prob;
  jfieldID attractive;
  jfieldID happy_score;

  jfieldID expression_type;
  jfieldID exp_probs;
  jfieldID racial_type;
  jfieldID rocial_probs;
} FACE_ATTRI;


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

static void
setFaceExt(JNIEnv *env, jobject faceextobj, bef_ai_face_ext_info *extInfo, bool iris_detect) {
    env->SetIntField(faceextobj, FACE_EXT.eye_count, extInfo->eye_count);
    env->SetIntField(faceextobj, FACE_EXT.eyebrow_count, extInfo->eyebrow_count);
    env->SetIntField(faceextobj, FACE_EXT.lips_count, extInfo->lips_count);
    env->SetIntField(faceextobj, FACE_EXT.iris_count, extInfo->iris_count);

    jobjectArray eye_left = env->NewObjectArray(22, FACE_POINT.clazz, NULL);
    for (int i = 0; i < 22; i++) {
        jobject pointObj = env->NewObject(FACE_POINT.clazz, FACE_POINT.constructor,
                                          extInfo->eye_left[i].x, extInfo->eye_left[i].y);
        env->SetObjectArrayElement(eye_left, i, pointObj);
        env->DeleteLocalRef(pointObj);
    }
    env->SetObjectField(faceextobj, FACE_EXT.eye_left, eye_left);

    jobjectArray eye_right = env->NewObjectArray(22, FACE_POINT.clazz, NULL);
    for (int i = 0; i < 22; i++) {
        jobject pointObj = env->NewObject(FACE_POINT.clazz, FACE_POINT.constructor,
                                          extInfo->eye_right[i].x, extInfo->eye_right[i].y);
        env->SetObjectArrayElement(eye_right, i, pointObj);
        env->DeleteLocalRef(pointObj);
    }
    env->SetObjectField(faceextobj, FACE_EXT.eye_right, eye_right);

    jobjectArray eyebrow_left = env->NewObjectArray(13, FACE_POINT.clazz, NULL);
    for (int i = 0; i < 13; i++) {
        jobject pointObj = env->NewObject(FACE_POINT.clazz, FACE_POINT.constructor,
                                          extInfo->eyebrow_left[i].x, extInfo->eyebrow_left[i].y);
        env->SetObjectArrayElement(eyebrow_left, i, pointObj);
        env->DeleteLocalRef(pointObj);
    }
    env->SetObjectField(faceextobj, FACE_EXT.eyebrow_left, eyebrow_left);

    jobjectArray eyebrow_right = env->NewObjectArray(13, FACE_POINT.clazz, NULL);
    for (int i = 0; i < 13; i++) {
        jobject pointObj = env->NewObject(FACE_POINT.clazz, FACE_POINT.constructor,
                                          extInfo->eyebrow_right[i].x, extInfo->eyebrow_right[i].y);
        env->SetObjectArrayElement(eyebrow_right, i, pointObj);
        env->DeleteLocalRef(pointObj);
    }
    env->SetObjectField(faceextobj, FACE_EXT.eyebrow_right, eyebrow_right);

    jobjectArray lips = env->NewObjectArray(64, FACE_POINT.clazz, NULL);
    for (int i = 0; i < 64; i++) {
        jobject pointObj = env->NewObject(FACE_POINT.clazz, FACE_POINT.constructor,
                                          extInfo->lips[i].x, extInfo->lips[i].y);
        env->SetObjectArrayElement(lips, i, pointObj);
        env->DeleteLocalRef(pointObj);
    }
    env->SetObjectField(faceextobj, FACE_EXT.lips, lips);

    if(iris_detect  ) {
      jobjectArray left_iris = env->NewObjectArray(20, FACE_POINT.clazz, NULL);
      for (int i = 0; i < 20; i++) {
        jobject pointObj = env->NewObject(FACE_POINT.clazz, FACE_POINT.constructor,
                                          extInfo->left_iris[i].x, extInfo->left_iris[i].y);
        env->SetObjectArrayElement(left_iris, i, pointObj);
        env->DeleteLocalRef(pointObj);
      }
      env->SetObjectField(faceextobj, FACE_EXT.left_iris, left_iris);

      jobjectArray right_iris = env->NewObjectArray(20, FACE_POINT.clazz, NULL);
      for (int i = 0; i < 20; i++) {
        jobject pointObj = env->NewObject(FACE_POINT.clazz, FACE_POINT.constructor,
                                          extInfo->right_iris[i].x, extInfo->right_iris[i].y);
        env->SetObjectArrayElement(right_iris, i, pointObj);
        env->DeleteLocalRef(pointObj);
      }
      env->SetObjectField(faceextobj, FACE_EXT.right_iris, right_iris);
    }
}

static jint
nativeInit(JNIEnv *env, jobject thiz, jint config0, jstring strModelFile) {
    jclass clazz = env->FindClass(CLASS);
    jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
    env->DeleteLocalRef(clazz);

    const char *modelFile = env->GetStringUTFChars(strModelFile, 0);

    bef_effect_handle_t handle = NULL;
    unsigned long long config = config0;

    bef_effect_result_t result = bef_effect_ai_face_detect_create(config, modelFile, &handle);
    if (result == BEF_RESULT_SUC) {
        env->SetLongField(thiz, field, (jlong) handle);
    }

    env->ReleaseStringUTFChars(strModelFile, modelFile);

    return result;
}

static jint nativeInitExtra(JNIEnv* env, jobject thiz, jint extraType, jstring strModelFile)
{
    jclass clazz = env->FindClass(CLASS);
    jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
    env->DeleteLocalRef(clazz);

    const char *modelFile = env->GetStringUTFChars(strModelFile, 0);

    bef_effect_handle_t handle = (bef_effect_handle_t) env->GetLongField(thiz, field);

    unsigned long long config = extraType;

    bef_effect_result_t result = bef_effect_ai_face_detect_add_extra_model(handle, config, modelFile);

    env->ReleaseStringUTFChars(strModelFile, modelFile);

    return result;
}


static jint nativeInitAttri(JNIEnv* env, jobject thiz, jint jconfig,  jstring strAttriModelFile, jobject context, jstring strLicense) {
    const char *license = env->GetStringUTFChars(strLicense, 0);

    jclass clazz = env->FindClass(CLASS);
    jfieldID field = env->GetFieldID(clazz, "mAttriNativePtr", "J");
    env->DeleteLocalRef(clazz);

    const char *modelFile = env->GetStringUTFChars(strAttriModelFile, 0);

    bef_effect_handle_t handle = NULL;

    unsigned long long config = jconfig;

    bef_effect_result_t result = bef_effect_ai_face_attribute_create(config, modelFile, &handle);
    if(result == BEF_RESULT_SUC)
    {
        env->SetLongField(thiz, field, (jlong) handle);

        result = bef_effect_ai_face_attribute_check_license(env, context, handle, license);

    }
    env->ReleaseStringUTFChars(strAttriModelFile, modelFile);
    env->ReleaseStringUTFChars(strLicense, license);

    return result;
}

int cvfloor2(double value) {
    int i = (int)value;
    return i - (i > value);
}

static jint
detectFace(JNIEnv *env, jobject thiz, jobject buffer, jint pixel_format,
           jint image_width, jint image_height,
           jint image_stride, jint orientation, jlong detection_config, jobject info) {
    bef_ai_face_info face_info;

    jclass clazz = env->FindClass(CLASS);
    jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
    //face atrribute
    jfieldID attriconfigfield = env->GetFieldID(clazz, "mFaceAttriConfig", "I");
    int attriconfig = env->GetIntField(thiz, attriconfigfield);
    jfieldID attrhandlefield = env->GetFieldID(clazz, "mAttriNativePtr", "J");
    bef_effect_handle_t attrihandle =  (bef_effect_handle_t) env->GetLongField(thiz, attrhandlefield);
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

    bef_effect_result_t result = bef_effect_ai_face_detect(handle, image,
                                      (bef_ai_pixel_format) pixel_format,
                                                           image_width, image_height,
                                                           image_stride,
                                      (bef_ai_rotate_type) orientation,
                                      detection_config, &face_info);

    if (result != BEF_RESULT_SUC) {
        return result;
    }

    jobjectArray face106s = env->NewObjectArray(face_info.face_count, FACE106.clazz, NULL);
    env->SetObjectField(info, FACE_INFO.face106s, face106s);

    // face extra
    bool detectextra = TT_MOBILE_FACE_240_DETECT & detection_config;
    jobjectArray faceexts = NULL;
    if( detectextra ) {
      faceexts = env->NewObjectArray(face_info.face_count, FACE_EXT.clazz, NULL);
      env->SetObjectField(info, FACE_INFO.extras, faceexts);
    }

    for (int i = 0; i < face_info.face_count; i++) {
        jobject face106obj = env->NewObject(FACE106.clazz, FACE106.constructor);
        env->SetObjectArrayElement(face106s, i, face106obj);
        setFace106(env, face106obj, &face_info.base_infos[i]);
        env->DeleteLocalRef(face106obj);

      if(detectextra ) {
        jobject faceextobj = env->NewObject(FACE_EXT.clazz, FACE_EXT.constructor);
        env->SetObjectArrayElement(faceexts, i, faceextobj);
        setFaceExt(env, faceextobj, &face_info.extra_infos[i],
                   TT_MOBILE_FACE_280_DETECT & detection_config);
        env->DeleteLocalRef(faceextobj);
      }
    }
    //end face extra

    //face atrribute
    if( -1 != attriconfig && face_info.face_count > 0)
    {
        bef_ai_face_attribute_result attribute_result;
        if(face_info.face_count  == 1)
        {
            attribute_result.face_count = 1;
            result = bef_effect_ai_face_attribute_detect(attrihandle, image, (bef_ai_pixel_format) pixel_format,
                                                         image_width, image_height,
                                                         image_stride,
                                                         face_info.base_infos, attriconfig, &attribute_result.attr_info[0]);
        }
        else
        {
            result = bef_effect_ai_face_attribute_detect_batch(attrihandle, image, (bef_ai_pixel_format) pixel_format,
                                                         image_width, image_height,
                                                         image_stride,
                                                         face_info.base_infos,  face_info.face_count, attriconfig, &attribute_result);
        }

        jobjectArray faceattris = env->NewObjectArray(attribute_result.face_count, FACE_ATTRI.clazz, NULL);
        for (int i = 0; i < attribute_result.face_count; i++) {
            jobject  faceatr = env->NewObject(FACE_ATTRI.clazz, FACE_ATTRI.constructor);
            env->SetFloatField(faceatr, FACE_ATTRI.age,  (jfloat)attribute_result.attr_info[i].age);
            env->SetFloatField(faceatr, FACE_ATTRI.boy_prob,  (jfloat)attribute_result.attr_info[i].boy_prob);
            env->SetFloatField(faceatr, FACE_ATTRI.attractive,  (jfloat)attribute_result.attr_info[i].attractive);
            env->SetFloatField(faceatr, FACE_ATTRI.happy_score,  (jfloat)attribute_result.attr_info[i].happy_score);
            env->SetIntField(faceatr, FACE_ATTRI.expression_type,  (jint)attribute_result.attr_info[i].exp_type);
            const int  probarrlen = bef_ai_face_attribute_expression_type::BEF_FACE_ATTRIBUTE_NUM_EXPRESSION;
            jfloat probarr[probarrlen];
            for(int jarri = 0; jarri < probarrlen; jarri++)
                probarr[jarri] = attribute_result.attr_info[i].exp_probs[jarri];
            jfloatArray jprobsar =  env->NewFloatArray(probarrlen);
            env->SetFloatArrayRegion(jprobsar, 0, probarrlen, probarr);
            env->SetObjectField(faceatr, FACE_ATTRI.exp_probs, jprobsar);
            env->SetIntField(faceatr, FACE_ATTRI.racial_type,  (jint)attribute_result.attr_info[i].racial_type);
            const int  rocialarrlen = bef_ai_face_attribute_racial_type::BEF_FACE_ATTRIBUTE_NUM_RACIAL;
            jfloat rocialarr[rocialarrlen];
            for(int jarri = 0; jarri < rocialarrlen; jarri++)
                rocialarr[jarri] = attribute_result.attr_info[i].racial_probs[jarri];
            jfloatArray jrocialar =  env->NewFloatArray(rocialarrlen);
            env->SetFloatArrayRegion(jrocialar, 0, rocialarrlen, rocialarr);
            env->SetObjectField(faceatr, FACE_ATTRI.rocial_probs, jrocialar);
            env->SetObjectArrayElement(faceattris, i, faceatr);
            env->DeleteLocalRef(faceatr);
        }
        env->SetObjectField(info, FACE_INFO.attris, faceattris);
        env->DeleteLocalRef(faceattris);
    }
    // end face attibute
    env->DeleteLocalRef(face106s);
    env->DeleteLocalRef(faceexts);

    return result;
}

static void
nativeRelease(JNIEnv *env, jobject thiz) {
    jclass clazz = env->FindClass(CLASS);
    jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
    env->DeleteLocalRef(clazz);

    bef_effect_handle_t handle = (bef_effect_handle_t) env->GetLongField(thiz, field);

    if (handle != NULL) {
        bef_effect_ai_face_detect_destroy(handle);
        env->SetLongField(thiz, field, 0);
    }
}


static void
nativeReleaseAttri(JNIEnv *env, jobject thiz) {
    jclass clazz = env->FindClass(CLASS);
    jfieldID field = env->GetFieldID(clazz, "mAttriNativePtr", "J");
    env->DeleteLocalRef(clazz);

    bef_effect_handle_t handle = (bef_effect_handle_t) env->GetLongField(thiz, field);

    if (handle != NULL) {
        bef_effect_ai_face_attribute_destroy(handle);
        env->SetLongField(thiz, field, 0);
    }
}

extern "C" jint JNICALL
nativeCheckLicense(JNIEnv *env,  jobject thiz, jobject context, jstring strLicense) {
    const char *license = env->GetStringUTFChars(strLicense, 0);
    //const char *version = env->GetStringUTFChars(strVersion, 0);

    jclass clazz = env->FindClass(CLASS);
    jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
    env->DeleteLocalRef(clazz);
    bef_effect_handle_t handle = (bef_effect_handle_t) env->GetLongField(thiz, field);

    if (handle == NULL) {
        return BEF_RESULT_INVALID_EFFECT_HANDLE;
    }
    bef_effect_result_t result = bef_effect_ai_face_check_license(env, context, handle, license);
    env->ReleaseStringUTFChars(strLicense, license);

    return  result;
}
extern "C" jint JNICALL
nativeSetParam(JNIEnv *env, jobject thiz, jint maxFaceNum, jint detectInterval) {
    jclass clazz = env->FindClass(CLASS);
    jfieldID field = env->GetFieldID(clazz, "mNativePtr", "J");
    env->DeleteLocalRef(clazz);
    bef_effect_handle_t handle = (bef_effect_handle_t) env->GetLongField(thiz, field);

    if (handle == NULL) {
        return BEF_RESULT_INVALID_EFFECT_HANDLE;
    }

    int ret = bef_effect_ai_face_detect_setparam(handle, BEF_FACE_PARAM_MAX_FACE_NUM, maxFaceNum);
    if (ret != BEF_RESULT_SUC) {
        return ret;
    }
    return bef_effect_ai_face_detect_setparam(handle, BEF_FACE_PARAM_FACE_DETECT_INTERVAL, detectInterval);

}



static JNINativeMethod gMethods[] = {
        {"nativeInit",   "(ILjava/lang/String;)I", (void *) nativeInit},
        {"nativeInitExtra",   "(ILjava/lang/String;)I", (void *) nativeInitExtra},
        {"nativeInitAttri",   "(ILjava/lang/String;Landroid/content/Context;Ljava/lang/String;)I", (void *) nativeInitAttri},
        {"nativeDetect", "(Ljava/nio/ByteBuffer;IIIIIJLcom/bytedance/labcv/effectsdk/BefFaceInfo;)I", (void *) detectFace},
        {"nativeRelease", "()V",                                                                      (void *) nativeRelease},
        {"nativeReleaseAttri", "()V",                                                                  (void *) nativeReleaseAttri},
        {"nativeCheckLicense", "(Landroid/content/Context;Ljava/lang/String;)I",
                (void *) nativeCheckLicense},
        {"nativeSetParam", "(II)I",
                (void *) nativeSetParam},
};

int register_face_detect(JNIEnv *env) {
    jclass clazz = env->FindClass(CLASS);

    jclass faceInfoClazz = env->FindClass("com/bytedance/labcv/effectsdk/BefFaceInfo");
    FACE_INFO.clazz = (jclass) env->NewGlobalRef(faceInfoClazz);
    FACE_INFO.face106s = env->GetFieldID(FACE_INFO.clazz, "face106s",
                                         "[Lcom/bytedance/labcv/effectsdk/BefFaceInfo$Face106;");
    FACE_INFO.extras = env->GetFieldID(FACE_INFO.clazz, "extras",
                                       "[Lcom/bytedance/labcv/effectsdk/BefFaceInfo$ExtraInfo;");
    FACE_INFO.attris = env->GetFieldID(FACE_INFO.clazz, "attris",
                                       "[Lcom/bytedance/labcv/effectsdk/BefFaceInfo$FaceAttri;");
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

    jclass faceExtClazz = env->FindClass(
            "com/bytedance/labcv/effectsdk/BefFaceInfo$ExtraInfo");
    FACE_EXT.clazz = (jclass) env->NewGlobalRef(faceExtClazz);
    FACE_EXT.constructor = env->GetMethodID(FACE_EXT.clazz, "<init>", "()V");
    FACE_EXT.eye_count = env->GetFieldID(FACE_EXT.clazz, "eye_count", "I");
    FACE_EXT.eyebrow_count = env->GetFieldID(FACE_EXT.clazz, "eyebrow_count", "I");
    FACE_EXT.lips_count = env->GetFieldID(FACE_EXT.clazz, "lips_count", "I");
    FACE_EXT.iris_count = env->GetFieldID(FACE_EXT.clazz, "iris_count", "I");
    FACE_EXT.eye_left = env->GetFieldID(FACE_EXT.clazz, "eye_left",
                                        "[Lcom/bytedance/labcv/effectsdk/BefFaceInfo$FacePoint;");
    FACE_EXT.eye_right = env->GetFieldID(FACE_EXT.clazz, "eye_right",
                                         "[Lcom/bytedance/labcv/effectsdk/BefFaceInfo$FacePoint;");
    FACE_EXT.eyebrow_left = env->GetFieldID(FACE_EXT.clazz, "eyebrow_left",
                                            "[Lcom/bytedance/labcv/effectsdk/BefFaceInfo$FacePoint;");
    FACE_EXT.eyebrow_right = env->GetFieldID(FACE_EXT.clazz, "eyebrow_right",
                                             "[Lcom/bytedance/labcv/effectsdk/BefFaceInfo$FacePoint;");
    FACE_EXT.lips = env->GetFieldID(FACE_EXT.clazz, "lips",
                                    "[Lcom/bytedance/labcv/effectsdk/BefFaceInfo$FacePoint;");
    FACE_EXT.left_iris = env->GetFieldID(FACE_EXT.clazz, "left_iris",
                                         "[Lcom/bytedance/labcv/effectsdk/BefFaceInfo$FacePoint;");
    FACE_EXT.right_iris = env->GetFieldID(FACE_EXT.clazz, "right_iris",
                                          "[Lcom/bytedance/labcv/effectsdk/BefFaceInfo$FacePoint;");
    env->DeleteLocalRef(faceExtClazz);

    jclass faceAttriClazz = env->FindClass(
        "com/bytedance/labcv/effectsdk/BefFaceInfo$FaceAttri");
    FACE_ATTRI.clazz = (jclass) env->NewGlobalRef(faceAttriClazz);
    FACE_ATTRI.constructor = env->GetMethodID(FACE_ATTRI.clazz, "<init>", "()V");
    FACE_ATTRI.age = env->GetFieldID(FACE_ATTRI.clazz, "age", "F");
    FACE_ATTRI.boy_prob = env->GetFieldID(FACE_ATTRI.clazz, "boy_prob", "F");
    FACE_ATTRI.attractive = env->GetFieldID(FACE_ATTRI.clazz, "attractive", "F");
    FACE_ATTRI.happy_score = env->GetFieldID(FACE_ATTRI.clazz, "happy_score", "F");
    FACE_ATTRI.expression_type = env->GetFieldID(FACE_ATTRI.clazz, "expression_type","I");
    FACE_ATTRI.exp_probs = env->GetFieldID(FACE_ATTRI.clazz, "exp_probs","[F");
    FACE_ATTRI.racial_type = env->GetFieldID(FACE_ATTRI.clazz, "racial_type","I");
    FACE_ATTRI.rocial_probs = env->GetFieldID(FACE_ATTRI.clazz, "rocial_probs","[F");
    env->DeleteLocalRef(faceAttriClazz);

    return env->RegisterNatives(clazz, gMethods, NELEMS(gMethods));
}