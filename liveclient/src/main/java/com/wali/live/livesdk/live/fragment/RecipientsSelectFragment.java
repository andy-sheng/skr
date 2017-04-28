package com.wali.live.livesdk.live.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.event.KeyboardEvent;
import com.base.fragment.BaseEventBusFragment;
import com.base.fragment.BaseFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.view.BackTitleBar;
import com.mi.live.data.data.UserListData;
import com.mi.live.data.user.User;
import com.wali.live.event.EventClass;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.adapter.RecipientsSelectRecyclerAdapter;
import com.wali.live.livesdk.live.view.IndexableRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;

import butterknife.Bind;

/**
 * Created by yurui on 3/24/16.
 */
public class RecipientsSelectFragment extends BaseEventBusFragment implements View.OnClickListener {

    private static final int REQUEST_CODE = GlobalData.getRequestCode();

    public static final String KEY_REQUEST_CODE = "KEY_REQUEST_CODE";


    //模式
    public static final String SELECT_MODE = "INTENT_KEY_MODE";

    //数据类型
    public static final String DATA_TYPE = "DATA_TYPE";

    //最多选择人数 多选
    public static final String SELECT_MAX_CNT = "SELECT_MAX_CNT";

    //显示的头像
    public static final String SELECT_TITLE = "SELECT_TITLE";


    //显示的提示title
    public static final String INTENT_HINT_TITLE = "INTENT_HINT_TITLE";

    //显示性别和等级
    public static final String INTENT_SHOW_LEVEL_SEX = "INTENT_SHOW_LEVEL_SEX";

    //关注列表是否双向
    public static final String INTENT_SHOW_BOTH_WAY = "INTENT_SHOW_BOTH_WAY";

    //是否可以搜索
    public static final String INTENT_ENABLE_SEARCH = "INTENT_ENABLE_SEARCH";

    //是否显示字母索引
    public static final String INTENT_ENABLE_INDEX = "INTENT_ENABLE_INDEX";

    //房间id
    public static final String INTENT_LIVE_ROOM_ID = "INTENT_LIVE_ROOM_ID";


    //多选返回的uuid列表
    public static final String RESULT_ID_LIST = "INTENT_KEY_RESULT_LIST";

    //单选返回结果对象
    public static final String RESULT_SINGLE_OBJECT = "RESULT_SINGLE_USER";

    public static final int SELECT_MODE_SINGLE_CLICK = 0;
    public static final int SELECT_MODE_MULTI = 1;
    public static final int SELECT_MODE_SINGLE_SELECT = 2;

    public int requestCode;

    public BackTitleBar mTitleBar;

    View mPrivateLiveInviteePanel;

    RecyclerView mBottomList;

    IndexableRecyclerView mRecyclerView;

    TextView mHintTitle;

    EditText mSearch;

    View mSearchBar;

    View mDeleteBtn;

    TextView confirmBtn;

    private String roomId;
    private int mode;
    private int dataType;

    private RecipientsSelectRecyclerAdapter mRecyclerAdapter;

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.recipients_select_fragment, container, false);
    }

    @Override
    protected void bindView() {
        mTitleBar = (BackTitleBar)mRootView.findViewById(R.id.title_bar);
        mPrivateLiveInviteePanel = (View) mRootView.findViewById(R.id.invite_bottom);
        mBottomList = (RecyclerView)mRootView.findViewById(R.id.invite_list_bottom);
        mRecyclerView = (IndexableRecyclerView)mRootView.findViewById(R.id.recycler_view);
        mHintTitle = (TextView)mRootView.findViewById(R.id.hint_title);
        mSearch = (EditText)mRootView.findViewById(R.id.search_input_edit_text);
        mSearchBar = mRootView.findViewById(R.id.search_bar);
        mDeleteBtn = mRootView.findViewById(R.id.delete_btn);
        confirmBtn = (TextView)mRootView.findViewById(R.id.private_confirm);
        roomId = getArguments().getString(RecipientsSelectFragment.INTENT_LIVE_ROOM_ID);
        dataType = getArguments().getInt(RecipientsSelectFragment.DATA_TYPE, RecipientsSelectRecyclerAdapter.ITEM_TYPE_FOLLOWING);
        requestCode = getArguments().getInt(KEY_REQUEST_CODE);

        mode = getArguments().getInt(RecipientsSelectFragment.SELECT_MODE, RecipientsSelectFragment.SELECT_MODE_MULTI);
        if (mode > RecipientsSelectFragment.SELECT_MODE_SINGLE_SELECT) {
            mode = RecipientsSelectFragment.SELECT_MODE_SINGLE_SELECT;
        }

        if (mode == RecipientsSelectFragment.SELECT_MODE_MULTI) {
            mTitleBar.getRightTextBtn().setOnClickListener(this);
            mTitleBar.getRightTextBtn().setText(getString(R.string.match_ok_btn, 0, getArguments().getInt(RecipientsSelectFragment.SELECT_MAX_CNT, 5)));
            mTitleBar.getRightTextBtn().setEnabled(false);
        } else if (mode == RecipientsSelectFragment.SELECT_MODE_SINGLE_SELECT) {
            mTitleBar.getRightTextBtn().setOnClickListener(this);
            mTitleBar.getRightTextBtn().setText(getString(R.string.ok));
            mTitleBar.getRightTextBtn().setEnabled(false);
        }


        mTitleBar.getBackBtn().setOnClickListener(this);
        mTitleBar.setTitle(getArguments().getString(RecipientsSelectFragment.SELECT_TITLE));

        if (!TextUtils.isEmpty(getArguments().getString(RecipientsSelectFragment.INTENT_HINT_TITLE))) {
            mHintTitle.setText(getArguments().getString(RecipientsSelectFragment.INTENT_HINT_TITLE));
            mHintTitle.setVisibility(View.VISIBLE);
        } else {
            mHintTitle.setVisibility(View.GONE);
        }

        mRecyclerAdapter = new RecipientsSelectRecyclerAdapter(this, mRecyclerView);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                MyLog.d(TAG, "onScrolled");
                if (getActivity() != null) {
                    KeyboardUtils.hideKeyboard(getActivity());
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                MyLog.d(TAG, "onScrollStateChanged, newState:" + newState);
            }
        });
        mRecyclerAdapter.setCoverView(mRootView.findViewById(R.id.cover_view));
        mRecyclerAdapter.setBothWay(getArguments().getBoolean(RecipientsSelectFragment.INTENT_SHOW_BOTH_WAY, true));
        //mRecyclerAdapter.setItemTypeAndLoadData(dataType, mode, roomId);
        mRecyclerAdapter.setMaxSelectCount(getArguments().getInt(RecipientsSelectFragment.SELECT_MAX_CNT, 5));
        mRecyclerAdapter.setShowLevelSex(getArguments().getBoolean(RecipientsSelectFragment.INTENT_SHOW_LEVEL_SEX, true));
        mRecyclerAdapter.setShowIndex(getArguments().getBoolean(RecipientsSelectFragment.INTENT_ENABLE_INDEX, true));

        if (getArguments().getBoolean(RecipientsSelectFragment.INTENT_ENABLE_SEARCH, false)) {
            mSearchBar.setVisibility(View.VISIBLE);
            mRecyclerAdapter.editText = mSearch;
            mSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (TextUtils.isEmpty(s.toString())) {
                        mDeleteBtn.setVisibility(View.GONE);
                    } else {
                        mDeleteBtn.setVisibility(View.VISIBLE);
                    }
                    mRecyclerAdapter.search(s.toString());
                }
            });
            mDeleteBtn.setOnClickListener(this);
        } else {
            mSearchBar.setVisibility(View.GONE);
        }

        mTitleBar.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecyclerAdapter.setItemTypeAndLoadData(dataType, mode, roomId);
            }
        }, 300);

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.back_iv) {
            onBackPressed();
        } else if (i == R.id.private_confirm) {
            Intent intent = new Intent();
            intent.putExtra(RecipientsSelectFragment.RESULT_ID_LIST, (Serializable) mRecyclerAdapter.getResultList());
            EventBus.getDefault().post(new EventClass.OnActivityResultEvent(requestCode, Activity.RESULT_OK, intent));
            onBackPressed();

        } else if (i == R.id.right_text_btn) {
            Intent intent = new Intent();
            if (mode == RecipientsSelectFragment.SELECT_MODE_MULTI) {
                intent.putExtra(RecipientsSelectFragment.RESULT_ID_LIST, (Serializable) mRecyclerAdapter.getResultList());
            } else if (mode == RecipientsSelectFragment.SELECT_MODE_SINGLE_SELECT) {
                Object selObj = mRecyclerAdapter.getSelectItem();
                if (selObj != null) {
                    if (selObj instanceof UserListData) {
                        UserListData item = (UserListData) selObj;
                        intent.putExtra(RecipientsSelectFragment.RESULT_SINGLE_OBJECT, new User(item.userId, item.userNickname, item.level, item.avatar, item.certificationType));
                    }
                } else {
                    onBackPressed();
                }
            }

            EventBus.getDefault().post(new EventClass.OnActivityResultEvent(requestCode, Activity.RESULT_OK, intent));
            onBackPressed();


        } else if (i == R.id.delete_btn) {
            mSearch.setText("");
            KeyboardUtils.hideKeyboard(getActivity(), mSearch);

        }
    }

    @Override
    public boolean onBackPressed() {
        hideInput();
        try {
            FragmentNaviUtils.popFragmentFromStack(getActivity());
        } catch (Exception e) {
            MyLog.e(e);
        } catch (Error error) {
            MyLog.e(error);
        }
        return true;
    }

    private void hideInput() {
        if (mSearch.getVisibility() == View.VISIBLE) {
            KeyboardUtils.hideKeyboard(getActivity(), mSearch);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(KeyboardEvent event) {
        switch (event.eventType) {
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_VISIBLE:
                int keyboardHeight = Integer.parseInt(String.valueOf(event.obj1));
                mPrivateLiveInviteePanel.setTranslationY(-keyboardHeight);
                break;
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_HIDDEN:
                mPrivateLiveInviteePanel.setTranslationY(0);
                break;
        }
    }

    @Override
    public String getTAG() {
        return TAG;
    }

    @Override
    public boolean isStatusBarDark() {
        return true;
    }

    @Override
    public boolean isOverrideStatusBar() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.ChangeCancel event) {
        if (event == null) {
            return;
        }
        mRecyclerAdapter.setCancelBootom(event.userId);
    }

}
