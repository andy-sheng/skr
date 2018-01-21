package com.wali.live.watchsdk.contest.view;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.base.mvp.specific.RxRelativeLayout;
import com.base.utils.toast.ToastUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.contest.cache.ContestGlobalCache;
import com.wali.live.watchsdk.contest.share.ContestShareHelper;
import com.wali.live.watchsdk.ipc.service.ShareInfo;
import com.wali.live.watchsdk.watch.presenter.SnsShareHelper;

/**
 * Created by lan on 2018/1/11.
 */
public class ContestRevivalRuleView extends RxRelativeLayout implements View.OnClickListener, View.OnLongClickListener {
    private View mBgView;
    private View mReviveRuleArea;

    private TextView mRevivalCodeTv;

    private TextView mWechatIv;
    private TextView mMomentIv;
    private TextView mQQIv;
    private TextView mQzoneIv;

    private boolean mIsShown = false;

    public ContestRevivalRuleView(Context context) {
        super(context);
        init(context);
    }

    public ContestRevivalRuleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ContestRevivalRuleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.contest_revival_rule_view, this);

        mBgView = $(R.id.background_view);
        $click(mBgView, this);

        mReviveRuleArea = $(R.id.revive_rule_area);
        mReviveRuleArea.setOnClickListener(this);

        mRevivalCodeTv = $(R.id.revival_code_tv);
        mRevivalCodeTv.setOnLongClickListener(this);

        mQQIv = $(R.id.qq_btn);
        mQzoneIv = $(R.id.qzone_btn);
        mWechatIv = $(R.id.wechat_btn);
        mMomentIv = $(R.id.moment_btn);

        $click(mQQIv, this);
        $click(mQzoneIv, this);
        $click(mWechatIv, this);
        $click(mMomentIv, this);

        updateCode();
    }

    public void updateCode() {
        mRevivalCodeTv.setText(ContestGlobalCache.getRevivalCode());
    }

    public boolean isShown() {
        return mIsShown;
    }

    public void hide() {
        if (!mIsShown) {
            return;
        }
        mIsShown = false;
        setVisibility(View.GONE);
    }

    public void show() {
        if (mIsShown) {
            return;
        }
        mIsShown = true;
        setVisibility(View.VISIBLE);

        Animation alpha = AnimationUtils.loadAnimation(getContext(), R.anim.alpha_in_time300_fromp4);
        mBgView.startAnimation(alpha);

        Animation slide = AnimationUtils.loadAnimation(getContext(), R.anim.slide_bottom_in_time400_from80);
        mReviveRuleArea.startAnimation(slide);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.background_view) {
            hide();
        } else if (id == R.id.qq_btn) {
            String imgLocalPath = ContestShareHelper.saveContestInviteSharePic(ContestGlobalCache.getRevivalCode());
            SnsShareHelper.getInstance().shareLocalImageToSns(ShareInfo.TYPE_QQ, imgLocalPath);
            hide();
        } else if (id == R.id.qzone_btn) {
            String imgLocalPath = ContestShareHelper.saveContestInviteSharePic(ContestGlobalCache.getRevivalCode());
            SnsShareHelper.getInstance().shareLocalImageToSns(ShareInfo.TYPE_QZONE, imgLocalPath);
            hide();
        } else if (id == R.id.wechat_btn) {
            String imgLocalPath = ContestShareHelper.saveContestInviteSharePic(ContestGlobalCache.getRevivalCode());
            SnsShareHelper.getInstance().shareLocalImageToSns(ShareInfo.TYPE_WECHAT, imgLocalPath);
            hide();
        } else if (id == R.id.moment_btn) {
            String imgLocalPath = ContestShareHelper.saveContestInviteSharePic(ContestGlobalCache.getRevivalCode());
            SnsShareHelper.getInstance().shareLocalImageToSns(ShareInfo.TYPE_MOMENT, imgLocalPath);
            hide();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        int id = v.getId();
        if (id == R.id.revival_code_tv) {
            saveToClipboard();
            return true;
        }
        return false;
    }

    private void saveToClipboard() {
        ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(getResources().getString(R.string.contest_prepare_revival_code), ContestGlobalCache.getRevivalCode());
        cm.setPrimaryClip(clipData);
        ToastUtils.showToast(R.string.contest_prepare_revival_copy);
    }
}
