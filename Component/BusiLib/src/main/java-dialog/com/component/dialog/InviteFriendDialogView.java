package com.component.dialog;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.component.busilib.R;

public class InviteFriendDialogView extends ConstraintLayout {
    public final String TAG = "InviteFriendDialogView";

    ExTextView mTvTitle;
    TextView mTvKouling;
    ExTextView mTvText;
    TextView mTvQqShare;
    TextView mTvWeixinShare;
    private String mKouLingToken = "";  //口令

    Listener mListener;

    InviteFriendDialog.IInviteDialogCallBack mInviteCallBack;

    public InviteFriendDialogView(Context context, String kouLingToken, InviteFriendDialog.IInviteDialogCallBack inviteCallBack) {
        super(context);
        this.mKouLingToken = kouLingToken;
        this.mInviteCallBack = inviteCallBack;
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
            ApiMethods.subscribe(mInviteCallBack.getKouLingTokenObservable(), new ApiObserver<ApiResult>() {
                @Override
                public void process(ApiResult obj) {
                    if (obj.getErrno() == 0) {
                        mKouLingToken = obj.getData().getString("token");
                        mTvKouling.setText(mKouLingToken);
                    } else {
                        U.getToastUtil().showShort(obj.getErrmsg());
                    }
                }
            });
        }

        mTvQqShare.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    String text = mInviteCallBack.getInviteDialogText(mKouLingToken);
                    mListener.onClickQQShare(text);
                }
            }
        });

        mTvWeixinShare.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    String text = mInviteCallBack.getInviteDialogText(mKouLingToken);
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
