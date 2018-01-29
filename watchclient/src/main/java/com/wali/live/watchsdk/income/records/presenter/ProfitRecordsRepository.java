package com.wali.live.watchsdk.income.records.presenter;

import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.PayProto;
import com.wali.live.watchsdk.income.records.model.ProfitMonthDetail;

/**
 * Created by zhaomin on 17-6-29.
 */
public class ProfitRecordsRepository {

    private static final String TAG = "ProfitRecordsRepository";

    public ProfitMonthDetail fetchRecords(long uid, int year, int month) {
        PayProto.ProfitRecordRequest req = PayProto.ProfitRecordRequest.newBuilder()
                .setUuid(uid)
                .setYear(year)
                .setMonth(month)
                .build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_GET_PROFITLIST);
        data.setData(req.toByteArray());
        MyLog.w(TAG, "fetchRecords request:" + req.toString());
        PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
        if (null != rspData && rspData.getData() != null) {
            try {
                PayProto.ProfitRecordResponse rsp = PayProto.ProfitRecordResponse.parseFrom(rspData.getData());
                MyLog.w(TAG, "fetchRecords response: " + (rsp != null ? rsp.getRetCode() : "NULL"));
                if (rsp != null && rsp.getRetCode() == 0) {
                    return new ProfitMonthDetail(rsp);
                }
            } catch (InvalidProtocolBufferException e) {
                MyLog.e(e.toString());
            }
        } else {
            MyLog.e(TAG, "pullExchangeListSync rspData is null");
        }
        return null;
    }

}
