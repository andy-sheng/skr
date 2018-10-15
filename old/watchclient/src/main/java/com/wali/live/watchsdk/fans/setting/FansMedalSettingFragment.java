package com.wali.live.watchsdk.fans.setting;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.base.activity.BaseSdkActivity;
import com.base.event.KeyboardEvent;
import com.base.fragment.BaseEventBusFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.view.BackTitleBar;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.fans.setting.presenter.FansMedalSettingPresenter;
import com.wali.live.watchsdk.fans.setting.presenter.IFansMedalSettingView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by lan on 2017/11/24.
 */
public class FansMedalSettingFragment extends BaseEventBusFragment implements View.OnClickListener, IFansMedalSettingView {
    private static final String EXTRA_ZUID = "extra_zuid";

    private static final int[][] ID_ARRAY = {
            {R.id.level_1_area, R.id.level_1_edt},
            {R.id.level_2_area, R.id.level_2_edt},
            {R.id.level_3_area, R.id.level_3_edt},
            {R.id.level_4_area, R.id.level_4_edt},
            {R.id.level_5_area, R.id.level_5_edt},
            {R.id.level_6_area, R.id.level_6_edt},
            {R.id.level_7_area, R.id.level_7_edt},
            {R.id.level_8_area, R.id.level_8_edt},
    };

    private BackTitleBar mTitleBar;
    private SparseArray<MedalItemUIWrapper> mMedalItemArray;

    private View mPlaceholderView;

    private FansMedalSettingPresenter mPresenter;
    private long mZuid;

//    private int mLastInputMode = 0;

    @Override
    public int getRequestCode() {
        return 0;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        initData(args);
    }

    private void initData(Bundle bundle) {
        if (bundle == null) {
            finish();
            return;
        }
        mZuid = bundle.getLong(EXTRA_ZUID);
        MyLog.d(TAG, "user id=" + mZuid);
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_fans_medal_setting, container, false);
    }

    @Override
    protected void bindView() {
        mTitleBar = $(R.id.title_bar);
        mTitleBar.setTitle(R.string.vfans_set_group_title);
        mTitleBar.getBackBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mMedalItemArray = new SparseArray<>(8);
        for (int i = 0; i < ID_ARRAY.length; i++) {
            mMedalItemArray.put(ID_ARRAY[i][0], new MedalItemUIWrapper(i));
        }

        mPlaceholderView = $(R.id.placeholder_view);

//        setSoftInputMode();
        initPresenter();
    }

//    private void setSoftInputMode() {
//        mLastInputMode = getActivity().getWindow().getAttributes().softInputMode;
//        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
//    }
//
//    private void recoverSoftInputMode() {
//        if (mLastInputMode != 0) {
//            getActivity().getWindow().setSoftInputMode(mLastInputMode);
//        }
//    }

    private void initPresenter() {
        mPresenter = new FansMedalSettingPresenter(this, mZuid);
        mPresenter.getGroupMedal();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        MedalItemUIWrapper medalItem = mMedalItemArray.get(id);
        if (medalItem != null) {
            medalItem.clickItem();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        for (int i = 0; i < mMedalItemArray.size(); i++) {
            mMedalItemArray.valueAt(i).destroy();
        }
    }

    private void finish() {
        KeyboardUtils.hideKeyboardImmediately(getActivity());
//        recoverSoftInputMode();

        FragmentNaviUtils.popFragment(getActivity());
    }

    @Override
    public void setGroupMedal(List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            MedalItemUIWrapper medalItem = mMedalItemArray.get(ID_ARRAY[i][0]);
            if (medalItem != null) {
                medalItem.setMedalText(list.get(i));
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(KeyboardEvent event) {
        MyLog.d(TAG, "KeyboardEvent eventType=" + event.eventType);
        switch (event.eventType) {
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_VISIBLE: {
                int keyboardHeight = Integer.parseInt(String.valueOf(event.obj1));
                ViewGroup.MarginLayoutParams layoutParams =
                        (ViewGroup.MarginLayoutParams) mPlaceholderView.getLayoutParams();
                layoutParams.height = keyboardHeight;
                mPlaceholderView.setLayoutParams(layoutParams);
                break;
            }
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_HIDDEN: {
                ViewGroup.MarginLayoutParams layoutParams =
                        (ViewGroup.MarginLayoutParams) mPlaceholderView.getLayoutParams();
                layoutParams.height = 0;
                mPlaceholderView.setLayoutParams(layoutParams);
                break;
            }
        }
    }

    public class MedalItemUIWrapper implements TextWatcher {
        private RelativeLayout mLevelArea;
        private EditText mLevelEt;

        private int mLevel;

        public MedalItemUIWrapper(int index) {
            mLevelArea = $(ID_ARRAY[index][0]);
            mLevelEt = $(ID_ARRAY[index][1]);

            mLevel = index + 1;

            mLevelArea.setOnClickListener(FansMedalSettingFragment.this);
            mLevelEt.addTextChangedListener(this);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            mPresenter.setGroupMedal(mLevel, s.toString().trim());
        }

        public void setMedalText(String text) {
            mLevelEt.setText(text);
        }

        public void clickItem() {
            mLevelEt.requestFocus();
            mLevelEt.setSelection(mLevelEt.length());
            KeyboardUtils.showKeyboard(getContext(), mLevelEt);
        }

        public void destroy() {
            mLevelEt.removeTextChangedListener(this);
        }
    }

    public static void openFragment(BaseSdkActivity activity, long zuid) {
        Bundle bundle = new Bundle();
        bundle.putLong(EXTRA_ZUID, zuid);
        FragmentNaviUtils.addFragmentToBackStack(activity, R.id.main_act_container, FansMedalSettingFragment.class, bundle, false, R.anim.slide_left_in, R.anim.slide_right_out);
    }
}
