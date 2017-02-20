package com.wali.live.common.gift.exception;

/**
 * Created by chengsimin on 16/3/11.
 */
public class GiftErrorCode {
    public static final int SUCC = 0;// 成功
    public static final int FAIL = 6501;// 未知失败
    public static final int PARAM_ERROR = 6502;// 参数错误
    public static final int SERVER_ERROR = 6503;// 服务器错误
    public static final int RPC_ERROR = 6504;// rpc调用失败
    public static final int DB_ERROR = 6505;// 数据库操作失败
    public static final int PB_PARSER_FAIL = 6506;// pb转换失败
    public static final int RPC_PAYMENT_FAIL = 6507;// 调用支付失败
    public static final int BUY_GIFT_FOR_YOURSELF = 6508;// 不能自己给自己送礼物
    public static final int CAN_NOT_BUY_GIFT = 6509;// 此时不允许购买礼物
    // 礼物、订单相关
    public static final int GIFT_NOT_EXIST = 6601;// 礼物已经下架，不存在了
    public static final int GIFT_IS_EMPTY = 6602;// 该平台尚无礼物

    public static final int UNKNOW_ERROR = 11000;// 红包未支付成功
    public static final int REQUEST_PARAM_ERROR = 11071; //参数错误
    public static final int REDENVELOP_NOT_AVAILABLE = 16003;// 红包未开放
    public static final int REDENVELOP_EXPIRED = 11151;// 红包已经过期失效
    public static final int REDENVELOP_GAME_OVER = 11152;// 红包已经抢完
    public static final int REDENVELOP_GAME_BUSY = 11153; // 红包繁忙、再试
    public static final int REDENVELOP_HAS_DONE = 11155; // 红包已经抢过

    public static final int GIFT_CARD_INSUFFICIENT = 11013; // 礼物卡数量不足
    public static final int GIFT_INSUFFICIENT_BALANCE = 11014;//余额不足

    // 米币购买礼物相关 BuyGiftRsp、GetMibiBalanceResponse用
    public static final int CODE_RISK_CONTROL = 11073;// 风控
    public static final int CODE_NOT_MIBI_USER = 11352;// 非米币用户
    public static final int CODE_MIBI_INSUFFICIENT = 11353;// 米币余额不足
    public static final int CODE_MIBI_ACCOUNT_FROZEN = 11354;// 米币账号冻结
    public static final int CODE_MIBI_CONSUME_TIMEOUT = 11355;// 米币消费超时
    public static final int CODE_MIBI_CONSUME_ERROR = 11356;// 米币消费其他异常

    public static final int CODE_LOW_LEVEL_ERROR = 11376;// 用户级别低不满足购买特权礼物
}
