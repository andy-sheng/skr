package com.wali.live.watchsdk.fans.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.wali.live.watchsdk.R;

public class FansProgressView extends RelativeLayout {
    private final String TAG = "FansProgressView";
    private ProgressBar mProgressBar;
    private TextView mProgressBarTextView;
    private Drawable drawable = null;
    private Drawable drawable2 = null;

    public FansProgressView(Context context) {
        super(context);
        init(context);
    }

    public FansProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FansProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.vfans_progress, this);
        mProgressBar = (ProgressBar) this.findViewById(R.id.progress);
        mProgressBarTextView = (TextView) this.findViewById(R.id.progress_tv);
    }

    public void setProgress(int progress, int max) {
        if ((double) progress / (double) max < 0.04) { //太小的话都显示不出来，所以设置最小0.2
            if (drawable == null) {
                drawable = GlobalData.app().getResources().getDrawable(R.drawable.vfans_pet_value_progress_min);
            }
            mProgressBar.setProgressDrawable(drawable);
        } else {
            if (drawable2 == null) {
                drawable2 = GlobalData.app().getResources().getDrawable(R.drawable.vfans_pet_value_progress);
            }
            mProgressBar.setProgressDrawable(drawable2);
        }
        mProgressBar.setMax(max);
        mProgressBar.setProgress(progress);
        mProgressBar.invalidate();
        mProgressBarTextView.setText(progress + "/" + max);
    }
}
