package com.wali.live.livesdk.live.liveshow.presenter.button;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import com.base.log.MyLog;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.livesdk.live.liveshow.view.button.PlusControlBtnView;

import rx.Subscription;

import static com.wali.live.componentwrapper.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;

/**
 * Created by yangli on 2017/03/08.
 *
 * @module 直播加按钮表现
 */
public class PlusControlBtnPresenter extends ComponentPresenter<PlusControlBtnView.IView>
        implements PlusControlBtnView.IPresenter {
    private static final String TAG = "PlusControlBtnPresenter";

    public static final int STATE_IDLE = 0;
    public static final int STATE_LINK_MIC_WAITING = 1;
    public static final int STATE_LINK_MIC_SPEAKING = 2;
    public static final int STATE_SHARING_PHOTO = 3;
    public static final int STATE_SHARING_VIDEO = 4;
    public static final int STATE_LINK_DEVICE = 5;

    private static final int MAX_LINK_MIC_WAITING_TIME = 45;

    private Context mContext;

    private Subscription mCountDownSub;
    private int mState = STATE_IDLE;
    private String mInfoText = "";
    private boolean mIsLandscape = false;
    private boolean mInsertNewline = false;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public PlusControlBtnPresenter(
            @NonNull IEventController controller,
            @NonNull Context context) {
        super(controller);
        mContext = context;
        String country = mContext.getResources().getConfiguration().locale.getCountry();
        mInsertNewline = "CN".equals(country) || "TW".equals(country);
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();
        stopCountingDown();
    }

    @Override
    public void notifyInfoViewClick() {
        switch (mState) {
            case STATE_LINK_MIC_WAITING:
            case STATE_LINK_MIC_SPEAKING: // fall through
//                mPresenter.onEndLinkMicPrompt(state);
                break;
            case STATE_SHARING_PHOTO:
//                mPresenter.onEndSharePhotoPrompt();
                break;
            case STATE_SHARING_VIDEO:
//                mPresenter.onEndShareVideoPrompt();
                break;
            case STATE_LINK_DEVICE:
//                mPresenter.onCloseLinkDevice();
            default:
                break;
        }
    }

//    private void updateWaitingTime(int count) {
//        setInfoText(mContext.getResources().getString(
//                R.string.live_line_waiting, count));
//    }

    private void setInfoText(@StringRes int id) {
        setInfoText(mContext.getResources().getString(id));
    }

    private void setInfoText(String infoText) {
        mInfoText = infoText;
        adjustTextView();
    }

    private boolean isNumber(char val) {
        return val >= '0' && val <= '9';
    }

    private void adjustTextView() {
        if (!mInsertNewline || TextUtils.isEmpty(mInfoText)) {
            mView.setInfoText(mInfoText);
            return;
        }
        if (mIsLandscape) {
            StringBuilder stringBuilder = new StringBuilder("");
            char prev = mInfoText.charAt(0);
            stringBuilder.append(prev);
            for (int i = 1; i < mInfoText.length(); ++i) {
                char curr = mInfoText.charAt(i);
                if ((!isNumber(prev) || !isNumber(curr)) && prev != '\n' && curr != '\n') {
                    stringBuilder.append('\n'); // if neither prev nor curr is not a number, add a '\n'
                }
                stringBuilder.append(curr);
                prev = curr;
            }
            mInfoText = stringBuilder.toString();
        } else {
            mInfoText = mInfoText.replace("\n", "");
        }
        mView.setInfoText(mInfoText);
    }

//    private void switchToIdle() {
//        MyLog.w(TAG, "switchToIdle mState=" + mState);
//        if (mState != STATE_IDLE) {
//            stopCountingDown();
//            mState = STATE_IDLE;
//            setInfoText("");
//            mView.exitInfoMode();
//        }
//    }

//    private void switchToLinkMicWaiting(boolean showCountDown) {
//        if (mState == STATE_IDLE) {
//            MyLog.w(TAG, "switchToLinkMicWaiting showCountDown=" + showCountDown);
//            mState = STATE_LINK_MIC_WAITING;
//            mView.enterInfoMode();
//            if (showCountDown) {
//                mView.exitInfoMode();
//                updateWaitingTime(MAX_LINK_MIC_WAITING_TIME);
//                startCountingDown(MAX_LINK_MIC_WAITING_TIME);
//            }
//        } else {
//            MyLog.e(TAG, "switchToLinkMicWaiting, but mState=" + mState);
//        }
//    }

//    private void switchToLinkMicSpeaking() {
//        if (mState == STATE_LINK_MIC_WAITING) {
//            MyLog.w(TAG, "switchToLinkMicSpeaking");
//            mState = STATE_LINK_MIC_SPEAKING;
//            stopCountingDown();
//            setInfoText(R.string.live_line_close);
//        } else {
//            MyLog.e(TAG, "switchToLinkMicSpeaking, but mState=" + mState);
//        }
//    }

//    private void switchToDeviceLinking() {
//        if (mState == STATE_IDLE) {
//            MyLog.w(TAG, "switchToDeviceLinking");
//            mState = STATE_LINK_DEVICE;
//            mView.enterInfoMode();
//            setInfoText(R.string.live_close_device);
//        } else {
//            MyLog.e(TAG, "switchToDeviceLinking, but mState=" + mState);
//        }
//    }

//    private void switchToSharing(boolean isPhoto) {
//        if (mState == STATE_IDLE) {
//            MyLog.w(TAG, "switchToSharing isPhoto=" + isPhoto);
//            mView.enterInfoMode();
//            if (isPhoto) {
//                mState = STATE_SHARING_PHOTO;
//                setInfoText(R.string.end_share_photo);
//            } else {
//                mState = STATE_SHARING_VIDEO;
//                setInfoText(R.string.end_share_video);
//            }
//        } else {
//            MyLog.e(TAG, "switchToSharing, but mState=" + mState);
//        }
//    }

//    private void startCountingDown(final int startVal) {
//        stopCountingDown();
//        mCountDownSub = Observable
//                .interval(1, TimeUnit.SECONDS)
//                .take(startVal)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Action1<Long>() {
//                    @Override
//                    public void call(Long value) {
//                        long count = startVal - 1 - value;
//                        if (count == 0) {
//                            // TODO 倒计时结束处理
//                        } else {
//                            updateWaitingTime((int) count);
//                        }
//                    }
//                });
//    }

    private void stopCountingDown() {
        if (mCountDownSub != null && !mCountDownSub.isUnsubscribed()) {
            mCountDownSub.unsubscribe();
        }
        mCountDownSub = null;
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null) {
            MyLog.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }
        switch (event) {
            case MSG_ON_ORIENT_PORTRAIT:
                mIsLandscape = false;
                mView.onOrientation(mIsLandscape);
                return true;
            case MSG_ON_ORIENT_LANDSCAPE:
                mIsLandscape = true;
                mView.onOrientation(mIsLandscape);
                return true;
            default:
                break;
        }
        return false;
    }
}
