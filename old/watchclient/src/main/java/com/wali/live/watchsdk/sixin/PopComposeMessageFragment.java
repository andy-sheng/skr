package com.wali.live.watchsdk.sixin;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.event.KeyboardEvent;
import com.base.fragment.BaseFragment;
import com.base.fragment.RxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.view.BackTitleBar;
import com.mi.live.data.preference.MLPreferenceUtils;
import com.wali.live.common.smiley.SmileyParser;
import com.wali.live.common.smiley.SmileyPicker;
import com.wali.live.dao.SixinMessage;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.personalcenter.fragment.ChatThreadHalfFragment;
import com.wali.live.watchsdk.sixin.cache.SendingMessageCache;
import com.wali.live.watchsdk.sixin.constant.SixinConstants;
import com.wali.live.watchsdk.sixin.data.SixinMessageLocalStore;
import com.wali.live.watchsdk.sixin.message.SixinMessageModel;
import com.wali.live.watchsdk.sixin.pojo.SixinTarget;
import com.wali.live.watchsdk.sixin.presenter.ISixinMessageView;
import com.wali.live.watchsdk.sixin.presenter.SixinMessagePresenter;
import com.wali.live.watchsdk.sixin.recycler.adapter.SixinMessageAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by lan on 2017/10/27.
 */
public class PopComposeMessageFragment extends RxFragment implements View.OnClickListener, ISixinMessageView {
    private static final String EXTRA_SIXIN_TARGET = "extra_sixin_target";

    private View mBgView;

    private BackTitleBar mTitleBar;

    private SwipeRefreshLayout mRefreshLayout;

    private RecyclerView mMessageRv;
    private LinearLayoutManager mLayoutManager;
    private SixinMessageAdapter mMessageAdapter;

    private EditText mInputEt;
    private ImageView mSmileyBtn;
    private TextView mSendBtn;

    private View mPlaceholderView;

    private View mPickerArea;
    private SmileyPicker mSmileyPicker;

    private SixinTarget mSixinTarget;
    private SixinMessagePresenter mMessagePresenter;

    private boolean mIsScrollToLast;

    private int mKeyboardHeight;
    private boolean mIsSmileyShown = false;

    @Override
    public int getRequestCode() {
        return 0;
    }

    @Override
    protected String getTAG() {
        return SixinConstants.LOG_PREFIX + getClass().getSimpleName();
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        initData(args);
    }

    private void initData(Bundle bundle) {
        if (bundle == null) {
            finish();
        }
        mSixinTarget = (SixinTarget) bundle.getSerializable(EXTRA_SIXIN_TARGET);
        if (mSixinTarget == null) {
            finish();
        }
        MyLog.d(TAG, "user id=" + mSixinTarget.getUid());
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        return inflater.inflate(R.layout.fragment_pop_compose_message, container, false);
    }

    @Override
    protected void bindView() {
        mBgView = $(R.id.bg_view);
        $click(mBgView, this);

        mTitleBar = $(R.id.title_bar);
        $click(mTitleBar.getBackBtn(), this);
        if (!TextUtils.isEmpty(mSixinTarget.getNickname())) {
            mTitleBar.setTitle(mSixinTarget.getNickname());
        } else {
            mTitleBar.setTitle(String.valueOf(mSixinTarget.getUid()));
        }

        mRefreshLayout = $(R.id.swipe_refresh_layout);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SixinMessageModel model = mMessageAdapter.getItem(0);
                if (model == null) {
                    mMessagePresenter.firstLoadDataFromDB();
                } else {
                    mMessagePresenter.pullOldMessage(model);
                }
            }
        });

        mMessageRv = $(R.id.message_rv);

        mMessageAdapter = new SixinMessageAdapter();
        mMessageRv.setAdapter(mMessageAdapter);

        mMessageRv.setItemAnimator(new DefaultItemAnimator());
        RecyclerView.ItemAnimator animator = mMessageRv.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        mLayoutManager = new LinearLayoutManager(getContext());
        mMessageRv.setLayoutManager(mLayoutManager);
        mMessageRv.setHasFixedSize(true);

        mInputEt = $(R.id.input_et);
        mSmileyBtn = $(R.id.smiley_btn);
        $click(mSmileyBtn, this);
        mSendBtn = $(R.id.send_btn);
        $click(mSendBtn, this);

        mPlaceholderView = $(R.id.placeholder_view);

        mPickerArea = $(R.id.picker_area);
        mSmileyPicker = $(R.id.smiley_picker);
        mSmileyPicker.setEditText(mInputEt);

        mKeyboardHeight = MLPreferenceUtils.getKeyboardHeight(true);

        initPresenter();
    }

    private void initPresenter() {
        mMessagePresenter = new SixinMessagePresenter(this, mSixinTarget);
        mMessagePresenter.firstLoadDataFromDB();
        mMessagePresenter.markConversationAsRead();
    }

    @Override
    public void loadData(List<SixinMessageModel> messageModelList) {
        MyLog.d(TAG, "loadData messageList size=" + messageModelList.size());
        if (messageModelList.size() > 0) {
            mMessageAdapter.setDataList(messageModelList);
            scrollToLastItem();
        }
    }

    @Override
    public void addData(List<SixinMessageModel> messageModelList) {
        MyLog.d(TAG, "addData messageList size=" + messageModelList.size());
        if (messageModelList.size() > 0) {
            mMessageAdapter.addDataList(messageModelList);
            scrollToLastItem();
        }
    }

    public void addOldData(List<SixinMessageModel> messageModelList) {
        MyLog.d(TAG, "loadOldData messageList size=" + messageModelList.size());
        if (messageModelList.size() > 0) {
            mMessageAdapter.addOldDataList(messageModelList);
        }
    }

    private void updateData(SixinMessageModel messageModel) {
        MyLog.d(TAG, "updateData");
        if (messageModel != null) {
            mMessageAdapter.updateData(messageModel);
        }
    }

    @Override
    public void stopRefreshing() {
        mRefreshLayout.setRefreshing(false);
    }

    private void scrollToLastItem() {
        mMessageRv.scrollToPosition(mMessageAdapter.getItemCount() - 1);
        mIsScrollToLast = true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(KeyboardEvent event) {
        switch (event.eventType) {
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_HIDDEN:
                if (mPlaceholderView != null) {
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mPlaceholderView.getLayoutParams();
                    layoutParams.height = 0;
                    mPlaceholderView.setLayoutParams(layoutParams);
                }
                break;
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_VISIBLE:
                if (mPlaceholderView != null) {
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mPlaceholderView.getLayoutParams();
                    layoutParams.height = mKeyboardHeight = (int) event.obj1;
                    mPlaceholderView.setLayoutParams(layoutParams);
                }
                if (mSmileyPicker != null) {
                    updateSmileyPicker(false);
                }
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final SixinMessageLocalStore.SixinMessageBulkInsertEvent event) {
        MyLog.d(TAG, "sixin message bulk insert");
        if (event == null || !event.needsUpdateUi) {
            return;
        }
        if (event.sixinMessages == null || event.sixinMessages.size() == 0) {
            return;
        }

        mMessagePresenter.notifyMessage(event.sixinMessages);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SixinMessageLocalStore.SixinMessageUpdateEvent event) {
        MyLog.d(TAG, "sixin message update");
        if (event == null) {
            return;
        }
        SixinMessage sixinMessage = event.sixinMessage;
        if (sixinMessage == null || sixinMessage.getTarget() != mSixinTarget.getUid()) {
            return;
        }

        if (!sixinMessage.getIsInbound() && sixinMessage.getOutboundStatus() == SixinMessage.OUTBOUND_STATUS_RECEIVED) {
            SendingMessageCache.remove(sixinMessage.getId());
        }
        updateData(new SixinMessageModel(sixinMessage));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public boolean onBackPressed() {
        if (getActivity() == null) {
            return false;
        }
        if (mIsSmileyShown) {
            updateSmileyPicker(false);
            return true;
        }
        finish();
        return true;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.bg_view || id == R.id.back_iv) {
            finish();
        } else if (id == R.id.send_btn) {
            sendText();
        } else if (id == R.id.smiley_btn) {
            updateSmileyPicker(!mIsSmileyShown);
        }
    }

    private void updateSmileyPicker(boolean showSmiley) {
        if (mIsSmileyShown != showSmiley) {
            mIsSmileyShown = showSmiley;
            if (mIsSmileyShown) {
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mPickerArea.getLayoutParams();
                layoutParams.height = mKeyboardHeight;
                mPickerArea.setLayoutParams(layoutParams);

                mSmileyPicker.show(mKeyboardHeight);
            } else {
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mPickerArea.getLayoutParams();
                layoutParams.height = 0;
                mPickerArea.setLayoutParams(layoutParams);

                mSmileyPicker.hide();
            }
        }
    }

    private void sendText() {
        String input = mInputEt.getText().toString();
        if (TextUtils.isEmpty(input)) {
            return;
        }

        String message = SmileyParser.getInstance().convertString(input, SmileyParser.TYPE_LOCAL_TO_GLOBAL).toString();
        MyLog.d(TAG, "sendText=" + message);

        mMessagePresenter.sendMessage(message);
        mInputEt.setText("");
    }

    private void finish() {
        KeyboardUtils.hideKeyboardImmediately(getActivity());
        FragmentNaviUtils.popFragment(getActivity());
    }

//    @Override
//    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
//        if (enter) {
//            Animation animation = null;
//            try {
//                animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_half_bottom_in);
//            } catch (Exception e) {
//                MyLog.e(e);
//            }
//            return animation;
//        } else {
//            Animation animation = null;
//            try {
//                animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_half_bottom_out);
//            } catch (Exception e) {
//                MyLog.e(e);
//            }
//            return animation;
//        }
//    }

    public static void open(BaseActivity activity, SixinTarget sixinTarget, boolean isNeedSaveToStack) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_SIXIN_TARGET, sixinTarget);

        BaseFragment fragment = FragmentNaviUtils.addFragment(activity, R.id.main_act_container, PopComposeMessageFragment.class,
                bundle, true, true, true);

    }
}
