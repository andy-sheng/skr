#ifndef LEVEL_CONTROL_H
#define LEVEL_CONTROL_H



#define VWWINDOWLEN (5)
#define STATIS_DB_N (40)

#ifdef __cplusplus
extern "C"
{
#endif
	typedef struct Level_channel_memory{
		float meminputLpeak_db[1];//输入的峰值电平
		
		float Tao_dBpT;//to be calcu
		
		
		int channel;
		int samplerate;
		float LevelAttackms;
		float LevelRealeasems;
	}Level_s;

	typedef struct VWLevel_channel_memory{
		unsigned int memTotalframes;         //O

		float memTMPLEVEL;
		float memframeTMPLEVEL[VWWINDOWLEN];
		float memPavglevel[STATIS_DB_N];       //O
		unsigned int memPlevel[STATIS_DB_N];   //O
		int memframeTMP_i;
		
		
		int chanel;
		int samplerate;
		int WindowLen;//		
	}VWLevel_s;

#ifdef __cplusplus
}
#endif

#endif