package com.wali.live.common.model;

import android.support.annotation.ColorRes;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.StringUtils;
import com.base.utils.display.DisplayUtils;
import com.google.protobuf.InvalidProtocolBufferException;
import com.live.module.common.R;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.model.BarrageMsgExt;
import com.mi.live.data.push.model.BarrageMsgType;
import com.mi.live.data.room.model.VfansPrivilegeModel;
import com.mi.live.data.user.User;
import com.wali.live.common.smiley.SmileyParser;
import com.wali.live.proto.LiveMessageProto;

import java.util.ArrayList;
import java.util.List;

import static com.mi.live.data.push.model.BarrageMsg.RoomTxtMessageExt.MSG_SMART_NORMAL;

/**
 * Created by chengsimin on 16/7/18.
 */
public class CommentModel implements Comparable<CommentModel> {
    private static final String TAG = CommentModel.class.getSimpleName();

    private String shopName;
    private double shopPrice;
    private long productId;
    private String shopUrl;

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

    private long giftId;

    public int getGiftCount() {
        return giftCount;
    }

    public void setGiftCount(int giftCount) {
        this.giftCount = giftCount;
    }

    private int giftCount = 1;

    public long getGiftId() {
        return giftId;
    }

    public void setGiftId(long giftId) {
        this.giftId = giftId;
    }

    private int msgType;

    public long getSenderMsgId() {
        return senderMsgId;
    }

    public void setSenderMsgId(long senderMsgId) {
        this.senderMsgId = senderMsgId;
    }

    private long senderMsgId;
    private int level = -1;
    private String name;
    private int certificationType = -1;
    private long senderId;
    //去掉换行，已经转换为表情的
    private CharSequence body;
    @ColorRes
    private int commentColor = R.color.color_white;

    private boolean canClickable = true;

    public int getBackGround() {
        return backGround;
    }

    public void setBackGround(int backGround) {
        this.backGround = backGround;
    }

    private int backGround;
    @ColorRes
    private int nameColor = R.color.ffd267;


    private String likePath;

    private boolean redName;

    private List<Integer> innerGlobalRoomMessageTypeList;

    private String innerGlobalRoomMessageSchemaUrl;

    private List<String> beforeNickNameConfigList;

    private List<String> afterNickNameConfigList;

    private List<String> beforeContentConfigList;

    private List<String> afterContentConfigList;

    private List<String> effectConfigList;

    private int vipLevel;//vip等级
    private boolean isVipFrozen;//vip是否被冻结
    private boolean isVipHide;//vip用户是否设置隐身

    private int nobleLevel;

    private int vfansLevel; // 宠爱团等级
    private String vfansMedal;// 宠爱团勋章

    public long getSentTime() {
        return sentTime;
    }

    public void setSentTime(long sentTime) {
        this.sentTime = sentTime;
    }

    private long sentTime;

    public int getLikeId() {
        return likeId;
    }

    public void setLikeId(int likeId) {
        this.likeId = likeId;
    }

    public void setCommentColor(@ColorRes int commentColor) {
        this.commentColor = commentColor;
    }

    @ColorRes
    public int getNameColor() {
        return nameColor;
    }

    public void setNameColor(@ColorRes int nameColor) {
        this.nameColor = nameColor;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    private int likeId;

    @ColorRes
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
                    DisplayUtils.dip2px(13.33f), true, false, true);
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

    public List<Integer> getInnerGlobalRoomMessageTypeList() {
        return innerGlobalRoomMessageTypeList;
    }

    public void setInnerGlobalRoomMessageTypeList(List<Integer> innerGlobalRoomMessageTypeList) {
        this.innerGlobalRoomMessageTypeList = innerGlobalRoomMessageTypeList;
    }

    public String getInnerGlobalRoomMessageSchemaUrl() {
        return innerGlobalRoomMessageSchemaUrl;
    }

    public void setInnerGlobalRoomMessageSchemaUrl(String innerGlobalRoomMessageSchemaUrl) {
        this.innerGlobalRoomMessageSchemaUrl = innerGlobalRoomMessageSchemaUrl;
    }

    public List<String> getBeforeNickNameConfigList() {
        return beforeNickNameConfigList;
    }

    public void setBeforeNickNameConfigList(List<String> beforeNickNameConfigList) {
        this.beforeNickNameConfigList = beforeNickNameConfigList;
    }

    public List<String> getAfterNickNameConfigList() {
        return afterNickNameConfigList;
    }

    public void setAfterNickNameConfigList(List<String> afterNickNameConfigList) {
        this.afterNickNameConfigList = afterNickNameConfigList;
    }

    public List<String> getBeforeContentConfigList() {
        return beforeContentConfigList;
    }

    public void setBeforeContentConfigList(List<String> beforeContentConfigList) {
        this.beforeContentConfigList = beforeContentConfigList;
    }

    public List<String> getAfterContentConfigList() {
        return afterContentConfigList;
    }

    public void setAfterContentConfigList(List<String> afterContentConfigList) {
        this.afterContentConfigList = afterContentConfigList;
    }

    public List<String> getEffectConfigList() {
        return effectConfigList;
    }

    public void setEffectConfigList(List<String> effectConfigList) {
        this.effectConfigList = effectConfigList;
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


    public int getVfansLevel() {
        return vfansLevel;
    }

    public void setVfansLevel(int vfansLevel) {
        this.vfansLevel = vfansLevel;
    }

    public String getVfansMedal() {
        return vfansMedal;
    }

    public void setVfansMedal(String vfansMedal) {
        this.vfansMedal = vfansMedal;
    }

    public int getNobleLevel() {
        return nobleLevel;
    }

    public void setNobleLevel(int nobleLevel) {
        this.nobleLevel = nobleLevel;
    }

    public boolean isNoble() {
        return this.nobleLevel == User.NOBLE_LEVEL_FIFTH || this.nobleLevel == User.NOBLE_LEVEL_FOURTH
                || this.nobleLevel == User.NOBLE_LEVEL_THIRD || this.nobleLevel == User.NOBLE_LEVEL_SECOND
                || this.nobleLevel == User.NOBLE_LEVEL_TOP;
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
        liveComment.setCommentColor(R.color.white);
        liveComment.setNameColor(R.color.color_f0d388);
        liveComment.setRedName(msg.isRedName());
        liveComment.setVipLevel(msg.getVipLevel());
        liveComment.setNobleLevel(msg.getNobleLevel());
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
            case BarrageMsgType.B_MSG_TYPE_ANCHOR_LEAVE:
            case BarrageMsgType.B_MSG_TYPE_ANCHOR_JOIN:
            case BarrageMsgType.B_MSG_TYPE_GLOBAL_SYS_MSG:
            case BarrageMsgType.B_MSG_TYPE_ROOM_SYS_MSG:
            case BarrageMsgType.B_MSG_TYPE_LIVE_END:
            case BarrageMsgType.B_MSG_TYPE_ANIM:
            case BarrageMsgType.B_MSG_TYPE_TOP_GET:
            case BarrageMsgType.B_MSG_TYPE_TOP_LOSE:
            case BarrageMsgType.B_MSG_TYPE_LIVE_OWNER_MSG:
            case BarrageMsgType.B_MSG_TYPE_LINE_VIEWER_BACK:
            case BarrageMsgType.B_MSG_TYPE_LINE_VIEWER_LEAVE:
            case BarrageMsgType.B_MSG_TYPE_COMMEN_SYS_MSG: {
                liveComment.setBody(msg.getBody(), false);
                liveComment.setLevel(0);
                liveComment.setCertificationType(0);
                liveComment.setName(null);
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_ANNOUNCEMENT:
                liveComment.setCommentColor(R.color.color_2b2b2b);
                liveComment.setBody(msg.getBody(), false);
                liveComment.setLevel(0);
                liveComment.setCertificationType(0);
                liveComment.setName(null);
                break;
            //用户行为类 显示名字和级别
            case BarrageMsgType.B_MSG_TYPE_ROOM_FOUCES_ANCHOR: {
                if (!TextUtils.isEmpty(name)) {
                    liveComment.setBody(GlobalData.app().getString(R.string.barrage_focus_body), false);
                } else {
                    liveComment.setName(null);
                    liveComment.setBody(msg.getBody(), false);
                }
                liveComment.setCommentColor(R.color.color_7eeeff);
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
                    // 一个没被冻结、没隐身的VIP2以上(含)用户进入了直播间
                    if (msg.getVipLevel() >= 2 && !msg.isVipFrozen() && !msg.isVipHide()) {
                        String welcomeVipEnterRoomTip = getWelcomeVipEnterRoomTip();
                        if (!TextUtils.isEmpty(welcomeVipEnterRoomTip)) {
                            liveComment.setBody(welcomeVipEnterRoomTip, false);
                            break;
                        }
                    }

                    String s = GlobalData.app().getString(R.string.barrage_enter_live_body);
                    if (msg.getMsgExt() instanceof BarrageMsg.JoinRoomMsgExt) {
                        BarrageMsg.JoinRoomMsgExt ext = ((BarrageMsg.JoinRoomMsgExt) msg.getMsgExt());
                        if (ext.type == BarrageMsg.JoinRoomMsgExt.TYPE_NEARBY) {
                            s = GlobalData.app().getString(R.string.barrage_enter_live_by_near_body);
                        }
                    }
                    liveComment.setBody(s, false);
                } else {
                    liveComment.setName(null);
                    liveComment.setBody(msg.getBody(), false);
                }
                liveComment.setCommentColor(R.color.color_f0d388);
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_LIKE: {
                if (!TextUtils.isEmpty(name)) {
                    liveComment.setBody(GlobalData.app().getString(R.string.barrage_like_body), false);
                } else {
                    liveComment.setName(null);
                    liveComment.setBody(msg.getBody(), false);
                }
                liveComment.setCommentColor(R.color.color_f0d388);
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_RECORD_SHARE: {
                liveComment.setBody(msg.getBody(), false);
                liveComment.setCommentColor(R.color.color_7eeeff);
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_SHARE: {
                if (!TextUtils.isEmpty(name)) {
                    liveComment.setBody(GlobalData.app().getString(R.string.barrage_share_body), false);
                } else {
                    liveComment.setName(null);
                    liveComment.setBody(msg.getBody(), false);
                }
                liveComment.setCommentColor(R.color.color_7eeeff);
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_ADD_SHOP: {
                BarrageMsg.ShopMessageExt ext = (BarrageMsg.ShopMessageExt) msg.getMsgExt();
                if (ext.shop_type == 3) {
                    liveComment.setCommentColor(R.color.color_7eeeff);
                    liveComment.setBody(String.valueOf(ext.shop_content), false);
                }
            }
            break;
            case BarrageMsgType.B_MSG_TYPE_GIFT:
            case BarrageMsgType.B_MSG_TYPE_RED_ENVELOPE:
            case BarrageMsgType.B_MSG_TYPE_GLABAL_MSG:
            case BarrageMsgType.B_MSG_TYPE_ROOM_BACKGROUND_GIFT: {
                liveComment.setBody(msg.getBody(), false);
                if (liveComment.getMsgType() == BarrageMsgType.B_MSG_TYPE_GIFT || liveComment.getMsgType() == BarrageMsgType.B_MSG_TYPE_GLABAL_MSG) {
                    BarrageMsg.GiftMsgExt giftExt = (BarrageMsg.GiftMsgExt) msg.getMsgExt();
                    liveComment.setGiftId(giftExt.giftId);
                    liveComment.setGiftCount(giftExt.batch_count);
                }
                liveComment.setCommentColor(R.color.color_7eeeff);
                break;
            }
            case BarrageMsgType.B_MSG_TYPE_LIGHT_UP_GIFT:
                liveComment.setBody(msg.getBody(), false);
                if (liveComment.getMsgType() == BarrageMsgType.B_MSG_TYPE_GIFT || liveComment.getMsgType() == BarrageMsgType.B_MSG_TYPE_GLABAL_MSG) {
                    BarrageMsg.GiftMsgExt giftExt = (BarrageMsg.GiftMsgExt) msg.getMsgExt();
                    liveComment.setGiftId(giftExt.giftId);
                }
                liveComment.setCommentColor(R.color.color_f0d388);
                break;
            case BarrageMsgType.B_MSG_TYPE_PAY_BARRAGE:
            case BarrageMsgType.B_MSG_TYPE_TEXT: {
                String body = msg.getBody();
                if (msg.isRedName()) {
                    liveComment.setCommentColor(R.color.ccccccc);
                    liveComment.setLevel(0);
                } else if (msg.getMsgExt() instanceof BarrageMsg.RoomTxtMessageExt) {// @信息解析
                    BarrageMsg.RoomTxtMessageExt ext = (BarrageMsg.RoomTxtMessageExt) msg.getMsgExt();
                    switch (ext.getType()) {
                        case BarrageMsg.RoomTxtMessageExt.MSG_TYPE_NORMAL:
                            try {
                                LiveMessageProto.AtMessage atMessage = LiveMessageProto.AtMessage.parseFrom(ext.getExt());
                                if (atMessage.hasAtUser()
                                        && atMessage.getAtUser() == UserAccountManager.getInstance().getUuidAsLong()) {
                                    liveComment.setCommentColor(R.color.color_at_comment);// TODO: 17-6-5
                                }
                            } catch (InvalidProtocolBufferException e) {
                                MyLog.e(TAG, "parse AtMessage fail, type:" + ext.getType());
                            }
                            break;
                    }
                } else if (msg.getMsgExt() instanceof BarrageMsg.GiftMsgExt) {//飘屏弹幕
                    long atTargetUserId = StringUtils.getAtTargetUserId(msg.getBody());
                    if (atTargetUserId != 0) {
                        if (atTargetUserId == UserAccountManager.getInstance().getUuidAsLong()) {
                            liveComment.setCommentColor(R.color.color_at_comment);// TODO: 17-6-5
                        }
                        body = msg.getBody().replace("<" + atTargetUserId + ">", "");
                    }
                } else {
                    liveComment.setCommentColor(R.color.white);
                }
                if (msg.getNobleLevel() >= User.NOBLE_LEVEL_FOURTH) {
                    liveComment.setNameColor(R.color.noble_comment_nick_color);
                }
                if (msg.getNobleLevel() >= User.NOBLE_LEVEL_FIFTH) {
                    liveComment.setBackGround(R.drawable.live_bg_comment_noble);
                }
                liveComment.setBody(body, true);
            }
            break;
            //case BarrageMsgType.B_MSG_TYPE_VIP_LEVEL_CHANGED: {
            //    BarrageMsg.VipLevelChangedExt msgExt = (BarrageMsg.VipLevelChangedExt) msg.getMsgExt();
            //    String body = CommonUtils.getString(R.string.vip_level_upgrade_tip, name, msgExt.newVipLevel).toString();
            //    String schema = msg.getBody();
            //    MyLog.d(TAG, "vip level upgrade schema:" + schema);
            //    liveComment.setInnerGlobalRoomMessageSchemaUrl(schema);
            //    liveComment.setBody(body, false);
            //}
            //break;
            default: {
//                liveComment.setName(name + ": ");
                liveComment.setName(name);
                if (msg.isRedName()) {
                    liveComment.setCommentColor(R.color.ccccccc);
                    liveComment.setLevel(0);
                } else {
                    liveComment.setCommentColor(R.color.white);
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

        if (msg.getGlobalRoomMessageExt() != null) {
            BarrageMsg.GlobalRoomMessageExt globalRoomMessageExt = msg.getGlobalRoomMessageExt();
            if (globalRoomMessageExt != null) {
                //一个InnerGlobalRoomMessageExt仅有一个子类型,不然代码要改--现在需求
                List<BarrageMsg.InnerGlobalRoomMessageExt> innerGlobalRoomMessageExtList = globalRoomMessageExt.getInnerGlobalRoomMessageExtList();
                if (innerGlobalRoomMessageExtList != null && !innerGlobalRoomMessageExtList.isEmpty()) {
                    ArrayList<Integer> typeList = new ArrayList<>();
                    for (int i = 0; i < innerGlobalRoomMessageExtList.size(); i++) {
                        BarrageMsg.InnerGlobalRoomMessageExt innerGlobalRoomMessageExt = innerGlobalRoomMessageExtList.get(i);

                        if (innerGlobalRoomMessageExt != null) {
                            int type = innerGlobalRoomMessageExt.getType();
                            typeList.add(i, innerGlobalRoomMessageExt.getType());
                            if (type == BarrageMsg.INNER_GLOBAL_MEDAL_TYPE) {
                                BarrageMsgExt.MedalConfigMessage medalConfigMessage = innerGlobalRoomMessageExt.getMedalConfigMessage();

                                if (medalConfigMessage != null) {
                                    List<BarrageMsgExt.InnerMedalConfig> beforeNickNameCofigList = medalConfigMessage.getBeforeNickNameCofigList();
                                    setMedalList(beforeNickNameCofigList, BEFOER_NICKNAME_CONFIG_LIST_TYPE, liveComment);

                                    List<BarrageMsgExt.InnerMedalConfig> afterNickNameCofigList = medalConfigMessage.getAfterNickNameCofigList();
                                    setMedalList(afterNickNameCofigList, AFTER_NICKNAME_CONFIG_LIST_TYPE, liveComment);


                                    List<BarrageMsgExt.InnerMedalConfig> beforeContentCofigList = medalConfigMessage.getBeforeContentCofigList();
                                    setMedalList(beforeContentCofigList, BEFORE_CONTENT_CONFIG_LIST_TYPE, liveComment);

                                    List<BarrageMsgExt.InnerMedalConfig> afterContentCofigList = medalConfigMessage.getAfterContentCofigList();
                                    setMedalList(afterContentCofigList, AFTER_CONTENT_CONFIG_LIST_TYPE, liveComment);
                                    List<BarrageMsgExt.InnerMedalConfig> effectTemList = medalConfigMessage.getAfterContentCofigList();
                                    setMedalList(effectTemList, EFFECT_CONFIG_LIST_TYPE, liveComment);
                                }
                            } else if (type == BarrageMsg.INNER_GLOBAL_SCHEME_TYPE) {
                                BarrageMsg.TxtSchemeMessage txtSchemeMessage = innerGlobalRoomMessageExt.getTxtSchemeMessage();
                                if (txtSchemeMessage != null) {
                                    liveComment.setInnerGlobalRoomMessageSchemaUrl(txtSchemeMessage.getSchemeUrl());
                                }
                            } else if (type == BarrageMsg.INNER_GLOBAL_SHARE_JOIN_ROME_TYPE) {
                                BarrageMsg.ShareJoinRoomMessage shareJoinRoomMessage = innerGlobalRoomMessageExt.getShareJoinRoomMessage();
                                if (shareJoinRoomMessage != null && !TextUtils.isEmpty(shareJoinRoomMessage.getContent())) {
                                    liveComment.setBody(shareJoinRoomMessage.getContent(), false);
                                }
                            } else if (type == BarrageMsg.INNER_GLOBAL_VFAN) {
                                if (msg.getSender() != msg.getAnchorId()) {
                                    BarrageMsg.VFansMemberBriefInfo fansMemberBriefInfo = innerGlobalRoomMessageExt.getvFansMemberBriefInfo();

                                    if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_TEXT || msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_PAY_BARRAGE
                                            || msg.getMsgType() == MSG_SMART_NORMAL) {
                                        if (!fansMemberBriefInfo.isVipExpire() &&
                                                fansMemberBriefInfo.getPetLevel() >= VfansPrivilegeModel.SEND_COLOR_BARRAGE_VIP_LEVEL &&
                                                msg.getNobleLevel() <= 0) {
                                            liveComment.setBackGround(R.drawable.live_bg_comment_pink);
                                        }
                                        liveComment.setVfansLevel(fansMemberBriefInfo.getPetLevel());
                                        liveComment.setVfansMedal(fansMemberBriefInfo.getMedalValue());
                                    }


                                    if (msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_GIFT || msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_RED_ENVELOPE
                                            || msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_ROOM_BACKGROUND_GIFT || msg.getMsgType() == BarrageMsgType.B_MSG_TYPE_LIGHT_UP_GIFT) {
                                        liveComment.setVfansLevel(fansMemberBriefInfo.getPetLevel());
                                        liveComment.setVfansMedal(fansMemberBriefInfo.getMedalValue());
                                    }
                                }
                            }
                        }
                    }

                    liveComment.setInnerGlobalRoomMessageTypeList(typeList);
                }
            }
        }

        return liveComment;
    }

    private static String getWelcomeVipEnterRoomTip() {
        String[] tips = GlobalData.app().getResources().getStringArray(R.array.welcome_vip_enter_room_tip);
        return tips[(int) (System.currentTimeMillis() % tips.length)];
    }

    private static final int BEFOER_NICKNAME_CONFIG_LIST_TYPE = 1;
    private static final int AFTER_NICKNAME_CONFIG_LIST_TYPE = 2;
    private static final int BEFORE_CONTENT_CONFIG_LIST_TYPE = 3;
    private static final int AFTER_CONTENT_CONFIG_LIST_TYPE = 4;
    private static final int EFFECT_CONFIG_LIST_TYPE = 5;


    private static void setMedalList(List<BarrageMsgExt.InnerMedalConfig> list, int type, CommentModel liveComment) {
        if (list != null && !list.isEmpty()) {
            ArrayList<String> addList = new ArrayList<>();
            for (int index0 = 0; index0 < list.size(); index0++) {
                addList.add(list.get(index0).getPicId());
            }

            switch (type) {
                case BEFOER_NICKNAME_CONFIG_LIST_TYPE: {
                    liveComment.setBeforeNickNameConfigList(addList);
                }
                break;
                case AFTER_NICKNAME_CONFIG_LIST_TYPE: {
                    liveComment.setAfterNickNameConfigList(addList);
                }
                break;
                case BEFORE_CONTENT_CONFIG_LIST_TYPE: {
                    liveComment.setBeforeContentConfigList(addList);
                }
                break;
                case AFTER_CONTENT_CONFIG_LIST_TYPE: {
                    liveComment.setAfterContentConfigList(addList);
                }
                break;
                case EFFECT_CONFIG_LIST_TYPE: {
                    liveComment.setEffectConfigList(addList);
                }
                break;
            }
        }
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
