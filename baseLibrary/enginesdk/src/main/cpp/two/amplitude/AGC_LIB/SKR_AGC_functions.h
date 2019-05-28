#ifndef SKR_FAGC_FUNCTIONS_H
#define SKR_FAGC_FUNCTIONS_H

#include "AGC_control.h"


#ifdef __cplusplus
extern "C"
{
#endif
	int noise_db2(float x_db,int nLen,float *memnoise_db,int *memUpwatch,float *memnewnoise_db,int updateThreshold);
    int noise_db3(float x_db,int nLen,float *memnoise_db,int *memUpwatch,float *memnewnoise_db,int updateThreshold);
	int noise_dbvip(float x_db,int nLen,float *memnoise_db,int *memUpwatch,float *memnewnoise_db,int updateThreshold);
	int noise_db4(float x_db,float *memnoise_db,int *memUpwatch,float *memnewnoise_db,int updateThreshold,int *localnoisechanged,int *memholdlowernoisewatch,float *memminxdb/*if don't hold,this xdb will become the noise*/,int *hold/*now it is for debug*/,short *input,int inlen,short *memtherealnoise_down,short *memtherealnoise_up,short *therealnoise/*output when localnoisechanged*/);
	int noise_db5(float x_db,float *memnoise_db,int *memUpwatch,float *memnewnoise_db,int updateThreshold,int *localnoisechanged,int *memholdlowernoisewatch,float *memminxdb/*if don't hold,this xdb will become the noise*/,int *hold/*now it is for debug*/,float *input,int inlen,short *memtherealnoise_down,short *memtherealnoise_up,short *therealnoise/*output when localnoisechanged*/);
	void UpdateCurv(Y_X_db_Curve *CurveOption,float noise_db);
	int ClippingDetec(short *input, int inLen, /*short *meminput,Œ¥ π”√*/ int chanel, int samplerate);
	void Options_for_TRAE_AGC(AGC_ID *mAGC);
	void BoostChangeFeedbackParameter(AGC_ID *mAGC,float boostpostion);
	void GainSpeechCurv(Y_X_db_Curve *CurveOption,float noise_db,float avg_db,int somein);
	void UpdateCurv2(Y_X_db_Curve *CurveOption,float noise_db);
	void UpdateCurv3(Y_X_db_Curve *CurveOption,float noise_db,float avg_db,int VAD,int BubbleGate);
	int Max_dbCalcu(float *mem_db,int *memwatch,float *memnew_db,float x_db,int updateThreshold);
	//void UpdateCurv_ES(Y_X_db_Curve *CurveOption,float noise_db,int ES);

	float boostdB(THE_MIC *amic,int boostmove,float boostpostion_dB,int *boost,int *vboost,float boost_dBlimit);
	int AGCVAD(int *memSpeech,int *memSilenceCountDown,float memnoiseVAD_db,float avgstax_db,float avgendx_db,float avgx_db);
	int vipVAD(int *memSpeech,int *memSilenceCountDown,float memnoiseVAD_db,float avgstax_db,float avgendx_db,float avgx_db,float vippesvdb,float lasthighdb,float period);
	int vipVADmod3(int *memSpeech,int *memSilenceCountDown,float memnoiseVAD_db,float avgstax_db,float avgendx_db,float avgx_db,float vippesvdb,float period);
	int vipVADmod4(int *memSpeech,int *memSilenceCountDown,float memnoiseVAD_db,float avgstax_db,float avgendx_db,float avgx_db,float vippesvdb,float lasthighdb,float period);
	void ESDetect_Simplex(ECNLP *NLP_SIMPLEX,int ecinf,int Nearendvad);
	void SCDetect_Simplex(ECNLP *NLP_SIMPLEX,float vol,int agressive);
	void FB_Simplex(ECNLP *NLP_SIMPLEX,int FBSimplex);
	float GainMicMod_fast(THE_MIC *amic,float boost_dB,float vol,float Gain);
	float GainMicMod_mid(THE_MIC *amic,float boost_dB,float vol,float Gain);
	float GainMicMod_slow(THE_MIC *amic,float boost_dB,float vol,float Gain);
	float volSmoothgain(float boostdB);
	void MicAdjust(THE_MIC *amic,int *fbinfo,int boost,int vboost,float *memvol);
	int ESdy(Dynamic_ID *theDy,int es,float noise_db,float k);
	int UpdateDy(Dynamic_ID *theDy,int dykind,int es,float noise_db,float avg_db,int vadresult,int bubble/*,int samplerate*/);
	void StaticCurvReset(Y_X_db_Curve *CurveOption,int kind);
	void SetMicProperty(THE_MIC *amic,int fbkind,float *boostinfo,/*,int vol*/float vvolmaxdB,float vvolmindB);

#ifdef __cplusplus
}
#endif

#endif
