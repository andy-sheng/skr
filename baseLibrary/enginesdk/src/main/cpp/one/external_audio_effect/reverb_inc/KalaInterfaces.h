/************************************************************************/
/* kala ok audio base module interface.                                 */
/************************************************************************/

#ifndef KALA_AUDIO_BASE_H
#define KALA_AUDIO_BASE_H

/* 混响接口 */
class IKalaReverb
{
public:
	virtual int     Init(int sampleRate, int channel) = 0;
	virtual void	Reset()								= 0;
	virtual void    Uninit()							= 0;

	virtual void    GetIdRange(int* maxVal, int* minVal) = 0;
	virtual int     GetIdDefault()					  = 0;

	virtual int     SetTypeId(int typeID)		  = 0;
	virtual int     GetTypeId()					  = 0;
	virtual char *  GetNameById	(int typeID)		  = 0;
	virtual int		SetRoomSize(float roomSize) = 0;
	virtual int		SetWet(float wet) = 0;

	virtual int     GetLatence() = 0;

	virtual int     Process(short * inBuffer, int inSize, short * outBuffer, int outSize) = 0;
    virtual int     Process(float * inBuffer, int inSize, float * outBuffer, int outSize) = 0;

	virtual int     ProcessLRIndependent(float * inLeft, float * inRight, float * outLeft, float * outRight, int inOutSize) = 0;
};

/* 留声机接口 **/
class IKalaDspProcessor
{
public:

	virtual int Init(int inSampleRate, int inChannel) = 0;	// set sample rate and channel;
	virtual void Uninit() = 0;	// uninit

	// process input buffer and output size.
	virtual int Process(short* inBuffer, int inSize) = 0;
	virtual int Process(float* inBuffer, int inSize) = 0;
};

#endif
