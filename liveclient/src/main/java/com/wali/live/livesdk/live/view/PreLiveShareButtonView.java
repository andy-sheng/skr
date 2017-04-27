package com.wali.live.livesdk.live.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.utils.CommonUtils;
import com.base.utils.display.DisplayUtils;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.preference.PreferenceKeys;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.presenter.IShareView;
import com.wali.live.livesdk.live.presenter.SharePresenter;
import com.wali.live.proto.ShareProto;
import com.wali.live.watchsdk.watch.presenter.SnsShareHelper;

import java.util.List;

import butterknife.ButterKnife;

import static com.wali.live.watchsdk.watch.presenter.SnsShareHelper.BTN_FACEBOOK;
import static com.wali.live.watchsdk.watch.presenter.SnsShareHelper.BTN_INSTAGRAM;
import static com.wali.live.watchsdk.watch.presenter.SnsShareHelper.BTN_MILIAO;
import static com.wali.live.watchsdk.watch.presenter.SnsShareHelper.BTN_MILIAO_FEEDS;
import static com.wali.live.watchsdk.watch.presenter.SnsShareHelper.BTN_QQ;
import static com.wali.live.watchsdk.watch.presenter.SnsShareHelper.BTN_QZONE;
import static com.wali.live.watchsdk.watch.presenter.SnsShareHelper.BTN_TWITTER;
import static com.wali.live.watchsdk.watch.presenter.SnsShareHelper.BTN_WECHAT;
import static com.wali.live.watchsdk.watch.presenter.SnsShareHelper.BTN_WECHAT_MOMENT;
import static com.wali.live.watchsdk.watch.presenter.SnsShareHelper.BTN_WEIBO;
import static com.wali.live.watchsdk.watch.presenter.SnsShareHelper.BTN_WHATSAPP;
import static com.wali.live.watchsdk.watch.presenter.SnsShareHelper.isWechatInstalled;

/**
 * Created by lan on 16/2/20.
 *
 * @module 直播预览页
 * @description 预览页分享按钮的view
 */
public class PreLiveShareButtonView extends LinearLayout implements View.OnClickListener, IShareView {
    private static final String TAG = PreLiveShareButtonView.class.getSimpleName();

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

    private static final int[] SHARE_TV_ID = new int[]{
            R.string.weixin_friend,
            R.string.moment,
            R.string.QQ,
            R.string.qzone,
            R.string.blog,
            R.string.facebook,
            R.string.twitter,
            R.string.instagram,
            R.string.whatsapp,
            R.string.miliao,
            R.string.miliao_feeds
    };

    /***
     * BTN统一用SnsShareHelper定义的
     */
    private static final int[] SHARE_BTN_INDEX_DOMESTIC = new int[]{//国内btn顺序
            BTN_WECHAT,
            BTN_WECHAT_MOMENT,
            BTN_QQ,
            BTN_QZONE,
            BTN_WEIBO,
            BTN_MILIAO,
            BTN_MILIAO_FEEDS
    };
    private static final int[] SHARE_BTN_INDEX_ABROAD = new int[]{//  //国外btn顺序
            BTN_WHATSAPP,
            BTN_FACEBOOK,
            BTN_TWITTER,
            BTN_INSTAGRAM,
            BTN_WECHAT,
            BTN_WECHAT_MOMENT
    };

    private boolean[] mShareBtnStateSet = new boolean[SHARE_SPECIES_CNT];
    private ImageView[] mBtnSet = new ImageView[SHARE_BTN_CNT];

    private boolean mIsFirstTips;
    private int mShareTipsMarginBottom;
    private int mShareTipsMarginLeft;
    private int mShareBtnWidth;
    private int mShareToastWidth;
    private CustomToast mCustomToast;
    private SharePresenter mSharePresenter;

    //这里index是显示的实际button的下标, shareBtnIndex是对应SHARE_DRAWABLE_ID，SHARE_TV_ID的下标索引。
    private void bindShareBtnArrays(int index, int shareBtnIndex) {
        mBtnSet[index] = (ImageView) findViewById(SHARE_BTN_ID[index]);
        mBtnSet[index].setImageResource(SHARE_DRAWABLE_ID[shareBtnIndex]);
        mBtnSet[index].setTag(shareBtnIndex); //index as tag 0-10
        mBtnSet[index].setSelected(mShareBtnStateSet[shareBtnIndex]);
        mBtnSet[index].setVisibility(VISIBLE);
        mBtnSet[index].setOnClickListener(this);
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
        onClickInPrepareLive(v, action);
    }

    public void onClickInPrepareLive(View v, int action) {
        boolean isSelected = !v.isSelected();
        String tips;
        String platform = "";
        switch (action) {
            case BTN_WECHAT:
                platform = GlobalData.app().getString(R.string.weixin_friend);
                if (!isWechatInstalled()) {
                    tips = GlobalData.app().getString(R.string.uninstall_share_tips, platform);
                    showShareToast(v, tips);
                    return;
                }
                mShareBtnStateSet[BTN_WECHAT] = isSelected;
                break;
            case BTN_WECHAT_MOMENT:
                platform = GlobalData.app().getString(R.string.moment);
                if (!isWechatInstalled()) {
                    platform = GlobalData.app().getString(R.string.weixin_friend);
                    tips = GlobalData.app().getString(R.string.uninstall_share_tips, platform);
                    showShareToast(v, tips);
                    return;
                }
                mShareBtnStateSet[BTN_WECHAT_MOMENT] = isSelected;
                break;
            case BTN_QQ:
                platform = GlobalData.app().getString(R.string.QQ);
                if (!SnsShareHelper.isQQInstalled()) {
                    tips = GlobalData.app().getString(R.string.uninstall_share_tips, platform);
                    showShareToast(v, tips);
                    return;
                }
                mShareBtnStateSet[BTN_QQ] = isSelected;
                break;
            case BTN_QZONE:
                if (!SnsShareHelper.isQQInstalled()) {
                    platform = GlobalData.app().getString(R.string.QQ);
                    tips = GlobalData.app().getString(R.string.uninstall_share_tips, platform);
                    showShareToast(v, tips);
                    return;
                }
                platform = GlobalData.app().getString(R.string.qzone);
                mShareBtnStateSet[BTN_QZONE] = isSelected;
                break;
            case BTN_WEIBO:
                platform = GlobalData.app().getString(R.string.blog);
                if (!SnsShareHelper.isWeiboInstalled()) {
                    tips = GlobalData.app().getString(R.string.uninstall_share_tips, platform);
                    showShareToast(v, tips);
                    return;
                }
                mShareBtnStateSet[BTN_WEIBO] = isSelected;
                break;
            case BTN_FACEBOOK:
                platform = GlobalData.app().getString(R.string.facebook);
                if (!SnsShareHelper.isFacebookInstalled()) {
                    tips = GlobalData.app().getString(R.string.uninstall_share_tips, platform);
                    showShareToast(v, tips);
                    return;
                }
                mShareBtnStateSet[BTN_FACEBOOK] = isSelected;
                break;
            case BTN_TWITTER:
                platform = GlobalData.app().getString(R.string.twitter);
                if (!SnsShareHelper.isTwitterInstalled()) {
                    tips = GlobalData.app().getString(R.string.uninstall_share_tips, platform);
                    showShareToast(v, tips);
                    return;
                }
                mShareBtnStateSet[BTN_TWITTER] = isSelected;
                break;
            case BTN_INSTAGRAM:
                platform = GlobalData.app().getString(R.string.instagram);
                if (!SnsShareHelper.isInstagramInstalled()) {
                    tips = GlobalData.app().getString(R.string.uninstall_share_tips, platform);
                    showShareToast(v, tips);
                    return;
                }
                mShareBtnStateSet[BTN_INSTAGRAM] = isSelected;
                break;
            case BTN_WHATSAPP:
                platform = GlobalData.app().getString(R.string.whatsapp);
                if (!SnsShareHelper.isWhatsappInstalled()) {
                    tips = GlobalData.app().getString(R.string.uninstall_share_tips, platform);
                    showShareToast(v, tips);
                    return;
                }
                mShareBtnStateSet[BTN_WHATSAPP] = isSelected;
                break;
            case BTN_MILIAO:
                platform = GlobalData.app().getString(R.string.miliao);
                if (!SnsShareHelper.isMiliaoInstalled()) {
                    tips = GlobalData.app().getString(R.string.uninstall_share_tips, platform);
                    showShareToast(v, tips);
                    return;
                }
                mShareBtnStateSet[BTN_MILIAO] = isSelected;
                break;
            case BTN_MILIAO_FEEDS:
                platform = GlobalData.app().getString(R.string.miliao);
                if (!SnsShareHelper.isMiliaoInstalled()) {
                    tips = GlobalData.app().getString(R.string.uninstall_share_tips, platform);
                    showShareToast(v, tips);
                    return;
                }
                platform = GlobalData.app().getString(R.string.miliao_feeds);
                mShareBtnStateSet[BTN_MILIAO_FEEDS] = isSelected;
                break;
            default:
                break;
        }
        v.setSelected(isSelected);
        if (isSelected) {
            tips = GlobalData.app().getString(R.string.prepare_live_share_tips, platform);
        } else {
            tips = GlobalData.app().getString(R.string.cancel_share_tips, platform);
        }
        showShareToast(v, tips);
    }

    private void showShareToast(View view, String message) {
        if (mCustomToast != null) {
            mCustomToast.hide();
        }
        int[] location = new int[2];
        if (mShareTipsMarginLeft <= 0) {
            mShareBtnWidth = findViewById(R.id.share_btn1).getWidth();
            mShareToastWidth = this.getWidth();
            //mShareTipsMarginBottom = mBeginBtn.getHeight();
            mShareTipsMarginLeft = (DisplayUtils.getScreenWidth() - mShareToastWidth) / 2;
        }
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        x -= mShareTipsMarginLeft;
        x += mShareBtnWidth / 4;
        y -= mShareTipsMarginBottom;
        mCustomToast = new CustomToast(x, y, message);
        mCustomToast.setWidth(mShareToastWidth);
        mCustomToast.showUntilCancel(5);
    }

    public void hideShareToast() {
        if (mCustomToast != null) {
            mCustomToast.hide();
        }
    }

    public PreLiveShareButtonView(Context context) {
        super(context);
        init(context);
    }

    public PreLiveShareButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PreLiveShareButtonView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.share_button_view, this);
        ButterKnife.bind(this);
        initData();
        initView();
        initPresenter();
    }

    private void initPresenter() {
        mSharePresenter = new SharePresenter(this);
        mSharePresenter.getTagTailForShare(UserAccountManager.getInstance().getUuidAsLong(), ShareProto.RoleType.ANCHOR,
                ShareProto.PeriodType.PRE_LIVE);
    }

    private void initData() {
        //共用的
        mIsFirstTips = PreferenceUtils.getSettingBoolean(GlobalData.app(), PreferenceKeys.SHARE_FIRST_TIPS, true);
        mShareBtnStateSet[BTN_WECHAT] = isWechatInstalled() ? PreferenceUtils.getSettingBoolean(GlobalData.app(), PreferenceKeys.SHARE_WEIXIN_FRIEND_SELECTED, false) : false;

        if (!CommonUtils.isLocalChina()) {
            mShareBtnStateSet[BTN_FACEBOOK] = SnsShareHelper.isFacebookInstalled() ? PreferenceUtils.getSettingBoolean(GlobalData.app(), PreferenceKeys.SHARE_FACEBOOK_SELECTED, true) : false;
            mShareBtnStateSet[BTN_TWITTER] = SnsShareHelper.isTwitterInstalled() ? PreferenceUtils.getSettingBoolean(GlobalData.app(), PreferenceKeys.SHARE_TWITTER_SELECTED, false) : false;
            mShareBtnStateSet[BTN_INSTAGRAM] = SnsShareHelper.isInstagramInstalled() ? PreferenceUtils.getSettingBoolean(GlobalData.app(), PreferenceKeys.SHARE_INSTAGRAM_SELECTED, false) : false;
            mShareBtnStateSet[BTN_WHATSAPP] = SnsShareHelper.isWhatsappInstalled() ? PreferenceUtils.getSettingBoolean(GlobalData.app(), PreferenceKeys.SHARE_WHATSAPP_SELECTED, false) : false;
        } else {
            mShareBtnStateSet[BTN_QZONE] = SnsShareHelper.isQQInstalled() ? PreferenceUtils.getSettingBoolean(GlobalData.app(), PreferenceKeys.SHARE_QZONE_SELECTED, false) : false;
            mShareBtnStateSet[BTN_WEIBO] = SnsShareHelper.isWeiboInstalled() ? PreferenceUtils.getSettingBoolean(GlobalData.app(), PreferenceKeys.SHARE_WEIBO_SELECTED, false) : false;
            mShareBtnStateSet[BTN_WECHAT_MOMENT] = isWechatInstalled() ? PreferenceUtils.getSettingBoolean(GlobalData.app(), PreferenceKeys.SHARE_WEIXIN_MOMENT_SELECTED, true) : false;
            mShareBtnStateSet[BTN_QQ] = SnsShareHelper.isQQInstalled() ? PreferenceUtils.getSettingBoolean(GlobalData.app(), PreferenceKeys.SHARE_QQ_SELECTED, false) : false;
            mShareBtnStateSet[BTN_MILIAO] = SnsShareHelper.isMiliaoInstalled() ? PreferenceUtils.getSettingBoolean(GlobalData.app(), PreferenceKeys.SHARE_MILIAO_SELECTED, false) : false;
            mShareBtnStateSet[BTN_MILIAO_FEEDS] = SnsShareHelper.isMiliaoInstalled() ? PreferenceUtils.getSettingBoolean(GlobalData.app(), PreferenceKeys.SHARE_MILIAO_FEEDS_SELECTED, false) : false;
        }

        float mDensity = DisplayUtils.getDensity();
        if (mDensity >= 3) {
            mShareTipsMarginBottom = (int) getResources().getDimension(R.dimen.view_dimen_220);
        } else if (mDensity >= 2.5) {
            mShareTipsMarginBottom = (int) getResources().getDimension(R.dimen.view_dimen_230);
        } else {
            mShareTipsMarginBottom = (int) getResources().getDimension(R.dimen.view_dimen_240);
        }
    }

    private void initView() {
        boolean isLocalChina = CommonUtils.isLocalChina();
        final int defaultSelectBtnId = isLocalChina ? BTN_WECHAT_MOMENT : BTN_FACEBOOK;
        int defaultSelectBtn = BTN_WECHAT;
        int btnCnt = isLocalChina ? SHARE_BTN_INDEX_DOMESTIC.length : SHARE_BTN_INDEX_ABROAD.length;
        for (int i = 0; i < btnCnt; i++) {
            int shareBtnIndex = isLocalChina ? SHARE_BTN_INDEX_DOMESTIC[i] : SHARE_BTN_INDEX_ABROAD[i];
            //btn id始终保持1-?
            bindShareBtnArrays(i, shareBtnIndex);
            if (shareBtnIndex == defaultSelectBtnId) {
                defaultSelectBtn = i;//标记默认第几个btn被选中
            }
        }
        if (!mIsFirstTips || defaultSelectBtn > SHARE_BTN_CNT ||
                !((isLocalChina && isWechatInstalled()) || (!isLocalChina && SnsShareHelper.isFacebookInstalled()))) {
            return;
        }
        final int defaultBtn = defaultSelectBtn;
        mBtnSet[defaultBtn].setSelected(true);
        mShareBtnStateSet[defaultSelectBtnId] = true;
        mBtnSet[defaultBtn].postDelayed(new Runnable() {
            @Override
            public void run() {
                String tips = GlobalData.app().getString(R.string.prepare_live_share_tips, GlobalData.app().getString(SHARE_TV_ID[defaultSelectBtnId]));
                showShareToast(mBtnSet[defaultBtn], tips);
                PreferenceUtils.setSettingBoolean(GlobalData.app(), PreferenceKeys.SHARE_FIRST_TIPS, false);
            }
        }, 500);
    }

    public boolean isWXSelected() {
        return mShareBtnStateSet[BTN_WECHAT];
    }

    public boolean isMomentSelected() {
        return mShareBtnStateSet[BTN_WECHAT_MOMENT];
    }

    public boolean isQQSelected() {
        return mShareBtnStateSet[BTN_QQ];
    }

    public boolean isQzoneSelected() {
        return mShareBtnStateSet[BTN_QZONE];
    }

    public boolean isWeiboSelected() {
        return mShareBtnStateSet[BTN_WEIBO];
    }

    public boolean isFacebookSelected() {
        return mShareBtnStateSet[BTN_FACEBOOK];
    }

    public boolean isTwitterSelected() {
        return mShareBtnStateSet[BTN_TWITTER];
    }

    public boolean isInstagramSelected() {
        return mShareBtnStateSet[BTN_INSTAGRAM];
    }

    public boolean isWhatsAppSelected() {
        return mShareBtnStateSet[BTN_WHATSAPP];
    }

    public boolean isMiliaoSelected() {
        return mShareBtnStateSet[BTN_MILIAO];
    }

    public boolean isMiliaoFeedsSelected() {
        return mShareBtnStateSet[BTN_MILIAO_FEEDS];
    }

    @Override
    public void notifyShareControlPanel(List<ShareProto.TagTail> tagTail) {
        SnsShareHelper.getInstance().setShareTagTailMap(tagTail);
    }

    public void destroy() {
        if (mSharePresenter != null) {
            mSharePresenter.destroy();
        }
    }

}
