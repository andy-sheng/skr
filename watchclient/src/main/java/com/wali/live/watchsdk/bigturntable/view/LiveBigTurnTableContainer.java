package com.wali.live.watchsdk.bigturntable.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.repository.model.turntable.PrizeItemModel;
import com.mi.live.data.repository.model.turntable.TurnTableConfigModel;
import com.wali.live.proto.BigTurnTableProto;
import com.wali.live.watchsdk.R;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

import static com.wali.live.watchsdk.bigturntable.TurnTableType.MODE_BIG;
import static com.wali.live.watchsdk.bigturntable.TurnTableType.MODE_SMALL;

/**
 * Created by zhujianning on 18-7-10.
 */

public class LiveBigTurnTableContainer extends RelativeLayout {
    private static final String TAG = "LiveBigTurnTableContainer";

    //data
    private List<PrizeItemModel> mDatas;//每个item的数据源
    private BigTurnTableProto.TurntableType mMode = MODE_SMALL;
    private boolean mHasOpenSMode;
    private boolean mHasOpenBMode;

    //ui
    private TextView mCustomTipsTv;
    private RelativeLayout mCustomContainer;
    private EditText mInputEt;
    private BigTurnTableView mTurnTableContainer;
    private TextView mTypeSTv;
    private TextView mTypeBTv;
    private TextView mOpenTv;
    private RelativeLayout mOpenContainer;
    private TextView mTicketTipsTv;

    //listener
    private OnLiveTurnTableListener mOnLiveTurnTableListener;

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(mDatas != null && !mDatas.isEmpty()) {
                for(int i = 0; i < mDatas.size(); i++) {
                    PrizeItemModel prizeItemModel = mDatas.get(i);
                    if(prizeItemModel != null && prizeItemModel.isCustom()) {
                        prizeItemModel.setCustomDes(mInputEt.getText().toString());
                        mTurnTableContainer.setDatas(mDatas);
                        break;
                    }
                }
            }
        }
    };

    public LiveBigTurnTableContainer(Context context) {
        this(context, null);
    }

    public LiveBigTurnTableContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveBigTurnTableContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.big_turn_table_view, this);

        initView();
        initListener();
    }

    private void initView() {
        mTurnTableContainer = (BigTurnTableView) findViewById(R.id.turn_table_container);
        mCustomTipsTv = (TextView) findViewById(R.id.custom_tips_tv);
        mCustomContainer = (RelativeLayout) findViewById(R.id.custom_comtainer);
        mInputEt = (EditText) findViewById(R.id.edit_et);
        mTypeSTv = (TextView) findViewById(R.id.type_small_tv);
        mTypeSTv.setSelected(true);
        mTypeBTv = (TextView) findViewById(R.id.type_big_tv);
        mOpenTv = (TextView) findViewById(R.id.open_tv);
        mOpenContainer = (RelativeLayout) findViewById(R.id.open_container);
        mTicketTipsTv = (TextView) findViewById(R.id.ticket_tips_tv);
        mTicketTipsTv.setText(String.format(GlobalData.app().getResources().getString(R.string.big_turn_host_hint_tips_txt), String.valueOf(50)));
    }

    private void initListener() {
        RxView.clicks(mTypeSTv)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        switchMode(true);
                    }
                });
        RxView.clicks(mTypeBTv)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        switchMode(false);
                    }
                });
        RxView.clicks(mOpenContainer)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        openOpt();
                    }
                });
        mInputEt.addTextChangedListener(mTextWatcher);
    }

    private void switchMode(boolean isSmall) {
        mTurnTableContainer.changeBigTurnTableBg(!isSmall);
        mMode = isSmall ? MODE_SMALL : MODE_BIG;
        mTurnTableContainer.switchMode(mMode);
        mOnLiveTurnTableListener.switchMode(mMode);
        if(isSmall) {
            mTypeSTv.setSelected(true);
            mTypeBTv.setSelected(false);
            if(mHasOpenSMode) {
                mOpenTv.setText(GlobalData.app().getResources().getString(R.string.close_small_turn_table));
                modifyInputEt();
            } else {
                mOpenTv.setText(GlobalData.app().getResources().getString(R.string.open_small_turn_table));
                resumeInputEx();
            }
            mTicketTipsTv.setText(String.format(GlobalData.app().getResources().getString(R.string.big_turn_host_hint_tips_txt), String.valueOf(50)));
        } else {
            mTypeSTv.setSelected(false);
            mTypeBTv.setSelected(true);
            if(mHasOpenBMode) {
                mOpenTv.setText(GlobalData.app().getResources().getString(R.string.close_big_turn_table));
                modifyInputEt();
            } else {
                mOpenTv.setText(GlobalData.app().getResources().getString(R.string.open_big_turn_table));
                resumeInputEx();
            }
            mTicketTipsTv.setText(String.format(GlobalData.app().getResources().getString(R.string.big_turn_host_hint_tips_txt), String.valueOf(188)));
        }
    }

    private void openOpt() {
        if(mMode == MODE_SMALL) {
            if(mHasOpenSMode) {
                mOnLiveTurnTableListener.close(mMode);
            } else {
                String txt = null;
                if(!TextUtils.isEmpty(mInputEt.getText())) {
                    txt = mInputEt.getText().toString();
                } else {
                    txt = GlobalData.app().getResources().getString(R.string.big_turn_host_hint_tips);
                }
                mOnLiveTurnTableListener.open(mMode, txt, mHasOpenBMode);
            }
        } else {
            if(mHasOpenBMode) {
                mOnLiveTurnTableListener.close(mMode);
            } else {
                String txt = null;
                if(!TextUtils.isEmpty(mInputEt.getText())) {
                    txt = mInputEt.getText().toString();
                } else {
                    txt = GlobalData.app().getResources().getString(R.string.big_turn_host_hint_tips);
                }
                mOnLiveTurnTableListener.open(mMode, txt, mHasOpenSMode);
            }
        }
    }

    public void openOptSuccess(BigTurnTableProto.TurntableType type) {
        if(type == MODE_SMALL) {
            mHasOpenBMode = false;
            mHasOpenSMode = true;
            mOpenTv.setText(GlobalData.app().getResources().getString(R.string.close_small_turn_table));
        } else {
            mHasOpenBMode = true;
            mHasOpenSMode = false;
            mOpenTv.setText(GlobalData.app().getResources().getString(R.string.close_big_turn_table));
        }
    }

    public void notifyOpenStatus(BigTurnTableProto.TurntableType type) {
        if(type == BigTurnTableProto.TurntableType.TYPE_128) {
            mHasOpenBMode = false;
            mHasOpenSMode = true;
            mOpenTv.setText(GlobalData.app().getResources().getString(R.string.close_small_turn_table));
            mTicketTipsTv.setText(String.format(GlobalData.app().getResources().getString(R.string.big_turn_host_hint_tips_txt), String.valueOf(50)));
            if(mDatas != null && mHasOpenSMode) {
                modifyInputEt();
            }
        } else {
            mHasOpenBMode = true;
            mHasOpenSMode = false;
        }
    }

    private void modifyInputEt() {
        for(int i = 0; i < mDatas.size(); i++) {
            if(mDatas.get(i).isCustom()) {
                mInputEt.setText(mDatas.get(i).getCustomDes());
                mInputEt.setEnabled(false);
                break;
            }
        }
    }

    private void resumeInputEx() {
        mInputEt.setEnabled(true);
        mInputEt.setText("");
    }

    public void closeOptSuccess(BigTurnTableProto.TurntableType type) {
        if(type == MODE_SMALL) {
            mHasOpenSMode = false;
            mOpenTv.setText(GlobalData.app().getResources().getString(R.string.open_small_turn_table));
        } else {
            mHasOpenBMode = false;
            mOpenTv.setText(GlobalData.app().getResources().getString(R.string.open_big_turn_table));
        }
        switchMode(type == MODE_SMALL);
    }

    public void setDatas(TurnTableConfigModel model) {
        this.mDatas = model.getTurnTablePreConfigModel().getPrizeItems();
        mTurnTableContainer.setDatas(mDatas);
    }

    public BigTurnTableProto.TurntableType getMode() {
        return mMode;
    }

    public void onDestory() {
        mTextWatcher = null;
        mTurnTableContainer.destory();
    }

    public void setOnLiveTurnTableListener(OnLiveTurnTableListener listener) {
        this.mOnLiveTurnTableListener = listener;
    }

    public interface OnLiveTurnTableListener {
        void open(BigTurnTableProto.TurntableType mode, String inputTxt, boolean needCloseOtherMode);

        void close(BigTurnTableProto.TurntableType mode);

        void switchMode(BigTurnTableProto.TurntableType mode);
    }
}
