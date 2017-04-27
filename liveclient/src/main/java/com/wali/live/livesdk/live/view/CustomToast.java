package com.wali.live.livesdk.live.view;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.base.global.GlobalData;
import com.wali.live.livesdk.R;

public class CustomToast {
    public static final int LENGTH_MAX = -1;
    private boolean mCanceled = false;
    private boolean isDurationSetted = false;
    private Toast mToast;
    private TextView tips;

    public CustomToast(int x, int y, String message) {
        View layout = LayoutInflater.from(GlobalData.app()).inflate(R.layout.share_toast_view, null);

        tips = (TextView) layout.findViewById(R.id.message);
        ImageView mNavImage = (ImageView) layout.findViewById(R.id.navi_image);
        mNavImage.setPadding(x, mNavImage.getPaddingTop(), mNavImage.getPaddingRight(), mNavImage.getPaddingBottom());
        tips.setText(message);
        if (TextUtils.isEmpty(message)) {
            layout.setVisibility(View.INVISIBLE);
        }
        mToast = new Toast(GlobalData.app());
        mToast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, y);
        mToast.setDuration(Toast.LENGTH_LONG);
        mToast.setView(layout);
        mCanceled = false;
    }

    public void setWidth(int width) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) tips.getLayoutParams();
        layoutParams.width = width;
        tips.setLayoutParams(layoutParams);
    }

    public void setGravity(int x, int y) {
        mToast.setGravity(Gravity.TOP | Gravity.LEFT, x, y);
    }

    public void show(int resId, int duration) {
        tips.setText(resId);
        mCanceled = false;
        showUntilCancel(duration);
    }


    public void show(String text, int duration) {
        tips.setText(text);
        mCanceled = false;
        showUntilCancel(duration);
    }

    /**
     * 隐藏Toast
     */
    public void hide() {
        mToast.cancel();
        mCanceled = true;
    }

    public boolean isShowing() {
        return !mCanceled;
    }

    public void showUntilCancel(final int duration) {
        if (mCanceled) {
            return;
        }
        mToast.show();
        tips.postDelayed(new Runnable() {
            public void run() {
                showUntilCancel(duration);
            }
        }, 1000);

        if (!isDurationSetted) {
            tips.postDelayed(new Runnable() {
                public void run() {
                    hide();
                }
            }, 1000 * duration);
            isDurationSetted = true;
        }

    }
}