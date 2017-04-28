package com.wali.live.livesdk.live.view;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.user.User;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.presenter.IShareView;
import com.wali.live.livesdk.live.presenter.SharePresenter;
import com.wali.live.proto.ShareProto;
import com.wali.live.watchsdk.watch.presenter.SnsShareHelper;

import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by lan on 16/2/20.
 *
 * @module 直播结束页面
 * @description 直播结束分享按钮的view
 */
public class ShareButtonView extends LinearLayout implements View.OnClickListener, IShareView {
    private static final String TAG = ShareButtonView.class.getSimpleName();

    public static final int SHARE_BTN_CNT = 7;
    public static final int SHARE_SPECIES_CNT = 11;

    private static final int[] SHARE_BTN_ID = new int[]{
            R.id.share_btn1,
            R.id.share_btn2,
            R.id.share_btn3,
            R.id.share_btn4,
            R.id.share_btn5,
            R.id.share_btn6,
            R.id.share_btn7
    };

    private static final int[] SHARE_DRAWABLE_ID = new int[]{
            R.drawable.live_begin_icon_share_weixin_bg,
            R.drawable.live_begin_icon_share_pengyouquan_bg,
            R.drawable.live_begin_icon_share_qq_bg,
            R.drawable.live_begin_icon_share_qzone_bg,
            R.drawable.live_begin_icon_share_weibo_bg,
            R.drawable.live_begin_icon_share_facebook_bg,
            R.drawable.live_begin_icon_share_twitter_bg,
            R.drawable.live_begin_icon_share_instagram_bg,
            R.drawable.live_begin_icon_share_whatsapp_bg,
            R.drawable.live_begin_icon_share_miliao_bg,
            R.drawable.live_begin_icon_share_weiguangbo_bg
    };

    //国内btn顺序
    private static final int[] SHARE_BTN_INDEX_DOMESTIC = new int[]{
            SnsShareHelper.BTN_WECHAT,
            SnsShareHelper.BTN_WECHAT_MOMENT,
            SnsShareHelper.BTN_QQ,
            SnsShareHelper.BTN_QZONE,
            SnsShareHelper.BTN_WEIBO,
            SnsShareHelper.BTN_MILIAO,
            SnsShareHelper.BTN_MILIAO_FEEDS
    };

    //国外btn顺序  BTN_INSTAGRAM,
    private static final int[] SHARE_BTN_INDEX_ABROAD = new int[]{
            SnsShareHelper.BTN_WHATSAPP,
            SnsShareHelper.BTN_FACEBOOK,
            SnsShareHelper.BTN_TWITTER,
            SnsShareHelper.BTN_WECHAT,
            SnsShareHelper.BTN_WECHAT_MOMENT,
    };

    private boolean[] mShareBtnStateSet = new boolean[SHARE_SPECIES_CNT];

    private ImageView[] mBtnSet = new ImageView[SHARE_BTN_CNT];

    private HashMap<ShareProto.ChannelType, ShareProto.TagTail> mTagTailMap = new HashMap<>();

    private User mOwner;
    private String mLocation = "";
    private String mShareUrl = "";
    private String mLiveTitle = "";
    private String mLiveCoverUrl = "";
    private long mAvatarTs;
    private int mLiveType;
    private SharePresenter mSharePresenter;

    private int mAllowShareType;

    private Activity mActivity;

    private void bindShareBtnArrays(int index, int shareBtnIndex) {
        mBtnSet[index] = (ImageView) findViewById(SHARE_BTN_ID[index]);
        mBtnSet[index].setImageResource(SHARE_DRAWABLE_ID[shareBtnIndex]);
        mBtnSet[index].setTag(shareBtnIndex); //index as tag

        if ((mAllowShareType & (1 << shareBtnIndex)) != 0) {
            mBtnSet[index].setSelected(mShareBtnStateSet[shareBtnIndex]);
            mBtnSet[index].setVisibility(VISIBLE);
            mBtnSet[index].setOnClickListener(this);
        } else {
            mBtnSet[index].setVisibility(GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if (CommonUtils.isFastDoubleClick()) {
            return;
        }
        int action = -1;
        try {
            if (v.getTag() != null) {
                action = Integer.valueOf(String.valueOf(v.getTag()));
            }
        } catch (NumberFormatException e) {
            MyLog.d(TAG, e);
            return;
        }
        shareToSns(action);
    }

    private void shareToSns(int type) {
        if (mOwner == null || mActivity == null || TextUtils.isEmpty(mShareUrl)) {
            MyLog.w(TAG, "mOwner == null || mActivity == null");
            return;
        }

        if (SnsShareHelper.getInstance().isInstallApp(type)) {
            SnsShareHelper.getInstance().shareToSns(type, mShareUrl, mLiveCoverUrl, mLocation, mLiveTitle, mOwner);
        }
    }

    public ShareButtonView(Context context) {
        super(context);
        init(context);
    }

    public ShareButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ShareButtonView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.share_button_view, this);
        ButterKnife.bind(this);
    }

    public void setShareType(int shareType) {
        mAllowShareType = shareType;
        initViews();
    }

    private void initViews() {
        boolean isLocalChina = CommonUtils.isLocalChina();
        int btnCnt = isLocalChina ? SHARE_BTN_INDEX_DOMESTIC.length : SHARE_BTN_INDEX_ABROAD.length;
        for (int i = 0; i < btnCnt; i++) {
            int shareBtnIndex = isLocalChina ? SHARE_BTN_INDEX_DOMESTIC[i] : SHARE_BTN_INDEX_ABROAD[i];
            //btn id始终保持1-5
            bindShareBtnArrays(i, shareBtnIndex);
        }
    }

    private void getShareDataFromServer() {
        mSharePresenter = new SharePresenter(this);
        mSharePresenter.getTagTailForShare(mOwner.getUid(),
                (mOwner.getUid() == UserAccountManager.getInstance().getUuidAsLong()) ? ShareProto.RoleType.ANCHOR : ShareProto.RoleType.VISITOR,
                ShareProto.PeriodType.END_LIVE);
    }

    public void setShareData(Activity activity, User owner, String shareUrl, String liveCoverUrl, String liveTitle, String location, long avatarTs, int liveType) {
        MyLog.w(TAG, "owner " + owner.toString() + " shareUrl " + shareUrl + " liveCoverUrl =" + liveCoverUrl + " liveTitle =" + liveTitle + " location=" + location + "avatarTs =" + avatarTs);
        mActivity = activity;
        mOwner = owner;
        mShareUrl = shareUrl;
        mLiveCoverUrl = liveCoverUrl;
        mLiveTitle = liveTitle;
        mLocation = location;
        mAvatarTs = avatarTs;
        mLiveType = liveType;
        getShareDataFromServer();
    }

    @Override
    public void notifyShareControlPanel(List<ShareProto.TagTail> tagTailList) {
        if (tagTailList == null || tagTailList.size() <= 0) {
            return;
        }
        SnsShareHelper.getInstance().setShareTagTailMap(tagTailList);
    }

    public void destroy() {
        if (mSharePresenter != null) {
            mSharePresenter.destroy();
        }
    }
}
