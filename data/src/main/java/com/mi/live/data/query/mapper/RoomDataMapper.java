package com.mi.live.data.query.mapper;

import com.base.log.MyLog;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.location.Location;
import com.mi.live.data.push.model.BarrageMsgExt;
import com.mi.live.data.query.model.EnterRoomInfo;
import com.mi.live.data.query.model.LiveCover;
import com.mi.live.data.query.model.MessageRule;
import com.mi.live.data.query.model.ViewerModel;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.proto.CommonProto;
import com.wali.live.proto.Live2Proto;
import com.wali.live.proto.LiveCommonProto;
import com.wali.live.proto.LiveProto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengsimin on 16/9/8.
 */
public class RoomDataMapper {

    /**
     * 解析enter后pb
     *
     * @param rsp
     * @return
     */
    public static EnterRoomInfo loadLiveBeanFromPB(LiveProto.EnterLiveRsp rsp) {
        EnterRoomInfo enterRoomInfo = new EnterRoomInfo();
        if (rsp != null) {
            enterRoomInfo.setRetCode(rsp.getRetCode());
            enterRoomInfo.setViewerCount(rsp.getViewerCnt());

            List<ViewerModel> viewerModelList = new ArrayList<>();
            List<LiveCommonProto.Viewer> viewers = rsp.getViewerList();
            if (viewers != null) {
                for (LiveCommonProto.Viewer v : viewers) {
                    viewerModelList.add(new ViewerModel(v));
                }
            }
            enterRoomInfo.setViewerModelList(viewerModelList);

            enterRoomInfo.setManager(rsp.getIsManager());
            enterRoomInfo.setBanSpeak(rsp.getBanSpeak());

            Location location = new Location();
            CommonProto.Location loc = rsp.getLocation();
            if (loc != null) {
                location.parse(loc);
            }
            enterRoomInfo.setLocation(location);

            enterRoomInfo.setType(rsp.getType());

            enterRoomInfo.setShareUrl(rsp.getShareUrl());

            if (rsp.hasMicInfo()) {
                enterRoomInfo.setMicBeginInfo(new BarrageMsgExt.MicBeginInfo()
                        .parseFromPB(rsp.getMicInfo()));
            }
            if (rsp.hasNewPkInfo()) {
                enterRoomInfo.setPkStartInfo(new BarrageMsgExt.PkStartInfo()
                        .parseFromInfo(rsp.getNewPkInfo(), rsp.getTimestamp()));
            }

            enterRoomInfo.setDownStreamUrl(rsp.getDownStreamUrl());

            enterRoomInfo.setEnterTs(rsp.getTimestamp());
            Live2Proto.LiveCover lc = rsp.getLiveCover();
            if (lc != null) {
                enterRoomInfo.setLiveCover(LiveCover.loadFromPb(lc));
            }

            enterRoomInfo.setLiveTitle(rsp.getLiveTitle());
            enterRoomInfo.setMessageMode(rsp.getMessageMode());

            LiveCommonProto.MsgRule msgRule = rsp.getMsgRule();
            if (msgRule != null) {
                enterRoomInfo.setMessageRule(new MessageRule(msgRule));
            }

            enterRoomInfo.setShop(rsp.getIsShop());
            enterRoomInfo.setHideGift(rsp.getHideGift());
            enterRoomInfo.setSupportMgicFace(rsp.getSupportMagicFace());
            enterRoomInfo.setEnterTs(System.currentTimeMillis());
        }
        return enterRoomInfo;
    }

    public static void fillRoomDataModelByEnterRoomInfo(RoomBaseDataModel mMyRoomData, EnterRoomInfo enterRoomInfo) {
        mMyRoomData.setViewerCnt(enterRoomInfo.getViewerCount());

        mMyRoomData.getViewersList().clear();
        mMyRoomData.getViewersList().addAll(enterRoomInfo.getViewerModelList());
        mMyRoomData.notifyViewersChange("processEnterLive");
//                mIsManager = (boolean) objects[2];
        mMyRoomData.setCanSpeak(!enterRoomInfo.isBanSpeak());
        mMyRoomData.setMsgRule(enterRoomInfo.getMessageRule());

        /**
         * TODO
         * @notice 以外面进房间的liveType为准
         * 但是如果liveType不为0，就设置进去
         * 类型不一致应该是要做保护的，这里先简单赋值
         */
        if (enterRoomInfo.getType() != LiveManager.TYPE_LIVE_PUBLIC) {
            mMyRoomData.setLiveType(enterRoomInfo.getType());
        }
        MyLog.d("RoomDataMapper", "ignore liveType=" + enterRoomInfo.getType() + "@" + mMyRoomData.hashCode());
        mMyRoomData.setShareUrl(enterRoomInfo.getShareUrl());

        mMyRoomData.canUpdateLastUpdateViewerCount(enterRoomInfo.getEnterTs());
        if (enterRoomInfo.getLiveCover() != null) {
            mMyRoomData.setCoverUrl(enterRoomInfo.getLiveCover().getUrl());
        }
        mMyRoomData.setLiveTitle(enterRoomInfo.getLiveTitle());

        mMyRoomData.setGetMessageMode(enterRoomInfo.getMessageMode());
        mMyRoomData.setShop(enterRoomInfo.isShop());
        mMyRoomData.setHideGift(enterRoomInfo.isHideGift());
        if (enterRoomInfo.getLocation() != null) {
            mMyRoomData.setCity(enterRoomInfo.getLocation().getCity());
        }
        mMyRoomData.setSupportMagicFace(enterRoomInfo.isSupportMgicFace());
        mMyRoomData.setEnterRoomTime(enterRoomInfo.getEnterTs());
    }
}
