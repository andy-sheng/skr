#include <string.h>
#include <stdlib.h>

#include "jni_util.h"
#include "util.h"
#include "log.h"

jstring ToJString(JNIEnv* env, const std::string& value) {
	return env->NewStringUTF(value.c_str());
}

std::string ToCppString(JNIEnv* env, jstring value) {
	jboolean isCopy;
	const char* c_value = env->GetStringUTFChars(value, &isCopy);
	std::string result(c_value);
	if (isCopy == JNI_TRUE)
		env->ReleaseStringUTFChars(value, c_value);
	return result;
}

jboolean ToJBool(bool value) {
	return value ? JNI_TRUE : JNI_FALSE;
}

bool ToCppBool(jboolean value) {
	return value == JNI_TRUE;
}

bool IsJavaInstanceOf(JNIEnv* env, jobject object,
		const std::string& class_name) {
	jclass clazz = env->FindClass(class_name.c_str());
	return clazz ? env->IsInstanceOf(object, clazz) == JNI_TRUE : false;
}

template<typename T>
jobject CreateJObject(JNIEnv* env, const std::string& class_name,
		const std::string& signature, T value) {
	jobject result = JNI_NULL;

	return result;
}

char* ToCString(JNIEnv* env, jstring jstr) {
	char* rtn = NULL;
	jclass clsstring = env->FindClass("java/lang/String");
	jstring strencode = env->NewStringUTF("utf-8");
	jmethodID mid = env->GetMethodID(clsstring, "getBytes",
			"(Ljava/lang/String;)[B");
	jbyteArray barr = (jbyteArray) env->CallObjectMethod(jstr, mid, strencode);
	jsize alen = env->GetArrayLength(barr);
	jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
	if (alen > 0) {
		rtn = (char*) malloc(alen + 1);
		memcpy(rtn, ba, alen);
		rtn[alen] = 0;
	}
	env->ReleaseByteArrayElements(barr, ba, 0);
	return rtn;
}

jobject ToJObject(JNIEnv* env, const Value& value) {
	jobject result = JNI_NULL;
	if (ValueIsInt(value)) {
		jclass clazz = env->FindClass("java/lang/Integer");
		jmethodID constructorID = env->GetMethodID(clazz, "<init>", "(I)V");
		result = env->NewObject(clazz, constructorID, GetIntValue(value));
	} else if (ValueIsFloat(value)) {
		jclass clazz = env->FindClass("java/lang/Float");
		jmethodID constructorID = env->GetMethodID(clazz, "<init>", "(F)V");
		result = env->NewObject(clazz, constructorID, GetFloatValue(value));
	} else if (ValueIsString(value)) {
		result = ToJString(env, GetStringValue(value));
	} else if (ValueIsIntArray(value)) {
		result = env->NewIntArray(GetValueCount(value));
		env->SetIntArrayRegion(static_cast<jintArray>(result), 0,
				GetValueCount(value),
				reinterpret_cast<const jint*>(GetIntArrayValue(value)));
	} else if (ValueIsFloatArray(value)) {
		result = env->NewFloatArray(GetValueCount(value));
		env->SetFloatArrayRegion(static_cast<jfloatArray>(result), 0,
				GetValueCount(value),
				reinterpret_cast<const jfloat*>(GetFloatArrayValue(value)));
	}
	return result;
}