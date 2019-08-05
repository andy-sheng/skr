package com.common.core.share;

import android.app.Activity;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.TextView;

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
import com.umeng.socialize.media.UMusic;

public class SharePanel {
    public final String TAG = "SharePanel";

    DialogPlus mDialogPlus;

    Activity mActivity;
    public String mTitle;
    public String mDes;
    public String mPlayMusicUrl;
    public String mUrl;
    public String mShareImage;
    public String mDefaultIconUrl = "http://res-static.inframe.mobi/app/app_icon.webp";

    public SharePanel(Activity activity) {
        mActivity = activity;
    }

    public void setShareContent(String shareImage, String title, String des, String url) {
        mTitle = title;
        mDes = des;
        mUrl = url;
        mShareImage = shareImage;
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

    UMShareListener mOUMShareListener;

    public void setUMShareListener(UMShareListener umShareListener) {
        mOUMShareListener = umShareListener;
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

    public void dismiss() {
        if (mDialogPlus != null) {
            mDialogPlus.dismiss();
        }
    }

    public void dismiss(boolean useAnimation) {
        if (mDialogPlus != null) {
            mDialogPlus.dismiss(useAnimation);
        }
    }

    UMShareListener mUMShareListener = new UMShareListener() {
        @Override
        public void onStart(SHARE_MEDIA share_media) {
            if (mOUMShareListener != null) {
                mOUMShareListener.onStart(share_media);
            }
        }

        @Override
        public void onResult(SHARE_MEDIA share_media) {
            U.getToastUtil().showShort("分享成功");
            if (mOUMShareListener != null) {
                mOUMShareListener.onResult(share_media);
            }
        }

        @Override
        public void onError(SHARE_MEDIA share_media, Throwable throwable) {
            MyLog.e(TAG, throwable);
            if (mOUMShareListener != null) {
                mOUMShareListener.onError(share_media, throwable);
            }
            U.getToastUtil().showShort("分享失败, " + throwable.getMessage());
        }

        @Override
        public void onCancel(SHARE_MEDIA share_media) {
            if (share_media != SHARE_MEDIA.QQ) {
                U.getToastUtil().showShort("分享取消");
                if (mOUMShareListener != null) {
                    mOUMShareListener.onCancel(share_media);
                }
            }

        }
    };

    public void share(SharePlatform sharePlatform, ShareType type) {
        switch (type) {
            case URL:
                shareUrl(sharePlatform);
                break;
            case IMAGE_RUL:
                shareImageUrl(sharePlatform);
                break;
            case TEXT:
                shareText(sharePlatform);
                break;
            case MUSIC:
                shareMusic(sharePlatform);
                break;
        }

    }

    public void shareMusic(SharePlatform sharePlatform) {
        UMusic music = new UMusic(mPlayMusicUrl);
        music.setTitle("" + mTitle);
        music.setDescription(mDes);
        if (TextUtils.isEmpty(mShareImage)) {
            if (sharePlatform == SharePlatform.WEIXIN || sharePlatform == SharePlatform.WEIXIN_CIRCLE) {
                music.setThumb(new UMImage(mActivity, R.drawable.share_app_weixin_circle_icon));
            } else {
                music.setThumb(new UMImage(mActivity, mDefaultIconUrl));
            }
        } else {
            music.setThumb(new UMImage(mActivity, mShareImage));
        }
        music.setmTargetUrl(mUrl);

        switch (sharePlatform) {
            case WEIXIN:
                new ShareAction(mActivity).withMedia(music)
                        .setPlatform(SHARE_MEDIA.WEIXIN)
                        .setCallback(mUMShareListener).share();
                break;
            case QQ:
                new ShareAction(mActivity).withMedia(music)
                        .setPlatform(SHARE_MEDIA.QQ)
                        .setCallback(mUMShareListener).share();
                break;
            case WEIXIN_CIRCLE:
                new ShareAction(mActivity).withMedia(music)
                        .setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE)
                        .setCallback(mUMShareListener).share();
                break;
            case QZONE:
                new ShareAction(mActivity).withMedia(music)
                        .setPlatform(SHARE_MEDIA.QZONE)
                        .setCallback(mUMShareListener).share();
                break;
        }
    }

    private void shareText(SharePlatform sharePlatform) {
        StringBuilder sb = new StringBuilder();
        sb.append(mTitle).append("\n")
                .append(mUrl).append("\n")
                .append(mDes);

        switch (sharePlatform) {
            case WEIXIN:
                new ShareAction(mActivity).withText(sb.toString())
                        .setPlatform(SHARE_MEDIA.WEIXIN)
                        .setCallback(mUMShareListener).share();
                break;
            case QQ:
                new ShareAction(mActivity).withText(sb.toString())
                        .setPlatform(SHARE_MEDIA.QQ)
                        .setCallback(mUMShareListener).share();
                break;
            case WEIXIN_CIRCLE:
                new ShareAction(mActivity).withText(sb.toString())
                        .setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE)
                        .setCallback(mUMShareListener).share();
                break;
            case QZONE:
                new ShareAction(mActivity).withText(sb.toString())
                        .setPlatform(SHARE_MEDIA.QZONE)
                        .setCallback(mUMShareListener).share();
                break;
        }
    }

    public void shareUrl(SharePlatform sharePlatform) {
        UMWeb web = new UMWeb(mUrl);
        web.setTitle(mTitle);
        if (!TextUtils.isEmpty(mShareImage)) {
            web.setThumb(new UMImage(mActivity, mShareImage));
        } else {
            web.setThumb(new UMImage(mActivity, mDefaultIconUrl));
        }
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
            case QZONE:
                new ShareAction(mActivity).withMedia(web)
                        .setPlatform(SHARE_MEDIA.QZONE)
                        .setCallback(mUMShareListener).share();
                break;
        }
    }

    public void shareImageUrl(SharePlatform sharePlatform) {
        UMImage imageurl = new UMImage(mActivity, mUrl);
        imageurl.setThumb(new UMImage(mActivity, mUrl));
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
            case QZONE:
                new ShareAction(mActivity).withMedia(imageurl)
                        .setPlatform(SHARE_MEDIA.QZONE)
                        .setCallback(mUMShareListener).share();
                break;
        }
    }
}
