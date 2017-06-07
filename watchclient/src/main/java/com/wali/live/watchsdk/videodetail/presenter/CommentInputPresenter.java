package com.wali.live.watchsdk.videodetail.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.base.event.KeyboardEvent;
import com.base.log.MyLog;
import com.mi.live.data.account.MyUserInfoManager;
import com.wali.live.component.ComponentController;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.view.InputAreaView;
import com.wali.live.watchsdk.videodetail.adapter.DetailCommentAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.wali.live.component.ComponentController.MSG_SEND_COMMENT;

/**
 * Created by yangli on 2017/6/6.
 */
public class CommentInputPresenter extends ComponentPresenter<InputAreaView.IView>
        implements InputAreaView.IPresenter {
    private static final String TAG = "CommentInputPresenter";

    private String mFeedsIdToReply;
    private DetailCommentAdapter.CommentItem mCommentToReply;

    public CommentInputPresenter(@NonNull IComponentController componentController) {
        super(componentController);
        registerAction(ComponentController.MSG_ON_BACK_PRESSED);
        registerAction(ComponentController.MSG_SHOW_COMMENT_INPUT);
        registerAction(ComponentController.MSG_HIDE_INPUT_VIEW);
        EventBus.getDefault().register(this);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(KeyboardEvent event) {
        MyLog.w(TAG, "KeyboardEvent eventType=" + event.eventType);
        if (mView == null) {
            MyLog.e(TAG, "KeyboardEvent but mView is null");
            return;
        }
        switch (event.eventType) {
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_VISIBLE_ALWAYS_SEND:
                int keyboardHeight = Integer.parseInt(String.valueOf(event.obj1));
                mView.onKeyboardShowed(keyboardHeight);
                break;
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_HIDDEN:
                mView.onKeyboardHidden();
                break;
        }
    }

    @Override
    public void sendBarrage(String msg, boolean isFlyBarrage) {
        if (TextUtils.isEmpty(msg) || mFeedsIdToReply == null) {
            return;
        }
        long toUid = 0;
        String toNickName = null;
        if (mCommentToReply != null) {
            toUid = mCommentToReply.fromUid;
            toNickName = mCommentToReply.fromNickName;
        }
        DetailCommentAdapter.CommentItem commentItem = new DetailCommentAdapter
                .CommentItem(0,
                MyUserInfoManager.getInstance().getLevel(),
                MyUserInfoManager.getInstance().getUuid(),
                MyUserInfoManager.getInstance().getNickname(),
                toUid,
                toNickName,
                msg);
        mComponentController.onEvent(MSG_SEND_COMMENT, new Params().putItem(mFeedsIdToReply)
                .putItem(commentItem));
        mView.hideInputView();
    }

    @Override
    public void notifyInputViewShowed() {
        mComponentController.onEvent(ComponentController.MSG_INPUT_VIEW_SHOWED);
    }

    @Override
    public void notifyInputViewHidden() {
        mComponentController.onEvent(ComponentController.MSG_INPUT_VIEW_HIDDEN);
    }

    @Nullable
    @Override
    protected IAction createAction() {
        return new Action();
    }

    public class Action implements IAction {
        @Override
        public boolean onAction(int source, @Nullable Params params) {
            if (mView == null) {
                MyLog.e(TAG, "onAction but mView is null, source=" + source);
                return false;
            }
            switch (source) {
                case ComponentController.MSG_ON_BACK_PRESSED:
                    return mView.processBackPress();
                case ComponentController.MSG_SHOW_COMMENT_INPUT: {
                    mFeedsIdToReply = params.getItem(0);
                    mCommentToReply = params.getItem(1);
                    String hint;
                    if (mCommentToReply != null) {
                        String name = mCommentToReply.fromNickName;
                        if (TextUtils.isEmpty(name)) {
                            name = String.valueOf(mCommentToReply.fromUid);
                        }
                        hint = mView.getRealView().getResources().getString(R.string.recomment_text) + name;
                    } else {
                        hint = mView.getRealView().getResources().getString(R.string.write_comment);
                    }
                    mView.setHint(hint);
                    return mView.showInputView();
                }
                case ComponentController.MSG_HIDE_INPUT_VIEW:
                    mFeedsIdToReply = null;
                    mCommentToReply = null;
                    mView.setHint("");
                    return mView.hideInputView();
                default:
                    break;
            }
            return false;
        }
    }
}
