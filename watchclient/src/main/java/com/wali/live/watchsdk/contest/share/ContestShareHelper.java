package com.wali.live.watchsdk.contest.share;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Environment;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.MD5;
import com.base.utils.image.ImageUtils;
import com.mi.live.data.push.model.contest.LastQuestionInfoModel;
import com.wali.live.watchsdk.R;

import java.io.File;

/**
 * Created by zyh on 2018/1/13.
 * <p>
 * 分享所需要的图的生成 以及分享接口
 */
public class ContestShareHelper {
    private static String TAG = "ContestShareHelper";
    private static final String share_pic = Environment.getExternalStorageDirectory() + "/Xiaomi/WALI_LIVE/pic/";

    private static String CONTEST_SHARE_INVITE_SUFFIX = "contest_invite_img_";
    private static String CONTEST_SHARE_WIN_SUFFIX = "contest_win_img";
    private static ContestShareHelper sInstance;
    private QQOAuth mQQOAuth;
    private WXOAuth mWXOAuth;

    public static ContestShareHelper getInstance() {
        if (sInstance == null) {
            sInstance = new ContestShareHelper();
        }
        return sInstance;
    }

    public static class ShareBitmapHolder {
        private View mView;
        private TextView mShareTipTv;
        private TextView mInviteCodeTv;
        private ImageView mBarcodeIv;
        private TextView mDownloadTv;

        public ShareBitmapHolder(View view) {
            mView = view;
            mShareTipTv = (TextView) mView.findViewById(R.id.share_tip_tv);
            mInviteCodeTv = (TextView) mView.findViewById(R.id.invite_code_tv);
            mBarcodeIv = (ImageView) mView.findViewById(R.id.barcode_iv);
            mDownloadTv = (TextView) mView.findViewById(R.id.download_tip_tv);
        }
    }

    private static Bitmap generateInvitePicInMainThread(String inviteCode) {
        MyLog.w(TAG, "generateInvitePicInMainThread inviteCode=" + inviteCode);
        LinearLayout sharePage = (LinearLayout) LayoutInflater.from(GlobalData.app()).inflate(R.layout.contest_share_holder, null);
        ShareBitmapHolder holder = new ShareBitmapHolder(sharePage);
        holder.mInviteCodeTv.setText(inviteCode);
        Spanned spanned = Html.fromHtml(GlobalData.app().getString(R.string.share_download_tips));
        holder.mDownloadTv.setText(spanned);
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(getQrCodeFilePath(true));
            MyLog.w(TAG, "barCode url=" + getQrCodeFilePath(true));
            if (bitmap == null) {
                MyLog.w(TAG, "barCode bitmap is null");
            } else {
                holder.mBarcodeIv.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
        sharePage.measure(View.MeasureSpec.makeMeasureSpec(GlobalData.screenWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(GlobalData.screenHeight, View.MeasureSpec.UNSPECIFIED));
        int width = sharePage.getMeasuredWidth(), height = sharePage.getMeasuredHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        sharePage.layout(0, 0, width, height);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.GREEN);
        sharePage.draw(canvas);
        return bitmap;
    }

    public static String saveContestInviteSharePic(String inviteCode) {
        try {
//            File hasExistsFile = AttachmentUtils.getFileByUrl(CONTEST_SHARE_INVITE_SUFFIX + inviteCode);
//            if (hasExistsFile != null && hasExistsFile.exists()) {
//                return hasExistsFile.getAbsolutePath();
//            }
            String fileName = MD5.MD5_16(CONTEST_SHARE_INVITE_SUFFIX + inviteCode);
            File file = new File(share_pic, fileName + ".JPEG");
            Bitmap bitmap;
            try {
                bitmap = generateInvitePicInMainThread(inviteCode);
            } catch (OutOfMemoryError error) {
                MyLog.e(error);
                return "";
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            ImageUtils.saveBmpToFile(bitmap, file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (Exception e) {
            MyLog.e(e);
        }
        return "";
    }

    public static class ShareWinBitmapHolder {
        private View mView;
        private TextView mWinTitleTv;
        private TextView mInviteCodeTv;
        private ImageView mBarcodeIv;
        private TextView mDownloadTv;

        public ShareWinBitmapHolder(View view) {
            mView = view;
            mWinTitleTv = (TextView) mView.findViewById(R.id.win_title_tv);
            mInviteCodeTv = (TextView) mView.findViewById(R.id.invite_code_tv);
            mBarcodeIv = (ImageView) mView.findViewById(R.id.barcode_iv);
            mDownloadTv = (TextView) mView.findViewById(R.id.download_tip_tv);
        }
    }

    private static Bitmap generateWinPicInMainThread(long zuid, long avatarTs, String nickName,
                                                     String inviteCode, LastQuestionInfoModel model) {
        MyLog.w(TAG, "generateWinPicInMainThread inviteCode=" + inviteCode);
        if (model == null) {
            MyLog.w(TAG, "generateWinPicInMainThread model is null");
            return null;
        }
        LinearLayout sharePage = (LinearLayout) LayoutInflater.from(GlobalData.app()).inflate(R.layout.contest_win_share_holder, null);
        ShareWinBitmapHolder holder = new ShareWinBitmapHolder(sharePage);
        Spanned myBonus = Html.fromHtml(GlobalData.app().getString(R.string.share_win_title, String.valueOf(model.getWinNum())));
        holder.mWinTitleTv.setText(myBonus);
        holder.mInviteCodeTv.setText(inviteCode);
        Spanned spanned = Html.fromHtml(GlobalData.app().getString(R.string.share_download_tips));
        holder.mDownloadTv.setText(spanned);
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(getQrCodeFilePath(true));
            MyLog.w(TAG, "barCode url=" + getQrCodeFilePath(true));
            if (bitmap == null) {
                MyLog.w(TAG, "barCode bitmap is null");
            } else {
                holder.mBarcodeIv.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
        sharePage.measure(View.MeasureSpec.makeMeasureSpec(GlobalData.screenWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(GlobalData.screenHeight, View.MeasureSpec.UNSPECIFIED));
        int width = sharePage.getMeasuredWidth(), height = sharePage.getMeasuredHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        sharePage.layout(0, 0, width, height);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.GREEN);
        sharePage.draw(canvas);
        return bitmap;
    }

    public static String saveContestWinSharePic(long zuid, long avatarTs, String nickName,
                                                String inviteCode, LastQuestionInfoModel lastQuestionInfoModel) {
        try {
//            File hasExistsFile = AttachmentUtils.getFileByUrl(CONTEST_SHARE_WIN_SUFFIX + inviteCode);
//            if (hasExistsFile != null && hasExistsFile.exists()) {
//                return hasExistsFile.getAbsolutePath();
//            }
            String fileName = MD5.MD5_16(CONTEST_SHARE_WIN_SUFFIX + inviteCode);
            File file = new File(share_pic, fileName + ".JPEG");
            Bitmap bitmap;
            try {
                bitmap = generateWinPicInMainThread(zuid, avatarTs, nickName, inviteCode, lastQuestionInfoModel);
            } catch (OutOfMemoryError error) {
                MyLog.e(error);
                return "";
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            ImageUtils.saveBmpToFile(bitmap, file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (Exception e) {
            MyLog.e(e);
        }
        return "";
    }

    public void shareLocalPicToQQ(Activity activity, String imgLocalPath) {
        if (mQQOAuth == null) {
            mQQOAuth = new QQOAuth();
        }
        mQQOAuth.shareImgToQQ(activity, imgLocalPath, true);
    }

    public void shareLocalPicToQzone(Activity activity, String imgLocalPath) {
        if (mQQOAuth == null) {
            mQQOAuth = new QQOAuth();
        }
        mQQOAuth.shareImgToQQ(activity, imgLocalPath, false);
    }

    public void shareLocalPicToWechat(String imgLocalPath) {
        if (mWXOAuth == null) {
            mWXOAuth = new WXOAuth();
        }
        mWXOAuth.shareImgToWeixin("", "", imgLocalPath, false);
    }

    public void shareLocalPicToMoment(String imgLocalPath) {
        if (mWXOAuth == null) {
            mWXOAuth = new WXOAuth();
        }
        mWXOAuth.shareImgToWeixin("", "", imgLocalPath, true);
    }
}

