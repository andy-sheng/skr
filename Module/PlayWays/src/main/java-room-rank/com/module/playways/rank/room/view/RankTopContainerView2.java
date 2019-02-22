package com.module.playways.rank.room.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.module.playways.RoomData;
import com.module.playways.rank.prepare.model.GameConfigModel;
import com.module.playways.rank.prepare.model.PkScoreTipMsgModel;
import com.module.playways.rank.prepare.model.ScoreTipTypeModel;
import com.module.playways.rank.room.event.PkSomeOneBurstLightEvent;
import com.module.playways.rank.room.event.PkSomeOneLightOffEvent;
import com.module.playways.rank.room.score.bar.EnergySlotView;
import com.module.playways.rank.room.score.bar.ScoreTipsView;
import com.module.rank.R;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class RankTopContainerView2 extends RelativeLayout {
    public final static String TAG = "RankTopContainerView";
    static final int MAX_USER_NUM = 3;
    ExImageView mMoreBtn;
    MoreOpView mMoreOpView;
    ExImageView mIvLed;
    ExImageView mIvLeft;
    ExImageView mIvCenter;
    ExImageView mIvRignt;
    EnergySlotView mEnergySlotView;
    ExImageView mIvGameRole;
    DialogPlus mGameRoleDialog;

    RankTopContainerView1.Listener mListener;

    ScoreTipsView.Item mLastItem;

    RoomData mRoomData;

    UserLightInfo mStatusArr[] = new UserLightInfo[MAX_USER_NUM];
    int mIndex = 0;

    static class UserLightInfo {
        int mUserId;
        LightState mLightState;
    }

    public enum LightState {
        BAO, MIE;
    }

    public RankTopContainerView2(Context context) {
        super(context);
        init();
    }

    public RankTopContainerView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setListener(RankTopContainerView1.Listener l) {
        mListener = l;
    }

    public void setRoomData(RoomData roomData) {
        mRoomData = roomData;
    }

    private void init() {
        inflate(getContext(), R.layout.rank_top_container_view, this);
        mMoreBtn = this.findViewById(R.id.more_btn);
        mIvLed = (ExImageView) findViewById(R.id.iv_led);
        mIvLeft = (ExImageView) findViewById(R.id.iv_left);
        mIvCenter = (ExImageView) findViewById(R.id.iv_center);
        mIvRignt = (ExImageView) findViewById(R.id.iv_rignt);
        mEnergySlotView = (EnergySlotView) findViewById(R.id.energy_slot_view);
        mIvGameRole = (ExImageView) findViewById(R.id.iv_game_role);

        mIvGameRole.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mGameRoleDialog != null) {
                    mGameRoleDialog.dismiss();
                }

                mGameRoleDialog = DialogPlus.newDialog(getContext())
                        .setContentHolder(new ViewHolder(R.layout.game_role_view_layout))
                        .setContentBackgroundResource(R.color.transparent)
                        .setOverlayBackgroundResource(R.color.black_trans_50)
                        .setExpanded(false)
                        .setGravity(Gravity.CENTER)
                        .create();

                mGameRoleDialog.show();
            }
        });

        mMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMoreOpView == null) {
                    mMoreOpView = new MoreOpView(getContext());
                    mMoreOpView.setListener(new MoreOpView.Listener() {
                        @Override
                        public void onClostBtnClick() {
                            if (mListener != null) {
                                mListener.closeBtnClick();
                            }
                        }

                        @Override
                        public void onVoiceChange(boolean voiceOpen) {
                            // 打开或者关闭声音 只是不听别人的声音
                            if (mListener != null) {
                                mListener.onVoiceChange(voiceOpen);
                            }
                        }
                    });
                    mMoreOpView.setRoomData(mRoomData);
                }
                mMoreOpView.showAt(mMoreBtn);
            }
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mGameRoleDialog != null && mGameRoleDialog.isShowing()) {
            mGameRoleDialog.dismiss();
        }
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PkSomeOneBurstLightEvent event) {
        for (int i = 0; i < mStatusArr.length && i < mIndex; i++) {
            UserLightInfo ul = mStatusArr[i];
            if (ul != null) {
                if (ul.mUserId == event.uid) {
                    ul.mLightState = LightState.BAO;
                    setLight(i, ul.mLightState);
                    return;
                }
            }
        }
        UserLightInfo ul = new UserLightInfo();
        ul.mUserId = event.uid;
        ul.mLightState = LightState.BAO;
        setLight(mIndex, ul.mLightState);
        mStatusArr[mIndex] = ul;
        mIndex = (mIndex + 1) % MAX_USER_NUM;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PkSomeOneLightOffEvent event) {
        for (int i = 0; i < mStatusArr.length & i < mIndex; i++) {
            UserLightInfo ul = mStatusArr[i];
            if (ul != null) {
                if (ul.mUserId == event.uid) {
                    ul.mLightState = LightState.MIE;
                    setLight(i, ul.mLightState);
                    return;
                }
            }
        }
        UserLightInfo ul = new UserLightInfo();
        ul.mUserId = event.uid;
        ul.mLightState = LightState.MIE;
        setLight(mIndex, ul.mLightState);
        mStatusArr[mIndex] = ul;
        mIndex = (mIndex + 1) % MAX_USER_NUM;
    }

    //轮次结束
    public void roundOver() {
        mIvLeft.setImageDrawable(U.getDrawable(R.drawable.yanchang_xiaolian));
        mIvCenter.setImageDrawable(U.getDrawable(R.drawable.yanchang_xiaolian));
        mIvRignt.setImageDrawable(U.getDrawable(R.drawable.yanchang_xiaolian));
        getEnergySlotView().setTarget(0);
        mIndex = 0;
    }

    private void setLight(int index, LightState lightState) {
        switch (index) {
            case 1:
                mIvLeft.setImageDrawable(lightState == LightState.BAO ? U.getDrawable(R.drawable.yanchang_bao) : U.getDrawable(R.drawable.yanchang_mie));
                break;
            case 2:
                mIvCenter.setImageDrawable(lightState == LightState.BAO ? U.getDrawable(R.drawable.yanchang_bao) : U.getDrawable(R.drawable.yanchang_mie));
                break;
            case 3:
                mIvRignt.setImageDrawable(lightState == LightState.BAO ? U.getDrawable(R.drawable.yanchang_bao) : U.getDrawable(R.drawable.yanchang_mie));
                break;
        }
    }

    public EnergySlotView getEnergySlotView() {
        return mEnergySlotView;
    }

    public void onGameFinish() {
        if (mMoreOpView != null) {
            mMoreOpView.dismiss();
        }
    }

    public void setScoreProgress(int progress) {
        for (int i = 0; i < 1; i++) {
            progress = (int) (Math.sqrt(progress) * 10);
        }

        GameConfigModel gameConfigModel = mRoomData.getGameConfigModel();
        ScoreTipsView.Item item = new ScoreTipsView.Item();

        if (gameConfigModel != null) {
            List<PkScoreTipMsgModel> scoreTipMsgModelList = gameConfigModel.getPkScoreTipMsgModelList();
            if (scoreTipMsgModelList != null) {
                for (PkScoreTipMsgModel m : scoreTipMsgModelList) {
                    if (progress >= m.getFromScore() && progress < m.getToScore()) {
                        // 命中
                        switch (m.getScoreTipTypeModel()) {
                            case ST_UNKNOWN:
                                break;
                            case ST_TOO_BAD:
                                item.setLevel(ScoreTipsView.Level.Bad);
                                break;
                            case ST_NOT_BAD:
                                item.setLevel(ScoreTipsView.Level.Ok);
                                break;
                            case ST_VERY_GOOD:
                                item.setLevel(ScoreTipsView.Level.Good);
                                break;
                            case ST_NICE_PERFECT:
                                item.setLevel(ScoreTipsView.Level.Perfect);
                                break;
                        }
                        break;
                    }
                }
            }
        } else {
            if (progress >= 95) {
                item.setLevel(ScoreTipsView.Level.Perfect);
            } else if (progress >= 90) {
                item.setLevel(ScoreTipsView.Level.Good);
            } else if (progress >= 70) {
                item.setLevel(ScoreTipsView.Level.Ok);
            } else if (progress < 20) {
                item.setLevel(ScoreTipsView.Level.Bad);
            }
        }
        if (item.getLevel() != null) {
            if (mLastItem != null && item.getLevel() == mLastItem.getLevel()) {
                if (item.getLevel() == ScoreTipsView.Level.Bad) {
                    item.setNum(1);
                } else {
                    item.setNum(mLastItem.getNum() + 1);
                }

            }
            mLastItem = item;
            ScoreTipsView.play(this, item);
        }
    }
}
