package com.wali.live.livesdk.live.component.utils;

import android.content.Context;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.wali.live.proto.LiveCommonProto;
import com.wali.live.proto.LiveProto;

/**
 * Created by yangli on 16-9-6.
 *
 * @Link http://wiki.n.miui.com/pages/diffpagesbyversion.action?pageId=25044158&selectedPageVersions=21&selectedPageVersions=22
 * @module 直播加按钮云端配置
 */
public class PlusParamUtils {
    private static final String TAG = "PlusParamUtils";

    private static final String KEY_PLUS_HIDE_LINK_MIC = "key_plus_hide_link_mic";       // 连麦
    private static final String KEY_PLUS_HIDE_MUSIC = "key_plus_hide_music";             // 音乐
    private static final String KEY_PLUS_HIDE_SHARE_PHOTO = "key_plus_hide_share_photo"; // 分享图片
    private static final String KEY_PLUS_HIDE_SHARE_VIDEO = "key_plus_hide_share_video"; // 分享视频
    private static final String KEY_PLUS_HIDE_ATMOSPHERE = "key_plus_hide_atmosphere";   // 氛围
    private static final String KEY_PLUS_HIDE_LINK_SCREEN = "key_plus_hide_link_screen"; // 屏幕共享
    private static final String KEY_PLUS_HIDE_LINK_DEVICE = "key_plus_hide_link_device"; // 连接外设
    private static final String KEY_PLUS_HIDE_LOTTERY = "key_plus_hide_lottery";         // 抽奖
    private static final String KEY_PLUS_HIDE_SHOPPING = "key_plus_hide_shopping";       // 购物
    private static final String KEY_PLUS_HIDE_EXPRESSION = "key_plus_hide_expression";   // 动态表情
    private static final String KEY_PLUS_HIDE_RED_PACKET = "key_plus_hide_red_packet";   // 红包

    public static boolean isHideAtmosphere() {
        return PreferenceUtils.getSettingBoolean(
                GlobalData.app(), KEY_PLUS_HIDE_ATMOSPHERE, false);
    }

    public static boolean isHideExpression(boolean defaultValue) {
        return PreferenceUtils.getSettingBoolean(
                GlobalData.app(), KEY_PLUS_HIDE_EXPRESSION, defaultValue);
    }

    public static void processIconConfig(LiveProto.GetRoomAttachmentRsp rsp) {
        if (rsp == null || !rsp.hasIconConfig()) {
            MyLog.w(TAG, "processIconConfig, but no IconConfig found");
            return;
        }
        Context context = GlobalData.app();
        LiveCommonProto.RoomIconConfig config = rsp.getIconConfig();
        PreferenceUtils.setSettingBoolean(context, KEY_PLUS_HIDE_LINK_MIC, config.hasNoMic() && config.getNoMic());
        PreferenceUtils.setSettingBoolean(context, KEY_PLUS_HIDE_SHARE_PHOTO, config.hasNoPic() && config.getNoPic());
        PreferenceUtils.setSettingBoolean(context, KEY_PLUS_HIDE_SHARE_VIDEO, config.hasNoVideo() && config.getNoVideo());
        PreferenceUtils.setSettingBoolean(context, KEY_PLUS_HIDE_LINK_SCREEN, config.hasNoScreenProjection() && config.getNoScreenProjection());
        PreferenceUtils.setSettingBoolean(context, KEY_PLUS_HIDE_ATMOSPHERE, config.hasNoAtmosphere() && config.getNoAtmosphere());
        PreferenceUtils.setSettingBoolean(context, KEY_PLUS_HIDE_MUSIC, config.hasNoMic() && config.getNoMusic());
        PreferenceUtils.setSettingBoolean(context, KEY_PLUS_HIDE_EXPRESSION, config.hasNoEmoticon() && config.getNoEmoticon());
        PreferenceUtils.setSettingBoolean(context, KEY_PLUS_HIDE_RED_PACKET, config.hasNoHongBao() && config.getNoHongBao());
    }

//    /**
//     * 获取直播加面板需要展示的按钮
//     */
//    public List<Integer>fetchAvailBtnSet(){
//        List<Integer>availSet=new ArrayList<>();
//        addIfNeed(availSet,KEY_PLUS_HIDE_LINK_MIC,PlusControlPanel.BTN_LINK_MIC,false);
//        addIfNeed(availSet,KEY_PLUS_HIDE_MUSIC,PlusControlPanel.BTN_MUSIC,true); // 音乐默认隐藏
//        addIfNeed(availSet,KEY_PLUS_HIDE_SHARE_PHOTO,PlusControlPanel.BTN_SHARE_PHOTO,false);
//        addIfNeed(availSet,KEY_PLUS_HIDE_SHARE_VIDEO,PlusControlPanel.BTN_SHARE_VIDEO,false);
//        addIfNeed(availSet,KEY_PLUS_HIDE_ATMOSPHERE,PlusControlPanel.BTN_ATMOSPHERE,false);
//        addIfNeed(availSet,PlusControlPanel.BTN_COMMENT,true); // 评论按钮总是可见
//        // addIfNeed(availSet, KEY_PLUS_HIDE_LINK_SCREEN, PlusControlPanel.BTN_LINK_SCREEN); // 屏幕共享暂时不用
//        addIfNeed(availSet,PlusControlPanel.BTN_LINK_DEVICE,GetConfigManager.getInstance().isLinkDeviceOn());
//        addIfNeed(availSet,KEY_PLUS_HIDE_REDPACKET,PlusControlPanel.BTN_REDENVELOP,false);
//        return availSet;
//    }

//    /**
//     * 获取导播台下直播加面板需要设置不可点击的按钮
//     */
//    public List<Integer>fetchDisableBtnSet(){
//        List<Integer>availSet=new ArrayList<>();
//        availSet.add(PlusControlPanel.BTN_LINK_MIC);
//        availSet.add(PlusControlPanel.BTN_MUSIC);
//        availSet.add(PlusControlPanel.BTN_SHARE_PHOTO);
//        availSet.add(PlusControlPanel.BTN_SHARE_VIDEO);
//        availSet.add(PlusControlPanel.BTN_ATMOSPHERE);
//        // availSet.add(PlusControlPanel.BTN_LINK_SCREEN);
//        availSet.add(PlusControlPanel.BTN_LINK_DEVICE);
//        return availSet;
//    }
}