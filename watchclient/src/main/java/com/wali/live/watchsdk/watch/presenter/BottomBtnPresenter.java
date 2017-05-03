//package com.wali.live.watchsdk.watch.presenter;
//
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.RelativeLayout;
//
//import com.base.activity.assist.IBindActivityLIfeCycle;
//import com.com.base.log.MyLog;
//import com.com.base.utils.display.DisplayUtils;
//import com.wali.live.watchsdk.R;
//
///**
// * Created by milive on 16/12/5.
// * <p>
// * 底部四个按钮的presenter 由于复用极低，所以简单实现
// */
//
//public class BottomBtnPresenter implements IBindActivityLIfeCycle {
//
//    private static final String TAG = BottomBtnPresenter.class.getSimpleName();
//
//    private static final int BTN_CNT = 2;
//
//    //    public static final int SHARE_BTN = 0;
//
//    private static final int BTN_MARGIN = DisplayUtils.dip2px(3.33f);
//
//    public static final int COMMENT_BTN = 0;
//    public static final int GIFT_BTN = 1;
//
//    protected final int[] mBtnResIdSet = new int[]{
////            R.id.share_btn,
//            R.id.send_comment_btn,
//            R.id.gift_button,
//    };
//    protected View[] mBtnSet = new View[BTN_CNT];
//
//    protected boolean mIsLandscape = false;
//
//    protected ViewGroup mBottomViewGroup;
//
//    public BottomBtnPresenter(View[] btnSet,ViewGroup bottomViewGroup) {
//        mBtnSet = btnSet;
//        mBottomViewGroup = bottomViewGroup;
//    }
//
//
//    public void onOrientation(boolean isLandscape) {
//        mIsLandscape = isLandscape;
//        orientBottomViewGroup(isLandscape);
//        orientChild(mIsLandscape);
//    }
//
//    private void orientBottomViewGroup(boolean isLandscape){
//        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mBottomViewGroup.getLayoutParams();
//        if(isLandscape){
//            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,RelativeLayout.TRUE);
//            layoutParams.bottomMargin = BTN_MARGIN;
//        }else{
//            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
//            layoutParams.bottomMargin = 0;
//        }
//        mBottomViewGroup.setLayoutParams(layoutParams);
//    }
//
//    private void orientChild(boolean isLandscape) {
//        if (isLandscape) {
//            int[] bottomSet = getBottomBtnSetLand();
//            if (bottomSet != null) {
//                for (int i = 0, guardId = 0; i < bottomSet.length; ++i) {
//                    alignChildToBottom(getActionBtn(bottomSet[i]), guardId);
//                    setBtnVisibility(bottomSet[i], View.VISIBLE);
//                    guardId = getBtnResId(bottomSet[i]);
//                }
//            }
//        } else {
//            int[] leftSet = getLeftBtnSetPort();
//            if (leftSet != null) {
//                for (int i = 0, guardId = 0; i < leftSet.length; ++i) {
//                    alignChildToLeft(getActionBtn(leftSet[i]), guardId);
//                    setBtnVisibility(leftSet[i], View.VISIBLE);
//                    guardId = getBtnResId(leftSet[i]);
//                }
//            }
//            int[] rightSet = getRightBtnSetPort();
//            if (rightSet != null) {
//                for (int i = 0, guardId = 0; i < rightSet.length; ++i) {
//                    alignChildToRight(getActionBtn(rightSet[i]), guardId);
//                    setBtnVisibility(rightSet[i], View.VISIBLE);
//                    guardId = getBtnResId(rightSet[i]);
//                }
//            }
//        }
//    }
//
//    protected int[] getBottomBtnSetLand() {
//        return new int[]{
////                SHARE_BTN,
//                GIFT_BTN,
//                COMMENT_BTN,
//        };
//    }
//
//
//    protected int[] getLeftBtnSetPort() {
//        return new int[]{
//                COMMENT_BTN
//        };
//    }
//
//    protected int[] getRightBtnSetPort() {
//        return new int[]{
//                GIFT_BTN,
////                SHARE_BTN
//        };
//    }
//
//
//    protected void alignChildToBottom(View view, int guardId) {
//        if (view == null) {
//            MyLog.e(TAG, "alignChildToBottom, but view is null");
//            return;
//        }
//        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
//        resetChildLayout(layoutParams);
//        if (guardId == 0) {
//            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
//            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
//        } else {
//            layoutParams.addRule(RelativeLayout.ABOVE, guardId);
//            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
//        }
//        view.setLayoutParams(layoutParams);
//    }
//
//
//
//    private void resetChildLayout(RelativeLayout.LayoutParams lp) {
//        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
//        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
//        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
//        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
//        lp.addRule(RelativeLayout.LEFT_OF, 0);
//        lp.addRule(RelativeLayout.RIGHT_OF, 0);
//        lp.addRule(RelativeLayout.ABOVE, 0);
//        lp.addRule(RelativeLayout.BELOW, 0);
//        lp.bottomMargin = BTN_MARGIN;
//        lp.topMargin = BTN_MARGIN;
//        lp.rightMargin = BTN_MARGIN;
//        lp.leftMargin = BTN_MARGIN;
//    }
//
//    protected View getActionBtn(int btnType) {
//        if (btnType < 0 || btnType >= BTN_CNT) {
//            return null;
//        }
//        return mBtnSet[btnType];
//    }
//
//    protected int getBtnResId(int btnType) {
//        if (btnType < 0 || btnType >= BTN_CNT) {
//            return 0;
//        }
//        return mBtnResIdSet[btnType];
//    }
//
//    protected void setBtnVisibility(int btnType, int visibility) {
//        View view = getActionBtn(btnType);
//        if (view != null) {
//            view.setVisibility(visibility);
//        }
//    }
//
//
//    protected void alignChildToLeft(View view, int guardId) {
//        if (view == null) {
//            MyLog.e(TAG, "alignChildToLeft, but view is null");
//            return;
//        }
//        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
//        resetChildLayout(layoutParams);
//        if (guardId == 0) {
//            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
//        } else {
//            layoutParams.addRule(RelativeLayout.RIGHT_OF, guardId);
//        }
//        view.setLayoutParams(layoutParams);
//    }
//
//    protected void alignChildToRight(View view, int guardId) {
//        if (view == null) {
//            MyLog.e(TAG, "alignChildToRight, but view is null");
//            return;
//        }
//        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
//        resetChildLayout(layoutParams);
//        if (guardId == 0) {
//            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
//        } else {
//            layoutParams.addRule(RelativeLayout.LEFT_OF, guardId);
//        }
//        view.setLayoutParams(layoutParams);
//    }
//
//
//    @Override
//    public void onActivityDestroy() {
//
//    }
//
//    @Override
//    public void onActivityCreate() {
//
//    }
//
//
//}
