#ifndef DEFINES_H
#define DEFINES_H

#ifdef __cplusplus
extern "C"
{
#endif

#define U0 32768.0//电平参考值,不对此建立define头，是因为这个没什么可改的，只是用符号表示而已
#define UdBFS 32768.0 

#define G10 (pow(10.0,0.3))
#define G2 (2.0)
#define FTAO (1000.0)

#if defined(_WIN32) || defined(WIN32)
#define SKR_MAX_FRAME_SAMPLE_MONO (20*192*1)//max time*max samplerate*mono
#else
#define SKR_MAX_FRAME_SAMPLE_MONO (22*48*1)//max time*max samplerate*mono
#endif
#define SKR_MAX_FRAME_SAMPLE_STEREO (SKR_MAX_FRAME_SAMPLE_MONO*2)//max time*max samplerate*mono

#define SKR_PAI (3.141592653589793238462643383279)
#define SKR_2PAI (6.283185307179586476925286766558)


#ifdef __cplusplus
}
#endif

#endif