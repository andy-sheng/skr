package com.component.dialog;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.view.AnimateClickListener;
import com.common.view.ex.ExTextView;
import com.component.busilib.R;

public class ShareWorksDialogView extends RelativeLayout {

    String mSongName;
    Listener mListener;
    boolean mContainSaveTips;

    ExTextView mTvTitle;
    View mDivider;
    ExTextView mTvText;
    TextView mTvQqShare;
    TextView mTvQzoneShare;
    TextView mTvWeixinShare;
    TextView mTvQuanShare;


    public ShareWorksDialogView(Context context, String songName, boolean containSaveTips, Listener listener) {
        super(context);
        this.mSongName = songName;
        this.mListener = listener;
        this.mContainSaveTips = containSaveTips;
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.share_works_panel, this);

        mTvTitle = (ExTextView) findViewById(R.id.tv_title);
        mDivider = (View) findViewById(R.id.divider);
        mTvText = (ExTextView) findViewById(R.id.tv_text);
        mTvQqShare = (TextView) findViewById(R.id.tv_qq_share);
        mTvQzoneShare = (TextView) findViewById(R.id.tv_qzone_share);
        mTvWeixinShare = (TextView) findViewById(R.id.tv_weixin_share);
        mTvQuanShare = (TextView) findViewById(R.id.tv_quan_share);

        mTvText.setText("分享《" + mSongName + "》");

        if (mContainSaveTips) {
            mTvTitle.setVisibility(VISIBLE);
            mDivider.setVisibility(VISIBLE);
        } else {
            mTvTitle.setVisibility(GONE);
            mDivider.setVisibility(GONE);
        }

        mTvQqShare.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View view) {
                if (mListener != null) {
                    mListener.onClickQQShare();
                }
            }
        });


        mTvQzoneShare.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View view) {
                if (mListener != null) {
                    mListener.onClickQZoneShare();
                }
            }
        });

        mTvWeixinShare.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View view) {
                if (mListener != null) {
                    mListener.onClickWeixinShare();
                }
            }
        });

        mTvQuanShare.setOnClickListener(new AnimateClickListener() {
            @Override
            public void click(View view) {
                if (mListener != null) {
                    mListener.onClickQuanShare();
                }
            }
        });

    }

    public interface Listener {
        void onClickQQShare();

        void onClickQZoneShare();

        void onClickWeixinShare();

        void onClickQuanShare();
    }
}
