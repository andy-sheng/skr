//
// Created by 昝晓飞 on 16/9/13.
//

#include <string.h>
#include "jni_cache.h"
#include "log.h"
/****************Class Section Define Begin*********************************************/

// Sample Begin
////////////// XXXX Define/////////////////////////////////////////////
//ST_METHODINFO stXXXNameClassMethod[] = {
//    {ARIBTV_XXXX_METHOD_XXXXX,        "()J",                      false},
//    {ARIBTV_XXXX_METHOD_XXXXX,        "(J)V",                     false}
//};

//ST_FIELDINFO stXXXClassField[] = {
//        {ARIBTV_XXXX_FIELD_XXXXX,"I"},
//        {ARIBTV_XXXX_FIELD_XXXXX,"Z"},
//};
//////////// end Date Define///////////////////////////////////////////
// Sample End

///////////// CredptModel Class Define ////////////////////////////////////////
ST_METHODINFO stCredptModelClassMethod[] = {
          {JAVA_CREDPTMODEL_METHOD_CONSTRUCT,                      "()V",            false},
          {JAVA_CREDPTMODEL_METHOD_GETKEY,                   "()Ljava/lang/String;", false},
          {JAVA_CREDPTMODEL_METHOD_SETKEY,                   "(Ljava/lang/String;)V",false},
          {JAVA_CREDPTMODEL_METHOD_GETBODY,                  "()Ljava/lang/String;",false},
          {JAVA_CREDPTMODEL_METHOD_SETBODY,                  "(Ljava/lang/String;)V", false},
          {JAVA_CREDPTMODEL_METHOD_SETTYPE,                  "(I)V", false},
          {JAVA_CREDPTMODEL_METHOD_GETTYPE,                  "()I",false}
  };

ST_FIELDINFO stCredptModelClassField[] = {
         {JAVA_CREDPTMODEL_FIELD_KEY,               "Ljava/lang/String;"},
         {JAVA_CREDPTMODEL_FIELD_BODY,              "Ljava/lang/String;"},
         {JAVA_CREDPTMODEL_FIELD_TYPE,              "I"}
};
///////////// end CredptModel Define ////////////////////////////////////////

///////////// ImageBufFrame Class Define ////////////////////////////////////////
ST_METHODINFO stImageBufFrameClassMethod[] = {
          {JAVA_IMGBUFFRAME_METHOD_CONSTRUCT,                      "()V",            false}
 };

ST_FIELDINFO stImageBufFrameClassField[] = {
         {JAVA_IMGBUFFRAME_FIELD_FORMAT,        "Lorg/opencdnunion/media/streamer/framework/ImgBufFormat;"},
         {JAVA_IMGBUFFRAME_FIELD_DTS, 			"J"},
         {JAVA_IMGBUFFRAME_FIELD_PTS,              "J"},
         {JAVA_IMGBUFFRAME_FIELD_FLAGS,              "I"},
         {JAVA_IMGBUFFRAME_FIELD_BUF,              "Ljava/nio/ByteBuffer;"},
};
///////////// end ImageBufFrame Define ////////////////////////////////////////


///////////// ImageBufFrameFormat Class Define ////////////////////////////////////////
ST_METHODINFO stImageBufFrameFormatClassMethod[] = {
          {JAVA_IMGBUFFRAME_FORMAT_METHOD_CONSTRUCT,                      "()V",            false}
 };

ST_FIELDINFO stImageBufFrameFormatClassField[] = {
         {JAVA_IMGBUFFRAME_FORMAT_FIELD_FORMAT,               "I"},
         {JAVA_IMGBUFFRAME_FORMAT_FIELD_ORIENTATION,              "I"},
         {JAVA_IMGBUFFRAME_FORMAT_FIELD_WIDTH,              "I"},
         {JAVA_IMGBUFFRAME_FORMAT_FIELD_HEIGHT,              "I"},
         {JAVA_IMGBUFFRAME_FORMAT_FIELD_STRIDE,               "[I"},
         {JAVA_IMGBUFFRAME_FORMAT_FIELD_STRIDENUMER,               "I"}
};
///////////// end ImageBufFrameFormat Define ////////////////////////////////////////

///////////// ImageBufMixerConfig Class Define ////////////////////////////////////////
ST_METHODINFO stImageBufMixerConfigClassMethod[] = {
          {JAVA_IMGBUFMIXERCONFIG_METHOD_CONSTRUCT,                      "()V",            false}
 };

ST_FIELDINFO stImageBufMixerConfigClassField[] = {
         {JAVA_IMGBUFMIXERCONFIG_FIELD_X,               "I"},
         {JAVA_IMGBUFMIXERCONFIG_FIELD_Y,              "I"},
         {JAVA_IMGBUFMIXERCONFIG_FIELD_W,              "I"},
         {JAVA_IMGBUFMIXERCONFIG_FIELD_H,              "I"},
         {JAVA_IMGBUFMIXERCONFIG_FIELD_ALPHA,               "I"}
};
///////////// end ImageBufMixerConfig Define ////////////////////////////////////////

///////////// AudioBufFormat Class Define ////////////////////////////////////////
ST_METHODINFO stAudioBufFormatClassMethod[] = {
    {JAVA_AUDIOBUFFORMAT_METHOD_CONSTRUCT,                      "()V",            false}
};

ST_FIELDINFO stAudioBufFormatClassField[] = {
    {JAVA_AUDIOBUFFORMAT_FIELD_FORMAT,               "I"},
    {JAVA_AUDIOBUFFORMAT_FIELD_RATE,              "I"},
    {JAVA_AUDIOBUFFORMAT_FIELD_CHANNELS,              "I"},
    {JAVA_AUDIOBUFFORMAT_FIELD_CODECID,              "I"}
};
/////////////  end AudioBufFormat Define ////////////////////////////////////////

///////////// AVFilterWrapper Class Define ////////////////////////////////////////
ST_METHODINFO stAVFilterWrapperClassMethod[] = {
        {JAVA_AVFILTERWRAPPER_METHOD_CONSTRUCT,                      "()V",            false},
        {JAVA_AVFILTERWRAPPER_METHOD_ONAUDIOFRAME,      "(Ljava/nio/ByteBuffer;J)V",      false}
};

ST_FIELDINFO *stAVFilterWrapperClassField = NULL;

/////////////  end AudioBufFormat Define ////////////////////////////////////////


///////////// ArrayList Class Define ////////////////////////////////////////
ST_METHODINFO stArrayListClassMethod[] = {
    {JAVA_ARRRAYLIST_METHOD_CONSTRUCT,   "()V",                      false},
    {JAVA_ARRRAYLIST_METHOD_SIZE,        "()I",                      false},
    {JAVA_ARRRAYLIST_METHOD_GET,         "(I)Ljava/lang/Object;",    false},
    {JAVA_ARRRAYLIST_METHOD_ADD,         "(Ljava/lang/Object;)Z",    false},
    {JAVA_ARRRAYLIST_METHOD_CLEAR,       "()V",                      false}
};

ST_FIELDINFO *stArrayListClassField = NULL;
///////////// end ArrayList Define ////////////////////////////////////

/////////////////////Integer Class Define/////////////////////////////
ST_METHODINFO stIntegerClassMethod[] = {
    {JAVA_INTEGER_METHOD_INTVALUE,    "()I",    false}
};

ST_FIELDINFO *stIntegerClassField = NULL;
/////////////////////end Integer Define///////////////////////////////
/****************Class Section Define End*********************************************/

const ST_CLASSINFO stClassList[] = {
    ST_CLASSLIST_ITEM(JAVA_CLASS_PATH_IMGBUFFRAME, stImageBufFrameClassMethod, stImageBufFrameClassField ),
    ST_CLASSLIST_ITEM(JAVA_CLASS_PATH_IMGBUFFRAME_FORMAT, stImageBufFrameFormatClassMethod, stImageBufFrameFormatClassField ),
    ST_CLASSLIST_ITEM(JAVA_CLASS_PATH_IMGBUFMIXERCONFIG, stImageBufMixerConfigClassMethod, stImageBufMixerConfigClassField ),
    ST_CLASSLIST_ITEM(JAVA_CLASS_PATH_AUDIOBUFFORMAT, stAudioBufFormatClassMethod, stAudioBufFormatClassField ),
    ST_CLASSLIST_ITEM(JAVA_CLASS_PATH_AVFILTERWRAPPER, stAVFilterWrapperClassMethod,
                      stAVFilterWrapperClassField),
    ST_CLASSLIST_ITEM(JAVA_CLASS_ARRRAYLIST, stArrayListClassMethod, stArrayListClassField),
    ST_CLASSLIST_ITEM(JAVA_CLASS_INTEGER, stIntegerClassMethod, stIntegerClassField ),
    ST_CLASSLIST_ITEM(JAVA_CLASS_PATH_CREDPTMODEL, stCredptModelClassMethod,
                        stCredptModelClassField)
};

JniCache *JniCache::GetInstance()
{
    static JniCache instance;
    return &instance;
}

JniCache::JniCache()
{
        m_idMap.clear();
}

JniCache::~JniCache()
{
        m_idMap.clear();
}

/**
* 缓存名字为pStrClassName的class 在上面Class Section Define定义的Method和Filed
* 防止在一次生命周期重重复的获取这个class的MethodID和FiledID
*@param: pStrClassName 需要缓存的Class的名字,例如JAVA_CLASS_PATH_CREDPTMODEL,建议类的相关定义放在jni_class_def中
*@return: 返回是否缓存成功
*/
bool JniCache::CacheClass(JNIEnv *env, const char* pStrClassName)
{
    if ( (NULL == pStrClassName) || (NULL == env) ) {
        LOGE("[MIDCACHE]null parameter.\n");
        return false;
    }

    if (CheckInCache(pStrClassName)) {
        return true;
    }
    else {
        return CacheClassInfo(env, pStrClassName);
    }
}

/**
* 返回FieldID
*@param pStrClassName Filed所在的Class的名字(在jni_class_def中定义)
*@param pStrFieldName Filed名字(在jni_class_def中定义)
*@return: filed id,没有找到返回null,在调用这个方法之前需要调用CacheClass,对Filed所在的类进行缓存
*/
jfieldID JniCache::GetFieldID(JNIEnv *env, const char* pStrClassName, const char* pStrFieldName)
{
    if ( (NULL == pStrClassName) || (NULL == pStrFieldName) || (NULL == env) ) {
        LOGE("[MIDCACHE]null parameter.\n");
        return NULL;
    }

    unFMID unFmId;
    if ( GetFMId(env, pStrClassName, pStrFieldName, &unFmId) ) {
        return unFmId.feildID;
    }
    else {
        return NULL;
    }
}

/**
* 返回MethodID
*@param pStrClassName Method所在的Class的名字(在jni_class_def中定义)
*@param pStrMethodName Method名字(在jni_class_def中定义)
*@return: Method ID,没有找到返回null,在调用这个方法之前需要调用CacheClass,对Method所在的类进行缓存
*/
jmethodID JniCache::GetMethodID(JNIEnv *env, const char* pStrClassName, const char* pStrMethodName)
{
    if ( (NULL == pStrClassName) || (NULL == pStrMethodName) || (NULL == env) ) {
        LOGE("[MIDCACHE]null parameter.\n");
        return NULL;
    }

    unFMID unFmId;
    if ( GetFMId(env, pStrClassName, pStrMethodName, &unFmId) ) {
        return unFmId.methodID;
    }
    else {
        return NULL;
    }
}

/**
* 检查类pStrClassName是否已经缓存过了
*@param: pStrClassName 需要检查的Class的名字
*@return: 返回是否缓存成功
*/
bool JniCache::CheckInCache(const char* pStrClassName)
{
    MIDMap::iterator iter;
        iter = m_idMap.find(pStrClassName);
        if(iter == m_idMap.end()) {
            return false;
        }

        return true;
}

/**
* 缓存名字为pStrClassName的class 在上面Class Section Define定义的Method和Filed
*@param: pStrClassName 需要缓存的Class的名字
*@return: 返回是否缓存成功
*/
bool JniCache::CacheClassInfo(JNIEnv *env, const char* pStrClassName)
{
        int nNum = INFO_NUMBER(stClassList);
        MIDItem mapItem;
        for (int i=0; i < nNum; i++) {
            if ( (strlen(stClassList[i].mstrName) == strlen(pStrClassName) )
                       && (0 == strncmp(pStrClassName,stClassList[i].mstrName,strlen(stClassList[i].mstrName)) )
                       ) {
                 mapItem.clear();
                 jclass jClass = env->FindClass(stClassList[i].mstrName);
                 if (NULL == jClass) {
                      return false;
                  }

                  unFMID unId;

                  // Load Method Info no static method
                  for (int j=0; j<stClassList[i].miMethodNum; j++) {
                       unId.methodID = NULL;
                       unId.methodID = env->GetMethodID(
                                  jClass,
                                  stClassList[i].mstMethodInfo[j].method_name,
                                  stClassList[i].mstMethodInfo[j].method_signature);
                                  if (NULL == unId.methodID) {
                                      LOGE("[MIDCACHE]GetMethodID failed. method_name:%s method_signature:%s\n",
                                                           stClassList[i].mstMethodInfo[j].method_name,
                                                           stClassList[i].mstMethodInfo[j].method_signature);
                                      continue;
                                  }
                       mapItem.insert(pair<string,unFMID>(stClassList[i].mstMethodInfo[j].method_name,unId));
                  }

                  // Load field Info
                  for (int k=0; k<stClassList[i].miFieldNum; k++) {
                       unId.feildID = NULL;
                       unId.feildID = (env)->GetFieldID(
                                       jClass,
                                       stClassList[i].mstFieldInfo[k].field_name,
                                       stClassList[i].mstFieldInfo[k].field_signature);
                       mapItem.insert(pair<string,unFMID>(stClassList[i].mstFieldInfo[k].field_name,unId));
                   }

                   m_idMap.insert(pair<string,MIDItem >(pStrClassName,mapItem));
                   (env)->DeleteLocalRef(jClass);
                   return true;
            }
        }
        return false;
}

bool JniCache::GetFMId(JNIEnv *env, const char* pStrClassName, const char* pStrFMName, unFMID* unFmId)
{
    MIDMap::iterator iter;
    iter = m_idMap.find(pStrClassName);
    if(iter == m_idMap.end()) {
        // no cache
        if (CacheClassInfo(env, pStrClassName)) {
            iter = m_idMap.find(pStrClassName);
            if(iter == m_idMap.end()) {
                LOGE("[MIDCACHE]not find the class. ClassName:%s\n", pStrClassName);
                //PrintMapInfo();
                return false;
            }
        }
        else {
            LOGW("[MIDCACHE]GetFMId failed. ClassName:%s\n", pStrClassName);
            return false;
        }
    }

    MIDItem::iterator iter2;
    iter2 = iter->second.find(pStrFMName);

    if(iter2 == iter->second.end()) {
        LOGW("[MIDCACHE]not find the method. ClassName:%s FMName:%s\n", pStrClassName, pStrFMName);
        return false;
    }

    *unFmId = iter2->second;
    return true;
}

JniCache *GetJniCacheInstance()
{
	return JniCache::GetInstance();
}