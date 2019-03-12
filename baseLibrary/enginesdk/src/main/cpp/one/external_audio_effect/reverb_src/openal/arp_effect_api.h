
#ifndef _ARP_EFFECT_API_H_
#define _ARP_EFFECT_API_H_


#include "arp_effect_error.h"
#include <stdint.h>

enum Effect_ID
{
	EFFECT_GENERRIC	= 0,    // KTV
	EFFECT_KTV	    = 1,    // 卧室
	EFFECT_CONCERT 	= 2,    // 音乐厅
	EFFECT_ARENA 	= 3,    // 舞台
	EFFECT_ICEPALACE= 4,    // 冰宫
	EFFECT_CUPBOARD = 5,    // 厨房

	EFFECT_STONEROOM	= 6,// 石室
	EFFECT_AUDITORIUM	= 7,// 剧院
	EFFECT_CAVE			= 8,// 洞穴
	EFFECT_ALLEY		= 9,// 长廊
	EFFECT_PARKING		=10,// 停车场
	EFFECT_CITY			=11 // 城市

};

// +-------------------------------
//  功能：创建音效实例（全局一次）
//  参数：pInst			：待创建的音效实例
//        vocalChannels	：人声的通道数{1,2}
//        accChannels	：伴奏的通道数{1,2}
//        freq			：音频采样频率，当前只支持44k
//		  preset_id		: 初始音效id,见 enum Effect_ID
//		  vocalRate		: 人声音量放大系数[0~2.0], 1.0为原始大小
//			accRate		: 伴奏音量放大系数[0~2.0], 1.0为原始大小
//  返回值：见错误码
// +-------------------------------
int32_t arp_effect_create(void ** pInst,
						  int32_t vocalChannels,
						  int32_t accChannels,
						  int32_t freq,
						  int32_t prest_id,
						  float	  vocalRate,
						  float	  accRate);


// +-------------------------------
//  功能：重置音效实例（可全局多次）
//  参数：pInst	：待重置的音效实例
//        new_id：重置效果ID
//     vocalRate：重置的人声音量
//       accRate：重置的伴奏音量
//  返回值：见错误码
// +-------------------------------
int32_t arp_effect_reset(void * pInst, int32_t new_id, float vocalRate, float accRate);


// +-------------------------------
//  功能：混音音效处理（全局多次）
//  参数：pInst		：音效实例
//        vocal		：人声buffer
//        accompany	：伴奏buffer
//	   sample_in_num：输入音频单个声道采样点数，同时也是输出音频单个声道采样点数
//     pout         ：输出音频buffer，大小为sample_in_num的2倍，可以为vocal/accompany中较大的一个而不必另开空间
//  返回值：见错误码
// +-------------------------------
int32_t arp_effect_mix(void  * pInst,
					   int16_t *vocal,
					   int16_t *accompany,
					   int32_t sample_in_num,
					   int16_t *pOut
					   );


// +-------------------------------
//  功能：销毁音效实例（全局一次）
//  参数：pInst：待销毁的音效实例
//  返回值：见错误码
// +-------------------------------
int32_t arp_effect_destory(void ** pInst);


#endif /* _ARP_EFFECT_API_H_ */



