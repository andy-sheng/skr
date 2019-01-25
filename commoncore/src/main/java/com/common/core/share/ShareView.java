package com.common.core.share;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.core.R;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

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

        RxView.clicks(mTvWechatShare)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) {
                if(mOnClickShareListener != null){
                    mOnClickShareListener.click(SharePlatform.WEIXIN);
                }
            }
        });

        RxView.clicks(mTvWechatCircleShare)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        if(mOnClickShareListener != null){
                            mOnClickShareListener.click(SharePlatform.WEIXIN_CIRCLE);
                        }
                    }
                });

        RxView.clicks(mTvQqShare)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
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