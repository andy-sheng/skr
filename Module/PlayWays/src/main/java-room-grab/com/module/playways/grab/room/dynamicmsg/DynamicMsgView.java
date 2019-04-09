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
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.module.playways.BaseRoomData;
import com.module.playways.grab.room.GrabRoomServerApi;
import com.module.rank.R;

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
    }

    public void initData(BaseRoomData roomData) {
        mRoomData = roomData;

        // TODO: 2019/4/9 加上缓存策略
        GrabRoomServerApi grabRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi.class);
        ApiMethods.subscribe(grabRoomServerApi.getDynamicEmoji(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    List<DynamicModel> list = JSON.parseArray(result.getData().getString("emojis"), DynamicModel.class);
                }
            }
        });
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

//        EventHelper.pretendCommentPush(content, mRoomData.getGameId());
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
