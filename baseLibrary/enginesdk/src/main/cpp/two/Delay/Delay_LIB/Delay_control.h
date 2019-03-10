#ifndef SKR_DELAY_CONTROL_H
#define SKR_DELAY_CONTROL_H

#include "two/common/defines.h"

#define MAXDELAY (SKR_MAX_FRAME_SAMPLE_STEREO*5)//100ms
#define BUFFLEN (SKR_MAX_FRAME_SAMPLE_STEREO*50)//1000ms
#define MAXDELAYINT (40)


#ifdef __cplusplus
extern "C"
{
#endif

	typedef struct buffer{
		short membuf[BUFFLEN];//
		int id;
		int front;
		int rear;
	}Buf16_s;//nothing with channel samplerate

	

#ifdef __cplusplus
}
#endif

#endif