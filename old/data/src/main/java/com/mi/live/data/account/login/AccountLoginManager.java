package com.mi.live.data.account.login;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.AccountProto;

/**
 * Created by lan on 16/11/23.
 *
 * @module 账号
 * @description 账号登录
 */
public class AccountLoginManager {
    private static final String TAG = AccountLoginManager.class.getSimpleName();

    /**
     * 小米帐号sso登录
     */
    public static AccountProto.MiSsoLoginRsp miSsoLogin(long mid, String miservicetoken, int channelId) {
        AccountProto.MiSsoLoginReq.Builder builder = AccountProto.MiSsoLoginReq.newBuilder();
        builder.setAccountType(AccountType.xiaomi);
        builder.setMid(mid);
        builder.setMiservicetoken(miservicetoken);
        AccountProto.MiSsoLoginReq req = builder.build();

        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_ACCOUNT_XIAOMI_SSO_LOGIN);
        data.setData(req.toByteArray());
        data.setChannelId(String.valueOf(channelId));

        MyLog.w(TAG, "missologin request : \n" + req.toString());
        PacketData rspData = MiLinkClientAdapter.getsInstance().sendDataByChannel(data, MiLinkConstant.TIME_OUT);
        MyLog.w(TAG, "missologin rspData =" + rspData);
        if (rspData != null) {
            try {
                AccountProto.MiSsoLoginRsp rsp = AccountProto.MiSsoLoginRsp.parseFrom(rspData.getData());
                MyLog.w(TAG, "missologin response : \n" + rsp.toString());
                return rsp;
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * passtoken换servicetoken
     * cmd:zhibo.account.getservicetoken
     */
    public static AccountProto.GetServiceTokenRsp getServiceTokenReq(String passToken, String uuid) {
        if (!TextUtils.isEmpty(uuid) && !TextUtils.isEmpty(passToken)) {
            AccountProto.GetServiceTokenReq.Builder builder = AccountProto.GetServiceTokenReq.newBuilder();
            try {
                long luid = Long.valueOf(uuid);
                builder.setUuid(luid)
                        .setPassToken(passToken);
                return getServiceTokenRspFromServer(builder);
            } catch (NumberFormatException e) {
                MyLog.e(e);
            }
        }
        return null;
    }

    private static AccountProto.GetServiceTokenRsp getServiceTokenRspFromServer(AccountProto.GetServiceTokenReq.Builder builder) {
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_GET_SERVICE_TOKEN);
        data.setData(builder.build().toByteArray());
        MyLog.w(TAG, "getServiceTokenRspFromServer request : \n" + builder.build().toString());

        PacketData rspData = MiLinkClientAdapter.getsInstance().sendDataByChannel(data, MiLinkConstant.TIME_OUT);
        MyLog.w(TAG, "loginRspFromServer rspData =" + rspData);
        if (rspData != null) {
            try {
                AccountProto.GetServiceTokenRsp rsp = AccountProto.GetServiceTokenRsp.parseFrom(rspData.getData());
                MyLog.w(TAG, "getServiceTokenRspFromServer response : \n" + rsp.toString());

                return rsp;
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * `
     * 第三方授权登录
     * cmd:zhibo.account.login
     */
    public static AccountProto.LoginRsp loginReq(int accountType, String code, String openId, String accessToken, String expires_in, String refreshToken, String channelId) {
        AccountProto.LoginReq.Builder builder = AccountProto.LoginReq.newBuilder();
        builder.setAccountType(accountType);

        if (!TextUtils.isEmpty(code)) {
            builder.setCode(code);
        }
        if (!TextUtils.isEmpty(openId)) {
            builder.setOpenid(openId);
        }
        if (!TextUtils.isEmpty(accessToken)) {
            builder.setAccessToken(accessToken);
        }
        if (!TextUtils.isEmpty(expires_in)) {
            builder.setExpiresIn(Integer.parseInt(expires_in));
        }
        if (!TextUtils.isEmpty(refreshToken)) {
            builder.setRefreshToken(refreshToken);
        }
        return loginRspFromServer(builder, channelId);
    }

    private static AccountProto.LoginRsp loginRspFromServer(AccountProto.LoginReq.Builder builder, String channelId) {
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_LOGIN);
        data.setData(builder.build().toByteArray());
        data.setChannelId(channelId);

        MyLog.w(TAG, "loginRspFromServer request : \n" + builder.build().toString());
        long start = System.currentTimeMillis();
        PacketData rspData = MiLinkClientAdapter.getsInstance().sendDataByChannel(data, MiLinkConstant.TIME_OUT);
        MyLog.w(TAG, "start=" + start + "end=" + System.currentTimeMillis() + "serverTime = end - start =" + (System.currentTimeMillis() - start));
        MyLog.w(TAG, "loginRspFromServer rspData =" + rspData);
        if (rspData != null) {
            try {
                AccountProto.LoginRsp rsp = AccountProto.LoginRsp.parseFrom(rspData.getData());
                MyLog.w(TAG, "loginRspFromServer response : \n" + rsp.toString());
                return rsp;
            } catch (InvalidProtocolBufferException e) {
                MyLog.e(e);
            }
        }
        return null;
    }

    /**
     * 对接第三方账号签名登陆，比如对接真真海淘，直播客户端用户进入直播房间要打通用户对输入参数进行签名
     *
     * @param channelId
     * @param xuid
     * @param sex
     * @param nickName
     * @param headUrl
     * @param sign
     * @return
     */
    public static AccountProto.ThirdPartSignLoginRsp thridPartLogin(int channelId, String xuid, int sex, String nickName, String headUrl, String sign) {
        AccountProto.ThirdPartSignLoginReq.Builder builder = AccountProto.ThirdPartSignLoginReq.newBuilder();
        builder.setNickname(nickName);
        builder.setChannelId(String.valueOf(channelId));
        builder.setXuid(xuid);
        builder.setHeadUrl(headUrl);
        builder.setSex(sex);
        builder.setSign(sign);

        AccountProto.ThirdPartSignLoginReq req = builder.build();
        MyLog.w(TAG, "thridPartLogin request : \n" + req.toString());
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_ACCOUNT_3PARTSIGNLOGIN);
        data.setData(builder.build().toByteArray());
        data.setChannelId(String.valueOf(channelId));

        long start = System.currentTimeMillis();
        PacketData rspData = MiLinkClientAdapter.getsInstance().sendDataByChannel(data, MiLinkConstant.TIME_OUT);
        MyLog.w(TAG, "start=" + start + "end=" + System.currentTimeMillis() + "serverTime = end - start =" + (System.currentTimeMillis() - start));
        MyLog.w(TAG, "thridPartLogin rspData =" + rspData);
        if (rspData != null) {
            try {
                AccountProto.ThirdPartSignLoginRsp rsp = AccountProto.ThirdPartSignLoginRsp.parseFrom(rspData.getData());
                MyLog.w(TAG, "thridPartLogin response : \n" + rsp.toString());
                return rsp;
            } catch (InvalidProtocolBufferException e) {
                MyLog.e(e);
            }
        }
        return null;
    }
}
