package com.wali.live.watchsdk.income;

import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.MibiTicketProto;
import com.wali.live.proto.PayProto;

/**
 * Created by liuyanyan on 16/2/29.
 */
public class WithdrawManager {

    private static final String TAG = "WithdrawManager";

    //private static List<Exchange> mExchangeListCache = new ArrayList<>();
    //
    //public static List<Exchange> getExchangeListCache() {
    //    return mExchangeListCache;
    //}
    //
    //public static void updateExchangeListCache(List<Exchange> list) {
    //    mExchangeListCache.clear();
    //    mExchangeListCache.addAll(list);
    //}

    /*
    * 拉取讯票换钻价格列表
    * */
    public static PayProto.GetExchangeResponse pullExchangeListSync() {
        PayProto.GetExchangeRequest req = PayProto.GetExchangeRequest.newBuilder()
                .setPlatform(PayProto.Platform.ANDROID)
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_GET_EXCHANGE_LIST);
        data.setData(req.toByteArray());
        MyLog.v(TAG, "pullExchangeListSync request:" + req.toString());
        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (null != rspData) {
            try {
                PayProto.GetExchangeResponse rsp = PayProto.GetExchangeResponse.parseFrom(rspData.getData());
                MyLog.v(TAG, "pullExchangeListSync response:" + rsp.toString());
                return rsp;
            } catch (InvalidProtocolBufferException e) {
                MyLog.e(e.toString());
            }
        } else {
            MyLog.e(TAG, "pullExchangeListSync rspData is null");
        }
        return null;
    }

    /*
    * 拉取游戏星票换钻价格列表
    * */
    public static MibiTicketProto.GetGameTicketExchangeResponse pullGameExchangeListSync() {
        MibiTicketProto.GetGameTicketExchangeRequest req = MibiTicketProto.GetGameTicketExchangeRequest.newBuilder()
                .setPlatform(PayProto.Platform.ANDROID)
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_GET_EXCHANGEGAME_LIST);
        data.setData(req.toByteArray());
        MyLog.v(TAG, "pullExchangeListSync request:" + req.toString());
        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (null != rspData) {
            try {
                MibiTicketProto.GetGameTicketExchangeResponse rsp = MibiTicketProto.GetGameTicketExchangeResponse.parseFrom(rspData.getData());
                MyLog.v(TAG, "pullExchangeListSync response:" + rsp.toString());
                return rsp;
            } catch (InvalidProtocolBufferException e) {
                MyLog.e(e.toString());
            }
        } else {
            MyLog.e(TAG, "pullExchangeListSync rspData is null");
        }
        return null;
    }

    /*
    * 查询用户收益
    * */
    public static PayProto.QueryProfitResponse pullUserProfitSync() {
        PayProto.QueryProfitRequest req = PayProto.QueryProfitRequest.newBuilder()
                .setPlatform(PayProto.Platform.ANDROID)
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_QUERY_PROFIT);
        data.setData(req.toByteArray());
        MyLog.v(TAG, "pullUserProfitSync request = " + req.toString());
        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (null != rspData) {
            try {
                PayProto.QueryProfitResponse rsp = PayProto.QueryProfitResponse.parseFrom(rspData.getData());
                MyLog.v(TAG, "pullUserProfitSync response:" + rsp.toString());
                return rsp;
            } catch (InvalidProtocolBufferException e) {
                MyLog.e(e.toString());
            }
        } else {
            MyLog.e(TAG, "pullUserProfitSync rspData is null");
        }
        return null;
    }

    /*
    * 票换钻
    * */
    public static PayProto.ExchangeGemResponse exchangeDiamondSync(int exchangeId, int diamondNum, int ticketNum, int giveDiamondNum) {
        PayProto.ExchangeGemRequest req = PayProto.ExchangeGemRequest.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setPlatform(PayProto.Platform.ANDROID)
                .setClientId(System.currentTimeMillis())
                .setExchangeId(exchangeId)
                .setGemCnt(diamondNum)
                .setTichketCnt(ticketNum)
                .setGiveGemCnt(giveDiamondNum)
                .build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_EXCHANGE);
        data.setData(req.toByteArray());
        MyLog.v(TAG, "exchangeDiamondSync request = " + req.toString());
        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (null != rspData) {
            try {
                PayProto.ExchangeGemResponse rsp = PayProto.ExchangeGemResponse.parseFrom(rspData.getData());
                MyLog.v(TAG, "exchangeDiamondSync response = " + rsp.toString());
                return rsp;
            } catch (InvalidProtocolBufferException e) {
                MyLog.e(TAG, e.toString());
            }
        } else {
            MyLog.e(TAG, "exchangeDiamondSync rspData is null");
        }
        return null;
    }

    /*
   * 票换钻
   * */
    public static MibiTicketProto.GameTicketExchangeGemResponse exchangeGameDiamondSync(int exchangeId, int diamondNum, int ticketNum, int giveDiamondNum) {
        MibiTicketProto.GameTicketExchangeGemRequest req = MibiTicketProto.GameTicketExchangeGemRequest.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setPlatform(PayProto.Platform.ANDROID)
                .setClientId(System.currentTimeMillis())
                .setExchangeId(exchangeId)
                .setGemCnt(diamondNum)
                .setTichketCnt(ticketNum)
                .setGiveGemCnt(giveDiamondNum)
                .build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_EXCHANGEGAME);
        data.setData(req.toByteArray());
        MyLog.v(TAG, "exchangeDiamondSync request = " + req.toString());
        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (null != rspData) {
            try {
                MibiTicketProto.GameTicketExchangeGemResponse rsp = MibiTicketProto.GameTicketExchangeGemResponse.parseFrom(rspData.getData());
                MyLog.v(TAG, "exchangeDiamondSync response = " + rsp.toString());
                return rsp;
            } catch (InvalidProtocolBufferException e) {
                MyLog.e(TAG, e.toString());
            }
        } else {
            MyLog.e(TAG, "exchangeDiamondSync rspData is null");
        }
        return null;
    }

    /*
    * 查看提现记录
    * */
    public static PayProto.WithdrawRecordResponse withdrawRecordsSync(String itemId, int limit) {
        if (null == itemId) {
            itemId = "";
        }
        PayProto.WithdrawRecordRequest req = PayProto.WithdrawRecordRequest.newBuilder()
                .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                .setItemId(itemId)
                .setLimit(limit)
                .build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_WITHDRAW_RECORD);
        data.setData(req.toByteArray());
        MyLog.v(TAG, "withdrawRecordsSync request = " + req.toString());
        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (null != rspData) {
            try {
                PayProto.WithdrawRecordResponse rsp = PayProto.WithdrawRecordResponse.parseFrom(rspData.getData());
                MyLog.v(TAG, "withdrawRecordsSync response = " + rsp.toString());
                return rsp;
            } catch (InvalidProtocolBufferException e) {
                MyLog.e(TAG, e.toString());
            }
        } else {
            MyLog.e(TAG, "withdrawRecordsSync rspData is null");
        }
        return null;
    }

}
