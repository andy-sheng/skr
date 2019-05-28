

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "arp_reverb.h"
#include "arp_effect_error.h"

#ifdef DOWN_AUDIO

FILE * g_fp;
int32_t	g_sumLen = 0;
#include "WavDef.h"

#endif

//static Arp_param aEaxPreset[ARP_EFFECT_ID_MAX] =
//{
//	// EFX_REVERB_PRESET_GENERIC
//	{ 1.0000f, 1.0000f, 0.3162f, 0.8913f, 1.0000f, 1.4900f, 0.8300f, 1.0000f, 0.0500f, 0.0070f, { 0.0000f, 0.0000f, 0.0000f }, 1.2589f, 0.0110f, { 0.0000f, 0.0000f, 0.0000f }, 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1 },
//
//	// EFX_REVERB_PRESET_ROOM
//	{ 0.4287f, 1.0000f, 0.3162f, 0.5929f, 1.0000f, 0.4000f, 0.8300f, 1.0000f, 0.1503f, 0.0020f, { 0.0000f, 0.0000f, 0.0000f }, 1.0629f, 0.0030f, { 0.0000f, 0.0000f, 0.0000f }, 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1 },
//
//	// EFX_REVERB_PRESET_CONCERTHALL
//	{ 1.0000f, 1.0000f, 0.3162f, 0.5623f, 1.0000f, 3.9200f, 0.7000f, 1.0000f, 0.2427f, 0.0200f, { 0.0000f, 0.0000f, 0.0000f }, 0.9977f, 0.0290f, { 0.0000f, 0.0000f, 0.0000f }, 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1 },
//
//	// EFX_REVERB_PRESET_ARENA
//	{ 1.0000f, 1.0000f, 0.3162f, 0.4477f, 1.0000f, 7.2400f, 0.3300f, 1.0000f, 0.2612f, 0.0200f, { 0.0000f, 0.0000f, 0.0000f }, 1.0186f, 0.0300f, { 0.0000f, 0.0000f, 0.0000f }, 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1 },
//
//	// EFX_REVERB_PRESET_ICEPALACE_HALL
//	{ 1.0000f, 0.7600f, 0.3162f, 0.4467f, 0.5623f, 5.4900f, 1.5300f, 0.3800f, 0.1122f, 0.0540f, { 0.0000f, 0.0000f, 0.0000f }, 0.6310f, 0.0520f, { 0.0000f, 0.0000f, 0.0000f }, 0.2260f, 0.1100f, 0.2500f, 0.0000f, 0.9943f, 12428.5000f, 99.6000f, 0.0000f, 0x1 },
//
//	// EFX_REVERB_PRESET_SPACESTATION_CUPBOARD
//	{ 0.1715f, 0.5600f, 0.3162f, 0.7079f, 0.8913f, 0.7900f, 0.8100f, 0.5500f, 1.4125f, 0.0070f, { 0.0000f, 0.0000f, 0.0000f }, 1.7783f, 0.0180f, { 0.0000f, 0.0000f, 0.0000f }, 0.1810f, 0.3100f, 0.2500f, 0.0000f, 0.9943f, 3316.1001f, 458.2000f, 0.0000f, 0x1 },
//};

static Arp_param aEaxPreset[ARP_EFFECT_ID_MAX] =
{
	// EFX_REVERB_PRESET_GENERIC				0
	{ 1.0000f, 1.0000f, 0.01f, 0.8913f, 1.0000f, 1.4900f, 0.8300f, 1.0000f, 0.0500f, 0.0070f, { 0.0000f, 0.0000f, 0.0000f }, 1.2589f, 0.0110f, { 0.0000f, 0.0000f, 0.0000f }, 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1 },
//    { 1.0000f, 1.0000f, 0.3162f, 0.8913f, 1.0000f, 1.4900f, 0.8300f, 1.0000f, 0.0500f, 0.0070f, { 0.0000f, 0.0000f, 0.0000f }, 1.2589f, 0.0110f, { 0.0000f, 0.0000f, 0.0000f }, 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1 },
    //{ 1.0000f, 1.0000f, 0.1f, 0.8913f, 1.0000f, 1.4900f, 0.8300f, 1.0000f, 0.0500f, 0.0070f, { 0.0000f, 0.0000f, 0.0000f }, 1.2589f, 0.0110f, { 0.0000f, 0.0000f, 0.0000f }, 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1 },
	//EFX_REVERB_PRESET_GENERIC
	{ 1.0000f, 1.0000f, 0.1f, 0.8913f, 1.0000f, 1.4900f, 0.8300f, 1.0000f, 0.0500f, 0.0070f, { 0.0000f, 0.0000f, 0.0000f }, 1.2589f, 0.0110f, { 0.0000f, 0.0000f, 0.0000f }, 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1 },

	//EFX_REVERB_PRESET_CITY_STREETS
	{ 1.0000f, 0.7800f, 0.1f, 0.7079f, 0.8913f, 1.7900f, 1.1200f, 0.9100f, 0.2818f, 0.0460f, { 0.0000f, 0.0000f, 0.0000f }, 0.1995f, 0.0280f, { 0.0000f, 0.0000f, 0.0000f }, 0.2500f, 0.2000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1 },

	//EFX_REVERB_PRESET_GENERIC
	{ 1.0000f, 1.0000f, 0.1f, 0.8913f, 1.0000f, 1.4900f, 0.8300f, 1.0000f, 0.0500f, 0.0070f, { 0.0000f, 0.0000f, 0.0000f }, 1.2589f, 0.0110f, { 0.0000f, 0.0000f, 0.0000f }, 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1 },

	//EFX_REVERB_PRESET_CASTLE_COURTYARD
	{ 1.0000f, 0.4200f, 0.1162f, 0.4467f, 0.1995f, 2.1300f, 0.6100f, 0.2300f, 0.2239f, 0.1600f, { 0.0000f, 0.0000f, 0.0000f }, 0.7079f, 0.0360f, { 0.0000f, 0.0000f, 0.0000f }, 0.2500f, 0.3700f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x0 },

	//EFX_REVERB_PRESET_CASTLE_HALL
	{ 1.0000f, 0.8100f, 0.1162f, 0.2818f, 0.1778f, 7.5400f, 0.7900f, 0.6200f, 0.1778f, 0.0560f, { 0.0000f, 0.0000f, 0.0000f }, 1.1220f, 0.0240f, { 0.0000f, 0.0000f, 0.0000f }, 0.2500f, 0.5000f, 0.2500f, 0.0000f, 0.9943f, 5168.6001f, 139.5000f, 0.0000f, 0x1 },


	//EFX_REVERB_PRESET_DIZZY_NEW
	{ 0.3645f, 0.6000f, 0.1f, 0.6310f, 1.0000f, 6.2300f, 0.5600f, 1.0000f, 0.1392f, 0.0200f, { 0.0000f, 0.0000f, 0.0000f }, 0.4937f, 0.0300f, { 0.0000f, 0.0000f, 0.0000f }, 0.8500f, 0.6000f, 0.8100f, 0.3100f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x0 },

	//EFX_REVERB_PRESET_CASTLE_HALL_NEW
	{ 1.0000f, 0.8100f, 0.1162f, 0.2818f, 0.1778f, 10.400f, 0.7900f, 0.6200f, 0.1778f, 0.0560f, { 0.0000f, 0.0000f, 0.0000f }, 1.1220f, 0.0240f, { 0.0000f, 0.0000f, 0.0000f }, 0.2500f, 0.9500f, 0.2500f, 0.0000f, 0.9943f, 5168.6001f, 139.5000f, 0.0000f, 0x1 },


	// EFX_REVERB_PRESET_ROOM					2
	//{ 0.4287f, 1.0000f, 0.3162f, 0.5929f, 1.0000f, 0.4000f, 0.8300f, 1.0000f, 0.1503f, 0.0020f, { 0.0000f, 0.0000f, 0.0000f }, 1.0629f, 0.0030f, { 0.0000f, 0.0000f, 0.0000f }, 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1 },

	// EFX_REVERB_PRESET_CONCERTHALL			7
	//{ 1.0000f, 1.0000f, 0.3162f, 0.5623f, 1.0000f, 3.9200f, 0.7000f, 1.0000f, 0.2427f, 0.0200f, { 0.0000f, 0.0000f, 0.0000f }, 0.9977f, 0.0290f, { 0.0000f, 0.0000f, 0.0000f }, 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1 },

	// EFX_REVERB_PRESET_ARENA					9
	//{ 1.0000f, 1.0000f, 0.3162f, 0.4477f, 1.0000f, 7.2400f, 0.3300f, 1.0000f, 0.2612f, 0.0200f, { 0.0000f, 0.0000f, 0.0000f }, 1.0186f, 0.0300f, { 0.0000f, 0.0000f, 0.0000f }, 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1 },

	// EFX_REVERB_PRESET_ICEPALACE_HALL			49
	//{ 1.0000f, 0.7600f, 0.3162f, 0.4467f, 0.5623f, 5.4900f, 1.5300f, 0.3800f, 0.1122f, 0.0540f, { 0.0000f, 0.0000f, 0.0000f }, 0.6310f, 0.0520f, { 0.0000f, 0.0000f, 0.0000f }, 0.2260f, 0.1100f, 0.2500f, 0.0000f, 0.9943f, 12428.5000f, 99.6000f, 0.0000f, 0x1 },

	// EFX_REVERB_PRESET_SPACESTATION_CUPBOARD	59
	//{ 0.1715f, 0.5600f, 0.3162f, 0.7079f, 0.8913f, 0.7900f, 0.8100f, 0.5500f, 1.4125f, 0.0070f, { 0.0000f, 0.0000f, 0.0000f }, 1.7783f, 0.0180f, { 0.0000f, 0.0000f, 0.0000f }, 0.1810f, 0.3100f, 0.2500f, 0.0000f, 0.9943f, 3316.1001f, 458.2000f, 0.0000f, 0x1 },


	// EFX_REVERB_PRESET_STONEROOM						5
	//{ 1.0000f, 1.0000f, 0.3162f, 0.7079f, 1.0000f, 2.3100f, 0.6400f, 1.0000f, 0.4411f, 0.0120f, { 0.0000f, 0.0000f, 0.0000f }, 1.1003f, 0.0170f, { 0.0000f, 0.0000f, 0.0000f }, 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1 },

	// EFX_REVERB_PRESET_AUDITORIUM						6
	//{ 1.0000f, 1.0000f, 0.3162f, 0.5781f, 1.0000f, 4.3200f, 0.5900f, 1.0000f, 0.4032f, 0.0200f, { 0.0000f, 0.0000f, 0.0000f }, 0.7170f, 0.0300f, { 0.0000f, 0.0000f, 0.0000f }, 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1 },

	// EFX_REVERB_PRESET_CAVE							8
	//{ 1.0000f, 1.0000f, 0.3162f, 1.0000f, 1.0000f, 2.9100f, 1.3000f, 1.0000f, 0.5000f, 0.0150f, { 0.0000f, 0.0000f, 0.0000f }, 0.7063f, 0.0220f, { 0.0000f, 0.0000f, 0.0000f }, 0.2500f, 0.0000f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x0 },

	// EFX_REVERB_PRESET_ALLEY							14
	//{ 1.0000f, 0.3000f, 0.3162f, 0.7328f, 1.0000f, 1.4900f, 0.8600f, 1.0000f, 0.2500f, 0.0070f, { 0.0000f, 0.0000f, 0.0000f }, 0.9954f, 0.0110f, { 0.0000f, 0.0000f, 0.0000f }, 0.1250f, 0.9500f, 0.2500f, 0.0000f, 0.9943f, 5000.0000f, 250.0000f, 0.0000f, 0x1 },

	// EFX_REVERB_PRESET_FACTORY_LARGEROOM				38, // 工厂，大房间  将此效果作为停车场的代替
	//{ 0.4287f, 0.7500f, 0.2512f, 0.7079f, 0.6310f, 4.2400f, 0.5100f, 1.3100f, 0.1778f, 0.0390f, { 0.0000f, 0.0000f, 0.0000f }, 1.1220f, 0.0230f, { 0.0000f, 0.0000f, 0.0000f }, 0.2310f, 0.0700f, 0.2500f, 0.0000f, 0.9943f, 3762.6001f, 362.5000f, 0.0000f, 0x1 },

	// EFX_REVERB_PRESET_CITY_LIBRARY					107, // 城市，图书馆
	//{ 1.0000f, 0.8200f, 0.3162f, 0.2818f, 0.0891f, 2.7600f, 0.8900f, 0.4100f, 0.3548f, 0.0290f, { 0.0000f, 0.0000f, -0.0000f }, 0.8913f, 0.0200f, { 0.0000f, 0.0000f, 0.0000f }, 0.1300f, 0.1700f, 0.2500f, 0.0000f, 0.9943f, 2854.3999f, 107.5000f, 0.0000f, 0x0 },

};

static void Set_PreSet(Arp_param & tp_param, int32_t preset_id)
{
	tp_param.flDensity = aEaxPreset[preset_id].flDensity;
	tp_param.flDiffusion = aEaxPreset[preset_id].flDiffusion;
	tp_param.flGain = aEaxPreset[preset_id].flGain;
	tp_param.flGainHF = aEaxPreset[preset_id].flGainHF;
	tp_param.flGainLF = aEaxPreset[preset_id].flGainLF;
	tp_param.flDecayTime = aEaxPreset[preset_id].flDecayTime;
	tp_param.flDecayHFRatio = aEaxPreset[preset_id].flDecayHFRatio;
	tp_param.flDecayLFRatio = aEaxPreset[preset_id].flDecayLFRatio;
	tp_param.flReflectionsGain = aEaxPreset[preset_id].flReflectionsGain;
	tp_param.flReflectionsDelay = aEaxPreset[preset_id].flReflectionsDelay;
	tp_param.flReflectionsPan[0] = aEaxPreset[preset_id].flReflectionsPan[0];
	tp_param.flReflectionsPan[1] = aEaxPreset[preset_id].flReflectionsPan[1];
	tp_param.flReflectionsPan[2] = aEaxPreset[preset_id].flReflectionsPan[2];
	tp_param.flLateReverbGain = aEaxPreset[preset_id].flLateReverbGain;
	tp_param.flLateReverbDelay = aEaxPreset[preset_id].flLateReverbDelay;
	tp_param.flLateReverbPan[0] = aEaxPreset[preset_id].flLateReverbPan[0];
	tp_param.flLateReverbPan[1] = aEaxPreset[preset_id].flLateReverbPan[1];
	tp_param.flLateReverbPan[2] = aEaxPreset[preset_id].flLateReverbPan[2];
	tp_param.flEchoTime = aEaxPreset[preset_id].flEchoTime;
	tp_param.flEchoDepth = aEaxPreset[preset_id].flEchoDepth;
	tp_param.flModulationTime = aEaxPreset[preset_id].flModulationTime;
	tp_param.flModulationDepth = aEaxPreset[preset_id].flModulationDepth;
	tp_param.flAirAbsorptionGainHF = aEaxPreset[preset_id].flAirAbsorptionGainHF;
	tp_param.flHFReference = aEaxPreset[preset_id].flHFReference;
	tp_param.flLFReference = aEaxPreset[preset_id].flLFReference;
	tp_param.flRoomRolloffFactor = aEaxPreset[preset_id].flRoomRolloffFactor;
	tp_param.iDecayHFLimit = aEaxPreset[preset_id].iDecayHFLimit;
}

Arp_Reverb::Arp_Reverb()
	:m_channels(ARP_EFFECT_STEREO)
	,m_freq(ARP_EFFECT_44K)
	,m_current_id(0)
{
	m_nTotalSamples = 0;
	m_pfSampleBuffer = NULL;

	// filter clear
	m_oLpFilter.FilterClear();
	m_oHpFilter.FilterClear();

	m_oDelay.Mask = 0;
	m_oDelay.Line = NULL;
	m_nDelayTap[0] = 0;
	m_nDelayTap[1] = 0;

	oDecorrelator.Mask = 0;
	oDecorrelator.Line = NULL;
	m_nDecoTap[0] = 0;
	m_nDecoTap[1] = 0;
	m_nDecoTap[2] = 0;

	m_nOffset = 0;
	m_pfGain = m_oLate.m_PanGain;
	m_current_id = 0;
}

Arp_Reverb::~Arp_Reverb()
{

}

//================================================================================ public mothod
int32_t Arp_Reverb::Init(int32_t channels, int32_t frequency, int32_t preset_id)
{
	m_channels = channels;
	m_freq = frequency;
	Set_PreSet(m_param, preset_id);

	// 初始化延迟线
	AllocLines(frequency);
	// 计算常数
	UpdateConst(frequency);
	// 根据效果ID,更新延迟线
	UpDateLines();

	m_current_id = preset_id;
    m_prevence_id = m_current_id;

#ifdef DOWN_AUDIO
	g_fp = fopen("out_set.wav", "wb");
	fseek(g_fp, 44, SEEK_SET);
#endif
	return ARP_EFFECT_SUCCESS;
}

void Arp_Reverb::Reset()
{
	// Clear the sample buffer.
	for (int index = 0; index < m_nTotalSamples; index++)
	{
		m_pfSampleBuffer[index] = 0.0f;
	}
	m_oLpFilter.FilterClear();
	m_oHpFilter.FilterClear();
	memset(m_flReverbSamples, ARP_EFFECT_BUFFERSIZE * 4 * sizeof(int16_t), 0);
	memset(m_flEarlySamples, ARP_EFFECT_BUFFERSIZE * 4 * sizeof(int16_t), 0);
	memset(m_feed, ARP_EFFECT_BUFFERSIZE * sizeof(int16_t), 0);
	memset(m_taps, ARP_EFFECT_BUFFERSIZE * 4 * sizeof(int16_t), 0);
}

void Arp_Reverb::Uninit()
{
	if (m_pfSampleBuffer != NULL)
	{
		free(m_pfSampleBuffer);
		m_pfSampleBuffer = NULL;
	}

#ifdef DOWN_AUDIO
	if (g_fp)
	{
		fseek(g_fp, 0, SEEK_SET);
#ifdef ARP_MONO
		write_wav_header(g_fp, 44100, 1, 16, g_sumLen*sizeof(short));
#else
		write_wav_header(g_fp, 44100, 2, 16, g_sumLen*sizeof(short));
#endif
		fclose(g_fp);
		g_fp = NULL;
	}
#endif
}

int32_t Arp_Reverb::ResetEffectID( int32_t new_id )
{
	if (m_current_id != new_id)
	{
		//Set_PreSet(m_param, new_id);
		m_current_id = new_id;
		//UpDateLines();
	}
	return ARP_EFFECT_SUCCESS;
}


void Arp_Reverb::PreProcess( short * pdata, int32_t len )
{
	if(ARP_EFFECT_MONO == m_channels)
	{
 		//	memcpy(m_wetbuffer,   pdata, sizeof(short) * len);
		//	memcpy(m_drybuffer[0],pdata, sizeof(short) * len);
		for(int32_t i = 0; i < len; ++i)
		{
			m_drybuffer[0][i] = pdata[i];
			m_wetbuffer[i]	  = pdata[i] >> 2;
		}
	}
	else
	{
		len /= 2;
		for(int32_t i = 0; i < len; ++i)
		{
			m_drybuffer[0][i] = pdata[i*2];
 			m_drybuffer[1][i] = pdata[i*2+1];
			m_wetbuffer[i]	  = (pdata[i*2] + pdata[i*2+1]) >> 3;
		}
	}
}

#define ARP_REVERB_MAX_BLOCK_SIZE 4096


int32_t Arp_Reverb::Process_set( char * pdata, int32_t len/*, float *pdataOut */)
{
    if(m_current_id != m_prevence_id)
    {
        m_prevence_id = m_current_id;
        Set_PreSet(m_param, m_current_id);
        UpDateLines();
    
    }
    if (len > ARP_REVERB_MAX_BLOCK_SIZE)
    {
        int32_t ret = len;
#if 1
        int i = 0;

        while (i+ARP_REVERB_MAX_BLOCK_SIZE<=len)
        {
            int internal_size = Process_set(pdata + i, ARP_REVERB_MAX_BLOCK_SIZE);

            if (internal_size < 0)
            {
                return internal_size;
            }

            i += ARP_REVERB_MAX_BLOCK_SIZE;
        }
        if (i<len)
        {
            len -= i;

            int internal_size = Process_set(pdata + i, len);
            
            if (internal_size < 0)
            {
                return internal_size;
            }        
        }
#else
        Process_set(pdata, len / 2);
        Process_set(pdata + len/2, len / 2);
#endif
        return ret;
    }

	PreProcess((short*)pdata, len/2);

	EAXProcess_set(m_wetbuffer, len/m_channels/2, (short*)pdata);

#ifdef DOWN_AUDIO
	if (NULL == g_fp)
		return -1;
	short * pTemp  = new short[len];
	for (int32_t i = 0; i < len; i++)
	{
		int32_t nValue =  int32_t( pdata[i] * 32768.0f);
		if (nValue >  32767) nValue = 32767;
		if (nValue < -32768) nValue =-32768;
		pTemp[i] = nValue;
	}
	fwrite(pTemp, sizeof(short), len, g_fp);
	g_sumLen += len;
	delete []pTemp;
#endif

	return len;
}


int32_t Arp_Reverb::AllocLines(uint frequency)
{
	uint totalSamples, index;
	float length;
	short *newBuffer = NULL;

	totalSamples = 0;

	length = (AL_EAXREVERB_MAX_MODULATION_TIME*MODULATION_DEPTH_COEFF/2.0f) + (1.0f / frequency);
	totalSamples += CalcLineLength(length, totalSamples, frequency,&m_oMod.m_Delay);

	// The initial delay is the sum of the reflections and late reverb delays.
	length = AL_EAXREVERB_MAX_REFLECTIONS_DELAY + AL_EAXREVERB_MAX_LATE_REVERB_DELAY;
	totalSamples += CalcLineLength(length, totalSamples, frequency,&m_oDelay);

	// The early reflection lines.
	for(index = 0;index < 4;index++)
		totalSamples += CalcLineLength(EARLY_LINE_LENGTH[index], totalSamples, frequency, &m_oEarly.m_Delay[index]);

	// The decorrelator line is calculated from the lowest reverb density (a parameter value of 1).
	length = (DECO_FRACTION * DECO_MULTIPLIER * DECO_MULTIPLIER) * LATE_LINE_LENGTH[0] * (1.0f + LATE_LINE_MULTIPLIER);
	totalSamples += CalcLineLength(length, totalSamples, frequency, &oDecorrelator);

	// The late all-pass lines.
	for(index = 0;index < 4;index++)
		totalSamples += CalcLineLength(ALLPASS_LINE_LENGTH[index], totalSamples,
		frequency, &m_oLate.m_ApDelay[index]);

	// The late delay lines are calculated from the lowest reverb density.
	for(index = 0;index < 4;index++)
	{
		length = LATE_LINE_LENGTH[index] * (1.0f + LATE_LINE_MULTIPLIER);
		totalSamples += CalcLineLength(length, totalSamples, frequency,&m_oLate.m_Delay[index]);
	}

	// The echo all-pass and delay lines.
	totalSamples += CalcLineLength(ECHO_ALLPASS_LENGTH, totalSamples, frequency, &m_oEcho.m_ApDelay);
	totalSamples += CalcLineLength(AL_EAXREVERB_MAX_ECHO_TIME, totalSamples, frequency, &m_oEcho.m_Delay);

	if(totalSamples != m_nTotalSamples)
	{
		newBuffer = (short *)realloc(m_pfSampleBuffer, sizeof(short) * totalSamples);
		if(newBuffer == NULL)
		{
			return ARP_EFFECT_BUFFER_RELLOC;
		}
		m_pfSampleBuffer = newBuffer;
		m_nTotalSamples = totalSamples;
	}

	// Update all delays to reflect the new sample buffer.
	RealizeLineOffset(m_pfSampleBuffer, &m_oDelay);
	RealizeLineOffset(m_pfSampleBuffer, &oDecorrelator);
	for(index = 0;index < 4;index++)
	{
		RealizeLineOffset(m_pfSampleBuffer, &m_oEarly.m_Delay[index]);
		RealizeLineOffset(m_pfSampleBuffer, &m_oLate.m_ApDelay[index]);
		RealizeLineOffset(m_pfSampleBuffer, &m_oLate.m_Delay[index]);
	}
	RealizeLineOffset(m_pfSampleBuffer, &m_oMod.m_Delay);
	RealizeLineOffset(m_pfSampleBuffer, &m_oEcho.m_ApDelay);
	RealizeLineOffset(m_pfSampleBuffer, &m_oEcho.m_Delay);

	// Clear the sample buffer.
	for(index = 0;index < m_nTotalSamples;index++)
	{
		m_pfSampleBuffer[index] = 0.0f;
	}
	return ARP_EFFECT_SUCCESS;
}

int32_t Arp_Reverb::UpdateConst( uint frequency )
{
	//计算调制滤波器参数，这个参数和当前的采样率有关
	m_oMod.m_Coeff = powf(MODULATION_FILTER_COEFF,MODULATION_FILTER_CONST / frequency);

	//早反射和后面的全通滤波器长度在DelayLines上长度固定，故偏移量只计算一次
	for(uint index = 0;index < 4;index++)
	{
		m_oEarly.m_Offset[index] = fastf2u(EARLY_LINE_LENGTH[index] * frequency);
		m_oLate.m_ApOffset[index] = fastf2u(ALLPASS_LINE_LENGTH[index] * frequency);
	}

	//用于模拟回声的全通滤波器同样在DelayLines上长度固定，偏移量只计算一次
	m_oEcho.m_ApOffset = fastf2u(ECHO_ALLPASS_LENGTH * frequency);

	return ARP_EFFECT_SUCCESS;
}

static void UpdateDelayLine(Arp_Reverb *oReverb, float earlyDelay, float lateDelay, uint frequency)
{
	// Calculate the initial delay taps.
	oReverb->m_nDelayTap[0] = fastf2u(earlyDelay * frequency);
	oReverb->m_nDelayTap[1] = fastf2u((earlyDelay + lateDelay) * frequency);
}

// Update the offsets for the decorrelator line.
static void UpdateDecorrelator(Arp_Reverb *oReverb, float density, uint frequency)
{
	uint index;
	float length;
	for(index = 0;index < 3;index++)
	{
		length = (DECO_FRACTION * powf(DECO_MULTIPLIER, (float)index)) * LATE_LINE_LENGTH[0] * (1.0f + (density * LATE_LINE_MULTIPLIER));
		oReverb->m_nDecoTap[index] = fastf2u(length * frequency);
	}
}

int32_t Arp_Reverb::UpDateLines()
{
	uint frequency = m_freq;
	float hfscale, hfRatio;
	float cw, x, y;

	hfscale =  m_param.flHFReference/m_freq;
	m_oLpFilter.SetParam(ARP_FILTER_HighShelf, m_param.flGainHF, hfscale);

	m_oHpFilter.SetParam(ARP_FILTER_LowShelf, m_param.flGainLF, m_param.flLFReference/m_freq);

	m_oMod.UpdateModulator(m_param.flModulationTime, m_param.flModulationDepth, frequency);
	UpdateDelayLine(this, m_param.flReflectionsDelay, m_param.flLateReverbDelay, frequency);
	m_oEarly.UpdateEarlyLines(m_param.flGain, m_param.flReflectionsGain, m_param.flLateReverbDelay);
	UpdateDecorrelator(this, m_param.flDensity, frequency);

	CalcMatrixCoeffs(m_param.flDiffusion, &x, &y);
	m_oLate.m_MixCoeff = QCONST16( y/x, 14);

	hfRatio = m_param.flDecayHFRatio;
	if(m_param.iDecayHFLimit && m_param.flAirAbsorptionGainHF < 1.0f)
	{
		hfRatio = CalcLimitedHfRatio(hfRatio, m_param.flAirAbsorptionGainHF, m_param.flDecayTime);
	}

	cw = cosf(F_2PI * hfscale);
	m_oLate.UpdateLateLines(m_param.flGain, m_param.flLateReverbGain, x, m_param.flDensity, m_param.flDecayTime, m_param.flDiffusion, hfRatio, cw, frequency);

	// Update the echo line.
	m_oEcho.UpdateEchoLine(m_param.flGain, m_param.flLateReverbGain,
		m_param.flEchoTime, m_param.flDecayTime,
		m_param.flDiffusion, m_param.flEchoDepth,
		hfRatio, cw, frequency);

	// Update early and late 3D panning.
	Update3DPanning(&m_oEarly, &m_oLate, m_channels);
	return ARP_EFFECT_SUCCESS;
}

void Arp_Reverb::EAXProcess_set( short * pdata, int32_t len, short * pdataOut )
{
	short (* early)[4] = m_flEarlySamples;
	short (* late)[4]  = m_flReverbSamples;
	int32_t index, nValue = 0;
	EAXVerbPass_set(pdata, len, NULL);

	if (m_channels == 1)
	{
		for (index = 0; index < len; index++)
		{
			nValue = (pdataOut[index]) + (EXTEND32( early[index][2] +  late[index][2] ) << 2);
			if (nValue > 32767)	 nValue =  32767;
			if (nValue < -32768) nValue = -32768;
			pdataOut[index] = nValue;
		}
	}
	else
	{
		int32_t temp = 0;
		for (index = 0; index < len; index++)
		{
			temp = 2*index;
			nValue	= EXTEND32(pdataOut[temp])   + (EXTEND32( early[index][0] + late[index][0]) << 3);
			if (nValue > 32767)	  nValue =  32767;
			if (nValue < -32768)  nValue = -32768;
			pdataOut[temp]	 = nValue;

			nValue	= EXTEND32(pdataOut[temp +1])+ (EXTEND32( early[index][1] + late[index][1]) << 3);
			if (nValue >  32767) nValue =  32767;
			if (nValue < -32768) nValue = -32768;
			pdataOut[temp +1] = nValue;
		}
	}
}

static void DelayLineIn_set(DelayLine * Delay, int16_t * in, int32_t len, uint arp_offset)
{
	uint temp_offset = arp_offset;
	for( int32_t i = 0; i < len; i++)
	{
		DelayLineIn(Delay, temp_offset, in[i]);
		temp_offset++;
	}
}

static void DelayLineOut_set(DelayLine * Delay, int16_t * in, int32_t len, uint arp_offset)
{
	uint temp_offset = arp_offset;
	for (int32_t i = 0; i < len; i++)
	{
		in[i] = DelayLineOut(Delay, temp_offset);
		temp_offset++;
	}
}

//static void FILE_WRITE(char szFileName[256], int len, short (*pdata)[4],  int  nIndex)
//{
//	short *p = new short[len];
//	for (int  i = 0 ;  i < len; i++)
//	{
//		p[i] = pdata[i][nIndex]*4;
//	}
//
//	FILE *fp = fopen(szFileName, "ab+");
//	fwrite(p, 2, len, fp);
//	fclose(fp);
//}

void Arp_Reverb::EAXVerbPass_set( short *in, int32_t len, float * out )
{
	short (* early)[4] = m_flEarlySamples;
	short (* late) [4]  = m_flReverbSamples;

	m_oLpFilter.Process(in, len);
	m_oHpFilter.Process(in, len);

 	m_oMod.EAXModulation_set(in, len, m_nOffset);

	DelayLineIn_set(&m_oDelay, in, len, m_nOffset);

	DelayLineOut_set(&m_oDelay, in, len, m_nOffset - m_nDelayTap[0]);
	m_oEarly.EarlyReflection_set(in, early, len, m_nOffset);

	DelayLineOut_set(&m_oDelay, in, len, m_nOffset - m_nDelayTap[1]);

	uint temp_offset = m_nOffset;
	short *pTemp = NULL;
 	for (int32_t i = 0; i < len; i++)
 	{

		m_feed[i] = MULT16_16_P15(in[i] , m_oLate.m_DensityGain);
		DelayLineIn(&oDecorrelator, temp_offset, m_feed[i]);
		pTemp = m_taps[i];
		pTemp[0] = m_feed[i];
		pTemp[1] = DelayLineOut(&oDecorrelator, temp_offset - m_nDecoTap[0]);
		pTemp[2] = DelayLineOut(&oDecorrelator, temp_offset - m_nDecoTap[1]);
		pTemp[3] = DelayLineOut(&oDecorrelator, temp_offset - m_nDecoTap[2]);
		temp_offset++;
 	}


 	m_oLate.LateReverb_set(m_taps, late, len, m_nOffset);

	m_oEcho.EAXEcho_set(in, late, len, m_nOffset);

 	//FILE_WRITE("late1.pcm", len, late, 0);
 	//FILE_WRITE("late2.pcm", len, late, 1);

 	m_nOffset += len;
}
