package com.zq.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.core.kouling.SkrKouLingUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.component.busilib.R;
import com.module.common.ICallback;

import static com.zq.dialog.InviteFriendDialog.INVITE_DOUBLE_GAME;
import static com.zq.dialog.InviteFriendDialog.INVITE_GRAB_FRIEND;
import static com.zq.dialog.InviteFriendDialog.INVITE_GRAB_GAME;

public class InviteFriendDialogView extends RelativeLayout {
    public final static String TAG = "InviteFriendDialogView";

    ExTextView mTvTitle;
    TextView mTvKouling;
    ExTextView mTvText;
    TextView mTvQqShare;
    TextView mTvWeixinShare;

    private int mType;      //类别
    private int mMediaType;
    private int mGameId;    //游戏id
    private String mKouLingToken = "";  //口令

    Listener mListener;

    public InviteFriendDialogView(Context context, int type, int gameId, int mediaType, String kouLingToken) {
        super(context);
        this.mType = type;
        this.mGameId = gameId;
        this.mMediaType = mediaType;
        this.mKouLingToken = kouLingToken;
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.invite_friend_panel, this);

        mTvTitle = (ExTextView) this.findViewById(R.id.tv_title);
        mTvKouling = (TextView) this.findViewById(R.id.tv_kouling);
        mTvText = (ExTextView) this.findViewById(R.id.tv_text);
        mTvQqShare = (TextView) this.findViewById(R.id.tv_qq_share);
        mTvWeixinShare = (TextView) this.findViewById(R.id.tv_weixin_share);

        if (!TextUtils.isEmpty(mKouLingToken)) {
            mTvKouling.setText(mKouLingToken);
        } else {
            if (mType == INVITE_GRAB_GAME) {
                if (mGameId == 0) {
                    MyLog.w(TAG, "init" + " context=" + context + "mGameId = 0");
                    return;
                }
                SkrKouLingUtils.genNormalJoinGrabGameKouling((int) MyUserInfoManager.getInstance().getUid(), mGameId, mMediaType, new ICallback() {
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
                SkrKouLingUtils.genDoubleJoinGrabGameKouling((int) MyUserInfoManager.getInstance().getUid(), mGameId, mMediaType, new ICallback() {
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
                SkrKouLingUtils.genNormalReqFollowKouling((int) MyUserInfoManager.getInstance().getUid(), new ICallback() {
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
