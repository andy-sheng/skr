package com.module.playways.grab.room.dynamicmsg;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.playways.BaseRoomData;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.playways.room.msg.event.EventHelper;
import com.module.playways.R;

import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * 评论区表情面板
 */
public class DynamicMsgView extends RelativeLayout {

    public final static String TAG = "DynamicMsgView";

    BaseRoomData mRoomData;

    RecyclerView mDynamicMsgRv;

    DynamicMsgAdapter mDynamicMsgAdapter;

    Listener mListener;

    public DynamicMsgView(Context context) {
        super(context);
        init();
    }

    public DynamicMsgView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DynamicMsgView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.dynamic_msg_view_layout, this);

        mDynamicMsgRv = (RecyclerView) this.findViewById(R.id.dynamic_msg_rv);

        mDynamicMsgRv = (RecyclerView) this.findViewById(R.id.dynamic_msg_rv);
        mDynamicMsgAdapter = new DynamicMsgAdapter(new RecyclerOnItemClickListener<DynamicModel>() {
            @Override
            public void onItemClicked(View view, int position, DynamicModel model) {
                MyLog.d(TAG, "onItemClicked" + " view=" + view + " position=" + position + " model=" + model);
                sendDynamicEmoji(model);
            }
        });
        mDynamicMsgRv.setAdapter(mDynamicMsgAdapter);
        setBackgroundResource(R.drawable.grab_pop_window_bg);

        loadEmoji();
    }

    public void loadEmoji() {
        long saveTs = U.getPreferenceUtils().getSettingLong(U.getPreferenceUtils().getSharedPreferencesSp2(), "pref_emojis_save_ts", 0);
        if (System.currentTimeMillis() - saveTs > 3600 * 1000 * 6) {
            syncEmojis();
        } else {
            String listStr = U.getPreferenceUtils().getSettingString(U.getPreferenceUtils().getSharedPreferencesSp2(), "pref_emojis", "");
            List<DynamicModel> list = JSON.parseArray(listStr, DynamicModel.class);
            if (list != null && list.size() > 0) {
                showDynamicModels(list);
            } else {
                syncEmojis();
            }
        }
    }

    private void syncEmojis() {
        GrabRoomServerApi grabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);
        ApiMethods.subscribe(grabRoomServerApi.getDynamicEmoji(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    String listStr = result.getData().getString("emojis");
                    List<DynamicModel> list = JSON.parseArray(listStr, DynamicModel.class);
                    U.getPreferenceUtils().setSettingString(U.getPreferenceUtils().getSharedPreferencesSp2(), "pref_emojis", listStr);
                    U.getPreferenceUtils().setSettingLong(U.getPreferenceUtils().getSharedPreferencesSp2(), "pref_emojis_save_ts", System.currentTimeMillis());
                    showDynamicModels(list);
                }
            }
        });
    }

    public void setData(BaseRoomData roomData) {
        mRoomData = roomData;
    }

    private void showDynamicModels(List<DynamicModel> list) {
        if (list != null && list.size() > 0) {
            mDynamicMsgAdapter.setDataList(list);
        }
    }

    private void sendDynamicEmoji(DynamicModel model) {
        GrabRoomServerApi grabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);

        HashMap<String, Object> map = new HashMap<>();
        map.put("gameID", mRoomData.getGameId());
        map.put("id", model.getId());

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(grabRoomServerApi.sendDynamicEmoji(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {

            }
        });

        EventHelper.pretendDynamicPush(model, mRoomData.getGameId());
        if (mListener != null) {
            mListener.onSendMsgOver();
        }
    }

    public void setListener(DynamicMsgView.Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        void onSendMsgOver();
    }
}
