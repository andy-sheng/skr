#ifndef AUDIO_COMMON_COMMON_H
#define AUDIO_COMMON_COMMON_H
#ifdef __ANDROID__

#include <android/log.h>
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__);
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__);
#elif defined(__APPLE__)    // IOS or OSX
#define LOGI(...)  printf("  ");printf("\t -  <%s> : ", LOG_TAG);printf("%s: ",__func__);printf(__VA_ARGS__);
#define LOGE(...)  printf(" Error: ");printf("\t -  <%s> : ", LOG_TAG);printf("%s: ",__func__);printf(__VA_ARGS__);
#else
#define LOGI(...)  printf(__FUNCTION__);printf(__VA_ARGS__);
#define LOGE(...)  printf(__FUNCTION__);printf(__VA_ARGS__);
#endif

#define DELETEOBJSAFE(_X_) if (_X_!=NULL) {delete _X_;_X_ = NULL;}
#define DELETEARRAYSAFE(_X_) if (_X_!=NULL) {delete[] _X_;_X_ = NULL;}

#ifndef DEBUG
#undef LOGI
#define LOGI(...)
#endif

#endif
