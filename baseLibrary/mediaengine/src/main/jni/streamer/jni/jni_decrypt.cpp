//
//return all decrypt
// Created by 昝晓飞 on 16/9/13.
//
#include "jni_decrypt.h"
#include "util/jni_cache.h"
#include "jni_util.h"
#include "cipher/CipherUtility.h"

jobject Java_com_zq_mediaengine_util_CredtpWrapper_getContentList
  (JNIEnv *env, jobject thiz)
{
     GetJniCacheInstance()->CacheClass(env, JAVA_CLASS_ARRRAYLIST);
     GetJniCacheInstance()->CacheClass(env, JAVA_CLASS_PATH_CREDPTMODEL);
     jmethodID jmListConstruct = JCOM_GET_METHOD_ID(env, JAVA_CLASS_ARRRAYLIST, JAVA_ARRRAYLIST_METHOD_CONSTRUCT);
     jmethodID jmListAdd = JCOM_GET_METHOD_ID(env, JAVA_CLASS_ARRRAYLIST, JAVA_ARRRAYLIST_METHOD_ADD);
     jmethodID jKsyConstruct = JCOM_GET_METHOD_ID(env, JAVA_CLASS_PATH_CREDPTMODEL, JAVA_CREDPTMODEL_METHOD_CONSTRUCT);
     jmethodID jsetkey =  JCOM_GET_METHOD_ID(env, JAVA_CLASS_PATH_CREDPTMODEL, JAVA_CREDPTMODEL_METHOD_SETKEY);
     jmethodID jsetbody = JCOM_GET_METHOD_ID(env, JAVA_CLASS_PATH_CREDPTMODEL, JAVA_CREDPTMODEL_METHOD_SETBODY);
     jmethodID jsettype = JCOM_GET_METHOD_ID(env, JAVA_CLASS_PATH_CREDPTMODEL, JAVA_CREDPTMODEL_METHOD_SETTYPE);

     //create shaderlist
     jclass jList = JCOM_FIND_CLASS(env, JAVA_CLASS_ARRRAYLIST);
     jclass jKsyFilterShaderModel = JCOM_FIND_CLASS(env, JAVA_CLASS_PATH_CREDPTMODEL);

     jobject joCipherList = NULL;
     joCipherList = (env)->NewObject(jList, jmListConstruct);
     CipherUtility* utility = CipherUtility::GetInstance();
     ST_CIPHER_INFO* cipherInfo = utility->GetCipherContentList();
     ST_CIPHERCONTENT* cipherContents = cipherInfo->mCipherInfos;
     for(int i  = 0; i < cipherInfo->mCipherNumber; i++) {

         std::string skey = cipherContents[i].key;
         std::string sbody = cipherContents[i].body;
         jstring jkey = ToJString(env, skey);
         jstring jbody = ToJString(env, sbody);

         jobject jCipherContent =  (env)->NewObject(jKsyFilterShaderModel, jKsyConstruct);

         env->CallVoidMethod(jCipherContent, jsetbody, jbody);
         env->CallVoidMethod(jCipherContent, jsetkey, jkey);
         env->CallVoidMethod(jCipherContent, jsettype, cipherContents[i].type);

         //add item
         env->CallBooleanMethod(joCipherList, jmListAdd, jCipherContent);

         env->DeleteLocalRef(jkey);
         env->DeleteLocalRef(jbody);
         env->DeleteLocalRef(jCipherContent);
     }
     env->DeleteLocalRef(jKsyFilterShaderModel);
     env->DeleteLocalRef(jList);

     return joCipherList;
 }
