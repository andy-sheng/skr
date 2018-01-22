package com.wali.live.watchsdk.income.auth;

import android.content.Intent;

import com.base.activity.BaseActivity;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.wali.live.watchsdk.R;

public class WXOAuth {
    private static final String TAG = WXOAuth.class.getSimpleName();
    public static final String APP_ID = "wx0b1f5dd377f1cc6c"; //现在staging环境和线上用一个app_id
    public static final int REQUEST_CODE_WX_SHARE = 3001;
    private static final String PACKAGE_WX = "com.tencent.mm";

    public static final String WEIXIN_REQ_SCOPE = "snsapi_userinfo";
    public static final String WEIXIN_REQ_MAIN_LOGIN_STATE = "xiaomi_live_main_wx_login";
    public static final String WEIXIN_REQ_LOGIN_STATE = "xiaomi_live_wx_login";
    public static final String WEIXIN_REQ_FINDPWD_STATE = "xiaomi_live_wx_findpwd_login";
    public static final String WEIXIN_REQ_GUIDE_STATE = "xiaomi_live_wx_guide_login";

    private IWXAPI mWxApi;

    public WXOAuth() {
        registerToWx();
    }

    public void registerToWx() {
        MyLog.w(TAG, "registerToWx");
        mWxApi = WXAPIFactory.createWXAPI(GlobalData.app().getApplicationContext(), WXOAuth.APP_ID, true);
        mWxApi.registerApp(WXOAuth.APP_ID);
    }

    public void handleIntent(Intent intent, IWXAPIEventHandler handler) {
        MyLog.w(TAG, "handleIntent");
        mWxApi.handleIntent(intent, handler);
    }

    /*微信登陆*/
    public void oAuthByWeiXin(BaseActivity activity, String state) {
        MyLog.w(TAG, "oAuthByWeiXin");
        activity.showProgress(R.string.logining);
        if (!mWxApi.isWXAppInstalled()) {
            activity.hideProgress();
            ToastUtils.showToast(activity, R.string.install_weixin);
            MyLog.e(TAG, "weixin is not installed");
            return;
        }
        final SendAuth.Req req = new SendAuth.Req();
        req.scope = WEIXIN_REQ_SCOPE;
        req.state = state;
        boolean flag = mWxApi.sendReq(req);
        MyLog.e(TAG, "flag =" + flag);
    }

    //不知道下面的代码是干什么的，直播和sdk里都没有用到
//    /**********
//     * 微信分享
//     **************/
//    // friendsCircle: 0 分享给具体人 ,1 分享到朋友圈
//    // 分享文本信息到微信
//    public void shareTextToWeixin(String title, String text, String imgUrl, boolean friendsCircle) {
//        if (!mWxApi.isWXAppInstalled()) {
//            ToastUtils.showToast(GlobalData.app(), R.string.install_weixin);
//            EventController.onActionShare(EventClass.ShareEvent.EVENT_TYPE_SHARE_CANCEL);
//            MyLog.e(TAG, "weixin is not installed");
//            return;
//        }
//        WXTextObject textObj = new WXTextObject();
//        textObj.text = text;
//
//        WXMediaMessage msg = new WXMediaMessage();
//        msg.mediaObject = textObj;
//        msg.title = title;
//        msg.setThumbImage(CommonUtils.extractThumbNail(imgUrl, 150, 150, true));
//        msg.description = text;
//
//        SendMessageToWX.Req req = new SendMessageToWX.Req();
//        req.transaction = buildTransaction("text"); //transaction字段用于唯一标识一个请求
//        req.message = msg;
//        //WXSceneTimeline(朋友圈)；WXSceneSession(个人会话)
//        req.scene = friendsCircle ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
//        mWxApi.sendReq(req);
//    }
//
//    //分享图片到微信
//    public void shareImgToWeixin(String title, String desText, String imgUrl, boolean friendsCircle) {
//        if (!mWxApi.isWXAppInstalled()) {
//            ToastUtils.showToast(GlobalData.app(), R.string.install_weixin);
//            EventController.onActionShare(EventClass.ShareEvent.EVENT_TYPE_SHARE_CANCEL);
//            MyLog.e(TAG, "weixin is not installed");
//            return;
//        }
//        File file = new File(imgUrl);
//        if (!file.exists()) {
//            ToastUtils.showToast(GlobalData.app(), R.string.file_error);
//            return;
//        }
//        WXImageObject imgObj = new WXImageObject();
//        imgObj.setImagePath(imgUrl);
//        WXMediaMessage msg = new WXMediaMessage();
//        msg.mediaObject = imgObj;
//        msg.title = title;
//        msg.description = desText;
//        msg.setThumbImage(CommonUtils.extractThumbNail(imgUrl, 150, 150, true));
//
//        SendMessageToWX.Req req = new SendMessageToWX.Req();
//        req.transaction = buildTransaction("img");
//        req.message = msg;
//        req.scene = friendsCircle ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
//        mWxApi.sendReq(req);
//    }
//
//    //分享音乐到微信
//    public void shareSongToWeixin(String title, String desText, String imgUrl, String musicUrl, boolean friendsCircle) {
//        //判断是否安装微信
//        if (!mWxApi.isWXAppInstalled()) {
//            ToastUtils.showToast(GlobalData.app(), R.string.install_weixin);
//            EventController.onActionShare(EventClass.ShareEvent.EVENT_TYPE_SHARE_CANCEL);
//            MyLog.e(TAG, "weixin is not installed");
//            return;
//        }
//        WXMusicObject music = new WXMusicObject();
//        music.musicUrl = musicUrl;
//
//        WXMediaMessage msg = new WXMediaMessage();
//        msg.mediaObject = music;
//        msg.title = title;
//        msg.description = desText;
//        msg.setThumbImage(CommonUtils.extractThumbNail(imgUrl, 150, 150, true));
//
//        SendMessageToWX.Req req = new SendMessageToWX.Req();
//        req.transaction = buildTransaction("music");
//        req.message = msg;
//        req.scene = friendsCircle ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
//        mWxApi.sendReq(req);
//    }
//
//    // 分享视频信息到微信
//    public void shareVideoToWeixin(String title, String desText, String imgUrl, String videoUrl, boolean friendsCircle) {
//        //判断是否安装微信
//        if (!mWxApi.isWXAppInstalled()) {
//            ToastUtils.showToast(GlobalData.app(), R.string.install_weixin);
//            EventController.onActionShare(EventClass.ShareEvent.EVENT_TYPE_SHARE_CANCEL);
//            MyLog.e(TAG, "weixin is not installed");
//            return;
//        }
//        WXVideoObject video = new WXVideoObject();
//        video.videoUrl = videoUrl;
//
//        WXMediaMessage msg = new WXMediaMessage(video);
//        msg.title = title;
//        msg.description = desText;
//        msg.setThumbImage(CommonUtils.extractThumbNail(imgUrl, 150, 150, true));
//
//        SendMessageToWX.Req req = new SendMessageToWX.Req();
//        req.transaction = buildTransaction("video");
//        req.message = msg;
//        req.scene = friendsCircle ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
//        mWxApi.sendReq(req);
//    }
//
//    // 分享app消息到weixin
//    public void shareAppDataToWeixin(String title, String desText, String imgUrl, boolean friendsCircle) {
//        if (!mWxApi.isWXAppInstalled()) {
//            ToastUtils.showToast(GlobalData.app(), R.string.install_weixin);
//            EventController.onActionShare(EventClass.ShareEvent.EVENT_TYPE_SHARE_CANCEL);
//            MyLog.e(TAG, "weixin is not installed");
//            return;
//        }
//        final WXAppExtendObject appdata = new WXAppExtendObject();
//        appdata.fileData = CommonUtils.readFromFile(imgUrl, 0, -1);
//        appdata.extInfo = "this is ext info";
//
//        final WXMediaMessage msg = new WXMediaMessage();
//        msg.setThumbImage(CommonUtils.extractThumbNail(imgUrl, 150, 150, true));
//        msg.title = title;
//        msg.description = desText;
//        msg.mediaObject = appdata;
//
//        SendMessageToWX.Req req = new SendMessageToWX.Req();
//        req.transaction = buildTransaction("appdata");
//        req.message = msg;
//        req.scene = friendsCircle ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
//        mWxApi.sendReq(req);
//    }
//
//    public boolean isWXAppInstalled() {
//        return mWxApi.isWXAppInstalled();
//    }
//
//
//    // 分享网页消息到weixin
//    public void shareWebDataByLocalImgToWeixin(String webUrl, String title, String desText, String imgUrl, boolean friendsCircle) {
//        MyLog.w(TAG, "shareWebDataByLocalImgToWeixin");
//        if (!mWxApi.isWXAppInstalled()) {
//            ToastUtils.showToast(GlobalData.app(), R.string.install_weixin);
//            EventController.onActionShare(EventClass.ShareEvent.EVENT_TYPE_SHARE_CANCEL);
//            MyLog.e(TAG, "weixin is not installed");
//        }
//        WXWebpageObject webpage = new WXWebpageObject();
//        webpage.webpageUrl = webUrl;
//        WXMediaMessage msg = new WXMediaMessage(webpage);
//        Bitmap thumb = null;
//        if (!TextUtils.isEmpty(imgUrl)) {
//            thumb = CommonUtils.extractThumbNail(imgUrl, 150, 150, true);
//        }
//        if (thumb == null) {
//            thumb = BitmapFactory.decodeResource(GlobalData.app().getResources(), R.mipmap.ic_launcher_live);
//        }
//        msg.setThumbImage(thumb);
//        if (friendsCircle) {
//            msg.title = desText;//产品要求分享朋友圈的title上显示描述内容 LIVEAND-10934
//            msg.description = title;
//        } else {
//            msg.title = title;
//            msg.description = desText;
//        }
//
//        SendMessageToWX.Req req = new SendMessageToWX.Req();
//        req.transaction = buildTransaction("webpage");
//        req.message = msg;
//        req.scene = friendsCircle ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
//        mWxApi.sendReq(req);
//    }
//
//    // 分享网页消息到weixin
//    public void shareWebDataByWebImgToWeixin(String webUrl, String title, String desText, String imgUrl, boolean friendsCircle) {
//        //判断是否安装微信
//        if (!mWxApi.isWXAppInstalled()) {
//            ToastUtils.showToast(GlobalData.app(), R.string.install_weixin);
//            MyLog.e(TAG, "weixin is not installed");
//            EventController.onActionShare(EventClass.ShareEvent.EVENT_TYPE_SHARE_CANCEL);
//            return;
//        }
//        WXWebpageObject webpage = new WXWebpageObject();
//        webpage.webpageUrl = webUrl;
//        WXMediaMessage msg = new WXMediaMessage(webpage);
//        msg.setThumbImage(CommonUtils.getBitMapByWebUrl(imgUrl));
//        msg.title = title;
//        msg.description = desText;
//
//        SendMessageToWX.Req req = new SendMessageToWX.Req();
//        req.transaction = buildTransaction("webpage");
//        req.message = msg;
//        req.scene = friendsCircle ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
//        mWxApi.sendReq(req);
//    }
//
//
//    public void shareWebDataByResImgToWeixin(String webUrl, String title, String desText, int res, boolean friendsCircle) {
//        //判断是否安装微信
//        if (!mWxApi.isWXAppInstalled()) {
//            ToastUtils.showToast(GlobalData.app(), R.string.install_weixin);
//            MyLog.e(TAG, "weixin is not installed");
//            EventController.onActionShare(EventClass.ShareEvent.EVENT_TYPE_SHARE_CANCEL);
//            return;
//        }
//        WXWebpageObject webpage = new WXWebpageObject();
//        webpage.webpageUrl = webUrl;
//        WXMediaMessage msg = new WXMediaMessage(webpage);
//        msg.title = title;
//        msg.description = desText;
//        Bitmap thumb = BitmapFactory.decodeResource(GlobalData.app().getResources(), res);
////        msg.thumbData = Util.bmpToByteArray(thumb, true); //TODO 新包没有bmpToByteArray函数
//        msg.setThumbImage(thumb);
//
//        SendMessageToWX.Req req = new SendMessageToWX.Req();
//        req.transaction = buildTransaction("webpage");
//        req.message = msg;
//        req.scene = friendsCircle ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
//        mWxApi.sendReq(req);
//    }
//
//    private String buildTransaction(final String type) {
//        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
//    }
//
//    //分享图片image type :image/* ;  分享视频 video mp3 type:video/*
//    //纯本地图分享
//    public Intent shareLocalImageIntent(String localImagePath) {
//        if (!TextUtils.isEmpty(localImagePath)) {
//            Intent share = new Intent(Intent.ACTION_SEND);
//            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
//                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//            share.setType("image/*");
//            share.putExtra("return-data", true);
//            Uri uri = Uri.fromFile(new File(localImagePath));
//            share.putExtra(Intent.EXTRA_STREAM, uri);
//            share.setPackage(PACKAGE_WX);
//            return share;
//        }
//        ToastUtils.showToast(GlobalData.app().getResources().getString(R.string.share_failed));
//        return null;
//    }
//
//    public void shareLocalImage(String localImgPath) {
//        Bitmap bmp = getBitmapForLocalPath(localImgPath);
//        if (bmp == null) {
//            ToastUtils.showToast(GlobalData.app().getResources().getString(R.string.share_failed));
//            return;
//        }
//        WXImageObject imgObj = new WXImageObject(bmp);
//
//        WXMediaMessage msg = new WXMediaMessage();
//        msg.mediaObject = imgObj;
//
//        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
//        bmp.recycle();
//        msg.setThumbImage(thumbBmp); // 设置缩略图
//
//        SendMessageToWX.Req req = new SendMessageToWX.Req();
//        req.transaction = buildTransaction("img");
//        req.message = msg;
//        req.scene = SendMessageToWX.Req.WXSceneSession;
//
//        mWxApi.sendReq(req);
//
//    }
//
//    public Bitmap getBitmapForLocalPath(String localImgPath) {
//        FileInputStream fis = null;
//        Bitmap bitmap = null;
//        try {
//            fis = new FileInputStream(localImgPath);
//            bitmap = BitmapFactory.decodeStream(fis);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        return bitmap;
//    }
//
//    /**
//     * 分享图片到微信朋友圈
//     **/
//    public void shareImageByIntent(Activity activity, String title, String txt, String imagePath) {
//        if (activity == null) {
//            return;
//        }
//        Intent intent = new Intent(Intent.ACTION_SEND);
//        intent.setType("image/*");
//        ComponentName componentName = new ComponentName("com.tencent.mm",
//                "com.tencent.mm.ui.tools.ShaeqToTimeLineUI");
//        intent.setComponent(componentName);
//        intent.putExtra(Intent.EXTRA_TEXT, txt);//正常使用的文字
//        intent.putExtra(Intent.EXTRA_SUBJECT, title);
////        intent.putExtra("sms_body", txt);//短信时使用
//        intent.putExtra("Kdescription", txt);//微信朋友圈专用
//        Uri image = Uri.fromFile(new File(imagePath));
//        intent.putExtra(Intent.EXTRA_STREAM, image);
//        activity.startActivity(intent);
//    }
}
