//
// Created by 昝晓飞 on 16/9/14.
//

#include "jni_string.h"
#include "jni_util.h"
#include "util/jni_cache.h"
#include "util/string_contants.h"

const char* stStrings[] = {
    LOG_ACCESS_KEY,
    LOG_SECRET_KEY,
    COUNT_ACCESS_KEY,
    COUNT_SECRET_KEY
};

jobject Java_com_zq_mediaengine_util_StringWrapper_getStringList
  (JNIEnv *env, jobject thiz)
{
    GetJniCacheInstance()->CacheClass(env, JAVA_CLASS_ARRRAYLIST);
    jmethodID jmListConstruct = JCOM_GET_METHOD_ID(env, JAVA_CLASS_ARRRAYLIST, JAVA_ARRRAYLIST_METHOD_CONSTRUCT);
    jmethodID jmListAdd = JCOM_GET_METHOD_ID(env, JAVA_CLASS_ARRRAYLIST, JAVA_ARRRAYLIST_METHOD_ADD);

    jclass jList = JCOM_FIND_CLASS(env, JAVA_CLASS_ARRRAYLIST);
    jobject joStringsList = NULL;
    joStringsList = (env)->NewObject(jList, jmListConstruct);

    int count = INFO_NUMBER(stStrings);
    for(int i = 0; i < count; i++) {
        jstring jcontent = ToJString(env, stStrings[i]);
        //add item
        env->CallBooleanMethod(joStringsList, jmListAdd, jcontent);
        env->DeleteLocalRef(jcontent);
    }

    env->DeleteLocalRef(jList);
    return joStringsList;
}