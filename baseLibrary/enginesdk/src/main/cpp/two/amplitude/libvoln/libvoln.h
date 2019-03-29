//-----how to use??????????????:
//void main()//voln
//{
//	void *pvoln;
//	FILE *frx,*fwy;
//	int frame;
//	int framelen;
//
//	int samplerate;
//	int channel;
//	float maxgaindB;
//	float fstgaindB;
//	int needDC;
//	int dykind;
//
//	short y[6000];
//	short x[6000];
//
//	frx = fopen("input.pcm", "rb");
//	fwy = fopen("output.pcm","wb");
//
//
//	samplerate = 16000;
//	channel = 1;
//  
//	maxgaindB = 20.f;
//	fstgaindB = 15.f;
//	needDC = 1;
//	dykind = 0;
//
//	framelen = (int)(samplerate*0.02);
//
//	SKR_agc_create(&pvoln);
//	SKR_agc_reset(pvoln);
//	SKR_agc_config(pvoln,samplerate,channel,dykind,maxgaindB,fstgaindB,needDC);
//
//	for(frame=0;;frame++)
//	{
//		if(fread(x, sizeof(short), framelen, frx) != framelen) break;
//
//		SKR_agc_proc(pvoln, x, framelen,y);  
//
//		fwrite(y, sizeof(short), framelen, fwy);	
//
//		printf("Doing volume normalization...frame%d\r",frame);
//
//	}
//
//	printf("\nFininsh!\n\n\n");
//
//	fclose(frx);
//	fclose(fwy);
//
//	SKR_agc_free(pvoln);
//
//}

#ifndef LIBVOLN_H
#define LIBVOLN_H



#ifdef __cplusplus
extern "C" {
#endif
////////////////////////////////Volume normalization:dc-vmic-gc//////////////////////////////////////////
int SKR_agc_create(void **mVOLN);// 0: OK, -1: Failed
int SKR_agc_free(void *mVOLN);   // 0: OK, -1: Failed
void SKR_agc_reset(void *mVOLN); // called only one time after calling VOLNCreate_API

// Recommended values of (maxgaindB, fstgaindB, DC, dynamic_kind) is (30, 15, 1, 0)
//
// samplerate: 8000/16000/48000
// channel: 1
// dynamic_kind: non-linear gain, can make sound more louder
//               use 0 by default which means to disable non-linear gain
// maxgaindB: max gain, use 29.f by default
// fstgaindB: initial gain, use 15.f by default
// DC: use 1 by default which means to eliminate DC offset, and you can set it to 0 to disable this operation
// 0: OK, otherwise
//        -1: samplerate was illegal 
//        -2: channel was illegal
//        -3: dynamic_kind was illegal
//        -4: maxgaindB was illegal 
//        -5: fstgaindB was illegal
//        -6: DC was illegal
int SKR_agc_config(void *mVOLN,int samplerate,int channel,int dynamic_kind,float maxgaindB,float fstgaindB,int DC);
int SKR_agc_config_int(void *mVOLN, int samplerate, int channel, int dykind, int maxgaindB, int fstgaindB, int DC);
// inLen: MUST be samplerate * 0.02
// 0: OK, -1: Failed, inLen was illegal
int SKR_agc_proc(void *mVOLN, short *input, int inLen, short *output);



#ifdef __cplusplus
}
#endif

#endif