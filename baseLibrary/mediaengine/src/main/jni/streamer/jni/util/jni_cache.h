//
//catch all java class MethodID and FieldID
//you may use CacheClass to cache the calss's all MethodID and FieldID
//you may use GetMethodID to get the MethodID
//you may use GetFieldID to get the FieldID
// Created by 昝晓飞 on 16/9/13.
//

#ifndef KSYSTREAMERANDROID_JNI_MID_CATCH_H
#define KSYSTREAMERANDROID_JNI_MID_CATCH_H
#include <string>
#include <map>
#include <jni.h>

#include "jni_class_def.h"

using namespace std;

/****************Marco Definition Section Begin*******************************************/
#define JCOM_GET_FIELD_ID(env, className, fieldName) \
    (GetJniCacheInstance()->GetFieldID(env, className,fieldName))

#define JCOM_GET_METHOD_ID(env, className, methodName) \
    (GetJniCacheInstance()->GetMethodID(env, className, methodName))

#define JCOM_GET_FIELD_L(env, obj, className, fieldName) \
    (env->GetObjectField(obj, JCOM_GET_FIELD_ID(env, className,fieldName)))

#define JCOM_GET_FIELD_I(env, obj, className, fieldName) \
        (env->GetIntField(obj, JCOM_GET_FIELD_ID(env, className,fieldName)))

#define JCOM_GET_FIELD_J(env, obj, className, fieldName) \
        (env->GetLongField(obj, JCOM_GET_FIELD_ID(env, className,fieldName)))

#define JCOM_SET_FIELD_L(env, obj, className, fieldName, value) \
    (env->SetObjectField(obj, JCOM_GET_FIELD_ID(env, className,fieldName), value))

#define JCOM_SET_FIELD_I(env, obj, className, fieldName, value) \
    (env->SetIntField(obj, JCOM_GET_FIELD_ID(env, className,fieldName), value))

#define JCOM_SET_FIELD_J(env, obj, className, fieldName, value) \
    (env->SetLongField(obj, JCOM_GET_FIELD_ID(env, className,fieldName), value))

#define JCOM_METHOD_L(env, obj, className, methodName, args...) \
(env->CallObjectMethod(obj, JCOM_GET_METHOD_ID(env, className, methodName), ##args))

#define JCOM_METHOD_I(env, obj, className, methodName, args...) \
(env->CallIntMethod(obj, JCOM_GET_METHOD_ID(env, className, methodName), ##args))

#define JCOM_METHOD_V(env, obj, className, methodName, args...) \
    (env->CallVoidMethod( obj, JCOM_GET_METHOD_ID(env, className, methodName), ##args))

#define JCOM_FIND_CLASS(env, className) \
    (env->FindClass(className))

#define INFO_NUMBER(a)    (a==NULL ? 0 : (sizeof(a) / sizeof(a[0])))

#define ST_CLASSLIST_ITEM(className, classMethodInfo, classFieldInfo) { \
    className, INFO_NUMBER(classMethodInfo), INFO_NUMBER(classFieldInfo), classMethodInfo, classFieldInfo }


#define KSY_MAX_CLASSNAME_LEN 80
#define KSY_MAX_METHOD_LEN 64
#define KSY_MAX_FIELD_LEN 64
#define KSY_MAX_METHOD_SIGNATURE_LEN 240
#define KSY_MAX_FIELD_SIGNATURE_LEN 64
/****************Marco Definition Section End*******************************************/

/****************Struct Definition Section Begin*********************************************/
union unFMID
{
    jfieldID feildID;
    jmethodID methodID;
};

typedef struct _JNI_COM_METHOD
{
     char   method_name[KSY_MAX_METHOD_LEN];
     char   method_signature[KSY_MAX_METHOD_SIGNATURE_LEN];
        bool   static_method;
}JNI_COM_METHOD;

typedef struct _JNI_COM_FIELD
{
      char       field_name[KSY_MAX_FIELD_LEN];
      char       field_signature[KSY_MAX_FIELD_SIGNATURE_LEN];
}JNI_COM_FIELD;

typedef JNI_COM_METHOD ST_METHODINFO;
typedef JNI_COM_FIELD ST_FIELDINFO;
typedef map< string, map<string, unFMID> > MIDMap;
typedef map<string, unFMID> MIDItem;

typedef struct st_classInfo {
    char mstrName[KSY_MAX_CLASSNAME_LEN];
    long unsigned int miMethodNum;
    long unsigned int miFieldNum;
    ST_METHODINFO* mstMethodInfo;
    ST_FIELDINFO*  mstFieldInfo;
} ST_CLASSINFO;
/****************Struct Definition Section End*********************************************/

class JniCache {
public:
    static JniCache *GetInstance();
    virtual ~JniCache();

    void CacheGloablJavaVM(JNIEnv *env);
    JavaVM* GetGloablJavaVM(void);

    bool CacheClass(JNIEnv *env, const char* pStrClassName);
    jmethodID GetMethodID(JNIEnv *env, const char* pStrClassName, const char* pStrMethodName);
    jfieldID GetFieldID(JNIEnv *env, const char* pStrClassName, const char* pStrFieldName);
protected:
    JniCache();
    bool CheckInCache(const char* pStrClassName);
    bool CacheClassInfo(JNIEnv *env, const char* pStrClassName);
    bool GetFMId(JNIEnv *env, const char* pStrClassName, const char* pStrFMName, unFMID* unFmId);
private:
    MIDMap m_idMap;
};

JniCache *GetJniCacheInstance();

#endif //KSYSTREAMERANDROID_JNI_MID_CATCH_H
