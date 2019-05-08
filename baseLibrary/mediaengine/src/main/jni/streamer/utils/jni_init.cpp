#include "jni_util.h"

JavaVM* g_current_java_vm_ = NULL;

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	g_current_java_vm_ = vm;
	return JNI_VERSION_1_4;
}
