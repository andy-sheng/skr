package com.zq.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;

import com.common.base.BaseActivity;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.component.busilib.R;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.zq.person.fragment.ImageBigPreviewFragment;
import com.zq.report.fragment.ReportFragment;

import static com.zq.report.fragment.ReportFragment.FORM_GAME;
import static com.zq.report.fragment.ReportFragment.REPORT_FROM_KEY;
import static com.zq.report.fragment.ReportFragment.REPORT_USER_ID;

// 个人信息卡片
public class PersonInfoDialog {

    boolean isReport = false;
    boolean isKick = false;

    Context mContext;
    DialogPlus mDialogPlus;

    KickListener mKickListener;

    public PersonInfoDialog(Context context, final int userId, boolean showReport, boolean showKick) {
        mContext = context;
        final PersonInfoDialogView personInfoDialogView = new PersonInfoDialogView(context, userId, showReport, showKick);

        mDialogPlus = DialogPlus.newDialog(mContext)
                .setContentHolder(new ViewHolder(personInfoDialogView))
                .setGravity(Gravity.BOTTOM)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_60)
                .setExpanded(false)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogPlus dialog, @NonNull View view) {
                        if (view.getId() == R.id.report) {
                            // 举报
                            dialog.dismiss();
                            isReport = true;
                        } else if (view.getId() == R.id.kick) {
                            // 踢人
                            dialog.dismiss();
                            isKick = true;
                        } else if (view.getId() == R.id.follow_area || view.getId() == R.id.follow_tv) {
                            // 关注
                            if (personInfoDialogView.getUserInfoModel().isFollow() || personInfoDialogView.getUserInfoModel().isFriend()) {
                                UserInfoManager.getInstance().mateRelation(personInfoDialogView.getUserInfoModel().getUserId(),
                                        UserInfoManager.RA_UNBUILD, personInfoDialogView.getUserInfoModel().isFriend());
                            } else {
                                UserInfoManager.getInstance().mateRelation(personInfoDialogView.getUserInfoModel().getUserId(),
                                        UserInfoManager.RA_BUILD, personInfoDialogView.getUserInfoModel().isFriend());
                            }

                        } else if (view.getId() == R.id.avatar_iv) {
                            dialog.dismiss();
                            Bundle bundle = new Bundle();
                            bundle.putString(ImageBigPreviewFragment.BIG_IMAGE_PATH, personInfoDialogView.getUserInfoModel().getAvatar());
                            U.getFragmentUtils().addFragment(
                                    FragmentUtils.newAddParamsBuilder((BaseActivity) mContext, ImageBigPreviewFragment.class)
                                            .setAddToBackStack(true)
                                            .setEnterAnim(R.anim.fade_in_center)
                                            .setExitAnim(R.anim.fade_out_center)
                                            .setHasAnimation(true)
                                            .setBundle(bundle)
                                            .build());
                        }
                    }
                })
                .setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(@NonNull DialogPlus dialog) {
                        if (isReport) {
                            showReportView(userId);
                            isReport = false;
                        }
                        if (isKick) {
                            if (mKickListener != null) {
                                mKickListener.onClickKick(personInfoDialogView.getUserInfoModel());
                            }
                            isKick = false;
                        }

                    }
                })
                .create();
    }

    public void setListener(KickListener kickListener) {
        this.mKickListener = kickListener;
    }

    public void show() {
        if (mDialogPlus != null) {
            mDialogPlus.show();
        }
    }

    public boolean isShowing() {
        if (mDialogPlus != null) {
            return mDialogPlus.isShowing();
        }
        return false;
    }

    public void dismiss() {
        if (mDialogPlus != null) {
            mDialogPlus.dismiss();
        }
        mKickListener = null;
    }

    private void showReportView(int userID) {
        Bundle bundle = new Bundle();
        bundle.putInt(REPORT_FROM_KEY, FORM_GAME);
        bundle.putInt(REPORT_USER_ID, userID);
        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder((BaseActivity) mContext, ReportFragment.class)
                        .setBundle(bundle)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .setEnterAnim(com.component.busilib.R.anim.slide_in_bottom)
                        .setExitAnim(com.component.busilib.R.anim.slide_out_bottom)
                        .build());
    }

   public interface KickListener {
        void onClickKick(UserInfoModel userInfoModel);
    }

}
