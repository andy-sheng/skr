package com.wali.live.common.model;

import android.support.annotation.DrawableRes;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.utils.CommonUtils;
import com.base.utils.display.DisplayUtils;
import com.live.module.common.R;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.push.model.GlobalRoomMsgExt;
import com.mi.live.data.room.model.FansPrivilegeModel;
import com.wali.live.common.smiley.SmileyParser;

import java.util.ArrayList;

/**
 * Created by chengsimin on 16/7/18.
 */
public class CommentModel implements Comparable<CommentModel> {
    private String shopName;
    private double shopPrice;
    private long productId;
    private String shopUrl;
    private long giftId;
    private int msgType;
    private long sentTime;
    private long senderMsgId;
    private int level = -1;
    private String name;
    private int certificationType = -1;
    private long senderId;
    //去掉换行，已经转换为表情的
    private CharSequence body;
    private int commentColor = R.color.color_white;
    private boolean canClickable = true;
    private int backGround;
    private int nameColor = R.color.ffd267;
    private int likeId;
    private String likePath;
    private boolean redName;

    private int fansLevel;                            // 宠爱团等级
    private String fansMedal;                         // 宠爱团勋章

    private int vipLevel;                             //vip等级
    private boolean isVipFrozen;                      //vip是否被冻结
    private boolean isVipHide;                        //vip用户是否设置隐身

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public double getShopPrice() {
        return shopPrice;
    }

    public void setShopPrice(double shopPrice) {
        this.shopPrice = shopPrice;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getShopUrl() {
        return shopUrl;
    }

    public void setShopUrl(String shopUrl) {
        this.shopUrl = shopUrl;
    }

    public long getGiftId() {
        return giftId;
    }

    public void setGiftId(long giftId) {
        this.giftId = giftId;
    }

    public long getSenderMsgId() {
        return senderMsgId;
    }

    public void setSenderMsgId(long senderMsgId) {
        this.senderMsgId = senderMsgId;
    }

    public int getBackGround() {
        return backGround;
    }

    public void setBackGround(@DrawableRes int backGround) {
        this.backGround = backGround;
    }

    public long getSentTime() {
        return sentTime;
    }

    public void setSentTime(long sentTime) {
        this.sentTime = sentTime;
    }

    public int getLikeId() {
        return likeId;
    }

    public void setLikeId(int likeId) {
        this.likeId = likeId;
    }

    public void setCommentColor(int commentColor) {
        this.commentColor = commentColor;
    }

    public int getNameColor() {
        return nameColor;
    }

    public void setNameColor(int nameColor) {
        this.nameColor = nameColor;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public int getCommentColor() {
        return commentColor;
    }

    public CharSequence getBody() {
        return body;
    }

    public void setBody(String body, boolean parser) {
        if (parser) {
            String commentTrim = body == null ? "" : body.replaceAll("\n", " ");
            this.body = SmileyParser.getInstance().addSmileySpans(GlobalData.app(), commentTrim,
                    DisplayUtils.dip2px(14), true, false, true);
        } else {
            this.body = body;
        }
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCertificationType() {
        return certificationType;
    }

    public void setCertificationType(int certificationType) {
        this.certificationType = certificationType;
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public int getMsgType() {
        return msgType;
    }

    public String getLikePath() {
        return likePath;
    }

    public void setLikePath(String likePath) {
        this.likePath = likePath;
    }

    public boolean isCanClickable() {
        return canClickable;
    }

    public void setCanClickable(boolean canClickable) {
        this.canClickable = canClickable;
    }

    public boolean isRedName() {
        return redName;
    }

    public void setRedName(boolean redName) {
        this.redName = redName;
    }

    public void setFansLevel(int fansLevel) {
        this.fansLevel = fansLevel;
    }

    public void setFansMedal(String fansMedal) {
        this.fansMedal = fansMedal;
    }

    public int getFansLevel() {
        return fansLevel;
    }

    public String getFansMedal() {
        return fansMedal;
    }

    public int getVipLevel() {
        return vipLevel;
    }

    public void setVipLevel(int vipLevel) {
        this.vipLevel = vipLevel;
    }

    public boolean isVipFrozen() {
        return isVipFrozen;
    }

    public void setVipFrozen(boolean vipFrozen) {
        isVipFrozen = vipFrozen;
    }

    public boolean isVipHide() {
        return isVipHide;
    }

    public void setVipHide(boolean vipHide) {
        isVipHide = vipHide;
    }

    public static CommentModel loadFromBarrage(BarrageMsg msg) {
        CommentModel liveComment = new CommentModel();
        liveComment.setSenderId(msg.getSender());
        liveComment.setMsgType(msg.getMsgType());
        liveComment.setCertificationType(msg.getCertificationType());
        liveComment.setLevel(msg.getSenderLevel());
        String name = msg.getSenderName();
        if (TextUtils.isEmpty(name)) {
            liveComment.setName(String.valueOf(liveComment.getSenderId()));
        } else {
            liveComment.setName(name);
        }
        liveComment.setSentTime(msg.getSentTime());
        liveComment.setSenderMsgId(msg.getSenderMsgId());
        liveComment.setCommentColor(R.color.f7a66b);
        liveComment.setNameColor(R.color.ffd267);
        liveComment.setRedName(msg.isRedName());
        liveComment.setVipLevel(msg.getVipLevel());
        liveComment.setVipFrozen(msg.isVipFrozen());
        liveComment.setVipHide(msg.isVipHide());

        switch (liveComment.getMsgType()) {
            //以下是　系统消息类 　不显示名字和级别
            case BarrageMsgType.B_MSG_TYPE_FORBIDDEN:
            case BarrageMsgType.B_MSG_TYPE_CANCEL_FORBIDDEN: {
                BarrageMsg.ForbiddenMsgExt msgExt = (BarrageMsg.ForbiddenMsgExt) msg.getMsgExt();
//                MyLog.w("ForbiddenMsg"+msg.toString());
                String message = msgExt.getBanMessage(msg.getAnchorId(), UserAccountManager.getInstance().getUuidAsLong(), msg.getMsgType(), msg.getSenderName());
                liveComment.setBody(message, false);
                liveComment.setLevel(0);
                liveComment.setCertificationType(0);
                liveComment.setName(null);
                liveComment.setCanClickable(false);
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_SET_MANAGER: {
                liveComment.setBody(GlobalData.app().getString(R.string.barrage_set_manager_body), false);
                liveComment.setLevel(0);
                liveComment.setCertificationType(0);
                liveComment.setName(null);
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_CANCEL_MANAGER: {
                liveComment.setBody(GlobalData.app().getString(R.string.barrage_cancel_manager_body), false);
                liveComment.setLevel(0);
                liveComment.setCertificationType(0);
                liveComment.setName(null);
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_LIVE_END:
            case BarrageMsgType.B_MSG_TYPE_ANIM:
            case BarrageMsgType.B_MSG_TYPE_GLOBAL_SYS_MSG:
            case BarrageMsgType.B_MSG_TYPE_ROOM_SYS_MSG:
            case BarrageMsgType.B_MSG_TYPE_TOP_GET:
            case BarrageMsgType.B_MSG_TYPE_TOP_LOSE:
            case BarrageMsgType.B_MSG_TYPE_LIVE_OWNER_MSG:
            case BarrageMsgType.B_MSG_TYPE_ANCHOR_LEAVE:
            case BarrageMsgType.B_MSG_TYPE_ANCHOR_JOIN:
            case BarrageMsgType.B_MSG_TYPE_LINE_VIEWER_BACK:
            case BarrageMsgType.B_MSG_TYPE_LINE_VIEWER_LEAVE: {
                liveComment.setBody(msg.getBody(), false);
                liveComment.setLevel(0);
                liveComment.setCertificationType(0);
                liveComment.setName(null);
            }
            break;
            //用户行为类 显示名字和级别
            case BarrageMsgType.B_MSG_TYPE_ROOM_FOUCES_ANCHOR: {
                if (!TextUtils.isEmpty(name)) {
                    liveComment.setBody(GlobalData.app().getString(R.string.barrage_focus_body), false);
                } else {
                    liveComment.setName(null);
                    liveComment.setBody(msg.getBody(), false);
                }
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_LEAVE: {
                if (!TextUtils.isEmpty(name)) {
                    liveComment.setBody(GlobalData.app().getString(R.string.barrage_leave_live_body), false);
                } else {
                    liveComment.setName(null);
                    liveComment.setBody(msg.getBody(), false);
                }
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_JOIN: {
                if (!TextUtils.isEmpty(name)) {
                    if (msg.getVipLevel() > 1 && !msg.isVipFrozen() && !msg.isVipHide()) {
                        String[] tips = GlobalData.app().getResources().getStringArray(R.array.welcome_vip_enter_room_tip);
                        String body = tips[(int) (System.currentTimeMillis() % tips.length)];
                        liveComment.setBody(body, false);
                    } else {
                        liveComment.setBody(GlobalData.app().getString(R.string.barrage_enter_live_body), false);
                    }
                } else {
                    liveComment.setName(null);
                    liveComment.setBody(msg.getBody(), false);
                }
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_LIKE: {
                if (!TextUtils.isEmpty(name)) {
                    liveComment.setBody(GlobalData.app().getString(R.string.barrage_like_body), false);
                } else {
                    liveComment.setName(null);
                    liveComment.setBody(msg.getBody(), false);
                }
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_SHARE: {
                if (!TextUtils.isEmpty(name)) {
                    liveComment.setBody(GlobalData.app().getString(R.string.barrage_share_body), false);
                } else {
                    liveComment.setName(null);
                    liveComment.setBody(msg.getBody(), false);
                }
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_ADD_SHOP:
                BarrageMsg.ShopMessageExt ext = (BarrageMsg.ShopMessageExt) msg.getMsgExt();
                if (ext.shop_type == 3) {
                    liveComment.setCommentColor(R.color.f7a66b);
                    liveComment.setBody(String.valueOf(ext.shop_content), false);
                }
                break;
            case BarrageMsgType.B_MSG_TYPE_GIFT:
            case BarrageMsgType.B_MSG_TYPE_RED_ENVELOPE:
            case BarrageMsgType.B_MSG_TYPE_GLABAL_MSG:
            case BarrageMsgType.B_MSG_TYPE_LIGHT_UP_GIFT:
            case BarrageMsgType.B_MSG_TYPE_ROOM_BACKGROUND_GIFT: {
                liveComment.setBody(msg.getBody(), false);
                if (liveComment.getMsgType() == BarrageMsgType.B_MSG_TYPE_GIFT || liveComment.getMsgType() == BarrageMsgType.B_MSG_TYPE_GLABAL_MSG) {
                    BarrageMsg.GiftMsgExt giftExt = (BarrageMsg.GiftMsgExt) msg.getMsgExt();
                    liveComment.setGiftId(giftExt.giftId);
                }
            }

            break;
            default: {
                liveComment.setName(name + ": ");
                if (msg.isRedName()) {
                    liveComment.setCommentColor(R.color.ccccccc);
                    liveComment.setNameColor(R.color.ccccccc);
                    liveComment.setLevel(0);
                } else {
                    liveComment.setCommentColor(R.color.white);
                    liveComment.setNameColor(R.color.ffd267);
                }
                liveComment.setBody(msg.getBody(), true);
            }
            break;
        }
        if (liveComment.getMsgType() == BarrageMsgType.B_MSG_TYPE_LIKE) {
            BarrageMsg.LikeMsgExt ext = (BarrageMsg.LikeMsgExt) msg.getMsgExt();
            liveComment.setLikeId(ext.id);
            liveComment.setLikePath(ext.bitmapPath);
        }
        GlobalRoomMsgExt globalRoomMsgExt = msg.getGlobalRoomMsgExt();
        if (globalRoomMsgExt != null && globalRoomMsgExt.getRoomMsgExtList() != null
                && !globalRoomMsgExt.getRoomMsgExtList().isEmpty()) {
            ArrayList<GlobalRoomMsgExt.BaseRoomMessageExt> msgExtList
                    = globalRoomMsgExt.getRoomMsgExtList();
            for (GlobalRoomMsgExt.BaseRoomMessageExt msgExt : msgExtList) {
                switch (msgExt.getType()) {
                    case GlobalRoomMsgExt.INNER_GLOBAL_VFAN:
                        GlobalRoomMsgExt.FansMemberMsgExt fansMemberMsgExt = (GlobalRoomMsgExt.FansMemberMsgExt) msgExt;
                        if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_TEXT || msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_PAY_BARRAGE
                               /* || msg.getMsgType() == MSG_SMART_NORMAL*/) {
                            //TODO zyh 飘萍弹幕之后加（智能弹幕相关）
                            if (!fansMemberMsgExt.isVipExpire() && fansMemberMsgExt.getPetLevel() >= FansPrivilegeModel.SEND_COLOR_BARRAGE_VIP_LEVEL) {
                                liveComment.setBackGround(R.drawable.live_bg_comment_pink);
                            }
                            liveComment.setFansLevel(fansMemberMsgExt.getPetLevel());
                            liveComment.setFansMedal(fansMemberMsgExt.getMedalValue());
                        }
                        if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_GIFT || msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_RED_ENVELOPE
                                || msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_ROOM_BACKGROUND_GIFT || msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_LIGHT_UP_GIFT) {
                            liveComment.setFansLevel(fansMemberMsgExt.getPetLevel());
                            liveComment.setFansMedal(fansMemberMsgExt.getMedalValue());
                        }
                        break;
                }
            }
        }
        return liveComment;
    }

    @Override
    public int compareTo(CommentModel another) {
        if (another == null) {
            return -1;
        } else {
            if (this.sentTime < another.sentTime) {
                return -1;
            } else if (this.sentTime > another.sentTime) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        CommentModel that = (CommentModel) o;
        return senderId == that.senderId && senderMsgId == that.senderMsgId;
    }
}
