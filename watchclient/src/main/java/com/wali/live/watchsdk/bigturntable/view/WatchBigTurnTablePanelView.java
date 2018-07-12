package com.wali.live.watchsdk.bigturntable.view;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.utils.display.DisplayUtils;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.repository.model.turntable.PrizeItemModel;
import com.mi.live.data.user.User;
import com.wali.live.proto.BigTurnTableProto;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

/**
 * Created by zhujianning on 18-7-11.
 */

public class WatchBigTurnTablePanelView extends RelativeLayout {
    private static final String TAG = "WatchBigTurnTablePanelView";

    private Context mContext;

    //data
    private LayoutParams mParams;
    private boolean mIsLandscape;
    private List<PrizeItemModel> mDatas;//每个item的数据源
    private User mZUser;

    //ui
    private BigTurnTableView mBigTurnTableView;
    private TextView mStartTv;
    private View mBgView;
    private TextView mCancelTv;
    private BaseImageView mHostIv;
    private TextView mRuleTv;
//    private BigTurnTableRuleView mBigTurnTableRuleView;
    private TextView mNameTv;
    private RelativeLayout mPOptContainer;
    private RelativeLayout mLOptContainer;
    private TextView mLCancelTv;
    private TextView mLStartTv;
    private RelativeLayout mContainer;
    private RelativeLayout mHostInfoContainer;

    private OnDrawTurnTableListener mOnDrawTurnTableListener;

    public WatchBigTurnTablePanelView(Context context) {
        this(context, null);
    }

    public WatchBigTurnTablePanelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WatchBigTurnTablePanelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.big_turn_table_panel, this);

        mContext = context;

        initView();
        initListener();
        initData();
    }

    private void initView() {
        mBigTurnTableView = (BigTurnTableView) findViewById(R.id.turn_table_container);
        mHostInfoContainer = (RelativeLayout) findViewById(R.id.host_info_container);
        mStartTv = (TextView) findViewById(R.id.start_btn);
        mBgView = (View) findViewById(R.id.touch_view);
        mCancelTv = (TextView) findViewById(R.id.cancel_tv);
        mHostIv = (BaseImageView) findViewById(R.id.host_iv);
        mRuleTv = (TextView) findViewById(R.id.rule_tv);
        mNameTv = (TextView) findViewById(R.id.name_tv);
        mPOptContainer = (RelativeLayout) findViewById(R.id.p_opt_container);
        mLOptContainer = (RelativeLayout) findViewById(R.id.l_opt_container);
        mLCancelTv = (TextView) findViewById(R.id.l_cancel_tv);
        mLStartTv = (TextView) findViewById(R.id.l_start_btn);
        mContainer = (RelativeLayout) findViewById(R.id.container);
        mParams = (LayoutParams)mContainer.getLayoutParams();
    }

    private void initListener() {
        mHostInfoContainer.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        RxView.clicks(mStartTv)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        drawTurnTable();
                    }
                });

        RxView.clicks(mLStartTv)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        drawTurnTable();
                    }
                });

        RxView.clicks(mBgView)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        onClickBgView();
                    }
                });
        RxView.clicks(mCancelTv)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        WatchBigTurnTablePanelView.this.setVisibility(GONE);
                    }
                });
        RxView.clicks(mLCancelTv)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        WatchBigTurnTablePanelView.this.setVisibility(GONE);
                    }
                });
        RxView.clicks(mRuleTv)
                .throttleFirst(300, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        showRuleTips();
                    }
                });
        mBigTurnTableView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        mBigTurnTableView.setOnBigTurnTableListener(new BigTurnTableView.OnBigTurnTableListener() {
            @Override
            public void onRotateAnimatorFinish(PrizeItemModel data, String prizeKey) {
                mOnDrawTurnTableListener.onRotateAnimatorFinish(data, prizeKey);
                if(mIsLandscape) {
                    mLOptContainer.setVisibility(VISIBLE);
                } else {
                    mPOptContainer.setVisibility(VISIBLE);
                }
            }
        });
    }

    private void initData() {
        if(mDatas != null && !mDatas.isEmpty()) {
            mBigTurnTableView.setDatas(mDatas);
        }
    }

    public void onClickBgView() {
        if(mBigTurnTableView.isRotating()) {
            return;
        }
        this.setVisibility(GONE);
    }

    public void switchOrient(boolean isLandscape) {
        mIsLandscape = isLandscape;
        if(isLandscape) {
            mPOptContainer.setVisibility(GONE);
            mLOptContainer.setVisibility(VISIBLE);
            mParams.width = LayoutParams.MATCH_PARENT;
            mParams.topMargin = DisplayUtils.dip2px(20f);

        } else {
            mPOptContainer.setVisibility(VISIBLE);
            mLOptContainer.setVisibility(GONE);
            mParams.width = LayoutParams.WRAP_CONTENT;
            mParams.topMargin = DisplayUtils.dip2px(200f);
        }
        mContainer.setLayoutParams(mParams);
        //TODO-暂时去了
//        if(mBigTurnTableRuleView != null) {
//            mBigTurnTableRuleView.switchOrient(mIsLandscape);
//        }
    }

    private void showRuleTips() {
        //TODO-暂时去了
//        if(mBigTurnTableRuleView == null) {
//            mBigTurnTableRuleView = new BigTurnTableRuleView(mContext);
//        }
//        mBigTurnTableRuleView.switchOrient(mIsLandscape);
//        mBigTurnTableRuleView.show(this);
    }

    public void startRotate(int index, String prizeKey) {
        mBigTurnTableView.startRotate(index, prizeKey);
        if(mIsLandscape) {
            mLOptContainer.setVisibility(GONE);
        } else {
            mPOptContainer.setVisibility(GONE);
        }
    }

    private void drawTurnTable() {
        mOnDrawTurnTableListener.onDrawTurnTable();
    }

    public void setDatas(List<PrizeItemModel> datas) {
        this.mDatas = datas;
        initData();
    }

    public void setUser(long zuid, String zName, long zavator , BigTurnTableProto.TurntableType type) {
        AvatarUtils.loadAvatarByUidTs(mHostIv, zuid, zavator, true);
        String txt1 = type == BigTurnTableProto.TurntableType.TYPE_128 ? String.valueOf(128) : String.valueOf(500);
        mBigTurnTableView.changeBigTurnTableBg(type != BigTurnTableProto.TurntableType.TYPE_128);
        String txt = null;
        String name = null;
        if(!TextUtils.isEmpty(zName)) {
            if(zName.length() > 5) {
                name = zName.substring(0, 5);
                name = name + "...";
            } else {
                name = zName;
            }
            txt = name + " " + String.format(GlobalData.app().getResources().getString(R.string.host_open_big_turn_table_tips)
                    , txt1);
        } else {
            txt = zuid + " " + String.format(GlobalData.app().getResources().getString(R.string.host_open_big_turn_table_tips)
                    , txt1);
        }
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(txt);
        builder.setSpan(new ForegroundColorSpan(GlobalData.app().getResources().getColor(R.color.color_ff2966)),
                0, name == null ? String.valueOf(zuid).length() : name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(new ForegroundColorSpan(GlobalData.app().getResources().getColor(R.color.color_d67003)),
                txt.length() - txt1.length() - 2, txt.length() - 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mNameTv.setText(builder);
    }

    public void destory() {

    }

    public void setOnDrawTurnTableListener(OnDrawTurnTableListener listener) {
        mOnDrawTurnTableListener = listener;
    }

    public interface OnDrawTurnTableListener {
        void onDrawTurnTable();

        void onRotateAnimatorFinish(PrizeItemModel data, String prizeKey);
    }
}
