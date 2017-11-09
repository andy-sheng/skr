package com.wali.live.watchsdk.fans.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.wali.live.watchsdk.R;

/**
 * Created by anping on 17/6/12.
 */
public class VfansProgressView extends RelativeLayout {
    private ProgressBar mProgressBar;
    private TextView mProgressTv;

    private Drawable mDrawable = null;
    private Drawable mDrawable2 = null;

    public VfansProgressView(Context context) {
        super(context);
        init(context);
    }

    public VfansProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VfansProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.fans_progress_view, this);
        mProgressBar = (ProgressBar) this.findViewById(R.id.progress);
        mProgressTv = (TextView) this.findViewById(R.id.progress_tv);
    }


    public void setProgress(int progress, int max) {
        if ((double) progress / (double) max < 0.04) { //太小的话都显示不出来，所以设置最小0.2
            if (mDrawable == null) {
                mDrawable = GlobalData.app().getResources().getDrawable(R.drawable.vfans_pet_value_progress2);
            }
            mProgressBar.setProgressDrawable(mDrawable);
        } else {
            if (mDrawable2 == null) {
                mDrawable2 = GlobalData.app().getResources().getDrawable(R.drawable.vfans_pet_value_progress);
            }
            mProgressBar.setProgressDrawable(mDrawable2);
        }
        mProgressBar.setMax(max);
        mProgressBar.setProgress(progress);
        mProgressBar.invalidate();
        mProgressTv.setText(progress + "/" + max);
    }
}
