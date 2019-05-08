#include "util.h"
#include <time.h>
#include "log.h"
#include <libavutil/log.h>

char ffmpeg_error_str[512];
int ffmpeg_error_code = 0;

long long getTime() {
	struct timeval now;
	gettimeofday(&now, NULL);
	return (long long) ((long long)now.tv_sec * 1000 + now.tv_usec / 1000);
}

void sanitizein(uint8_t *line) {
    while (*line) {
        if (*line < 0x08 || (*line > 0x0D && *line < 0x20))
            *line = '?';
        line++;
    }
}

