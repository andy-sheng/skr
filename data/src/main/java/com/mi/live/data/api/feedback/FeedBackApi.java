package com.mi.live.data.api.feedback;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.proto.CommonChannelProto;
import com.wali.live.proto.ReportProto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhujianning on 18-7-5.
 */

public class FeedBackApi {
    private static final String TAG = "FeedBackApi";

    public static final int FLAG_ITEM_TYPE_FEEDBACK = 6;


    public static boolean sendDisLikeLiveFeedBack(long date, long anchorUid, String roomId) {
        CommonChannelProto.LikeFeedback likeFeedback = CommonChannelProto.LikeFeedback.newBuilder()
                .setRoomId(roomId)
                .setZuid(anchorUid)
                .build();

        CommonChannelProto.LiveRecvFlagItem.Builder builder = CommonChannelProto.LiveRecvFlagItem.newBuilder().setDate(date)
                .setType(FLAG_ITEM_TYPE_FEEDBACK);
        if (roomId != null) {
            builder.setRecommend(roomId);
        }
        if (likeFeedback.toByteArray() != null) {
            builder.setExtData(ByteString.copyFrom(likeFeedback.toByteArray()));
        }
        List<CommonChannelProto.LiveRecvFlagItem> items = new ArrayList<>();
        items.add(builder.build());

        MyLog.i(TAG, "sendStatisticsData " + items.size());
        CommonChannelProto.LiveRecvFlagReq req = CommonChannelProto.LiveRecvFlagReq.newBuilder()
                .addAllItems(items).build();

        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_STATISTICS_RECOMMEND_TAG);
        packetData.setData(req.toByteArray());
        PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
        if (responseData != null) {
            try {
                CommonChannelProto.LiveRecvFlagRsp rsp = CommonChannelProto.LiveRecvFlagRsp.parseFrom(responseData.getData());
                MyLog.e(TAG, "sendStatisticsData RSP recCode  : " + rsp.getRetCode());
                if (rsp.getRetCode() == 0) {
                    return true;
                }

            } catch (InvalidProtocolBufferException e) {
                return false;
            }
        } else {
            MyLog.e(TAG, "sendStatisticsData RSP null ");
        }
        return false;
    }

    public static boolean sendReport(long targetId
            , int reportType
            , String roomId
            , String liveUrl
            , String reprotPos
            , String commentProof
            , int contentType
            , String otherReason) {
        ReportProto.ReportReq.Builder builder = ReportProto.ReportReq.newBuilder();
        builder.setReportFrom(UserAccountManager.getInstance().getUuidAsLong())
                .setReportTo(targetId)
                .setReportType(reportType);
        if (!TextUtils.isEmpty(roomId)) {
            builder.setRoomId(roomId);
        }
        if (!TextUtils.isEmpty(liveUrl)) {
            builder.setLiveUrl(liveUrl);
        }
        if (!TextUtils.isEmpty(reprotPos)) {
            builder.setPosition(reprotPos);
        }
        if (!TextUtils.isEmpty(commentProof)) {
            builder.setProof(commentProof);
        }
        if (contentType == 1) {
            builder.setReportContent(otherReason);
        }
        PacketData packetData = new PacketData();
        packetData.setCommand(MiLinkCommand.COMMAND_REPORT);
        packetData.setData(builder.build().toByteArray());
        PacketData result = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
        if(result != null) {
            try {
                ReportProto.ReportRsp reportRsp = ReportProto.ReportRsp.parseFrom(result.getData());
                if (reportRsp != null && reportRsp.getRetCode() == 0) {
                    return true;
                } else {
                    MyLog.d(TAG, "ReportProto.ReportRsp == null");
                }
            } catch (Exception e) {
                MyLog.e(e);
            }
        } else {
            MyLog.d(TAG, "PacketData == null");
        }

        return false;
    }
}
