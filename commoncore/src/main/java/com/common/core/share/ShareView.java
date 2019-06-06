package com.common.core.share;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.common.core.R;
import com.common.view.DebounceViewClickListener;

public class ShareView extends FrameLayout {

    TextView mTvWechatShare;
    TextView mTvWechatCircleShare;
    TextView mTvQqShare;

    OnClickShareListener mOnClickShareListener;

    public ShareView(Context context) {
        this(context, null);
    }

    public ShareView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShareView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, 0);
        init();
    }

    public void setOnClickShareListener(OnClickShareListener onClickShareListener){
        mOnClickShareListener = onClickShareListener;
    }

    public void init(){
        ShareManager.init();
        inflate(getContext(), R.layout.share_view_layout, this);

        mTvWechatShare = (TextView)findViewById(R.id.tv_wechat_share);
        mTvWechatCircleShare = (TextView)findViewById(R.id.tv_wechat_circle_share);
        mTvQqShare = (TextView)findViewById(R.id.tv_qq_share);

        mTvWechatShare.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if(mOnClickShareListener != null){
                    mOnClickShareListener.click(SharePlatform.WEIXIN);
                }
            }
        });

        mTvWechatCircleShare.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if(mOnClickShareListener != null){
                    mOnClickShareListener.click(SharePlatform.WEIXIN_CIRCLE);
                }
            }
        });

        mTvQqShare.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if(mOnClickShareListener != null){
                    mOnClickShareListener.click(SharePlatform.QQ);
                }
            }
        });
    }

    public interface OnClickShareListener{
        void click(SharePlatform sharePlatform);
    }
}