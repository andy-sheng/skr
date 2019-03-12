
#ifndef _ARP_EFFECT_ERROR_H_
#define _ARP_EFFECT_ERROR_H_

enum Common_Error{
	ARP_EFFECT_SUCCESS						=	0,      // 通用成功

	ARP_EFFECT_INST_INITED					= 11000,    // 实例已初始化
	ARP_EFFECT_INST_UNINIT					,           // 实例未初始化

	ARP_EFFECT_NOT_SUPPORT_CHANNEL			,           // 通道数错误
	ARP_EFFECT_NOT_SUPPORT_FREQUENCY		,           // 采样率不支持
	ARP_EFFECT_NOT_SUPPORT_EFFECT_ID		,           // 效果ID错误
	ARP_EFFECT_NOT_SUPPORT_RATE				,           // 音量缩放比例错误
	ARP_EFFECT_BUFFER_RELLOC				,           // 内存分配失败
	ARP_EFFECT_LEN_NEGATIVE					,           // 输入采样点数<0
	ARP_EFFECT_POINT_NULL					,           // 传入指针为空


};


#endif /* _ARP_EFFECT_ERROR_H_ */
