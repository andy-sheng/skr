package com.wali.live.watchsdk.watch.presenter;

import android.text.TextUtils;
import android.util.Pair;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.HostChannelManager;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.data.user.User;
import com.wali.live.proto.ShareProto;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.ipc.service.MiLiveSdkBinder;
import com.wali.live.watchsdk.ipc.service.ShareInfo;

import java.util.HashMap;
import java.util.List;

public class SnsShareHelper {
    private static final String TAG = "SnsShareHelper";
    //TODO 统计各个途径分享的打点参数
    public static final String STATISTICS_MOMENT = "&pf=moment";
    public static final String STATISTICS_WECHAT = "&pf=wechat";
    public static final String STATISTICS_QQ = "&pf=qq";
    public static final String STATISTICS_QZONE = "&pf=qzone";
    public static final String STATISTICS_WEIBO = "&pf=weibo";
    public static final String STATISTICS_FACEBOOK = "&pf=fb";
    public static final String STATISTICS_TWITTER = "&pf=tw";
    public static final String STATISTICS_INSTAGRAM = "&pf=instagram";
    public static final String STATISTICS_WHATSAPP = "&pf=whatsapp";
    public static final String STATISTICS_MILIAO = "&pf=miliao";
    public static final String STATISTICS_MILIAO_FEEDS = "&pf=miliaowall";

    public static final String SHARE_MOMENT = "?pf=moment";
    public static final String SHARE_WECHAT = "?pf=wechat";
    public static final String SHARE_QQ = "?pf=qq";
    public static final String SHARE_QZONE = "?pf=qzone";
    public static final String SHARE_WEIBO = "?pf=weibo";
    public static final String SHARE_FACEBOOK = "?pf=fb";
    public static final String SHARE_TWITTER = "?pf=tw";
    public static final String SHARE_INSTAGRAM = "?pf=instagram";
    public static final String SHARE_WHATSAPP = "?pf=whatsapp";
    public static final String SHARE_MILIAO = "?pf=miliao";
    public static final String SHARE_MILIAO_FEEDS = "?pf=miliaowall";

    /**
     * 保持和ShareType一致的顺序
     */
    public static final int BTN_WECHAT = 0;
    public static final int BTN_WECHAT_MOMENT = 1;
    public static final int BTN_QQ = 2;
    public static final int BTN_QZONE = 3;
    public static final int BTN_WEIBO = 4;
    public static final int BTN_FACEBOOK = 5;
    public static final int BTN_TWITTER = 6;
    public static final int BTN_INSTAGRAM = 7;
    public static final int BTN_WHATSAPP = 8;
    public static final int BTN_MILIAO = 9;
    public static final int BTN_MILIAO_FEEDS = 10;

    public static final int SHARE_NICK_NAME_MAX_LENGTH = 16;
    private HashMap<ShareProto.ChannelType, ShareProto.TagTail> mTagTailMap = new HashMap<>();
    public static final int DEFAULT_TAIL_ABROAD = 40;
    public static final int DEFAULT_TAIL_DOMESTIC = 39;

    private static SnsShareHelper sSnsShareHelper = new SnsShareHelper();

    public static SnsShareHelper getInstance() {
        return sSnsShareHelper;
    }

    public void shareToSns(int type, RoomBaseDataModel shareData) {
        if (shareData == null || TextUtils.isEmpty(shareData.getShareUrl()) || TextUtils.isEmpty(shareData.getNickName())) {
            MyLog.w(TAG, "share params is error");
            return;
        }
        shareToSns(type, shareData.getShareUrl(), shareData.getCoverUrl(), shareData.getCity(), shareData.getLiveTitle(), shareData.getUser());
    }

    public void shareToSns(int type, String shareUrl, String imgUrl, String location, String liveTitle, User owner) {
        if (TextUtils.isEmpty(shareUrl) || owner == null || TextUtils.isEmpty(owner.getNickname())) {
            MyLog.w(TAG, "share params is error");
            return;
        }

        // 产品策略：分享主播昵称不超过16个字符，超过了用...代替,分享地点拿掉默认地点文案小米直播
        String ownerName = owner.getNickname();
        if (ownerName.length() > SHARE_NICK_NAME_MAX_LENGTH) {
            ownerName = ownerName.substring(0, SHARE_NICK_NAME_MAX_LENGTH) + "...";
        }
        if (location == null || GlobalData.app().getString(R.string.back_show_default_location).equals(location)) {
            location = "";
        }

        String shareTitle = String.format(GlobalData.app().getString(R.string.share_title), ownerName);
        String desText = "";
        String shareTagTailValue = "";
        String shareTagTailSeq = "";

        Pair<String, ShareProto.TagTail> pair = getTagTail(shareUrl, type);
        shareUrl = pair.first;
        ShareProto.TagTail tagTail = pair.second;

        boolean isChinese = CommonUtils.isChinese();
        if (tagTail != null) {
            shareTagTailSeq = "&tseq=" + tagTail.getSeq();
            shareTagTailValue = tagTail.getValue();
        }
        if (TextUtils.isEmpty(shareTagTailValue)) {
            shareTagTailValue = GlobalData.app().getString(R.string.share_description_default);
            shareTagTailSeq = "&tseq=" + (isChinese ? DEFAULT_TAIL_DOMESTIC : DEFAULT_TAIL_ABROAD);
        }

        boolean isVisitor = (owner.getUid() != UserAccountManager.getInstance().getUuidAsLong());
        if (!isChinese) {//国外
            if (type == BTN_TWITTER) {
                //国外app, twitter分享英文文案不加小尾巴，产品策略。
                shareTagTailSeq = "";
                shareTagTailValue = "";
            }
            desText = String.format(GlobalData.app().getString(isVisitor ? R.string.share_description_visitor_abroad : R.string.share_description_anchor_abroad),
                    ownerName, owner.getUid(), shareTagTailValue, "", "");
        } else {//国内
            desText = String.format(GlobalData.app().getString(isVisitor ? R.string.share_description_visitor : R.string.share_description_anchor),
                    ownerName, owner.getUid(), location, liveTitle, shareTagTailValue);
        }

        imgUrl = TextUtils.isEmpty(imgUrl) ? AvatarUtils.getAvatarUrlByUid(owner.getUid(), owner.getAvatar()) : imgUrl;
        shareUrl += (shareUrl.contains("?") ? shareTagTailSeq : "");

        //通知上层分享
        MiLiveSdkBinder.getInstance().onEventShare(HostChannelManager.getInstance().getChannelId(),
                new ShareInfo(type, shareTitle, desText, imgUrl, shareUrl));
        MyLog.w(TAG, "shareInfo=" + (new ShareInfo(type, shareTitle, desText, imgUrl, shareUrl).toString()));
    }

    public void setShareTagTailMap(List<ShareProto.TagTail> tagTailList) {
        if (tagTailList == null || tagTailList.size() <= 0) {
            return;
        }
        mTagTailMap.clear();
        for (ShareProto.TagTail tagTail : tagTailList) {
            mTagTailMap.put(tagTail.getChannel(), tagTail);
        }
    }

    public void clearShareTagTailMap() {
        mTagTailMap.clear();
    }

    private Pair<String, ShareProto.TagTail> getTagTail(String shareUrl, int type) {
        ShareProto.TagTail tagTail = null;
        boolean hasParam = shareUrl.contains("?");
        if (mTagTailMap.size() > 0) {
            switch (type) {
                case BTN_WECHAT:
                    tagTail = mTagTailMap.get(ShareProto.ChannelType.WEIXIN);
                    shareUrl += (hasParam ? STATISTICS_WECHAT : SHARE_WECHAT);
                    break;
                case BTN_WECHAT_MOMENT:
                    tagTail = mTagTailMap.get(ShareProto.ChannelType.WEIXIN_CIRCLE);
                    shareUrl += (hasParam ? STATISTICS_MOMENT : SHARE_MOMENT);
                    break;
                case BTN_QQ:
                    tagTail = mTagTailMap.get(ShareProto.ChannelType.QQ);
                    shareUrl += (hasParam ? STATISTICS_QQ : SHARE_QQ);
                    break;
                case BTN_QZONE:
                    tagTail = mTagTailMap.get(ShareProto.ChannelType.QZONE);
                    shareUrl += (hasParam ? STATISTICS_QZONE : SHARE_QZONE);
                    break;
                case BTN_WEIBO:
                    tagTail = mTagTailMap.get(ShareProto.ChannelType.WEIBO_SINA);
                    shareUrl += (hasParam ? STATISTICS_WEIBO : SHARE_WEIBO);
                    break;
                case BTN_FACEBOOK:
                    tagTail = mTagTailMap.get(ShareProto.ChannelType.FACEBOOK);
                    shareUrl += (hasParam ? STATISTICS_FACEBOOK : SHARE_FACEBOOK);
                    break;
                case BTN_TWITTER:
                    tagTail = mTagTailMap.get(ShareProto.ChannelType.TWITTER);
                    shareUrl += (hasParam ? STATISTICS_TWITTER : SHARE_TWITTER);
                    break;
                case BTN_INSTAGRAM:
                    tagTail = mTagTailMap.get(ShareProto.ChannelType.INSTAGRAM);
                    shareUrl += (hasParam ? STATISTICS_INSTAGRAM : SHARE_INSTAGRAM);
                    break;
                case BTN_WHATSAPP:
                    tagTail = mTagTailMap.get(ShareProto.ChannelType.WHATSAPP);
                    shareUrl += (hasParam ? STATISTICS_WHATSAPP : SHARE_WHATSAPP);
                    break;
                case BTN_MILIAO:
                    tagTail = mTagTailMap.get(ShareProto.ChannelType.MLDIALOG);
                    shareUrl += (hasParam ? STATISTICS_MILIAO : SHARE_MILIAO);
                    break;
                case BTN_MILIAO_FEEDS:
                    tagTail = mTagTailMap.get(ShareProto.ChannelType.MLBROADCAST);
                    shareUrl += (hasParam ? STATISTICS_MILIAO_FEEDS : SHARE_MILIAO_FEEDS);
                    break;
                default:
                    break;
            }
        }
        return new Pair<>(shareUrl, tagTail);
    }

    public static boolean isAppInstalled(int type) {
        switch (type) {
            case BTN_WECHAT:
            case BTN_WECHAT_MOMENT:
                if (!isWechatInstalled()) {
                    ToastUtils.showToast(GlobalData.app().getString(R.string.uninstall_share_tips, GlobalData.app().getString(R.string.weixin_friend)));
                    return false;
                }
                break;
            case BTN_QQ:
            case BTN_QZONE:
                if (!isQQInstalled()) {
                    ToastUtils.showToast(GlobalData.app().getString(R.string.uninstall_share_tips, GlobalData.app().getString(R.string.QQ)));
                    return false;
                }
                break;
            case BTN_WEIBO:
                if (!isWeiboInstalled()) {
                    ToastUtils.showToast(GlobalData.app().getString(R.string.uninstall_share_tips, GlobalData.app().getString(R.string.blog)));
                    return false;
                }
                break;
            case BTN_FACEBOOK:
                if (!isFacebookInstalled()) {
                    ToastUtils.showToast(GlobalData.app().getString(R.string.uninstall_share_tips, GlobalData.app().getString(R.string.facebook)));
                    return false;
                }
                break;
            case BTN_TWITTER:
                if (!isTwitterInstalled()) {
                    ToastUtils.showToast(GlobalData.app().getString(R.string.uninstall_share_tips, GlobalData.app().getString(R.string.twitter)));
                    return false;
                }
                break;
            case BTN_INSTAGRAM: {
                if (!isInstagramInstalled()) {
                    ToastUtils.showToast(GlobalData.app().getString(R.string.uninstall_share_tips, GlobalData.app().getString(R.string.instagram)));
                    return false;
                }
            }
            break;
            case BTN_WHATSAPP: {
                if (!isWhatsappInstalled()) {
                    ToastUtils.showToast(GlobalData.app().getString(R.string.uninstall_share_tips, GlobalData.app().getString(R.string.whatsapp)));
                    return false;
                }
            }
            break;
            case BTN_MILIAO:
            case BTN_MILIAO_FEEDS:
                if (!isMiliaoInstalled()) {
                    ToastUtils.showToast(GlobalData.app().getString(R.string.uninstall_share_tips, GlobalData.app().getString(R.string.miliao)));
                    return false;
                }
                break;
            default:
                return false;
        }
        return true;
    }

    public static boolean isWechatInstalled() {
        return CommonUtils.isAppInstalled(GlobalData.app(), "com.tencent.mm");
    }

    public static boolean isQQInstalled() {
        return CommonUtils.isAppInstalled(GlobalData.app(), "com.tencent.mobileqq") || CommonUtils.isAppInstalled(GlobalData.app(), "com.tencent.mobileqqi");
    }

    public static boolean isWeiboInstalled() {
        return CommonUtils.isAppInstalled(GlobalData.app(), "com.sina.weibo");
    }

    public static boolean isFacebookInstalled() {
        return CommonUtils.isAppInstalled(GlobalData.app(), "com.facebook.katana");
    }

    public static boolean isTwitterInstalled() {
        return CommonUtils.isAppInstalled(GlobalData.app(), "com.twitter.android");
    }

    public static boolean isInstagramInstalled() {
        return CommonUtils.isAppInstalled(GlobalData.app(), "com.instagram.android");
    }

    public static boolean isWhatsappInstalled() {
        return CommonUtils.isAppInstalled(GlobalData.app(), "com.whatsapp");
    }

    public static boolean isMiliaoInstalled() {
        return CommonUtils.isAppInstalled(GlobalData.app(), "com.xiaomi.channel");
    }
}
