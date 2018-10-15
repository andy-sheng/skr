package com.wali.live.watchsdk.income;

import android.os.AsyncTask;

import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.event.EventClass;
import com.wali.live.proto.PayProto;
import com.wali.live.task.IActionCallBack;
import com.wali.live.utils.AsyncTaskUtils;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;

/**
 * Created by qianyuan on 16/3/3.
 */
public class FillAccountInfoTask {

    private final String TAG = "FillAccountInfoTask";

    public FillAccountInfoTask() {
    }

    public void commitWithDrawAccountInfo(final WithdrawCallBack callBack, final String realName, final String accountAliPay, final String accountCid) {
        AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Integer doInBackground(Void... params) {
                PayProto.BindRequest req = PayProto.BindRequest.newBuilder()
                        .setRealName(realName)
                        .setAccount(accountAliPay)
                        .setCardId(accountCid)
                        .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                        .build();
                PacketData data = new PacketData();
                data.setCommand(MiLinkCommand.COMMAND_COMMIT_PAY_INFO);
                data.setData(req.toByteArray());
                MyLog.w(TAG, "FillAccountAccount request:" + req.toString());
                PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
                if (null != rspData) {
                    try {
                        int retCode = ErrorCode.CODE_ERROR_NORMAL;
                        PayProto.BindResponse rsp = PayProto.BindResponse.parseFrom(rspData.getData());
                        MyLog.w(TAG, "FillAccountAccount response:" + rsp.toString());
                        retCode = rsp.getRetCode();
                        return retCode;
                    } catch (InvalidProtocolBufferException e) {
                        MyLog.e(e.toString());
                    }
                } else {
                    MyLog.e(TAG, "commitWithDrawAccountInfo rsp is null");
                }
                return ErrorCode.CODE_ERROR_NORMAL;
            }

            @Override
            protected void onPostExecute(Integer result) {
                if (result == ErrorCode.CODE_SUCCESS) {
                    callBack.commitSuccess();
                } else {
                    callBack.commitError(result);
                }
            }
        };

        AsyncTaskUtils.exe(task);
    }

    public void commitWithWxCode(WeakReference<IActionCallBack> callBack, String oauthCode, PayProto.WithdrawType type) {
        commitBindInfo(callBack, MiLinkCommand.COMMAND_COMMIT_PAY_INFO, oauthCode, type, "", "", "");
    }

    private void commitBindInfo(final WeakReference<IActionCallBack> callBack, final String milinkCommand, final String oauthCode, final PayProto.WithdrawType Type, final String realName, final String account, final String cardId) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            private int errCode = ErrorCode.CODE_ERROR_NORMAL;
            private String openId;
            private int bindType;
            private UserProfit.AliPayAccount aliPayAccount;
            private UserProfit.WeixinPayAccount wxPayAccount;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                PayProto.BindRequest req = PayProto.BindRequest.newBuilder()
                        .setOauthCode(oauthCode)
                        .setType(Type)
                        .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                        .setRealName(realName)
                        .setAccount(account)
                        .setCardId(cardId)
                        .build();
                PacketData data = new PacketData();
                data.setCommand(milinkCommand);
                data.setData(req.toByteArray());
                MyLog.w(TAG, "commitAuthenticationInfo request:" + req.toString());
                PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
                if (null != rspData) {
                    try {
                        PayProto.BindResponse rsp = PayProto.BindResponse.parseFrom(rspData.getData());
                        errCode = rsp.getRetCode();
                        MyLog.w(TAG, "commitAuthenticationInfo errorCode = " + errCode);
                        if (errCode == ErrorCode.CODE_SUCCESS) {
                            openId = rsp.getOpenid();
                            if (rsp.getAlipay() != null) {
                                aliPayAccount = new UserProfit.AliPayAccount(rsp.getAlipay());
                            }
                            if (rsp.getWxpay() != null) {
                                wxPayAccount = new UserProfit.WeixinPayAccount(rsp.getWxpay());
                            }
                            EventBus.getDefault().post(new EventClass.WithdrawEvent(EventClass.WithdrawEvent.EVENT_TYPE_ACCOUNT_BIND_CHANGE));
                            MyLog.w(TAG, "commitAuthenticationInfo response:" + rsp.toString());
                            return true;
                        }
                    } catch (InvalidProtocolBufferException e) {
                        MyLog.e(TAG, e.toString());
                    }
                } else {
                    MyLog.e(TAG, "commitAuthenticationInfo rsp is null");
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (callBack.get() != null) {
                    if (result) {
                        callBack.get().processAction("", errCode, openId, bindType, wxPayAccount, aliPayAccount);
                    } else {
                        callBack.get().processAction("", errCode);
                    }
                }
            }
        };
        AsyncTaskUtils.exe(task);
    }


    public void commitBindAndWithdrawInfo(final WeakReference<IActionCallBack> callBack, final String oauthCode, final PayProto.WithdrawType type, final String realName, final String account, final String cardId) {
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            private int errCode = ErrorCode.CODE_ERROR_NORMAL;
            private String openId;
            private int bindType;
            private UserProfit.AliPayAccount aliPayAccount;
            private UserProfit.WeixinPayAccount wxPayAccount;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                PayProto.AuthAndWithdrawRequest req = PayProto.AuthAndWithdrawRequest.newBuilder()
                        .setOauthCode(oauthCode)
                        .setType(type)
                        .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                        .setRealName(realName)
                        .setAccount(account)
                        .setCardId(cardId)
                        .setPlatform(PayProto.Platform.ANDROID)
                        .build();
                PacketData data = new PacketData();
                data.setCommand(MiLinkCommand.COMMAND_BANK_AUTH_AND_WITHDRAW);
                data.setData(req.toByteArray());
                MyLog.w(TAG, "commitBindAndWithdrawInfo request:" + req.toString());
                PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
                if (null != rspData) {
                    try {
                        PayProto.BindResponse rsp = PayProto.BindResponse.parseFrom(rspData.getData());
                        errCode = rsp.getRetCode();
                        MyLog.w(TAG, "commitBindAndWithdrawInfo errorCode = " + errCode);
                        if (errCode == ErrorCode.CODE_SUCCESS) {
                            openId = rsp.getOpenid();
                            if (rsp.getAlipay() != null) {
                                aliPayAccount = new UserProfit.AliPayAccount(rsp.getAlipay());
                            }
                            if (rsp.getWxpay() != null) {
                                wxPayAccount = new UserProfit.WeixinPayAccount(rsp.getWxpay());
                            }
                            EventBus.getDefault().post(new EventClass.WithdrawEvent(EventClass.WithdrawEvent.EVENT_TYPE_ACCOUNT_BIND_CHANGE));
                            MyLog.w(TAG, "commitBindAndWithdrawInfo response:" + rsp.toString());
                            return true;
                        }
                    } catch (InvalidProtocolBufferException e) {
                        MyLog.e(TAG, e.toString());
                    }
                } else {
                    MyLog.e(TAG, "commitBindAndWithdrawInfo rsp is null");
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (callBack.get() != null) {
                    if (result) {
                        callBack.get().processAction("", errCode, openId, bindType, wxPayAccount, aliPayAccount);
                    } else {
                        callBack.get().processAction("", errCode);
                    }
                }
            }
        };
        AsyncTaskUtils.exe(task);
    }

    /**
     * 绑定PayPal帐号
     *
     * @param callBack
     * @param account
     * @param firstName
     * @param lastName
     */
    public void bindPayPalAccount(final WeakReference<IActionCallBack> callBack, final String account, final String firstName, final String lastName) {
        AsyncTask<Void, Void, Integer> task = new AsyncTask<Void, Void, Integer>() {

            private UserProfit.PaypalPay paypalPay;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Integer doInBackground(Void... params) {
                PayProto.BindRequest req = PayProto.BindRequest.newBuilder()
                        .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                        .setType(PayProto.WithdrawType.PAYPAL_WITHDRAW)
                        .setPaypalAccount(account)
                        .setPaypalFirstname(firstName)
                        .setPaypalLastname(lastName)
                        .build();
                PacketData data = new PacketData();
                data.setCommand(MiLinkCommand.COMMAND_COMMIT_PAY_INFO);
                data.setData(req.toByteArray());
                MyLog.w(TAG, "FillPayPalAccount request:" + req.toString());
                PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
                if (null != rspData) {
                    try {
                        int retCode = ErrorCode.CODE_ERROR_NORMAL;
                        PayProto.BindResponse rsp = PayProto.BindResponse.parseFrom(rspData.getData());
                        MyLog.w(TAG, "FillPayPalAccount response:" + rsp.toString());
                        retCode = rsp.getRetCode();
                        if (retCode == ErrorCode.CODE_SUCCESS) {
                            if (rsp.getPaypal() != null) {
                                paypalPay = new UserProfit.PaypalPay(rsp.getPaypal());
                            }
                            EventBus.getDefault().post(new EventClass.WithdrawEvent(EventClass.WithdrawEvent.EVENT_TYPE_ACCOUNT_BIND_CHANGE));
                        }
                        return retCode;
                    } catch (InvalidProtocolBufferException e) {
                        MyLog.e(e.toString());
                    }
                } else {
                    MyLog.e(TAG, "bindPayPalAccount rsp is null");
                }
                return ErrorCode.CODE_ERROR_NORMAL;
            }

            @Override
            protected void onPostExecute(Integer result) {
                IActionCallBack actionCallBack = callBack.get();
                if (actionCallBack != null) {
                    actionCallBack.processAction(MiLinkCommand.COMMAND_COMMIT_PAY_INFO, result, paypalPay);
                }
            }
        };

        AsyncTaskUtils.exe(task);
    }
}
