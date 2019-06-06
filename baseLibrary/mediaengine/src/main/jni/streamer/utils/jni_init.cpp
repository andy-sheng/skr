#include "jni_util.h"

JavaVM* g_current_java_vm_ = NULL;

extern int register_android_nio_utils(JNIEnv* env);

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    g_current_java_vm_ = vm;

    JNIEnv *env = NULL;
    vm->GetEnv((void**)&env, JNI_VERSION_1_4);
    register_android_nio_utils(env);
    return JNI_VERSION_1_4;
}
