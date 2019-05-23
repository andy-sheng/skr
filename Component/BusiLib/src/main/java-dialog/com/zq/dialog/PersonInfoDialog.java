package com.zq.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;

import com.common.base.BaseActivity;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.component.busilib.R;
import com.imagebrowse.big.BigImageBrowseFragment;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.zq.person.model.PhotoModel;
import com.zq.report.fragment.QuickFeedbackFragment;

import static com.zq.report.fragment.ReportFragment.FORM_GAME;
import static com.zq.report.fragment.ReportFragment.REPORT_FROM_KEY;
import static com.zq.report.fragment.ReportFragment.REPORT_USER_ID;

// 个人信息卡片
public class PersonInfoDialog {

    Context mContext;
    int mRoomID;
    DialogPlus mDialogPlus;

    KickListener mKickListener;

    public PersonInfoDialog(Context context, final int userId, final boolean showReport, boolean showKick) {
        mContext = context;
        init(context, userId, showReport, showKick);
    }

    public PersonInfoDialog(Context context, final int userId, final boolean showReport, boolean showKick, int roomID) {
        mContext = context;
        mRoomID = roomID;
        init(context, userId, showReport, showKick);
    }

    private void init(Context context, final int userId, final boolean showReport, boolean showKick) {
        PersonInfoDialogView2 personInfoDialogView = new PersonInfoDialogView2(context, userId, showReport, showKick);
        personInfoDialogView.setListener(new PersonCardClickListener() {
            @Override
            public void onClickReport(int userID) {
                if (mDialogPlus != null) {
                    mDialogPlus.dismiss(false);
                }
                showReportView(userID);
            }

            @Override
            public void onClickKick(UserInfoModel userInfoModel) {
                if (mDialogPlus != null) {
                    mDialogPlus.dismiss(false);
                }
                if (mKickListener != null) {
                    mKickListener.onClickKick(userInfoModel);
                }
            }

            @Override
            public void onClickAvatar(String avatar) {
                if (mDialogPlus != null) {
                    mDialogPlus.dismiss(false);
                }
                BigImageBrowseFragment.open(false, (BaseActivity) mContext, avatar);
            }

            @Override
            public void onClickFollow(int userID, boolean isFriend, boolean isFollow) {
                // 关注
                if (isFollow || isFriend) {
                    // TODO: 2019/3/28 个人信息卡片不让取关
//                                UserInfoManager.getInstance().mateRelation(personInfoDialogView.getUserInfoModel().getUserId(),
//                                        UserInfoManager.RA_UNBUILD, personInfoDialogView.getUserInfoModel().isFriend());
                } else {
                    UserInfoManager.getInstance().mateRelation(userID, UserInfoManager.RA_BUILD, isFriend, mRoomID, null);
                }
            }

            @Override
            public void onClickMessage(UserInfoModel userInfoModel) {
                // TODO: 2019/4/8 私信
                if (mDialogPlus != null) {
                    mDialogPlus.dismiss(false);
                }
            }

            @Override
            public void onClickPhoto(PhotoModel photoModel, int position) {
                if (mDialogPlus != null) {
                    mDialogPlus.dismiss(false);
                }
                // TODO: 2019/4/8 打开大图浏览
            }

            @Override
            public void onClickOut() {
                mDialogPlus.dismiss();
            }
        });

        mDialogPlus = DialogPlus.newDialog(mContext)
                .setContentHolder(new ViewHolder(personInfoDialogView))
                .setGravity(Gravity.BOTTOM)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_60)
                .setExpanded(false)
                .setCancelable(true)
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
            mDialogPlus.dismiss(false);
        }
        mKickListener = null;
    }

    private void showReportView(int userID) {
        Bundle bundle = new Bundle();
        bundle.putInt(REPORT_FROM_KEY, FORM_GAME);
        bundle.putInt(REPORT_USER_ID, userID);

        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder((BaseActivity) mContext, QuickFeedbackFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .addDataBeforeAdd(0, 1)
                        .addDataBeforeAdd(1, userID)
                        .setEnterAnim(R.anim.slide_in_bottom)
                        .setExitAnim(R.anim.slide_out_bottom)
                        .build());
    }

    public interface KickListener {
        void onClickKick(UserInfoModel userInfoModel);
    }

    public interface PersonCardClickListener {
        void onClickReport(int userID);

        void onClickKick(UserInfoModel userInfoModel);

        void onClickAvatar(String avatar);

        void onClickFollow(int userID, boolean isFriend, boolean isFollow);

        void onClickMessage(UserInfoModel userInfoModel);

        void onClickPhoto(PhotoModel photoModel, int position);

        void onClickOut();
    }
}
