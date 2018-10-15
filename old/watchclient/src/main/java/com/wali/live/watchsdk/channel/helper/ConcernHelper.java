package com.wali.live.watchsdk.channel.helper;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;

import com.base.activity.RxActivity;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.user.User;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.dao.Relation;
import com.wali.live.dao.RelationDaoAdapter;
import com.wali.live.utils.AsyncTaskUtils;
import com.wali.live.utils.relation.RelationUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.auth.AccountAuthManager;

import java.util.concurrent.TimeUnit;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by zhaomin on 16-9-14.
 */
public class ConcernHelper {
    private static final String TAG = "ConcernHelper";

    private Context mContext;

    public ConcernHelper(Context context) {
        mContext = context;
    }

    public void initFocusData(final User user, final TextView concernBtns) {

        AsyncTaskUtils.exeIOTask(new AsyncTask<Object, Object, Boolean>() {

            @Override
            protected Boolean doInBackground(Object... params) {
                boolean isFocus = false;
                Relation relation = RelationDaoAdapter.getInstance().getRelationByUUid(user.getUid());
                if (null != relation) {
                    isFocus = relation.getIsFollowing();
                    MyLog.i(TAG, "initFocusData " + user.getNickname() + " id : " + user.getUid() + " focus: " + isFocus);
                } else {
                    MyLog.i(TAG, "initFocusData relation is null " + user.getNickname() + " id : " + user.getUid());
                }
                return isFocus;
            }

            @Override
            protected void onPostExecute(Boolean isFocus) {
                switchConcernUI(isFocus, concernBtns);
                if (!isFocus) {
                    setConcernClick(user, concernBtns, null);
                }
            }
        });
    }

    public void switchConcernUI(boolean isFocus, TextView concernBtns) {
        concernBtns.setEnabled(!isFocus);
        if (isFocus) {
            concernBtns.setText(R.string.already_followed);
        } else {
            concernBtns.setText(R.string.follow);
        }
    }

    //点击关注，匿名跳转登录
    public void setConcernClick(final User user, final TextView concernBtns, final OnFollowSuccessedListener listener) {
        RxView.clicks(concernBtns)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .filter(new Func1<Void, Boolean>() {
                    @Override
                    public Boolean call(Void aVoid) {
                        return AccountAuthManager.triggerActionNeedAccount(mContext);
                    }
                }).observeOn(Schedulers.io())
                .map(new Func1<Void, Boolean>() {
                    @Override
                    public Boolean call(Void aVoid) {
                        return RelationUtils.follow(UserAccountManager.getInstance().getUuidAsLong(),
                                user.getUid(), user.getRoomId()) >= RelationUtils.FOLLOW_STATE_SUCCESS;
                    }
                }).compose(((RxActivity) mContext).<Boolean>bindUntilEvent(ActivityEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Boolean result) {
                        if (result) {
                            ToastUtils.showToast(R.string.follow_success);
                            user.setIsFocused(true);
                            Relation relation = user.getRelation();
                            RelationDaoAdapter.getInstance().insertRelation(relation);
                            if (listener != null) {
                                listener.followSuccess();
                            }
                        } else {
                            if (!AccountAuthManager.triggerActionNeedAccount(mContext)) {
                                return;
                            }
                            if (RelationUtils.sErrorCode == RelationUtils.ERROR_CODE_BLACK) {
                                ToastUtils.showToast(R.string.setting_black_follow_hint);
                            } else {
                                ToastUtils.showToast(R.string.follow_failed);
                            }
                        }
                    }
                });
    }

    public interface OnFollowSuccessedListener {
        /**
         * 关注成功
         */
        void followSuccess();
    }

}
