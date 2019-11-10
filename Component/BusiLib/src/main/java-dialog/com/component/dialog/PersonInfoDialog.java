package com.component.dialog;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.common.base.BaseActivity;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.UserInfoServerApi;
import com.common.core.userinfo.event.RelationChangeEvent;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.component.busilib.R;
import com.component.busilib.recommend.RA;
import com.component.busilib.verify.SkrVerifyUtils;
import com.dialog.view.TipsDialogView;
import com.imagebrowse.big.BigImageBrowseFragment;
import com.module.RouterConstants;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.component.person.photo.model.PhotoModel;
import com.component.person.view.EditRemarkView;
import com.component.report.fragment.QuickFeedbackFragment;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;


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

    PersonInfoDialogView2 personInfoDialogView;

    private void init() {
        personInfoDialogView = new PersonInfoDialogView2(mActivity, mUserID, mShowKick, mShowInvite);
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
                    UserInfoManager.getInstance().mateRelation(userID, UserInfoManager.RA_UNBUILD, isFriend, mRoomID, null);
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

            @Override
            public void showUnSpFollowDialog(int userID) {
                if (mDialogPlus != null) {
                    mDialogPlus.dismiss(false);
                }
                showCancelSpFollowDialog(userID);
            }

            @Override
            public void showOpenVipDialog() {
                if (mDialogPlus != null) {
                    mDialogPlus.dismiss(false);
                }
                showVipOpenDialog();
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
        if (personInfoDialogView != null && personInfoDialogView.getMPersonMoreOpView() != null) {
            personInfoDialogView.getMPersonMoreOpView().dismiss();
        }
        mKickListener = null;
        mInviteDoubleListener = null;
        if (mDialogPlus != null) {
            mDialogPlus.dismiss(false);
        }

    }

    public void dismiss(boolean useAnimation) {
        U.getKeyBoardUtils().hideSoftInputKeyBoard(mActivity);
        if (personInfoDialogView != null && personInfoDialogView.getMPersonMoreOpView() != null) {
            personInfoDialogView.getMPersonMoreOpView().dismiss();
        }
        mKickListener = null;
        mInviteDoubleListener = null;

        if (mDialogPlus != null) {
            mDialogPlus.dismiss(useAnimation);
        }
    }

    private void showVipOpenDialog() {
        TipsDialogView tipsDialogView = new TipsDialogView.Builder(mActivity)
                .setMessageTip("非VIP最多特别关注3个用户，是否开通vip享受15人上限～")
                .setConfirmTip("开通VIP")
                .setCancelTip("取消")
                .setConfirmBtnClickListener(new DebounceViewClickListener() {
                    @Override
                    public void clickValid(View v) {
                        if (mDialogPlus != null) {
                            mDialogPlus.dismiss();
                        }
                        ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                                .withString("url", ApiManager.getInstance().findRealUrlByChannel("https://app.inframe.mobi/user/vip?title=1"))
                                .greenChannel().navigation();
                    }
                })
                .setCancelBtnClickListener(new DebounceViewClickListener() {
                    @Override
                    public void clickValid(View v) {
                        if (mDialogPlus != null) {
                            mDialogPlus.dismiss();
                        }
                    }
                })
                .build();

        mDialogPlus = DialogPlus.newDialog(mActivity)
                .setContentHolder(new ViewHolder(tipsDialogView))
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_50)
                .setInAnimation(R.anim.fade_in)
                .setOutAnimation(R.anim.fade_out)
                .setExpanded(false)
                .setGravity(Gravity.BOTTOM)
                .create();
        mDialogPlus.show();
    }

    private void showCancelSpFollowDialog(int userID) {
        TipsDialogView tipsDialogView = new TipsDialogView.Builder(mActivity)
                .setMessageTip("是否对ta关闭特别关注\n关闭后，你将无法收到关于ta的特别提醒啦")
                .setConfirmTip("取消")
                .setCancelTip("关闭")
                .setConfirmBtnClickListener(new DebounceViewClickListener() {
                    @Override
                    public void clickValid(View v) {
                        if (mDialogPlus != null) {
                            mDialogPlus.dismiss();
                        }
                        delSpecialFollow(userID);
                    }
                })
                .setCancelBtnClickListener(new DebounceViewClickListener() {
                    @Override
                    public void clickValid(View v) {
                        if (mDialogPlus != null) {
                            mDialogPlus.dismiss();
                        }
                    }
                })
                .build();

        mDialogPlus = DialogPlus.newDialog(mActivity)
                .setContentHolder(new ViewHolder(tipsDialogView))
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_50)
                .setInAnimation(R.anim.fade_in)
                .setOutAnimation(R.anim.fade_out)
                .setExpanded(false)
                .setGravity(Gravity.BOTTOM)
                .create();
        mDialogPlus.show();
    }

    private void delSpecialFollow(int userId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("toUserID", userId);

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));

        UserInfoServerApi mUserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi.class);
        ApiMethods.subscribe(mUserInfoServerApi.delSpecialFollow(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult obj) {
                if (obj.getErrno() == 0) {
                    Boolean isFriend = obj.getData().getJSONObject("relationInfo").getBooleanValue("isFriend");
                    Boolean isFollow = obj.getData().getJSONObject("relationInfo").getBooleanValue("isFollow");
                    Boolean isSpFollow = obj.getData().getJSONObject("relationInfo").getBooleanValue("isSPFollow");
                    EventBus.getDefault().post(new RelationChangeEvent(RelationChangeEvent.UN_SP_FOLLOW_TYPE, userId, isFriend, isFollow, isSpFollow));
                    U.getToastUtil().showShort("取消特别关注成功");
                } else {
                    U.getToastUtil().showShort(obj.getErrmsg());
                }
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                super.onNetworkError(errorType);
                U.getToastUtil().showShort("网络异常，请检查网络状态后重试");
            }
        }, (BaseActivity) mActivity);
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

        void showUnSpFollowDialog(int userID);

        void showOpenVipDialog();
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
