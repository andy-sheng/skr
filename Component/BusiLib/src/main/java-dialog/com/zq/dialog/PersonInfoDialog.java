package com.zq.dialog;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;

import com.common.base.BaseActivity;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.component.busilib.R;
import com.component.busilib.recommend.RA;
import com.component.busilib.verify.SkrVerifyUtils;
import com.imagebrowse.big.BigImageBrowseFragment;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.zq.person.model.PhotoModel;
import com.zq.person.view.EditRemarkView;
import com.zq.report.fragment.QuickFeedbackFragment;

import java.util.HashMap;


// 个人信息卡片
public class PersonInfoDialog {

    Activity mActivity;
    int mFrom;
    int mUserID;
    boolean mShowKick;
    boolean mShowInvite = true;
    int mRoomID;

    SkrVerifyUtils mRealNameVerifyUtils = new SkrVerifyUtils();

    KickListener mKickListener;
    InviteDoubleListener mInviteDoubleListener;
    DialogPlus mDialogPlus;

    private PersonInfoDialog(Builder builder) {
        mActivity = builder.mActivity;
        mFrom = builder.mFrom;
        mUserID = builder.mUserID;
        mShowKick = builder.mShowKick;
        mShowInvite = builder.mShowInvite;
        mRoomID = builder.mRoomID;
        mKickListener = builder.mKickListener;
        mInviteDoubleListener = builder.mInviteDoubleListener;

        init();
    }

    private void init() {
        PersonInfoDialogView2 personInfoDialogView = new PersonInfoDialogView2(mActivity, mUserID, mShowKick, mShowInvite);
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
                BigImageBrowseFragment.open(false, (BaseActivity) mActivity, avatar);
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
                    if (RA.hasTestList()) {
                        HashMap map = new HashMap();
                        map.put("testList", RA.getTestList());
                        StatisticsAdapter.recordCountEvent("ra", "follow", map);
                    }
                }
            }

            @Override
            public void onClickPhoto(PhotoModel photoModel, int position) {

            }

            @Override
            public void onClickOut() {
                mDialogPlus.dismiss();
            }

            @Override
            public void onClickRemark(UserInfoModel userInfoModel) {
                if (mDialogPlus != null) {
                    mDialogPlus.dismiss(false);
                }

                showRemarkDialog(userInfoModel);
            }

            @Override
            public void onClickDoubleInvite(final UserInfoModel userInfoModel) {
                if (mDialogPlus != null) {
                    mDialogPlus.dismiss(false);
                }
                if (mInviteDoubleListener != null) {
                    mRealNameVerifyUtils.checkJoinDoubleRoomPermission(new Runnable() {
                        @Override
                        public void run() {
                            mInviteDoubleListener.onClickDoubleInvite(userInfoModel);
                        }
                    });
                }
            }
        });

        mDialogPlus = DialogPlus.newDialog(mActivity)
                .setContentHolder(new ViewHolder(personInfoDialogView))
                .setGravity(Gravity.BOTTOM)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_60)
                .setExpanded(false)
                .setCancelable(true)
                .create();
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
        U.getKeyBoardUtils().hideSoftInputKeyBoard(mActivity);
        mKickListener = null;
        mInviteDoubleListener = null;
        if (mDialogPlus != null) {
            mDialogPlus.dismiss(false);
        }

    }

    public void dismiss(boolean useAnimation) {
        U.getKeyBoardUtils().hideSoftInputKeyBoard(mActivity);
        mKickListener = null;
        mInviteDoubleListener = null;

        if (mDialogPlus != null) {
            mDialogPlus.dismiss(useAnimation);
        }
    }

    private void showRemarkDialog(final UserInfoModel userInfoModel) {
        EditRemarkView editRemarkView = new EditRemarkView((BaseActivity) mActivity, userInfoModel.getNickname(), userInfoModel.getNicknameRemark(null));
        editRemarkView.setListener(new EditRemarkView.Listener() {
            @Override
            public void onClickCancel() {
                if (mDialogPlus != null) {
                    mDialogPlus.dismiss();
                }
                U.getKeyBoardUtils().hideSoftInputKeyBoard(mActivity);
            }

            @Override
            public void onClickSave(String remarkName) {
                if (mDialogPlus != null) {
                    mDialogPlus.dismiss();
                }
                U.getKeyBoardUtils().hideSoftInputKeyBoard(mActivity);
                if (TextUtils.isEmpty(remarkName) && TextUtils.isEmpty(userInfoModel.getNicknameRemark())) {
                    // 都为空
                    return;
                } else if (!TextUtils.isEmpty(userInfoModel.getNicknameRemark()) && (userInfoModel.getNicknameRemark()).equals(remarkName)) {
                    // 相同
                    return;
                } else {
                    UserInfoManager.getInstance().updateRemark(remarkName, userInfoModel.getUserId());
                }
            }
        });

        mDialogPlus = DialogPlus.newDialog(mActivity)
                .setContentHolder(new ViewHolder(editRemarkView))
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_50)
                .setInAnimation(R.anim.fade_in)
                .setOutAnimation(R.anim.fade_out)
                .setExpanded(false)
                .setGravity(Gravity.BOTTOM)
                .setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(@NonNull DialogPlus dialog) {
                        U.getKeyBoardUtils().hideSoftInputKeyBoard(mActivity);
                    }
                })
                .create();
        mDialogPlus.show();
    }


    private void showReportView(int userID) {
        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder((BaseActivity) mActivity, QuickFeedbackFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .addDataBeforeAdd(0, mFrom)
                        .addDataBeforeAdd(1, QuickFeedbackFragment.REPORT)
                        .addDataBeforeAdd(2, userID)
                        .setEnterAnim(R.anim.slide_in_bottom)
                        .setExitAnim(R.anim.slide_out_bottom)
                        .build());
    }

    public interface KickListener {
        void onClickKick(UserInfoModel userInfoModel);
    }

    public interface InviteDoubleListener {
        void onClickDoubleInvite(UserInfoModel userInfoModel);
    }

    public interface PersonCardClickListener {
        void onClickReport(int userID);

        void onClickKick(UserInfoModel userInfoModel);

        void onClickAvatar(String avatar);

        void onClickFollow(int userID, boolean isFriend, boolean isFollow);

        void onClickPhoto(PhotoModel photoModel, int position);

        void onClickOut();

        void onClickRemark(UserInfoModel userInfoModel);

        void onClickDoubleInvite(UserInfoModel userInfoModel);
    }

    public static final class Builder {
        private Activity mActivity;
        private int mFrom;
        private int mUserID;
        private boolean mShowKick;
        private boolean mShowInvite;
        private int mRoomID;
        private KickListener mKickListener;
        private InviteDoubleListener mInviteDoubleListener;

        public Builder(Activity activity, int from, int userID, boolean showKick, boolean showInvite) {
            mActivity = activity;
            mFrom = from;
            mUserID = userID;
            mShowKick = showKick;
            mShowInvite = showInvite;
        }

        public Builder setRoomID(int roomID) {
            mRoomID = roomID;
            return this;
        }

        public Builder setKickListener(KickListener val) {
            mKickListener = val;
            return this;
        }

        public Builder setInviteDoubleListener(InviteDoubleListener val) {
            mInviteDoubleListener = val;
            return this;
        }

        public PersonInfoDialog build() {
            return new PersonInfoDialog(this);
        }
    }
}
