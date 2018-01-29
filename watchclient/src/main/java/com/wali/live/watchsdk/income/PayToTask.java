package com.wali.live.watchsdk.income;

import android.app.Activity;
import android.os.AsyncTask;

import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.PayProto;
import com.wali.live.utils.AsyncTaskUtils;

import java.lang.ref.WeakReference;

/**
 * Created by qianyuan on 16/3/3.
 */
public class PayToTask {

    private final String TAG = "FillAccountInfoTask";
    private Activity mActivity;

    public PayToTask() {
    }

    /**
     * @param callBack
     * @param uid
     * @param clientId
     * @param moneyCount
     */
    public void commitPayToTaget(final WeakReference<WithdrawCallBack> callBack, final long uid, final long clientId, final int moneyCount, final PayProto.WithdrawType type) {
        AsyncTask<Void, Void, PayProto.WithdrawResponse> task = new AsyncTask<Void, Void, PayProto.WithdrawResponse>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected PayProto.WithdrawResponse doInBackground(Void... params) {
                PayProto.WithdrawRequest req = PayProto.WithdrawRequest.newBuilder()
                        .setUuid(UserAccountManager.getInstance().getUuidAsLong())
                        .setClientId(clientId)
                        .setWithdrawAmount(moneyCount)
                        .setWithdrawType(type)
                        .build();
                PacketData data = new PacketData();
                data.setCommand(MiLinkCommand.COMMAND_COMMIT_PAY_TAGET);
                data.setData(req.toByteArray());
                MyLog.w(TAG, "WithdrawResponse request:" + req.toString());
                PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
                if (null != rspData) {
                    try {
                        PayProto.WithdrawResponse rsp = PayProto.WithdrawResponse.parseFrom(rspData.getData());
                        MyLog.w(TAG, "WithdrawResponse response:" + rsp.toString());
                        return rsp;
                    } catch (InvalidProtocolBufferException e) {
                        MyLog.e(e.toString());
                    }
                } else {
                    MyLog.e(TAG, "WithdrawResponse rsp is null");
                }
                return null;
            }

            @Override
            protected void onPostExecute(PayProto.WithdrawResponse rsp) {
                if (null != callBack.get()) {
                    if (null == rsp) {
                        MyLog.d(TAG, "rsp is null");
                        callBack.get().commitError(ErrorCode.CODE_WITHDRAW_ERROR_NULL);
                        return;
                    }
                    if (rsp.getRetCode() == ErrorCode.CODE_SUCCESS) {
//                    callBack.commitSuccess();
                        callBack.get().process(rsp);
                    } else {
                        callBack.get().commitError(rsp.getRetCode());
                    }
                    mActivity = null;
                }
            }
        };

        AsyncTaskUtils.exe(task);
    }
}
