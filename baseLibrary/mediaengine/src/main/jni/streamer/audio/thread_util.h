#ifndef KSYSTREAMERANDROID_THREAD_UTIL_H_H
#define KSYSTREAMERANDROID_THREAD_UTIL_H_H

#ifdef __cplusplus
extern "C" {
#endif

void* createThreadLock(void);

void waitThreadLock(void *lock);

void notifyThreadLock(void *lock);

void destroyThreadLock(void *lock);

#ifdef __cplusplus
}
#endif

#endif //KSYSTREAMERANDROID_THREAD_UTIL_H_H
