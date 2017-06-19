package com.wali.live.livesdk.live.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.live.data.preference.PreferenceKeys;
import com.mi.live.data.query.model.MessageRule;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.eventbus.LiveEventClass;
import com.wali.live.proto.LiveCommonProto;
import com.wali.live.proto.LiveProto;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.statistics.StatisticsWorker;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * TODO: document your custom view class.
 */
public class RoomSettingView extends LinearLayout {
    private static final String TAG = RoomSettingView.class.getSimpleName();

    private SwitchButton mRepeatStatement;

    private ListView mFrequencyControlLv;

    private List<Integer> mIntervalList;

    private int mSelectPosition;

    private MessageRule mMsgRule;

    private MessageRule mOriMsgRule;

    private String mRoomId;

    public RoomSettingView(Context context, MessageRule msgRule, String roomId) {
        super(context);
        mRoomId = roomId;
        mMsgRule = msgRule;
        initData();
        init(context);
    }

    public RoomSettingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData();
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.sample_room_setting_view, this);
        mRepeatStatement = (SwitchButton) findViewById(R.id.switch_btn);
        mFrequencyControlLv = (ListView) findViewById(R.id.frequency_control_lv);
        mRepeatStatement.setChecked(mMsgRule.isUnrepeatable());
        mRepeatStatement.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mMsgRule.setUnrepeatable(isChecked);
                StatisticsWorker.getsInstance().sendCommandRealTime(StatisticsWorker.AC_APP, StatisticsKey.KEY_ROOMSETTING_SPEAK_FREQUENCY_CONTROL, 1);
            }
        });
        mFrequencyControlLv.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return mIntervalList.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.room_setting_item_view, null, false);
                CheckedTextView checkedTextView = (CheckedTextView) view.findViewById(R.id.setting_item_cb);
                View line = view.findViewById(R.id.horizontal_line);
                if (position == mIntervalList.size() - 1) {
                    line.setVisibility(GONE);
                }
                checkedTextView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        mSelectPosition = position;
                        notifyDataSetChanged();
                        mMsgRule.setSpeakPeriod(mIntervalList.get(position));
                        StatisticsWorker.getsInstance().sendCommandRealTime(StatisticsWorker.AC_APP, String.format(StatisticsKey.KEY_ROOMSETTING_SPEAK_SPACING, mIntervalList.get(position)), 1);
//                        MyLog.d(TAG,String.format(StatisticsKey.KEY_ROOMSETTING_SPEAK_SPACING,mIntervalList.get(position)));
                    }
                });
                String text;
                int interval = mIntervalList.get(position);
                if (interval == 0) {
                    text = getResources().getString(R.string.barrage_not_control);
                } else {
                    if (interval / 60 != 0 && interval % 60 == 0) {
                        text = getResources().getString(R.string.barrage_control_in_min, interval / 60);
                    } else {
                        text = getResources().getString(R.string.barrage_control_in_second, interval);
                    }
                }
                checkedTextView.setText(text);
                if (interval == mMsgRule.getSpeakPeriod()) {
                    checkedTextView.setChecked(true);
                } else {
                    checkedTextView.setChecked(false);
                }

                return view;
            }
        });
    }

    private void initData() {
        mOriMsgRule = new MessageRule();
        mOriMsgRule.setUnrepeatable(mMsgRule.isUnrepeatable());
        mOriMsgRule.setSpeakPeriod(mMsgRule.getSpeakPeriod());

        String intervalConfig = PreferenceUtils.getSettingString(GlobalData.app(), PreferenceKeys.PRE_KEY_SEND_BARRAGE_INTERVAL, "");
        mIntervalList = new ArrayList<>();
        boolean hasMsgRuleInterval = false;
        boolean hasZeroInterval = false;
        if (!TextUtils.isEmpty(intervalConfig)) {
            String[] intervals = intervalConfig.split(",");
            for (int i = 0; i < intervals.length; i++) {
                String interval = intervals[i];
                if (TextUtils.isDigitsOnly(interval)) {
                    Integer val = Integer.valueOf(interval);
                    mIntervalList.add(val);
                    if (val.intValue() == mMsgRule.getSpeakPeriod()) {
                        hasMsgRuleInterval = true;
                    }
                    if (val.intValue() == 0) {
                        hasZeroInterval = true;
                    }
                }
            }
        }
        if (!hasZeroInterval) {
            mIntervalList.add(0);
        }
        if (mMsgRule.getSpeakPeriod() != 0 && !hasMsgRuleInterval) {
            mIntervalList.add(mMsgRule.getSpeakPeriod());
        }
        Collections.sort(mIntervalList);
    }

    /**
     * 发送设置变更事件
     */
    public void settingChangeNotify() {
        try {
            if (mMsgRule.isUnrepeatable() != mOriMsgRule.isUnrepeatable() || mMsgRule.getSpeakPeriod() != mOriMsgRule.getSpeakPeriod()) {
                updateMsgRule(UserAccountManager.getInstance().getUuidAsLong(), mRoomId, mMsgRule);
            }
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
    }

    public void setmMsgRule(MessageRule mMsgRule) {
        this.mMsgRule = mMsgRule;
    }

    public void setmRoomId(String mRoomId) {
        this.mRoomId = mRoomId;
    }

    /**
     * 更新房间内发言频率、是否重复
     *
     * @param uuid
     * @param liveId
     * @param messageRule
     * @return
     */
    public void updateMsgRule(final long uuid, final String liveId, final MessageRule messageRule) {
        Observable.just(0).map(new Func1<Integer, LiveProto.UpdateMsgRuleRsp>() {
            @Override
            public LiveProto.UpdateMsgRuleRsp call(Integer integer) {
                LiveProto.UpdateMsgRuleReq.Builder builder = LiveProto.UpdateMsgRuleReq.newBuilder();
                builder.setLiveId(liveId);
                builder.setZuid(uuid);
                LiveCommonProto.MsgRule.Builder msgRuleBuilder = LiveCommonProto.MsgRule.newBuilder();
                msgRuleBuilder.setSpeakPeriod(messageRule.getSpeakPeriod());
                msgRuleBuilder.setUnrepeatable(messageRule.isUnrepeatable());
                builder.setMsgRule(msgRuleBuilder.build());
                LiveProto.UpdateMsgRuleReq req = builder.build();
                PacketData data = new PacketData();
                data.setCommand(MiLinkCommand.COMMAND_UPDATE_MSGRULE);
                data.setData(req.toByteArray());
                MyLog.w(TAG, "update message rule request : \n" + req.toString());
                PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
                if (rspData != null) {
                    try {
                        LiveProto.UpdateMsgRuleRsp rsp = LiveProto.UpdateMsgRuleRsp.parseFrom(rspData.getData());
                        MyLog.w(TAG, "update message rule rsp : \n" + rsp.toString());
                        return rsp;
                    } catch (InvalidProtocolBufferException e) {
                        MyLog.e(TAG, e);
                    }

                }
                return null;
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<LiveProto.UpdateMsgRuleRsp>() {
                    @Override
                    public void call(LiveProto.UpdateMsgRuleRsp rsp) {
                        if (rsp != null && rsp.getRetCode() == ErrorCode.CODE_SUCCESS) {
                            EventBus.getDefault().post(new LiveEventClass.UpdateMsgRuleEvent(true, uuid, liveId, messageRule));
                        } else {
                            EventBus.getDefault().post(new LiveEventClass.UpdateMsgRuleEvent(false, uuid, liveId, messageRule));
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(throwable);
                    }
                });
    }
}
