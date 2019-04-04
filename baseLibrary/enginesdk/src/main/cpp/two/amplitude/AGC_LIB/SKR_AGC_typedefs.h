#ifndef SKR_AGC_TYPEDEFS_H
#define SKR_AGC_TYPEDEFS_H


#ifdef __cplusplus
extern "C"
{
#endif

	typedef struct micinfo{
		/*vmic:*/
		float vvol;
		float vvolmin;
		float vvolmax;

		float vboost_dB;
		float vboostmin_dB;
		float vboostmax_dB;
		float vstep_dB;

		/*rmic:*/
		float rvol;
		float rvolmin;//0
		float rvolmax;//65535

		float rboost_dB;
		float rboostmin_dB;
		float rboostmax_dB;
		float rstep_dB;

		short capability;//(...vvol,vboost,vol,boost,os,allowup)
		//int boostcondition;

		//vboost:(0/n*x,x,y,z)
		//noboost:(-1,-1,-1,-1)
		//xpboost:(0/1,-1,-1,-1)
		//w7boost:(0/n*x,x,y,z)

		float rvolnoise[7][21];//80-100//7 step boost
		//float mymic.rvolnoise[21];//80-100
		float bigsteprvol[7];

	}THE_MIC;

	typedef struct forecnlp{
		int memCS;
		int memCSSilenceCountDown;//capspeechlikely的尾音保护
		//int memSS;
		//int memSSSilenceCountDown;//防止支离破碎引起不平滑,弱保护
		
		float memmicvolSS;
		int ShortCircuit;
		float capavgx_db_nlpmod;
		float TargetD_db_nlpmod;
		float Smalldb_nlpmod;
		float DownIntevalTime_ms_nlpmod;
		float UPIntevalTime_ms_nlpmod;
		
		//here "ss && downvol" is named ssdown;
		int memSSdown_watch;
		float memSSdown_gain;	
		int SSdown_gainThreshold;//after this ssdown times,send a gain to agcin
		float SSdown_gainmax;//the max gain to agcin
		float SSdown_gainstep;
		
		int CS;
		int ES;
		int SS;//0,1,2

		int CSA;
		int ESA;
		int SSA;
	
	}ECNLP;






#ifdef __cplusplus
}
#endif


#endif