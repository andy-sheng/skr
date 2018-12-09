#ifndef _ARP_FIXED_GENERIC_H__
#define _ARP_FIXED_GENERIC_H__

#include <stdint.h>

#define QCONST16(x,bits) ((int16_t)(.5+(x)*(((int32_t)1)<<(bits))))
#define QCONST32(x,bits) ((int32_t)(.5+(x)*(((int32_t)1)<<(bits))))

#define NEG16(x) (-(x))
#define NEG32(x) (-(x))
#define EXTRACT16(x) ((int16_t)(x))
#define EXTEND32(x) ((int32_t)(x))
#define SHR16(a,shift) ((a) >> (shift))
#define SHL16(a,shift) ((a) << (shift))
#define SHR32(a,shift) ((a) >> (shift))
#define SHL32(a,shift) ((a) << (shift))
#define PSHR16(a,shift) (SHR16((a)+((1<<((shift))>>1)),shift))
#define PSHR32(a,shift) (SHR32((a)+((EXTEND32(1)<<((shift))>>1)),shift))
#define VSHR32(a, shift) (((shift)>0) ? SHR32(a, shift) : SHL32(a, -(shift)))
#define SATURATE16(x,a) (((x)>(a) ? (a) : (x)<-(a) ? -(a) : (x)))
#define SATURATE32(x,a) (((x)>(a) ? (a) : (x)<-(a) ? -(a) : (x)))

#define SHR(a,shift) ((a) >> (shift))
#define SHL(a,shift) ((int32_t)(a) << (shift))
#define PSHR(a,shift) (SHR((a)+((EXTEND32(1)<<((shift))>>1)),shift))
#define SATURATE(x,a) (((x)>(a) ? (a) : (x)<-(a) ? -(a) : (x)))


#define ADD16(a,b) ((int16_t)((int16_t)(a)+(int16_t)(b)))
#define SUB16(a,b) ((int16_t)(a)-(int16_t)(b))
#define ADD32(a,b) ((int32_t)(a)+(int32_t)(b))
#define SUB32(a,b) ((int32_t)(a)-(int32_t)(b))


/* result fits in 16 bits */
#define MULT16_16_16(a,b)     ((((int16_t)(a))*((int16_t)(b))))

/* (int32_t)(int16_t) gives TI compiler a hint that it's 16x16->32 multiply */
#define MULT16_16(a,b)     (((int32_t)(a))*((int32_t)(b)))

#define MAC16_16(c,a,b) (ADD32((c),MULT16_16((a),(b))))
#define MULT16_32_Q12(a,b) ADD32(MULT16_16((a),SHR((b),12)), SHR(MULT16_16((a),((b)&0x00000fff)),12))
#define MULT16_32_Q13(a,b) ADD32(MULT16_16((a),SHR((b),13)), SHR(MULT16_16((a),((b)&0x00001fff)),13))
#define MULT16_32_Q14(a,b) ADD32(MULT16_16((a),SHR((b),14)), SHR(MULT16_16((a),((b)&0x00003fff)),14))

#define MULT16_32_Q11(a,b) ADD32(MULT16_16((a),SHR((b),11)), SHR(MULT16_16((a),((b)&0x000007ff)),11))
#define MAC16_32_Q11(c,a,b) ADD32(c,ADD32(MULT16_16((a),SHR((b),11)), SHR(MULT16_16((a),((b)&0x000007ff)),11)))

#define MULT16_32_P15(a,b) ADD32(MULT16_16((a),SHR((b),15)), PSHR(MULT16_16((a),((b)&0x00007fff)),15))
#define MULT16_32_Q15(a,b) ADD32(MULT16_16((a),SHR((b),15)), SHR(MULT16_16((a),((b)&0x00007fff)),15))
#define MAC16_32_Q15(c,a,b) ADD32(c,ADD32(MULT16_16((a),SHR((b),15)), SHR(MULT16_16((a),((b)&0x00007fff)),15)))


#define MAC16_16_Q11(c,a,b)     (ADD32((c),SHR(MULT16_16((a),(b)),11)))
#define MAC16_16_Q13(c,a,b)     (ADD32((c),SHR(MULT16_16((a),(b)),13)))
#define MAC16_16_P13(c,a,b)     (ADD32((c),SHR(ADD32(4096,MULT16_16((a),(b))),13)))

#define MULT16_16_Q11_32(a,b) (SHR(MULT16_16((a),(b)),11))
#define MULT16_16_Q13(a,b) (SHR(MULT16_16((a),(b)),13))
#define MULT16_16_Q14(a,b) (SHR(MULT16_16((a),(b)),14))
#define MULT16_16_Q15(a,b) (SHR(MULT16_16((a),(b)),15))

#define MULT16_16_P13(a,b) (SHR(ADD32(4096,MULT16_16((a),(b))),13))
#define MULT16_16_P14(a,b) (SHR(ADD32(8192,MULT16_16((a),(b))),14))
#define MULT16_16_P15(a,b) (SHR(ADD32(0,MULT16_16((a),(b))),15))

#define MUL_16_32_R15(a,bh,bl) ADD32(MULT16_16((a),(bh)), SHR(MULT16_16((a),(bl)),15))

#define DIV32_16(a,b) ((int16_t)(((int32_t)(a))/((int16_t)(b))))
#define PDIV32_16(a,b) ((int16_t)(((int32_t)(a)+((int16_t)(b)>>1))/((int16_t)(b))))
#define DIV32(a,b) (((int32_t)(a))/((int32_t)(b)))
#define PDIV32(a,b) (((int32_t)(a)+((int16_t)(b)>>1))/((int32_t)(b)))


//static int  g_nCosTable[257] =
//{
//	32767,	32766,	32758,	32746,	32729,	32706,	32679,	32647,
//	32610,	32568,	32522,	32470,	32413,	32352,	32286,	32214,
//	32138,	32058,	31972,	31881,	31786,	31686,	31581,	31471,
//	31357,	31238,	31114,	30986,	30853,	30715,	30572,	30425,
//	30274,	30118,	29957,	29792,	29622,	29448,	29269,	29086,
//	28899,	28707,	28511,	28311,	28106,	27897,	27684,	27467,
//	27246,	27020,	26791,	26557,	26320,	26078,	25833,	25583,
//	25330,	25073,	24812,	24548,	24279,	24008,	23732,	23453,
//	23170,	22884,	22595,	22302,	22006,	21706,	21403,	21097,
//	20788,	20475,	20160,	19841,	19520,	19195,	18868,	18538,
//	18205,	17869,	17531,	17190,	16846,	16500,	16151,	15800,
//	15447,	15091,	14733,	14373,	14010,	13646,	13279,	12910,
//	12540,	12167,	11793,	11417,	11039,	10660,	10279,	9896,
//	9512,	9127,	8740,	8351,	7962,	7571,	7180,	6787,
//	6393,	5998,	5602,	5205,	4808,	4410,	4011,	3612,
//	3212,	2811,	2411,	2009,	1608,	1206,	804,	402,
//	0,		-402,	-804,	-1206,	-1608,	-2009,	-2411,	-2811,
//	-3212,	-3612,	-4011,	-4410,	-4808,	-5205,	-5602,	-5998,
//	-6393,	-6787,	-7180,	-7571,	-7962,	-8351,	-8740,	-9127,
//	-9512,	-9896,	-10279,	-10660,	-11039,	-11417,	-11793,	-12167,
//	-12540,	-12910,	-13279,	-13646,	-14010,	-14373,	-14733,	-15091,
//	-15447,	-15800,	-16151,	-16500,	-16846,	-17190,	-17531,	-17869,
//	-18205,	-18538,	-18868,	-19195,	-19520,	-19841,	-20160,	-20475,
//	-20788,	-21097,	-21403,	-21706,	-22006,	-22302,	-22595,	-22884,
//	-23170,	-23453,	-23732,	-24008,	-24279,	-24548,	-24812,	-25073,
//	-25330,	-25583,	-25833,	-26078,	-26320,	-26557,	-26791,	-27020,
//	-27246,	-27467,	-27684,	-27897,	-28106,	-28311,	-28511,	-28707,
//	-28899,	-29086,	-29269,	-29448,	-29622,	-29792,	-29957,	-30118,
//	-30274,	-30425,	-30572,	-30715,	-30853,	-30986,	-31114,	-31238,
//	-31357,	-31471,	-31581,	-31686,	-31786,	-31881,	-31972,	-32058,
//	-32138,	-32214,	-32286,	-32352,	-32413,	-32470,	-32522,	-32568,
//	-32610,	-32647,	-32679,	-32706,	-32729,	-32746,	-32758,	-32766,
//	-32768
//};
//
///* 查表并线形插值求cos */
//static int F_cos(int16_t i)
//{
//	int16_t t1, t2;
//	int32_t lt;
//	t1 = (int16_t)((i >> 7) & 0x01FF);
//	t2 = (int16_t)(i & 0x007F);
//	if ( t1 & 0x100 )
//	{
//		t1 = 0x1FF - t1;
//		t2 = 0x80 - t2;
//	}
//	lt = (0x80L - t2) * g_nCosTable[t1];
//	lt += (int32_t)t2 * g_nCosTable[t1 + 1];
//	return (int16_t)(lt >> 7);
//}
//
//#define div_s(var1,var2,shift)	(((var1)>(var2)) ? 32768:(int32_t)(((int32_t (var1))<<(shift))/(var2)))
//
//inline int16_t F_cos_Q17( int32_t var )
//{
///* 将32768-65536归一化到 0 - 32768 */
//	if (var & 0x7FFF8000 )
//		return F_cos( 0x10000 - var);
//	else
//		return F_cos(var);
//}
//
//inline int32_t F_lerp_Q16(int16_t val1, int16_t val2, int16_t mu)
//{
//	return QCONST16(val1,14) + SHR ((QCONST16(val2,14) - QCONST16(val1,14)) * QCONST16(mu,14), 14);
//}


#endif//_ARP_FIXED_GENERIC_H__


