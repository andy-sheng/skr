#ifndef FUNCTIOskrH
#define FUNCTIOskrH

#include <math.h>

#ifdef __cplusplus
extern "C"
{
#endif


static __inline short stoshort(float x){short y;y = (short)x;	if(x>32767)y=32767;if(x<-32768)y = -32768;return y;}
static __inline short smult(short x, float y){return stoshort(x*y);}

static __inline float idB(float x_dB){return pow(10,x_dB/20);}
static __inline float dB(float x){return 20*log10(x);}
static __inline float idb(float x_db, float x0){return x0*pow(10,x_db/20);}
static __inline float db(float x, float x0){return 20*log10(x/x0);}

static __inline int Loopadd(int head,int distance,int LoopT){return (head + distance) % LoopT;}


#define THEMAXOF(x,y)  ( (x)>(y)?(x):(y) )
#define THEMINOF(x,y)  ( (x)<(y)?(x):(y) )
    

static __inline float MchuyiNchangyiK(int theM,int theN,int theK)
{
	if (theN == theK)
	{
		return theM;
	} 
	else
	{
		return (float)theM/theN*theK;
	}
}

int fwrite_SKR(const void *towrite, int sizeofelement, int thecount,const char *filename);
int fread_SKR(void *dst, int sizeofelement, int thecount, const char *filename);
int fread_24bitto16(short *pcm, int len, char *filename);
void fclose_SKR(const char *filename);
int fwrite_t_SKR(const void *towrite, int sizeofelement, int thecount, const char *filename, short line);

int freadwavhead(int *ch, int *sr, int *bitpersample, char *filename);
//after readwavhead we can use freadSKR to read pcm from wav
void fwritewavpcm(short *pcm, int len, char *filename);
void fclosewav(int ch, int sr, char *filename);
void fwritewavpcm_t(short *pcm, int len, char *filename);

void fcloseall_SKR();
int findfile_SKR(const char *filename);

extern char gappfilepath_SKR[120];
int fwrite_findordef(const void *towrite, int sizeofelement, int thecount, const char *filename, short line, int hasdefine);
int findfile(const char *filename);
int fwritefile(const void *towrite, int sizeofelement, int thecount, const char *filename, short line);
int freadfile(void *dst, int sizeofelement, int thecount, const char *filename);

void ChanelConvert(int inchanel,int outchanel,int len,short *in1,short *in2,short *out1,short *out2);
void ChanelConvert_f(int inchanel,int outchanel,int len,float *in1,float *in2,float *out1,float *out2);
void ChannelSplit(short *in, int inlen, short *out[], int outchannel);
void ChannelMerge(short *in, int inlen, short *out[], int outchannel);
void ChebyII_Lowpassc(int Order,float f1,float f2,float dB,float *b,float *a);


void rfftmut2(float *y,float *x1,float *x2,int n);

#pragma pack(push,1)
typedef struct wavefilehead{

	char RIFF[4];//Resource Interchange File Flag (0-3) "RIFF"
	int FileLength;//File Length ( not include 8 bytes from the beginning ) (4-7)//RawDataFileLength + 44 - 8;
	char WAVEfmt_[8];//WAVE File Flag (8-15) "WAVEfmt "
	int SizeofPCMWAVEFORMAT;//Transitory Byte ( normally it is 10H 00H 00H 00H ) (16-19) //Sizeof(PCMWAVEFORMAT)
	short FormatCategory;//Format Category ( normally it is 1 means PCM-u Law ) (20-21)
	short NChannels;//NChannels (22-23)
	int SampleRate;//Sample Rate (24-27)//nSamplesPerSec
	int SampleBytes;//nAvgBytesperSec//l=NChannels*SampleRate*NBitsPersample/8 (28-31)
	short BytesPerSample;//nBlockAlign//i=NChannels*NBitsPersample/8 (32-33)
	short NBitsPersample;//NBitsPersample (34-35)
	char data[4];//Data Flag (36-39) "data"
	int RawDataFileLength;//Raw Data File Length (41-43)

}WavHead;
#pragma pack(pop)

#ifdef __cplusplus
}
#endif

#endif