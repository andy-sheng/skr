
#ifndef FFMPE_H
#define FFMPE_H
#ifdef __cplusplus
extern "C" {
#endif

#include <stdint.h>
//ms
#define FFMPEG_TIME_BASE            1000
#define FFMPEG_ERROR_STR_RET(str,ret) do{ \
ffmpeg_error_code = ret;\
 sprintf(ffmpeg_error_str, "[File:%s Line:%d] Fun:%s error:%s,%s", __FILE__, __LINE__, __FUNCTION__,str,av_err2str(ret)); \
}while(0)

#define FFMPEG_ERROR_STR(str) do{ \
ffmpeg_error_code = -1;\
sprintf(ffmpeg_error_str, "[File:%s Line:%d] Fun:%s error:%s", __FILE__, __LINE__, __FUNCTION__,str); \
}while(0)


long long getTime();
void sanitizein(uint8_t *line);
extern int ffmpeg_error_code;
extern char ffmpeg_error_str[512];
#ifdef __cplusplus
}
#endif

#endif
