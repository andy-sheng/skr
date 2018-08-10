package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.wali.live.watchsdk.R;

/**
 * Created by zhujianning on 18-8-10.
 */

public class WatchGameMenuDialog extends PopupWindow implements View.OnClickListener {

    private static final int WIDTH_DIALOG = DisplayUtils.dip2px(77.33f);

    private TextView mOptVoiceTv;
    private TextView mOptInsterestingTv;
    private TextView mOptReportTv;
    private final View mRootView;

    public WatchGameMenuDialog(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootView = inflater.inflate(R.layout.dialog_watch_game_menu, null);
        mRootView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        this.setContentView(mRootView);
        this.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
//         设置SelectPicPopupWindow弹出窗体可点击
        this.setOutsideTouchable(true);
        this.setFocusable(true);
        // 刷新状态
        this.update();
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(context.getResources().getColor(R.color.transparent));
        this.setBackgroundDrawable(dw);
        this.setAnimationStyle(R.style.popwin_anim_style);

        bindView(mRootView);
    }

    private void bindView(View view) {
        mOptVoiceTv = (TextView) view.findViewById(R.id.btn_0_tv);
        mOptInsterestingTv = (TextView) view.findViewById(R.id.btn_1_tv);
        mOptReportTv = (TextView) view.findViewById(R.id.btn_2_tv);

        mOptVoiceTv.setOnClickListener(this);
        mOptInsterestingTv.setOnClickListener(this);
        mOptReportTv.setOnClickListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void show(View parents, View view){
        int[] ints = calculateWindowPos(view);
        this.showAtLocation(parents, Gravity.NO_GRAVITY, ints[0]- WIDTH_DIALOG, ints[1]);
    }

    private int[] calculateWindowPos(View view) {
        int windowPos[] = new int[2];
        int loc[] = new int[2];
        view.getLocationInWindow(loc);
        windowPos[0] = loc[0];
        windowPos[1] = loc[1];
        return windowPos;
    }

    public void tryDismiss() {
        this.dismiss();
    }

    @Override
    public void onClick(View v) {
        if(mListener == null) {
            return;
        }

        int id = v.getId();
        if(id == R.id.btn_0_tv) {
            mListener.onVoiceControlBtnClick();
        } else if(id == R.id.btn_1_tv) {
            mListener.onNotInterestBtnClick();
        } else if(id == R.id.btn_2_tv) {
            mListener.onReportBtnClick();
        }
    }

    private OnWatchGameMenuDialogListener mListener;

    public void setListener(OnWatchGameMenuDialogListener l) {
        this.mListener = l;
    }

    public interface OnWatchGameMenuDialogListener {
        void onVoiceControlBtnClick();

        void onNotInterestBtnClick();

        void onReportBtnClick();
    }
}
