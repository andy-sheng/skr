package com.component.dialog;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.core.kouling.SkrKouLingUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExConstraintLayout;
import com.common.view.ex.ExTextView;
import com.component.busilib.R;
import com.module.common.ICallback;

import static com.component.dialog.InviteFriendDialog.INVITE_DOUBLE_GAME;
import static com.component.dialog.InviteFriendDialog.INVITE_GRAB_FRIEND;
import static com.component.dialog.InviteFriendDialog.INVITE_GRAB_GAME;
import static com.component.dialog.InviteFriendDialog.INVITE_MIC_GAME;
import static com.component.dialog.InviteFriendDialog.INVITE_PARTY_GAME;

public class InviteFriendDialogView extends ConstraintLayout {
    public final String TAG = "InviteFriendDialogView";

    ExTextView mTvTitle;
    TextView mTvKouling;
    ExTextView mTvText;
    TextView mTvQqShare;
    TextView mTvWeixinShare;

    private int mType;      //类别
    private int mMediaType;
    private int mGameId;    //游戏id
    private int mTagID;
    private String mKouLingToken = "";  //口令

    Listener mListener;

    public InviteFriendDialogView(Context context, int type, int gameId, int tagID, int mediaType, String kouLingToken) {
        super(context);
        this.mType = type;
        this.mGameId = gameId;
        this.mTagID = tagID;
        this.mMediaType = mediaType;
        this.mKouLingToken = kouLingToken;
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.invite_friend_panel, this);

        mTvTitle = this.findViewById(R.id.tv_title);
        mTvKouling = this.findViewById(R.id.tv_kouling);
        mTvText = this.findViewById(R.id.tv_text);
        mTvQqShare = this.findViewById(R.id.tv_qq_share);
        mTvWeixinShare = this.findViewById(R.id.tv_weixin_share);

        if (!TextUtils.isEmpty(mKouLingToken)) {
            mTvKouling.setText(mKouLingToken);
        } else {
            if (mType == INVITE_GRAB_GAME) {
                if (mGameId == 0) {
                    MyLog.w(TAG, "init" + " context=" + context + "mGameId = 0");
                    return;
                }
                SkrKouLingUtils.genNormalJoinGrabGameKouling((int) MyUserInfoManager.INSTANCE.getUid(), mGameId, mTagID, mMediaType, new ICallback() {
                    @Override
                    public void onSucess(Object obj) {
                        if (obj != null) {
                            mKouLingToken = (String) obj;
                            mTvKouling.setText(mKouLingToken);
                        } else {
                            U.getToastUtil().showShort("口令生成失败");
                        }
                    }

                    @Override
                    public void onFailed(Object obj, int errcode, String message) {
                        U.getToastUtil().showShort("口令生成失败");
                    }
                });
            } else if (mType == INVITE_DOUBLE_GAME) {
                if (mGameId == 0) {
                    MyLog.w(TAG, "init" + " context=" + context + "mGameId = 0");
                    return;
                }
                SkrKouLingUtils.genJoinDoubleGameKouling((int) MyUserInfoManager.INSTANCE.getUid(), mGameId, mMediaType, new ICallback() {
                    @Override
                    public void onSucess(Object obj) {
                        if (obj != null) {
                            mKouLingToken = (String) obj;
                            mTvKouling.setText(mKouLingToken);
                        } else {
                            U.getToastUtil().showShort("口令生成失败");
                        }
                    }

                    @Override
                    public void onFailed(Object obj, int errcode, String message) {
                        U.getToastUtil().showShort("口令生成失败");
                    }
                });
            } else if (mType == INVITE_GRAB_FRIEND) {
                SkrKouLingUtils.genNormalReqFollowKouling((int) MyUserInfoManager.INSTANCE.getUid(), new ICallback() {
                    @Override
                    public void onSucess(Object obj) {
                        if (obj != null) {
                            mKouLingToken = (String) obj;
                            mTvKouling.setText(mKouLingToken);
                        } else {
                            U.getToastUtil().showShort("口令生成失败");
                        }
                    }

                    @Override
                    public void onFailed(Object obj, int errcode, String message) {
                        U.getToastUtil().showShort("口令生成失败");
                    }
                });
            } else if (mType == INVITE_MIC_GAME) {
                SkrKouLingUtils.genJoinMicRoomKouling((int) MyUserInfoManager.INSTANCE.getUid(), mGameId, new ICallback() {
                    @Override
                    public void onSucess(Object obj) {
                        if (obj != null) {
                            mKouLingToken = (String) obj;
                            mTvKouling.setText(mKouLingToken);
                        } else {
                            U.getToastUtil().showShort("口令生成失败");
                        }
                    }

                    @Override
                    public void onFailed(Object obj, int errcode, String message) {
                        U.getToastUtil().showShort("口令生成失败");
                    }
                });
            }else if (mType == INVITE_PARTY_GAME) {
                SkrKouLingUtils.genJoinPartyGameKouling((int) MyUserInfoManager.INSTANCE.getUid(), mGameId,mMediaType, new ICallback() {
                    @Override
                    public void onSucess(Object obj) {
                        if (obj != null) {
                            mKouLingToken = (String) obj;
                            mTvKouling.setText(mKouLingToken);
                        } else {
                            U.getToastUtil().showShort("口令生成失败");
                        }
                    }

                    @Override
                    public void onFailed(Object obj, int errcode, String message) {
                        U.getToastUtil().showShort("口令生成失败");
                    }
                });
            }
        }

        mTvQqShare.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    String text = "";
                    if (mType == INVITE_GRAB_FRIEND) {
                        text = SkrKouLingUtils.genReqFollowKouling(mKouLingToken);
                    } else if (mType == INVITE_GRAB_GAME) {
                        text = SkrKouLingUtils.genJoinGrabGameKouling(mKouLingToken);
                    } else if (mType == INVITE_DOUBLE_GAME) {
                        text = SkrKouLingUtils.genJoinDoubleGameKouling(mKouLingToken);
                    } else if (mType == INVITE_MIC_GAME) {
                        text = SkrKouLingUtils.genJoinMicRoomText(mKouLingToken);
                    }else if (mType == INVITE_PARTY_GAME) {
                        text = SkrKouLingUtils.genJoinPartyRoomText(mKouLingToken);
                    }

                    mListener.onClickQQShare(text);
                }
            }
        });

        mTvWeixinShare.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    String text = "";
                    if (mType == INVITE_GRAB_FRIEND) {
                        text = SkrKouLingUtils.genReqFollowKouling(mKouLingToken);
                    } else if (mType == INVITE_GRAB_GAME) {
                        text = SkrKouLingUtils.genJoinGrabGameKouling(mKouLingToken);
                    } else if (mType == INVITE_DOUBLE_GAME) {
                        text = SkrKouLingUtils.genJoinDoubleGameKouling(mKouLingToken);
                    } else if (mType == INVITE_MIC_GAME) {
                        text = SkrKouLingUtils.genJoinMicRoomText(mKouLingToken);
                    }else if (mType == INVITE_PARTY_GAME) {
                        text = SkrKouLingUtils.genJoinPartyRoomText(mKouLingToken);
                    }

                    mListener.onClickWeixinShare(text);
                }
            }
        });
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    public interface Listener {
        void onClickQQShare(String text);

        void onClickWeixinShare(String text);
    }
}
