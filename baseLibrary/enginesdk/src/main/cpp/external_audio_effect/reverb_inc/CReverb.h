#ifndef C_REVERB_RATION_H
#define C_REVERB_RATION_H
/************************************************************************/
/* reverberation				                                        */
/************************************************************************/

#include "KalaInterfaces.h"

/* version before kala v2.6, only 0\4\5\6 used. kala 1.0 use id [0,3]*/
#define KALA_VB_RECORD_STUDIO	0		// lu yin peng
#define KALA_VB_KTV				1		// KTV
#define KALA_VB_CONCERT			2		// yan chang hui
#define KALA_VB_THEATER			3		// ju chang
#define KALA_VB_NEW_KTV			4		// new ktv
#define KALA_VB_NEW_CONCERT		5		// new concert
#define KALA_VB_NEW_THEATER		6		// new theater

/* id not used */
#define KALA_VB_ID_7			7		// lu yin pen
#define KALA_VB_ID_8			8		// lu yin pen
#define KALA_VB_ID_9			9		// lu yin pen

/* id used for kala v3.0's quick version */
#define KALA_VB_ID_10			10		// 
#define KALA_VB_ID_11			11		// 
#define KALA_VB_ID_12			12		// 
#define KALA_VB_ID_13			13		// 
#define KALA_VB_ID_14			14		// 
#define KALA_VB_ID_15			15		// 
#define KALA_VB_ID_16			16		// 
#define KALA_VB_ID_17			17		// 
#define KALA_VB_ID_18			18		// 

class CReverb : public IKalaReverb
{
public:
    int     Init(int sampleRate, int channel);
	void	Reset();
    void    Uninit();
    
    void    GetIdRange(int* maxVal, int* minVal);
    int     GetIdDefault();
    
    int     SetTypeId(int typeID);
    int     GetTypeId();
    char *  GetNameById(int typeID);

	int  SetRoomSize(float roomSize);
	int  SetWet(float wet);

	float  GetRoomSize();
	float  GetWet();

	int     GetLatence();
    
    int     Process(short * inBuffer, int inSize, short * outBuffer, int outSize);
    int     Process(float * inBuffer, int inSize, float * outBuffer, int outSize);

	int     ProcessLRIndependent(float * inLeft, float * inRight, float * outLeft, float * outRight, int inOutSize);
    
private:
	int m_id;
	int m_sampleRate;
	int m_channel;

	void* m_pvb;
};



#endif


