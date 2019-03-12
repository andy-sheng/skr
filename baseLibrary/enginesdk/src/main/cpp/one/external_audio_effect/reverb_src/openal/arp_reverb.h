
#ifndef _ARP_REVERB_H_
#define _ARP_REVERB_H_

#include "arp_effect_const.h"
#include "arp_filter.h"
#include <stdint.h>

class Arp_Reverb
{
public:
	Arp_Reverb();
	~Arp_Reverb();

public:
	int32_t Init(int32_t channels, int32_t freq, int32_t preset_id);
	void Reset();
	int32_t ResetEffectID(int32_t new_id);
	int32_t Process_set(char * pdata, int32_t len/*, float *pdataOut*/);
	void Uninit();
	
private:
	int32_t AllocLines(uint frequency);
	int32_t UpdateConst(uint frequency);
	int32_t UpDateLines();

	void PreProcess(short * pdata, int32_t len);
	void EAXProcess_set(short * pdata, int32_t len, short * pdataOut);
	void EAXVerbPass_set(short *in, int32_t len, float * out);

public:
	Arp_param	 m_param;
	uint		 m_nDelayTap[2];
	uint		 m_nDecoTap[3];
	uint		 m_nOffset;

private:
	int32_t m_channels;
	int32_t m_freq;
	int32_t	m_current_id;
    int32_t m_prevence_id;

	int16_t m_wetbuffer[ARP_EFFECT_BUFFERSIZE];
	int16_t m_drybuffer[ARP_EFFECT_STEREO][ARP_EFFECT_BUFFERSIZE];

	int16_t		 *m_pfSampleBuffer;
	uint		 m_nTotalSamples;
	Arp_Filter	 m_oLpFilter;
	Arp_Filter	 m_oHpFilter;
	Arp_Mod		 m_oMod;
	DelayLine	 m_oDelay;
	
	Arp_Early	 m_oEarly;
	DelayLine	 oDecorrelator;

	Arp_Late	 m_oLate;
	Arp_Echo	 m_oEcho;
	int16_t		 *m_pfGain;
	int16_t m_flReverbSamples[ARP_EFFECT_BUFFERSIZE][4];
	int16_t m_flEarlySamples[ARP_EFFECT_BUFFERSIZE][4];
	int16_t m_feed[ARP_EFFECT_BUFFERSIZE];
	int16_t m_taps[ARP_EFFECT_BUFFERSIZE][4];
};


#endif /* _ARP_REVERB_H_ */

