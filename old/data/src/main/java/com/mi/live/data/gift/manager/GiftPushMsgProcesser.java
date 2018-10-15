package com.mi.live.data.gift.manager;

import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.mi.live.data.R;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.event.GiftEventClass;
import com.mi.live.data.event.HotSpotEvent;
import com.mi.live.data.gift.model.GiftRecvModel;
import com.mi.live.data.gift.model.GiftType;
import com.mi.live.data.gift.model.giftEntity.BigPackOfGift;
import com.mi.live.data.gift.redenvelope.RedEnvelopeModel;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.repository.GiftRepository;

import org.greenrobot.eventbus.EventBus;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by chengsimin on 16/6/16.
 */
public class GiftPushMsgProcesser {
    private static HashSet<String> mGiftSetForDistinct = new HashSet();

    private static LinkedList<String> mGiftQueueForDistinct = new LinkedList();

    private static final int DUPLICATE_CACHE_NUMBER = 100;
    public static final String TAG = "GiftPushMsgProcesser";

    /**
     * 用作礼物去重
     *
     * @param orderId
     * @return
     */
    private static synchronized boolean checkIsDuplicateGift(String orderId) {
        if (TextUtils.isEmpty(orderId)) {
            //为空放行
            return false;
        }
        if (mGiftSetForDistinct.contains(orderId)) {
            return true;
        } else {
            if (mGiftQueueForDistinct.size() > DUPLICATE_CACHE_NUMBER) {
                String old = mGiftQueueForDistinct.remove();
                mGiftSetForDistinct.remove(old);
            }
            mGiftQueueForDistinct.add(orderId);
            mGiftSetForDistinct.add(orderId);
            return false;
        }
    }

    // 将ext传进来，由外层保证是一个giftext
    //TODO 根据类型来选处理方式

    /**
     * 普通礼物,特效礼物，push处理方式的入口
     *
     * @param msg
     * @param ext
     * @param roomId
     */
    public static void processGiftPush(BarrageMsg msg, BarrageMsg.GiftMsgExt ext, String roomId) {
        if (msg != null) {
            // 扩展是礼物的消息类型
            MyLog.w(TAG, "msg:" + msg + ",ext:" + ext);
            GiftRecvModel model = GiftRecvModel.loadFromBarrage(msg, ext);
            MyLog.w(TAG, "model:" + model);
            processGiftMsg(model, false, msg);
        }
    }

    /**
     * 红包礼物,push处理方式的入口
     *
     * @param ext
     */
    public static void processRedEnvelopGiftPush(BarrageMsg.RedEnvelopMsgExt ext) {
        RedEnvelopeModel redEnvelopeModel = new RedEnvelopeModel();
        redEnvelopeModel.setRedEnvelopeId(ext.redEnvolopId);
        redEnvelopeModel.setUserId(ext.userId);
        redEnvelopeModel.setNickName(ext.nickName);
        redEnvelopeModel.setAvatarTimestamp(ext.avatar);
        redEnvelopeModel.setLevel(ext.level);
        redEnvelopeModel.setRoomId(ext.roomId);
        redEnvelopeModel.setMsg(ext.msg);
        redEnvelopeModel.setGemCnt(ext.gemCnt);
        redEnvelopeModel.setType(ext.type);
        EventBus.getDefault().post(new GiftEventClass.GiftAttrMessage.RedEnvelope(redEnvelopeModel));
    }

    /**
     * 实际礼物处理入口
     *
     * @param model
     */
    public static void processGiftMsg(GiftRecvModel model, boolean fromEnterRoom, BarrageMsg msg) {
        if (model == null){
            return;
        }
        //验证一下是否重复
        // 根据礼物类型填充消息类型
        int gifType = model.getGifType();
        //特权礼物和米币礼物originType是真是的giftType wiki地址：http://wiki.n.miui.com/pages/viewpage.action?pageId=33083961
        if (model.getGifType() == GiftType.PRIVILEGE_GIFT || model.getGifType() == GiftType.Mi_COIN_GIFT) {
            gifType = model.getGiftOriginType();
        }
        MyLog.w(TAG, "giftType=" + gifType);
        switch (gifType) {
            case GiftType.Mi_COIN_GIFT:
            case GiftType.NORMAL_GIFT: {
                EventBus.getDefault().post(new GiftEventClass.GiftAttrMessage.Normal(model));
            }
            break;
            case GiftType.NORMAL_EFFECTS_GIFT: {
                EventBus.getDefault().post(new GiftEventClass.GiftAttrMessage.Normal(model));
            }
            break;
            case GiftType.HIGH_VALUE_GIFT: {
                if (checkIsDuplicateGift(model.getOrderId())) {
                    //重复的不处理
                    return;
                }

                //上报大礼物热点
                EventBus.getDefault().post(new HotSpotEvent(HotSpotEvent.SPOT_TYPE_GIFT, "", model.getGiftId()));

                EventBus.getDefault().post(new GiftEventClass.GiftAttrMessage.Big(model));
            }
            break;
            case GiftType.GLOBAL_GIFT: {
                // 目前就是大金龙礼物
                // 这个代码逻辑是针对，产生大金龙的送礼房间的动画展示，不需要弹出小黄条推送
                // 其他房间或者直播外的只弹窗，不播动画
                if (msg != null) {
                    String giftName = model.getGiftName();
                    if (TextUtils.isEmpty(giftName)) {
                        // 怕了 以防万一
                        msg.setBody(GlobalData.app().getString(R.string.i_sent) + "神龙");
                    } else {
                        msg.setBody(GlobalData.app().getString(R.string.i_sent) + giftName);
                    }
                }
                if (checkIsDuplicateGift(model.getOrderId())) {
                    //重复的不处理
                    return;
                }
                if (fromEnterRoom && model.getUserId() == MyUserInfoManager.getInstance().getUser().getUid()) {
                    // 如果是进入房间且id=自己的话就不处理
                    return;
                }
                EventBus.getDefault().post(new GiftEventClass.GiftAttrMessage.Big(model));
            }
            break;
            case GiftType.ROOM_BACKGROUND_GIFT: {
                //对于这个类型的消息，如果是从进入房间时拉取的，不去重
                if (!fromEnterRoom && checkIsDuplicateGift(model.getOrderId())) {
                    //重复的不处理
                    return;
                }
                EventBus.getDefault().post(new GiftEventClass.GiftAttrMessage.RoomBackGround(model));
                if (!fromEnterRoom) {
                    //走次普通的礼物
                    EventBus.getDefault().post(new GiftEventClass.GiftAttrMessage.Normal(model));
                }
            }
            break;
            case GiftType.LIGHT_UP_GIFT: {
                //对于这个类型的消息，如果是从进入房间时拉取的，不去重
                if (!fromEnterRoom && checkIsDuplicateGift(model.getOrderId())) {
                    //重复的不处理
                    return;
                }
                EventBus.getDefault().post(new GiftEventClass.GiftAttrMessage.LightUp(model));
                if (!fromEnterRoom) {
                    //走次普通的礼物
                    EventBus.getDefault().post(new GiftEventClass.GiftAttrMessage.Normal(model));
                }
            }
            break;
            case GiftType.BIG_PACK_OF_GIFT: {
                //大礼包
                List<BarrageMsg> barrageMsgList = GiftRepository.getBigPackOfGiftBarrageMsgList((BigPackOfGift) model.getGift(), msg);
                if (barrageMsgList != null) {
                    MyLog.d(TAG, "GiftBigPackOfGift:other" + barrageMsgList.size());
                    for (BarrageMsg barrageMsg : barrageMsgList) {
                        BarrageMsg.GiftMsgExt ext = (BarrageMsg.GiftMsgExt) barrageMsg.getMsgExt();
                        GiftRepository.processGiftMsgByPushWay(barrageMsg, ext, barrageMsg.getRoomId());
                    }
                }
            }
            break;
            default: {
            }
            break;
        }
    }
}
