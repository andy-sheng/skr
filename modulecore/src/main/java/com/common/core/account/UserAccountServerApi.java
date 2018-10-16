package com.common.core.account;

import android.text.TextUtils;

import com.common.log.MyLog;
import com.common.milink.MiLinkClientAdapter;
import com.common.milink.command.MiLinkCommand;
import com.common.milink.constant.MiLinkConstant;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.Account.LoginReq;
import com.wali.live.proto.Account.LoginRsp;
import com.wali.live.proto.Account.MiSsoLoginReq;
import com.wali.live.proto.Account.MiSsoLoginRsp;

import io.reactivex.Observable;

/**
 * Created by chengsimin on 16/7/1.
 */
public class UserAccountServerApi {
    public final static String TAG = "UserAccountServerApi";


    /**
     * 小米帐号sso登录
     */
    public static MiSsoLoginRsp loginByMiSso(final long miid, final String miservicetoken, final int channelId) {

        MiSsoLoginReq.Builder builder = new MiSsoLoginReq.Builder();
        builder.setAccountType(4);
        builder.setMid(miid);
        builder.setMiservicetoken(miservicetoken);
        MiSsoLoginReq req = builder.build();

        PacketData data = new PacketData();
        data.setCommand("zhibo.account.missologin");
        data.setData(req.toByteArray());
        data.setChannelId(String.valueOf(channelId));

        MyLog.w(TAG, "missologin request : \n" + req.toString());
        PacketData rspData = MiLinkClientAdapter.getInstance().sendDataByChannel(data, MiLinkConstant.TIME_OUT);
        MyLog.w(TAG, "missologin rspData =" + rspData);
        if (rspData != null) {
            try {
                MiSsoLoginRsp rsp = MiSsoLoginRsp.parseFrom(rspData.getData());
                MyLog.w(TAG, "missologin response : \n" + rsp.toString());
                return rsp;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 小米帐号sso登录
     */
    public static LoginRsp loginByThirdPartyOauthloginReq(int accountType, String code, String openId, String accessToken, String expires_in, String refreshToken, String channelId) {
        LoginReq.Builder builder = new LoginReq.Builder();
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


    private static LoginRsp loginRspFromServer(LoginReq.Builder builder, String channelId) {
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_LOGIN);
        data.setData(builder.build().toByteArray());
        data.setChannelId(channelId);

        MyLog.w(TAG, "loginRspFromServer request : \n" + builder.build().toString());
        long start = System.currentTimeMillis();
        PacketData rspData = MiLinkClientAdapter.getInstance().sendDataByChannel(data, MiLinkConstant.TIME_OUT);
        MyLog.w(TAG, "start=" + start + "end=" + System.currentTimeMillis() + "serverTime = end - start =" + (System.currentTimeMillis() - start));
        MyLog.w(TAG, "loginRspFromServer rspData =" + rspData);
        if (rspData != null) {
            try {
                LoginRsp rsp = LoginRsp.parseFrom(rspData.getData());
                MyLog.w(TAG, "loginRspFromServer response : \n" + rsp.toString());

                return rsp;
            } catch (Exception e) {
                MyLog.e(e);
            }
        }
        return null;
    }
}
