package share;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.utils.MD5;
import com.base.utils.image.ImageUtils;
import com.base.utils.version.VersionManager;
import com.mi.live.data.preference.PreferenceKeys;
import com.wali.live.watchsdk.R;
import util.QrcodeUtils;

import java.io.File;

/**
 * Created by zyh on 2018/1/13.
 * <p>
 * 分享所需要的图的生成 以及分享接口
 */
public class ContestShareHelper {
    private static String TAG = "ContestShareHelper";
    private final static int QR_IMAGE_HEIGHT = 210;
    private final static int QR_IMAGE_WIDTH = 210;

    private static final String share_pic = Environment.getExternalStorageDirectory() + "/Xiaomi/WALI_LIVE/pic/";

    private static String CONTEST_SHARE_INVITE_SUFFIX = "contest_invite_img_";
    private static String CONTEST_SHARE_WIN_SUFFIX = "contest_win_img";
    private static ContestShareHelper sInstance;

    public static ContestShareHelper getInstance() {
        if (sInstance == null) {
            sInstance = new ContestShareHelper();
        }
        return sInstance;
    }

    public static class ShareBitmapHolder {
        private View mView;
        private TextView mInviteCodeTv;
        private ImageView mBarcodeIv;
        private TextView mDownloadTv;

        public ShareBitmapHolder(View view) {
            mView = view;
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
            File dir = new File(share_pic);
            if (!dir.exists()) {
                try {
                    dir.mkdirs();
                } catch (Exception e) {
                    MyLog.e(e);
                }
            }
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
        private TextView mMyBonusTv;
        private TextView mInviteCodeTv;
        private ImageView mBarcodeIv;
        private TextView mDownloadTv;

        public ShareWinBitmapHolder(View view) {
            mView = view;
            mWinTitleTv = (TextView) mView.findViewById(R.id.win_title_tv);
            mMyBonusTv = (TextView) mView.findViewById(R.id.my_bonus_tv);
            mInviteCodeTv = (TextView) mView.findViewById(R.id.invite_code_tv);
            mBarcodeIv = (ImageView) mView.findViewById(R.id.barcode_iv);
            mDownloadTv = (TextView) mView.findViewById(R.id.download_tip_tv);
        }
    }

    private static Bitmap generateWinPicInMainThread(String inviteCode, float myBonus) {
        MyLog.w(TAG, "generateWinPicInMainThread inviteCode=" + inviteCode);
        LinearLayout sharePage = (LinearLayout) LayoutInflater.from(GlobalData.app()).inflate(R.layout.contest_win_share_holder, null);
        ShareWinBitmapHolder holder = new ShareWinBitmapHolder(sharePage);
        holder.mMyBonusTv.setText(String.valueOf(myBonus));
        holder.mInviteCodeTv.setText(inviteCode);
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

    public static String saveContestWinSharePic(String inviteCode, float myBonus) {
        try {
//            File hasExistsFile = AttachmentUtils.getFileByUrl(CONTEST_SHARE_WIN_SUFFIX + inviteCode);
//            if (hasExistsFile != null && hasExistsFile.exists()) {
//                return hasExistsFile.getAbsolutePath();
//            }
            File dir = new File(share_pic);
            if (!dir.exists()) {
                try {
                    dir.mkdirs();
                } catch (Exception e) {
                    MyLog.e(e);
                }
            }
            String fileName = MD5.MD5_16(CONTEST_SHARE_WIN_SUFFIX + inviteCode);
            File file = new File(share_pic, fileName + ".JPEG");
            Bitmap bitmap;
            try {
                bitmap = generateWinPicInMainThread(inviteCode, myBonus);
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

    //获取二维码的图片文件地址，如果发现已经有了并且是最新则直接使用，否则自己手动的生成一个
    public static String getQrCodeFilePath(boolean hasLogo) throws Exception {
        String qrcodeDefaultPath = Environment.getExternalStorageDirectory() + "/Xiaomi/WALI_LIVE";
        String qrcodePathAndVersion = PreferenceUtils.getSettingString(GlobalData.app(), PreferenceKeys.PRE_KEY_FEED_SHARE_IMG_LOCALPATH_VERSION, "");
        String homepage = PreferenceUtils.getSettingString(GlobalData.app(), PreferenceKeys.PRE_KEY_MILIVE_HOST, "http://live.mi.com");
        Bitmap logo = null;
        if (hasLogo) {
            logo = BitmapFactory.decodeResource(GlobalData.app().getResources(), R.drawable.ic_launcher);
        }
        String result = "";
        String currentVersionName = VersionManager.getVersionName(GlobalData.app());
        if (TextUtils.isEmpty(qrcodePathAndVersion)) {
            File qrfile = new File(qrcodeDefaultPath, "qrCode.jpg");
            if (!qrfile.exists()) {
                qrfile.createNewFile();
            }
            QrcodeUtils.createQRImage(homepage, QR_IMAGE_WIDTH, QR_IMAGE_HEIGHT, null, qrfile.getAbsolutePath());
            PreferenceUtils.setSettingString(GlobalData.app(), PreferenceKeys.PRE_KEY_FEED_SHARE_IMG_LOCALPATH_VERSION, homepage + ";" + qrfile.getAbsolutePath());
            result = qrfile.getAbsolutePath();
        } else {
            String[] datas = qrcodePathAndVersion.split(";");
            if (datas.length != 3 || !datas[0].equals(homepage) || !new File(datas[1]).exists() || !datas[2].equals(currentVersionName)) {
                File qrfile = new File(qrcodeDefaultPath, "qrCode.jpg");
                if (!qrfile.exists()) {
                    qrfile.createNewFile();
                }
                QrcodeUtils.createQRImage(homepage, QR_IMAGE_WIDTH, QR_IMAGE_HEIGHT, null, qrfile.getAbsolutePath());
                PreferenceUtils.setSettingString(GlobalData.app(), PreferenceKeys.PRE_KEY_FEED_SHARE_IMG_LOCALPATH_VERSION, homepage + ";" + qrfile.getAbsolutePath() + ";" + currentVersionName);
                result = qrfile.getAbsolutePath();
            } else {
                result = datas[1];
            }
        }
        if (hasLogo) {
            if (logo != null) {
                logo.recycle();
            }
        }
        return result;
    }
}

