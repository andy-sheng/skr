
#ifndef android_nio_utils_DEFINED
#define android_nio_utils_DEFINED
#include <jni.h>
# include <stdint.h>

void* nio_getPointer(JNIEnv *env, jobject buffer, jarray *array);

void nio_releasePointer(JNIEnv *env, jarray array, void *pointer,
		jboolean commit);

class AutoBufferPointer {
public:
	AutoBufferPointer(JNIEnv* env, jobject nioBuffer, jboolean commit);
	~AutoBufferPointer();

	void* pointer() const {
		return fPointer;
	}

private:
	JNIEnv* fEnv;
	void* fPointer;
	jarray fArray;
	jint fRemaining;
	jboolean fCommit;
};

#endif
