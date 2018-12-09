

#ifndef _ARP_PLATFORM_H_
#define _ARP_PLATFORM_H_


#include <cstdint>
#include "arp_config.h"

/* 定义数据类型 */
#ifdef ARP_PLATFORM_32
typedef		char		arp_int8;
typedef		short		arp_int16;
typedef		int			arp_int32;
#elif defined ARP_PLATFORM_64
typedef		char		arp_int8;
typedef		short		arp_int16;
typedef		int			arp_int32;
#else
#error Please define ARP_PLATFORM_32 or ARP_PLATFORM_64 guide by jiangyang
#endif


#ifdef ARP_FIXED_POINT

typedef		int8_t		arp_int8_t;
typedef		int16_t		arp_int16_t;
typedef		int32_t		arp_int32_t;
typedef		int16_t		arp_word16_t;

#else 

typedef		int8_t		arp_int8_t;
typedef		int16_t		arp_int16_t;
typedef		int32_t		arp_int32_t;
typedef		float		arp_word16_t;

#endif


#endif /* _ARP_PLATFORM_H_ */