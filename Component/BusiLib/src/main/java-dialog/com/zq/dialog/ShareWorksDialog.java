package com.zq.dialog;

import android.text.TextUtils;
import android.view.Gravity;

import com.common.base.BaseFragment;
import com.common.core.share.SharePlatform;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.utils.U;
import com.component.busilib.R;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;
import com.umeng.socialize.media.UMusic;

public class ShareWorksDialog {

    public final String TAG = "ShareWorksDialog";

    DialogPlus mShareDialog;

    BaseFragment mFragment;
    String mUrl;
    int mWorksId;
    int mUserId;
    String mSongName;
    String mNickName;
    String mCover;


    public ShareWorksDialog(BaseFragment fragment, String songName, boolean containSaveTips) {
        this.mFragment = fragment;

        ShareWorksDialogView shareWorksDialogView = new ShareWorksDialogView(fragment.getContext(), songName, containSaveTips
                , new ShareWorksDialogView.Listener() {
            @Override
            public void onClickQQShare() {
                if (!U.getCommonUtils().hasInstallApp("com.tencent.mobileqq")) {
                    U.getToastUtil().showShort("未安装QQ");
                    return;
                }
                if (mShareDialog != null) {
                    mShareDialog.dismiss();
                }
                shareUrl(SharePlatform.QQ);
            }

            @Override
            public void onClickQZoneShare() {
                if (!U.getCommonUtils().hasInstallApp("com.tencent.mobileqq")) {
                    U.getToastUtil().showShort("未安装QQ");
                    return;
                }
                if (mShareDialog != null) {
                    mShareDialog.dismiss();
                }
                shareUrl(SharePlatform.QZONE);
            }

            @Override
            public void onClickWeixinShare() {
                if (!U.getCommonUtils().hasInstallApp("com.tencent.mm")) {
                    U.getToastUtil().showShort("未安装微信");
                    return;
                }
                if (mShareDialog != null) {
                    mShareDialog.dismiss();
                }
                shareUrl(SharePlatform.WEIXIN);
            }

            @Override
            public void onClickQuanShare() {
                if (!U.getCommonUtils().hasInstallApp("com.tencent.mm")) {
                    U.getToastUtil().showShort("未安装微信");
                    return;
                }
                if (mShareDialog != null) {
                    mShareDialog.dismiss();
                }
                shareUrl(SharePlatform.WEIXIN_CIRCLE);
            }
        });

        mShareDialog = DialogPlus.newDialog(fragment.getContext())
                .setContentHolder(new ViewHolder(shareWorksDialogView))
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_50)
                .setExpanded(false)
                .setGravity(Gravity.BOTTOM)
                .create();
    }

    public void setData(int mUserId, String mNickName, String mCover, String mSongName, String mUrl, int mWorksId) {
        this.mUserId = mUserId;
        this.mNickName = mNickName;
        this.mCover = mCover;
        this.mSongName = mSongName;
        this.mUrl = mUrl;
        this.mWorksId = mWorksId;
    }

    private void shareUrl(SharePlatform sharePlatform) {
        if (!TextUtils.isEmpty(mUrl) && mWorksId > 0) {
            UMusic music = new UMusic(mUrl);
            music.setTitle("" + mSongName);
            music.setDescription(mNickName + "的撕歌精彩时刻");
            music.setThumb(new UMImage(mFragment.getActivity(), mCover));

            StringBuilder sb = new StringBuilder();
            sb.append("http://www.skrer.mobi/user/work")
                    .append("?skerId=").append(String.valueOf(mUserId))
                    .append("&workId=").append(String.valueOf(mWorksId));
            String mUrl = sb.toString();
            // TODO: 2019/5/22 微信分享不成功的原因可能是mUrl未上线，微信会检测这个
            music.setmTargetUrl(mUrl);

            switch (sharePlatform) {
                case QQ:
                    new ShareAction(mFragment.getActivity()).withMedia(music)
                            .setPlatform(SHARE_MEDIA.QQ)
                            .share();
                    break;
                case QZONE:
                    new ShareAction(mFragment.getActivity()).withMedia(music)
                            .setPlatform(SHARE_MEDIA.QZONE)
                            .share();
                    break;
                case WEIXIN:
                    new ShareAction(mFragment.getActivity()).withMedia(music)
                            .setPlatform(SHARE_MEDIA.WEIXIN)
                            .share();
                    break;

                case WEIXIN_CIRCLE:
                    new ShareAction(mFragment.getActivity()).withMedia(music)
                            .setPlatform(SHARE_MEDIA.WEIXIN_CIRCLE)
                            .share();
                    break;
            }
        } else {
            MyLog.w(TAG, "shareUrl" + " sharePlatform=" + sharePlatform);
        }
    }

    public void dismiss() {
        if (mShareDialog != null) {
            mShareDialog.dismiss();
        }
    }

    public void dismiss(boolean useAnimation) {
        if (mShareDialog != null) {
            mShareDialog.dismiss(useAnimation);
        }
    }

    public void show() {
        if (mShareDialog != null) {
            mShareDialog.show();
        }
    }
}
