#include "jni_util.h"
#include "jni_onload.h"

JavaVM* g_current_java_vm_ = NULL;

jint JNI_OnLoad_Streamer(JavaVM* vm, void* reserved) {
	g_current_java_vm_ = vm;

	return JNI_VERSION_1_4;
}
