package com.mi.live.data.push.model;

import com.base.log.MyLog;
import com.google.protobuf.InvalidProtocolBufferException;
import com.wali.live.proto.LiveMessageProto;

import java.util.ArrayList;

/**
 * Created by zyh on 2017/11/15.
 */

public class GlobalRoomMsgExt {
    public static final int INNER_GLOBAL_MEDAL_TYPE = 100;
    public static final int INNER_GLOBAL_SCHEME_TYPE = 101;
    public static final int INNER_GLOBAL_SHARE_JOIN_ROME_TYPE = 400;
    public static final int INNER_GLOBAL_ADMIN_FLY = 500;//弹幕
    public static final int INNER_GLOBAL_PAY_HORN = 501;//喇叭
    public static final int INNER_GLOBAL_VFAN = 600;//宠爱团简要信息
    private ArrayList<BaseRoomMessageExt> mRoomMsgExtList;

    public ArrayList<BaseRoomMessageExt> getRoomMsgExtList() {
        return mRoomMsgExtList;
    }

    public static GlobalRoomMsgExt loadFromPB(LiveMessageProto.GlobalRoomMessageExt globalRoomMsgExt) {
        if (globalRoomMsgExt == null || globalRoomMsgExt.getInnerGlobalRoomMsgExtList() == null
                || globalRoomMsgExt.getInnerGlobalRoomMsgExtList().isEmpty()) {
            return null;
        }
        GlobalRoomMsgExt msgExt = new GlobalRoomMsgExt();
        msgExt.mRoomMsgExtList = new ArrayList<>();
        for (LiveMessageProto.InnerGlobalRoomMessageExt pbMsgExt : globalRoomMsgExt.getInnerGlobalRoomMsgExtList()) {
            switch (pbMsgExt.getType()) {
                case INNER_GLOBAL_VFAN:
                    try {
                        msgExt.mRoomMsgExtList.add(FansMemberMsgExt.loadFromPB(LiveMessageProto.
                                VFansMemberBriefInfo.parseFrom(pbMsgExt.getExt().toByteArray())));
                    } catch (InvalidProtocolBufferException e) {
                        MyLog.e(e);
                    }
                    break;

            }
        }
        return msgExt;
    }


    public static class BaseRoomMessageExt {
        protected int type;

        public int getType() {
            return type;
        }
    }

    public static class FansMemberMsgExt extends BaseRoomMessageExt {
        private int petLevel;
        private String medalValue;
        private boolean isUseMedal;
        private boolean isVipExpire;
        private String barrageColor;

        public static FansMemberMsgExt loadFromPB(LiveMessageProto.VFansMemberBriefInfo message) {
            if (message == null) {
                return null;
            }
            FansMemberMsgExt fansMemberMsgExt = new FansMemberMsgExt();
            fansMemberMsgExt.type = INNER_GLOBAL_VFAN;
            fansMemberMsgExt.petLevel = message.getPetLevel();
            fansMemberMsgExt.medalValue = message.getMedalValue();
            fansMemberMsgExt.isUseMedal = message.getIsUseMedal();
            fansMemberMsgExt.isVipExpire = message.getIsVipExpire();
            fansMemberMsgExt.barrageColor = message.getBarrageColor();
            return fansMemberMsgExt;
        }

        public int getPetLevel() {
            return petLevel;
        }

        public String getMedalValue() {
            return medalValue;
        }

        public boolean isUseMedal() {
            return isUseMedal;
        }

        public boolean isVipExpire() {
            return isVipExpire;
        }

        public String getBarrageColor() {
            return barrageColor;
        }
    }
}
