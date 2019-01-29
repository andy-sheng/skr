package com.common.core.share;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;

import com.common.core.R;
import com.common.log.MyLog;
import com.common.utils.U;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

public class SharePanel {
    public final static String TAG = "SharePanel";

    DialogPlus mDialogPlus;

    Activity mActivity;
    String mTitle;
    String mDes;
    String mUrl;

    public SharePanel(Activity activity) {
        mActivity = activity;
    }

    public void setShareContent(String title, String des, String url) {
        mTitle = title;
        mDes = des;
        mUrl = url;
    }

    public void setShareContent(String url) {
        mUrl = url;
    }

    /**
     * 留着以后用，现在写死
     *
     * @param platformList
     */
    public void setPlatformList(SharePlatform... platformList) {

    }

    public void show(final ShareType shareType) {
        ShareView shareView = new ShareView(mActivity);
        shareView.setOnClickShareListener(new ShareView.OnClickShareListener() {
            @Override
            public void click(SharePlatform sharePlatform) {
                mDialogPlus.dismiss();
                share(sharePlatform, shareType);
            }
        });

        mDialogPlus = DialogPlus.newDialog(mActivity)
                .setContentHolder(new ViewHolder(shareView))
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_80)
                .setGravity(Gravity.BOTTOM)
                .create();

        mDialogPlus.show();
    }

    UMShareListener mUMShareListener = new UMShareListener() {
        @Override
        public void onStart(SHARE_MEDIA share_media) {

        }

        @Override
        public void onResult(SHARE_MEDIA share_media) {
            U.getToastUtil().showShort("分享成功");
        }

        @Override
        public void onError(SHARE_MEDIA share_media, Throwable throwable) {
            MyLog.e(TAG, throwable);
            U.getToastUtil().showShort("分享失败");
        }

        @Override
        public void onCancel(SHARE_MEDIA share_media) {
            U.getToastUtil().showShort("分享取消");
        }
    };

    public void share(SharePlatform sharePlatform, ShareType type) {
        switch (type){
            case URL:
                shareUrl(sharePlatform);
                break;
            case IMAGE_RUL:
                shareImageUrl(sharePlatform);
                break;
        }

    }

    public void shareUrl(SharePlatform sharePlatform){
        UMWeb web = new UMWeb(mUrl);
        web.setTitle(mTitle);
        web.setThumb(new UMImage(mActivity, R.drawable.share_app_icon));
        web.setDescription(mDes);

        switch (sharePlatform) {
            case WEIXIN:
                new ShareAction(mActivity).withMedia(web)
                        .setPlatform(SHARE_MEDIA.WEIXIN)
                        .setCallback(mUMShareListener).share();
                break;
            case QQ:
                new ShareAction(mActivity).withMedia(web)
                        .setPlatform(SHARE_MEDIA.QQ)
                        .setCallback(mUMShareListener).share();
                break;
            case WEIXIN_CIRCLE:
                new ShareAction(mActivity).withMedia(web)
                        .setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE)
                        .setCallback(mUMShareListener).share();
                break;
        }
    }

    public void shareImageUrl(SharePlatform sharePlatform){
        UMImage imageurl = new UMImage(mActivity, mUrl);
        imageurl.setThumb(new UMImage(mActivity , mUrl));
        switch (sharePlatform) {
            case WEIXIN:
                new ShareAction(mActivity).withMedia(imageurl)
                        .setPlatform(SHARE_MEDIA.WEIXIN)
                        .setCallback(mUMShareListener).share();
                break;
            case QQ:
                new ShareAction(mActivity).withMedia(imageurl)
                        .setPlatform(SHARE_MEDIA.QQ)
                        .setCallback(mUMShareListener).share();
                break;
            case WEIXIN_CIRCLE:
                new ShareAction(mActivity).withMedia(imageurl)
                        .setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE)
                        .setCallback(mUMShareListener).share();
                break;
        }
    }
}
